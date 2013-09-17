package eu.imagine.framework;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
    protected static boolean DEBUG_LOGGING = true;
    protected boolean RUN_OPENCV = true;
    protected boolean RUN_RENDERER = true;
    protected boolean DEBUG_FRAME = false;

    // Linking variables to sub classes:
    private Messenger log;
    private final String TAG = "MainInterface";
    private OpenCVInterface opencv;
    private RenderInterface render;
    private Activity mainActivity;
    private ViewGroup groupView;

    // Store markers per frame:
    private LinkedBlockingQueue<ArrayList<Marker>> detectedMarkers =
            new LinkedBlockingQueue<ArrayList<Marker>>(1);
    private boolean updatedData = false;

    public MainInterface(Activity mainActivity, ViewGroup groupView) {
        this.log = Messenger.getInstance();
        log.log(TAG, "Constructing framework.");
        this.mainActivity = mainActivity;
        // Sanity check:
        if (groupView == null)
            log.log(TAG, "Framework will crash, groupView is NULL!");
        this.groupView = groupView;
    }

    public void onCreate() {
        log.pushTimer(this, "start");
        // Create OpenCV part:
        if (RUN_OPENCV) {
            opencv = new OpenCVInterface(this, this.mainActivity);
            JavaCameraView cameraView = new JavaCameraView(mainActivity,
                    JavaCameraView.CAMERA_ID_ANY);
            cameraView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            cameraView.enableFpsMeter();
            cameraView.setVisibility(JavaCameraView.GONE);
            groupView.addView(cameraView);
            opencv.onCreate(cameraView);
        }
        // Create OpenGL render part:
        if (RUN_RENDERER) {
            render = new RenderInterface(this);
            GLSurfaceView renderView = new GLSurfaceView(this.mainActivity);
            renderView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            groupView.addView(renderView);
            render.onCreate(renderView);
        }
        // Set layout things:
        mainActivity.getWindow().addFlags(WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON);
        log.log(TAG, "Framework created in " + log.popTimer(this).time
                + "ms.");
    }

    public void onResume() {
        if (RUN_OPENCV)
            opencv.onResume(this.mainActivity);
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

    /**
     * Method that receives candidate markers. Here, they are filtered for
     * the markers we want and partnered with the entities we'll show.
     *
     * @param markerCandidates The list containing all detected markers.
     */
    protected synchronized void updateList(ArrayList<Marker> markerCandidates) {
        try {
            detectedMarkers.put(markerCandidates);
            updatedData = true;
        } catch (InterruptedException e) {
            log.log(TAG, "Error updateing marker list!");
        }
    }

    /**
     * Method for the RenderInterface to check if an updated list has been
     * posted.
     *
     * @return True when a new list has been posted, false else.
     */
    protected synchronized boolean getListUpdateStatus() {
        return updatedData;
    }

    /**
     * Method for retrieving the entities to render with the respective
     * information.
     *
     * @return List containing all markers for now.
     */
    // TODO: change to entity list, not simply all marker (meaning add
    // filtering)
    protected synchronized ArrayList<Marker> getList() {
        try {
            updatedData = false;
            return detectedMarkers.take();
        } catch (InterruptedException e) {
            log.log(TAG, "Error passing marker list to renderer!");
            updatedData = false;
            return null;
        }
    }
}
