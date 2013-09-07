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

    // FLAGS (in order!)
    private final boolean USE_CANNY = false;
    private final boolean DEBUG_PREP_FRAME = false;
    private final boolean DEBUG_CONTOURS = false;
    private final boolean DEBUG_POLY = true;
    private final boolean DEBUG_DRAW_FIRST_MARKER = true;
    private final boolean DEBUG_DRAW_SAMPLING = true;
    private final int PARALLEL_COUNT = 0;
    // Important numbers
    private final int MARKER_GRID = 6;
    private final int MARKER_SIZE = MARKER_GRID * 4;
    private final int RENDER_SCALE = 9;

    Mat out, in, tempPerspective;
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

    // On start, prepare things (instead of in the constructor?):
    public void onCameraViewStarted(int width, int height) {

        out = new Mat();
        in = new Mat();
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
            if (contour.total() > 200)
                contours.add(contour);
        }

        // DEBUG
        if (DEBUG_CONTOURS) {
            in = inputFrame.rgba();
            Imgproc.drawContours(in, contours, -1, new Scalar(255, 0, 0), 2);
            return in;
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
            if (!isMarker(out))
                continue;
            // Save marker candidate
            markerCandidates.add(new Marker(result, tempPerspective, out));
        }

        in = inputFrame.rgba();

        if (DEBUG_POLY) {
            ArrayList<MatOfPoint> temp = new ArrayList<MatOfPoint>();
            for (Marker mark : markerCandidates) temp.add(mark.getMOPCorners());
            Core.polylines(in, temp, true, new Scalar(255, 0, 0), 2);
        }

        if (DEBUG_DRAW_FIRST_MARKER && !markerCandidates.isEmpty()) {
            // TODO Slow, needs to be done quicker!
            Mat tempText = markerCandidates.get(0).getMarkerTextureReference();
            for (int i = 0; i < RENDER_SCALE * MARKER_SIZE; i++)
                for (int j = 0; j < RENDER_SCALE * MARKER_SIZE; j++) {
                    in.put(i, j, tempText.get(i / RENDER_SCALE,
                            j / RENDER_SCALE));
                }
        }

        return in;
    }

    /**
     * @param texture
     * @return
     */
    private boolean isMarker(Mat texture) {
        Imgproc.cvtColor(texture, texture, Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.adaptiveThreshold(texture, texture, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, MARKER_SIZE-3, 10);
        Imgproc.erode(texture, texture, Mat.ones(2, 2, texture.type()));
        Imgproc.dilate(texture, texture, Mat.ones(2, 2, texture.type()));
        Imgproc.threshold(texture, texture, 120, 255, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(texture, texture, Imgproc.COLOR_GRAY2RGBA);
        Boolean ret = true;
        // Check border:
        int step = MARKER_SIZE / MARKER_GRID;
        // TODO from 1 because otherwise we check the corners twice
        // TODO: Don't absolutely throw away, weigh over sample!
        for (int i = 0; ret && i < MARKER_GRID; i++) {
            // Check upper row:
            if (texture.get(step / 2 + i * step - 1, step / 2 - 1)[0] != 0)
                ret = false;
            // Check lower row:
            if (texture.get(step / 2 + i * step - 1, MARKER_SIZE - step / 2 -
                    1)[0] != 0d) ret = false;
            // Check left column:
            if (texture.get(step / 2 - 1, step / 2 + i * step - 1)[0] != 0d)
                ret = false;
            // Check right column:
            if (texture.get(MARKER_SIZE - step / 2 - 1,
                    step / 2 + i * step - 1)[0] != 0d) ret = false;
            if (DEBUG_DRAW_SAMPLING && !ret) {
                texture.put(step / 2 + i * step - 1, step / 2 - 1,
                        new double[]{255, 0, 0, 0});
                texture.put(step / 2 + i * step - 1, MARKER_SIZE - step / 2 -
                        1, new double[]{255, 0, 0, 0});
                texture.put(step / 2 - 1, step / 2 + i * step - 1,
                        new double[]{255, 0, 0, 0});
                texture.put(MARKER_SIZE - step / 2 - 1,
                        step / 2 + i * step - 1, new double[]{255, 0, 0, 0});
            }
        }
        return (DEBUG_DRAW_SAMPLING ? true : ret);
    }
}
