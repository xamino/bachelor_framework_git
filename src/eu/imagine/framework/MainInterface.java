package eu.imagine.framework;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import eu.imagine.framework.messenger.Messenger;
import eu.imagine.framework.opencv.Marker;
import eu.imagine.framework.opencv.OpenCVInterface;
import eu.imagine.framework.renderer.RenderInterface;

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
    public static final boolean DEBUG = false;
    public static final boolean DEBUG_FRAME = false;

    // Linking variables to sub classes:
    private Messenger log;
    private final String TAG = "MainInterface";
    private OpenCVInterface opencv;
    private RenderInterface render;

    // Store markers per frame:
    public static LinkedBlockingQueue<ArrayList<Marker>> detectedMarkers =
            new LinkedBlockingQueue<ArrayList<Marker>>(1);

    public void onCreate(Bundle savedInstanceState, Activity mainActivity,
                         View cameraView, View renderView) {
        log = Messenger.getInstance();
        log.log(TAG, "Starting framework.");
        // Sanity check:
        if (cameraView == null || renderView == null)
            log.log(TAG, "Framework will crash, cameraView or renderView are " +
                    "NULL!");
        log.pushTimer(this, "start");
        opencv = new OpenCVInterface(mainActivity);
        render = new RenderInterface();
        // do onCreate. Note that the views are also assigned here.
        render.onCreate(mainActivity, renderView);
        opencv.onCreate(cameraView);

        // Set layout things:
        mainActivity.getWindow().addFlags(WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON);

        log.log(TAG, "Framework started in " + log.popTimer(this).time
                + "ms.");
    }

    public void onResume(Activity mainActivity) {
        opencv.onResume(mainActivity);
    }

    public void onPause(Activity mainActivity) {
        opencv.onPause();
    }

    public void onDestroy(Activity mainActivity) {
        opencv.onDestroy();
        render.onDestroy();
        log.log(TAG, "Stopping.");
    }
}