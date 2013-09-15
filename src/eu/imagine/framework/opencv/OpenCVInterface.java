package eu.imagine.framework.opencv;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import eu.imagine.R;
import eu.imagine.framework.controller.MainInterface;
import eu.imagine.framework.messenger.Messenger;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 2:05 PM
 */
public class OpenCVInterface implements CameraBridgeViewBase
        .CvCameraViewListener2 {

    // OpenCV Android stuff:
    private CameraBridgeViewBase mOpenCvCameraView;

    // Vars that we need:
    private Messenger log;
    private final String TAG = "OpenCVInterface";
    public static LinkedBlockingQueue<TransportContainer> workerFeeder;
    private Detector det;
    private OpenCVWorker[] workers;
    private final int PARALLEL_COUNT = 4;

    private BaseLoaderCallback mLoaderCallback;

    public OpenCVInterface(Activity mainActivity) {
        mLoaderCallback = new BaseLoaderCallback(mainActivity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        log.log(TAG, "OpenCV loaded successfully");
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
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        det = new Detector();
        workerFeeder = new LinkedBlockingQueue<TransportContainer>(1);

        workers = new OpenCVWorker[PARALLEL_COUNT];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new OpenCVWorker("OpenCV Worker " + i);
            workers[i].start();
        }
    }

    @Override
    public void onCameraViewStopped() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].interrupt();
        }
    }

    /**
     * This is where the frame is processed by OpenCV.
     *
     * @param inputFrame
     * @return
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // If debug, do everything in line:
        if (MainInterface.DEBUG) {
            return det.detect(inputFrame.gray(), inputFrame.rgba());
        }
        // Else put the task on multiple threads:
        else {
            try {
                workerFeeder.put(new TransportContainer(inputFrame.gray().clone(),
                        inputFrame.rgba().clone()));
            } catch (InterruptedException e) {
                log.log(TAG, "Error feeding!");
                return inputFrame.gray();
            }
            return inputFrame.rgba();
        }
    }

    public void onCreate(Bundle savedInstanceState, Activity mainActivity) {
        mainActivity.getWindow().addFlags(WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON);
        mainActivity.setContentView(R.layout.main);
        mOpenCvCameraView = (CameraBridgeViewBase) mainActivity.findViewById
                (R.id.OpenCVScreen);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        this.log = Messenger.getInstance();
    }

    public void onResume(Activity mainActivity) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, mainActivity,
                mLoaderCallback);
    }

    public void onPause(Activity mainActivity) {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy(Activity mainActivity) {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
}
