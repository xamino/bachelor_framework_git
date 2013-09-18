package eu.imagine.framework;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import org.opencv.android.JavaCameraView;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 2:02 PM
 */
public class MainInterface {

    // Allow debug logging:
    protected static boolean DEBUG_LOGGING = false;
    protected static boolean DEBUG_FRAME_LOGGING = false;
    protected boolean ONLY_HOMOGRAPHY = false;
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
    private ArrayList<Marker> detectedMarkers;
    private boolean updatedData = false;

    // Store Homography listeners:
    private ArrayList<HomographyListener> listeners;

    public MainInterface(Activity mainActivity, ViewGroup groupView) {
        this.log = Messenger.getInstance();
        log.log(TAG, "Constructing framework.");
        this.mainActivity = mainActivity;
        // Sanity check:
        if (groupView == null)
            log.log(TAG, "Framework will crash, groupView is NULL!");
        this.groupView = groupView;
        listeners = new ArrayList<HomographyListener>();
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

    public void registerListener(HomographyListener homographyListener) {
        this.listeners.add(homographyListener);
    }

    public void removeListener(HomographyListener homographyListener) {
        this.listeners.remove(homographyListener);
    }

    /**
     * Method for setting debug flags.
     *
     * @param value The flag to set true.
     */
    public void setDebugFlag(Flags value) {
        setFlag(value, true);
    }

    /**
     * Method for removing debug flags.
     *
     * @param value The flag to set to false.
     */
    public void removeDebugFlag(Flags value) {
        setFlag(value, false);
    }

    /**
     * Helper function for setting flags.
     *
     * @param value The flag to set.
     * @param bool  The value to set that flag at.
     */
    private void setFlag(Flags value, boolean bool) {
        switch (value) {
            case ONLY_HOMOGRAPHY:
                this.RUN_RENDERER = !bool;
                this.ONLY_HOMOGRAPHY = bool;
                break;
            case RUN_OPENCV:
                this.RUN_OPENCV = bool;
                break;
            case RUN_RENDERER:
                this.RUN_RENDERER = bool;
                break;
            case DEBUG_FRAME_LOGGING:
                MainInterface.DEBUG_FRAME_LOGGING = bool;
                break;
            case DEBUG_LOGGING:
                MainInterface.DEBUG_LOGGING = bool;
                break;
            case DEBUG_FRAME:
                this.DEBUG_FRAME = bool;
                break;
            case USE_CANNY:
                Detector.USE_CANNY = bool;
                break;
            case DEBUG_PREP_FRAME:
                Detector.DEBUG_PREP_FRAME = bool;
                break;
            case DEBUG_CONTOURS:
                Detector.DEBUG_CONTOURS = bool;
                break;
            case DEBUG_POLY:
                Detector.DEBUG_POLY = bool;
                break;
            case DEBUG_DRAW_MARKERS:
                Detector.DEBUG_DRAW_MARKERS = bool;
                break;
            case DEBUG_DRAW_SAMPLING:
                Detector.DEBUG_DRAW_SAMPLING = bool;
                break;
            case DEBUG_DRAW_MARKER_ID:
                Detector.DEBUG_DRAW_MARKER_ID = bool;
                break;
            default:
                log.log(TAG, "Failed to set flag " + value + "!");
                break;
        }
    }

    /**
     * Method that receives candidate markers. Here, they are filtered for
     * the markers we want and partnered with the entities we'll show.
     *
     * @param markerCandidates The list containing all detected markers.
     */
    protected synchronized void updateList(ArrayList<Marker> markerCandidates) {
        detectedMarkers = markerCandidates;
        updatedData = true;
        if (ONLY_HOMOGRAPHY) {
            for (HomographyListener listener : listeners) {
                listener.receiveHomographies(detectedMarkers);
            }
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
        updatedData = false;
        return detectedMarkers;
    }
}
