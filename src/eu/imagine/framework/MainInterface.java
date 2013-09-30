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
    private ConvertHelper CONVERT;
    private final Object synLock = new Object();

    // Store markers per frame:
    private ArrayList<Entity> allTrackables;
    private ArrayList<Trackable> detectedTrackables;
    private boolean updatedData = false;

    // Store Homography listeners:
    private ArrayList<HomographyListener> listeners;

    // Values:
    float[][] camMatrix;
    float[] distCoef;

    public MainInterface(Activity mainActivity, ViewGroup groupView,
                         float[][] camMatrix, float[] distortionCoefficients) {
        this.log = Messenger.getInstance();
        log.log(TAG, "Constructing framework.");
        this.mainActivity = mainActivity;
        // Sanity check:
        if (groupView == null)
            log.log(TAG, "Framework will crash, groupView is NULL!");
        this.groupView = groupView;
        this.listeners = new ArrayList<HomographyListener>();
        this.allTrackables = new ArrayList<Entity>();
        this.detectedTrackables = new ArrayList<Trackable>();
        this.CONVERT = ConvertHelper.getInstance();
        // Set camera matrix:
        this.camMatrix = camMatrix;
        this.distCoef = distortionCoefficients;
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
     * Add a homogrpahy listener. NOTE: Will only be notified if the
     * ONLY_HOMOGRAPHY flag has been set!
     *
     * @param homographyListener The object to register.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void registerListener(HomographyListener homographyListener) {
        this.listeners.add(homographyListener);
    }

    /**
     * Remove a homography listener.
     *
     * @param homographyListener The object to remove.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeListener(HomographyListener homographyListener) {
        this.listeners.remove(homographyListener);
    }

    /**
     * Add an entity to be detected and rendered. NOTE: References are used,
     * meaning that changes outside of the framework WILL have effects here
     * too!
     *
     * @param entity The entity to register.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void registerEntity(Entity entity) {
        this.allTrackables.add(entity);
    }

    /**
     * Remove an entity from the list.
     *
     * @param entity Entity to remove.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeEntity(Entity entity) {
        this.allTrackables.remove(entity);
    }

    /**
     * Method for setting debug flags.
     *
     * @param value The flag to set true.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDebugFlag(Flags value) {
        setFlag(value, true);
    }

    /**
     * Method for removing debug flags.
     *
     * @param value The flag to set to false.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeDebugFlag(Flags value) {
        setFlag(value, false);
    }

    /**
     * Helper function for setting flags.
     *
     * @param value The flag to set.
     * @param bool  The value to set that flag at.
     */
    @SuppressWarnings("UnusedDeclaration")
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
            case USE_ADAPTIVE:
                Detector.USE_ADAPTIVE = bool;
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
    protected void updateList(ArrayList<Marker> markerCandidates) {
        synchronized (synLock) {

            updatedData = true;
            if (ONLY_HOMOGRAPHY) {
                for (HomographyListener listener : listeners) {
                    listener.receiveHomographies(markerCandidates);
                }
            }
            // Else case means we'll be rendering it,
            // so we filter the detected markers against the entity pairs we
            // want:
            else {
                // Remove old list:
                detectedTrackables.clear();
                // Now go through all trackables:
                for (Entity tracking : allTrackables) {
                    // Check if we want to render it:
                    if (!tracking.getVisibility())
                        continue;
                    // Now add it to the renderTrackable list if marker found:
                    Marker toRemove = null;
                    for (Marker mark : markerCandidates) {
                        // Add to rendering:
                        if (mark.getID() == tracking.getID()) {
                            detectedTrackables.add(new Trackable(mark.getID(),
                                    mark.getTranslation(),
                                    tracking.getFloatBuffer()));
                            toRemove = mark;
                            break;
                        }
                    }
                    // Remove marker for performance reasons
                    if (toRemove != null)
                        markerCandidates.remove(toRemove);
                }
            }
        }
    }

    /**
     * Method for the RenderInterface to check if an updated list has been
     * posted.
     *
     * @return True when a new list has been posted, false else.
     */

    protected boolean getListUpdateStatus() {
        synchronized (synLock) {
            return updatedData;
        }
    }

    /**
     * Method for retrieving the entities to render with the respective
     * information.
     *
     * @return List containing all markers for now.
     */
    protected ArrayList<Trackable> getList() {
        synchronized (synLock) {
            updatedData = false;
            //noinspection unchecked
            return (ArrayList<Trackable>) detectedTrackables.clone();
        }
    }
}
