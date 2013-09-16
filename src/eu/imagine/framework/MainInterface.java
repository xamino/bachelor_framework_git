package eu.imagine.framework;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import eu.imagine.framework.messenger.Messenger;
import eu.imagine.framework.opencv.Marker;
import eu.imagine.framework.opencv.OpenCVInterface;
import eu.imagine.framework.renderer.RenderInterface;
import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 2:02 PM
 */
public class MainInterface {

    // Allow debug logging:
    public static boolean DEBUG_LOGGING = true;
    public static boolean RUN_OPENCV = true;
    public static boolean RUN_RENDERER = true;
    public static boolean DEBUG_FRAME = false;

    // Linking variables to sub classes:
    private Messenger log;
    private final String TAG = "MainInterface";
    private OpenCVInterface opencv;
    private RenderInterface render;

    // Store markers per frame:
    public static LinkedBlockingQueue<ArrayList<Marker>> detectedMarkers =
            new LinkedBlockingQueue<ArrayList<Marker>>(1);

    public void onCreate(Activity mainActivity, ViewGroup groupView) {
        log = Messenger.getInstance();
        log.log(TAG, "Starting framework.");
        // Sanity check:
        if (groupView == null)
            log.log(TAG, "Framework will crash, cameraView or renderView are" +
                    " " +
                    "NULL!");
        log.pushTimer(this, "start");
        if (RUN_OPENCV) {
            opencv = new OpenCVInterface(mainActivity);
            JavaCameraView cameraView = new JavaCameraView(mainActivity,
                    JavaCameraView.CAMERA_ID_ANY);
            cameraView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            cameraView.enableFpsMeter();
            cameraView.setVisibility(JavaCameraView.GONE);
            groupView.addView(cameraView);
            opencv.onCreate(cameraView);
        }
        if (RUN_RENDERER) {
            render = new RenderInterface();
            GLSurfaceView renderView = new GLSurfaceView(mainActivity);
            renderView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            groupView.addView(renderView);
            render.onCreate(renderView);
        }

        // Set layout things:
        mainActivity.getWindow().addFlags(WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON);

        log.log(TAG, "Framework started in " + log.popTimer(this).time
                + "ms.");
    }

    public void onResume(Activity mainActivity) {
        if (RUN_OPENCV)
            opencv.onResume(mainActivity);
        if (RUN_RENDERER)
            render.onResume();
    }

    public void onPause() {
        if (RUN_OPENCV)
            opencv.onPause();
        if (RUN_RENDERER)
            render.onPause();
    }

    public void onDestroy() {
        if (RUN_OPENCV)
            opencv.onDestroy();
        log.log(TAG, "Stopping.");
    }
}
