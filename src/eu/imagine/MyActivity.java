package com.example.OpenCV;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MyActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OpenCV";
    public static LinkedBlockingQueue<Mat> workerFeeder;
    public static LinkedBlockingQueue<Mat> resultFeeder;

    // FLAGS (compositeFrameOut order!)
    private boolean USE_CANNY = false;
    private boolean DEBUG_PREP_FRAME = false;
    private boolean DEBUG_CONTOURS = false;
    private boolean DEBUG_POLY = false;
    private boolean DEBUG_DRAW_FIRST_MARKER = true;
    private boolean DEBUG_DRAW_SAMPLING = true;
    private boolean DEBUG_DRAW_MARKER_NOTIFIER = false;
    // Multithreading
    private final int PARALLEL_COUNT = 0;
    // Important numbers
    private final int MARKER_GRID = 6;
    private final int MARKER_SQUARE = 3;
    private final int MARKER_SIZE = MARKER_GRID * MARKER_SQUARE;
    private final int RENDER_SCALE = 10;
    private final int SAMPLING_ERRORS = 4;
    private int step = MARKER_SIZE / MARKER_GRID;
    private int half = step / 2;
    private int errorAllowance = 0;

    // Colors
    private final double[] GREEN = new double[]{0, 255, 0, 0};
    private final double[] RED = new double[]{255, 0, 0, 0};

    Mat out, compositeFrameOut, tempPerspective;
    ArrayList<MatOfPoint> contours, contoursAll;
    ArrayList<Marker> markerCandidates;
    MatOfPoint2f result, standardMarker;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private OpenCVWorker[] workers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    // On start, prepare things (instead of compositeFrameOut the constructor?):
    public void onCameraViewStarted(int width, int height) {

        out = new Mat();
        compositeFrameOut = new Mat();
        contours = new ArrayList<MatOfPoint>();
        contoursAll = new ArrayList<MatOfPoint>();
        markerCandidates = new ArrayList<Marker>();
        result = new MatOfPoint2f();
        // Prepare standard marker:
        standardMarker = new MatOfPoint2f();
        standardMarker.fromArray(new Point(MARKER_SIZE, MARKER_SIZE),
                new Point(0, MARKER_SIZE),
                new Point(0, 0),
                new Point(MARKER_SIZE, 0));

        workerFeeder = new LinkedBlockingQueue<Mat>(1);
        resultFeeder = new LinkedBlockingQueue<Mat>(1);

        workers = new OpenCVWorker[PARALLEL_COUNT];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new OpenCVWorker("OpenCV Worker " + i);
            workers[i].start();
        }
    }

    public void onCameraViewStopped() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].interrupt();
        }
    }

    /**
     * I think the magic happens here.
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        contours.clear();
        contoursAll.clear();
        markerCandidates.clear();

        if (USE_CANNY) {
            Imgproc.Canny(inputFrame.gray(), out, 50, 150);
        } else {
            // Imgproc.threshold(inputFrame.gray(), out, 127, 255, Imgproc.THRESH_BINARY);
            Imgproc.adaptiveThreshold(inputFrame.gray(), out, 255,
                    Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY, 81, 7);
        }

        if (DEBUG_PREP_FRAME)
            return out;

        Imgproc.findContours(out.clone(), contoursAll, new Mat(),
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_NONE);
        // Remove too small contours:
        for (MatOfPoint contour : contoursAll) {
            // TODO: Set good threshold (depends on marker size!)
            if (contour.total() > 250)
                contours.add(contour);
        }

        compositeFrameOut = inputFrame.rgba();

        // DEBUG
        if (DEBUG_CONTOURS) {
            Imgproc.drawContours(compositeFrameOut, contours, -1, new Scalar(255, 0, 0), 2);
            return compositeFrameOut;
        }

        // Get all 4-vertice polygons from contours:
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f input = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(input, result, input.total() * 0.05,
                    true);
            // Only take contours with ==4 points
            if (result.total() != 4 || !Imgproc.isContourConvex(new
                    MatOfPoint(result.toArray()))) {
                continue;
            }
            // Calculate perspective transform
            tempPerspective = Imgproc.getPerspectiveTransform(result,
                    standardMarker);
            // Apply to get marker texture
            Imgproc.warpPerspective(inputFrame.rgba(), out, tempPerspective,
                    new Size(MARKER_SIZE, MARKER_SIZE));
            // Check if marker
            int ID = isMarker(out);
            if (ID < 0)
                continue;
            // Save marker candidate
            markerCandidates.add(new Marker(result, tempPerspective, out));
        }

        if (DEBUG_POLY) {
            ArrayList<MatOfPoint> temp = new ArrayList<MatOfPoint>();
            for (Marker mark : markerCandidates) temp.add(mark.getMOPCorners());
            Core.polylines(compositeFrameOut, temp, true, new Scalar(255, 0, 0), 2);
        }

        if (DEBUG_DRAW_FIRST_MARKER && !markerCandidates.isEmpty()) {
            // TODO Slow, needs to be done quicker!
            Mat tempText = markerCandidates.get(0).getMarkerTextureReference();
            for (int i = 0; i < RENDER_SCALE * MARKER_SIZE; i++)
                for (int j = 0; j < RENDER_SCALE * MARKER_SIZE; j++) {
                    compositeFrameOut.put(i, j, tempText.get(i / RENDER_SCALE,
                            j / RENDER_SCALE));
                }
        }

        if (DEBUG_DRAW_MARKER_NOTIFIER && !markerCandidates.isEmpty()) {
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    compositeFrameOut.put(i, j, GREEN);
        }

        return compositeFrameOut;
    }

    /**
     * @param texture
     * @return
     */
    private int isMarker(Mat texture) {
        Imgproc.cvtColor(texture, texture, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.threshold(texture, texture, 40, 255, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(texture, texture, Imgproc.COLOR_GRAY2RGBA);

        // reset error allowance
        errorAllowance = 0;
        // Check border:
        for (int i = 1; i < MARKER_GRID - 1; i++) {
            if (testSample(half + i * step, half, texture) > 0)
                errorAllowance++;
            if (testSample(half, half + i * step, texture) > 0)
                errorAllowance++;
            if (testSample(half + i * step, MARKER_SIZE - 1 - half, texture) > 0)
                errorAllowance++;
            if (testSample(MARKER_SIZE - 1 - half, half + i * step, texture) > 0)
                errorAllowance++;
        }

        if (errorAllowance > SAMPLING_ERRORS)
            return -1;
        // Now ID it:
        int cornerCount = 0;
        cornerCount += testSample(half + step, half + step, texture);
        cornerCount += testSample(half + step, MARKER_SIZE - 1 - half - step,
                texture);
        cornerCount += testSample(MARKER_SIZE - 1 - half - step, half + step,
                texture);
        cornerCount += testSample(MARKER_SIZE - 1 - half - step,
                MARKER_SIZE - 1 - half - step, texture);
        // 3 Corners white (+1), 1 black (-1)
        if (cornerCount != 2)
            return -1;
        return 2;
    }

    private int countBlack = 0;

    /**
     * @param x
     * @param y
     * @param texture
     * @return Negative for black, positive for white
     */
    private int testSample(int x, int y, Mat texture) {
        countBlack = 0;
        for (int i = -MARKER_SQUARE / 2; i <= MARKER_SQUARE / 2; i++)
            for (int j = -MARKER_SQUARE / 2; j <= MARKER_SQUARE / 2; j++) {
                if (texture.get(x + i, y + j)[0] == 0d)
                    countBlack++;
            }
        if (countBlack > SAMPLING_ERRORS) {
            if (DEBUG_DRAW_SAMPLING)
                texture.put(x, y, GREEN);
            return -1;
        } else {
            if (DEBUG_DRAW_SAMPLING)
                texture.put(x, y, RED);
            return 1;
        }
    }
}
