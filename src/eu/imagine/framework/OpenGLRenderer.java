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

    private ArrayList<Trackable> toRender;

    protected OpenGLRenderer(MainInterface mainInterface) {
        this.log = Messenger.getInstance();
        this.mainInterface = mainInterface;
        this.toRender = new ArrayList<Trackable>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (MainInterface.DEBUG_FRAME_LOGGING)
            log.pushTimer(this, "opengl frame");
        // Clear:
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        // If new markerlist, get:
        if (mainInterface.getListUpdateStatus()) {
            this.toRender = mainInterface.getList();
            if (toRender == null) {
                log.log(TAG, "Error getting list!");
                toRender = new ArrayList<Trackable>();
            }
        }
        // ------------------------ RENDER ------------------------
        if (!toRender.isEmpty()) {
            for (Trackable trackable : toRender) {
                log.debug(TAG, "Rendering " + trackable.toString() + ".");
                trackable.draw();
            }
        }
        if (MainInterface.DEBUG_FRAME_LOGGING) {
            log.debug(TAG, "OpenGL rendered frame in " + log.popTimer(this).time
                    + "ms.");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}
