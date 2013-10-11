package eu.imagine.framework;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.Random;

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
    protected boolean ALLOW_DUPLICATE_MARKERS = false;
    protected boolean RUN_OPENCV = true;
    protected boolean RUN_RENDERER = true;
    protected boolean DEBUG_FRAME = false;

    // Linking variables to sub classes:
    private Messenger log;
    private final String TAG = "MainInterface";
    private OpenCVInterface opencv;
    private RenderInterface render;
    private Activity mainActivity;
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

    // Detector values
    protected int threshold = 100;

    public MainInterface(Activity mainActivity,
                         float[][] camMatrix, float[] distortionCoefficients) {
        this.log = Messenger.getInstance();
        log.log(TAG, "Constructing framework.");
        this.mainActivity = mainActivity;
        this.listeners = new ArrayList<HomographyListener>();
        this.allTrackables = new ArrayList<Entity>();
        this.detectedTrackables = new ArrayList<Trackable>();
        // Set camera matrix:
        this.camMatrix = camMatrix;
        this.distCoef = distortionCoefficients;
    }

    public void onCreate(ViewGroup groupView) {
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
            GLSurfaceView renderView = new GLSurfaceView(mainActivity.getApplicationContext());
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
     * Method for modifying the binary threshold for Detector. Only use after
     * checking with DEBUG_PREP_FRAME if the binarization actually is the
     * error! Used to set all 3 binarization methods, including Canny!
     *
     * @param value The value to set it to.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setBinaryThreshold(int value) {
        this.threshold = value;
    }

    /**
     * Call this method to allow markers to be detected where the Hamming
     * encoding is uncertain. Normally, these are discarded.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void allowUncertainHamming() {
        MarkerPatternHelper.hammingDeforce = true;
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
     * Function to create the marker for a given ID.
     *
     * @param ID The ID to encode into the marker.
     * @return The complete pattern that resembles the marker,
     *         including coded ID, coded direction, and borders.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean[][] getMarker(int ID) {
        return MarkerPatternHelper.createMarker(ID);
    }

    /**
     * Helper method for converting an .obj file to the correct float value.
     *
     * @param data  The string containing the obj file.
     * @param color The color to show. If null, each face will be colored
     *              randomly.
     * @param scale Value to scale object by.
     * @return The complete float representation of the object,
     *         ready to be converted to the FloatBuffer required.
     */
    public float[] importOBJ(String data, float[] color, float scale) {
        Random rand = new Random();
        boolean randColors = color == null;
        String[] lines = data.split("\n");
        ArrayList<float[]> vertices = new ArrayList<float[]>();
        ArrayList<int[]> faces = new ArrayList<int[]>();
        // Extract vertice and face info:
        for (String line : lines) {
            // If starts with v --> vertice
            if (line.charAt(0) == 'v') {
                // Split for spaces
                String[] floats = line.substring(1).trim().split(" ");
                float[] vert = new float[3];
                // get three coordinates
                for (int i = 0; i < vert.length; i++)
                    vert[i] = Float.valueOf(floats[i].trim());
                // save
                vertices.add(vert);
            } else if (line.charAt(0) == 'f') {
                String[] ints = line.substring(1).trim().split(" ");
                int[] face = new int[3];
                for (int i = 0; i < face.length; i++)
                    face[i] = Integer.valueOf(ints[i].trim());
                faces.add(face);
            }
        }
        // Set together float:
        float[] retData = new float[faces.size() * 21];
        // For each face
        for (int i = 0; i < faces.size(); i++) {
            // Put each vertice
            int[] face = faces.get(i);
            // Check color:
            if (randColors)
                color = new float[]{rand.nextFloat(), rand.nextFloat(),
                        rand.nextFloat(), 1f};
            for (int j = 0; j < face.length; j++) {
                float[] vertice = vertices.get(face[j] - 1);
                // apply scale
                retData[i * 21 + j * 7 + 0] = vertice[0] * scale;
                retData[i * 21 + j * 7 + 1] = vertice[1] * scale;
                retData[i * 21 + j * 7 + 2] = vertice[2] * scale;
                // write color data
                retData[i * 21 + j * 7 + 3] = color[0];
                retData[i * 21 + j * 7 + 4] = color[1];
                retData[i * 21 + j * 7 + 5] = color[2];
                retData[i * 21 + j * 7 + 6] = color[3];
            }
        }

        return retData;
    }

    /**
     * Helper function for setting flags.
     *
     * @param value The flag to set.
     * @param bool  The value to set that flag at.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setFlag(Flags value, boolean bool) {
        switch (value) {
            case ALLOW_DUPLICATE_MARKERS:
                this.ALLOW_DUPLICATE_MARKERS = bool;
                break;
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

                    for (Marker mark : markerCandidates)
                        // Add to rendering:
                        if (mark.getID() == tracking.getID()) {
                            detectedTrackables.add(new Trackable(mark.getID(),
                                    mark.getTranslation(),
                                    tracking.getFloatBuffer()));
                            // Simply continue with next entity if we don't
                            // want multiple renders:
                            if (!ALLOW_DUPLICATE_MARKERS)
                                break;
                        }
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
