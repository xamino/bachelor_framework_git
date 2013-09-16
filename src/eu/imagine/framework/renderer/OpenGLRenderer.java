package eu.imagine.framework.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import eu.imagine.framework.MainInterface;
import eu.imagine.framework.messenger.Messenger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:25 PM
 */
public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private Messenger log;
    private final String TAG = "OpenGLRenderer";

    public OpenGLRenderer() {
        log = Messenger.getInstance();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 0f, 0f, 0.1f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (MainInterface.DEBUG_LOGGING)
            log.pushTimer(this, "opengl frame");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        /*try {
            MainInterface.detectedMarkers.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
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
