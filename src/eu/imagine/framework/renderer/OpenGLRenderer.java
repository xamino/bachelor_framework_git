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
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        try {
            MainInterface.detectedMarkers.take();
            log.log(TAG, "Got markers!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}
