package eu.imagine;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.concurrent.LinkedBlockingQueue;

public class MyActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // FLAG
    public final boolean DEBUG = false;

    private static final String TAG = "OpenCV";
    public static LinkedBlockingQueue<TransportContainer> workerFeeder;
    private Detector det;
    private OpenCVWorker[] workers;
    private final int PARALLEL_COUNT = 4;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCVScreen);
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

        det = new Detector();
        workerFeeder = new LinkedBlockingQueue<TransportContainer>(1);

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
        // If debug, do everything in line:
        if (DEBUG) {
            return det.detect(inputFrame.gray(), inputFrame.rgba());
        }
        // Else put the task on multiple threads:
        else {
            try {
                workerFeeder.put(new TransportContainer(inputFrame.gray().clone(),
                        inputFrame.rgba().clone()));
            } catch (InterruptedException e) {
                Log.e(TAG, "Error feeding!");
                return inputFrame.gray();
            }
            return inputFrame.rgba();
        }
    }

}
