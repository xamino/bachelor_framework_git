package eu.imagine.framework;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:25 PM
 */
class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final MainInterface mainInterface;
    private Messenger log;
    private final String TAG = "OpenGLRenderer";

    private ArrayList<Marker> detectedMarkers;

    protected OpenGLRenderer(MainInterface mainInterface) {
        this.log = Messenger.getInstance();
        this.mainInterface = mainInterface;
        this.detectedMarkers = new ArrayList<Marker>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 0f, 0f, 0.1f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (MainInterface.DEBUG_LOGGING)
            log.pushTimer(this, "opengl frame");
        // Clear:
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        // If new markerlist, get:
        if(mainInterface.getListUpdateStatus()) {
            this.detectedMarkers = mainInterface.getList();
            if (detectedMarkers == null) {
                log.log(TAG, "Error getting list!");
                detectedMarkers = new ArrayList<Marker>();
            }
        }
        if (!detectedMarkers.isEmpty()) {
            log.debug(TAG, "Rendering markers!");
        }
        if (MainInterface.DEBUG_LOGGING) {
            log.debug(TAG, "OpenGL rendered frame in " + log.popTimer(this).time
                    + "ms.");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}