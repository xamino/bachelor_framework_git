package eu.imagine.framework;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:25 PM
 */
public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final MainInterface mainInterface;
    private Messenger log;
    private final String TAG = "OpenGLRenderer";

    private ArrayList<Trackable> toRender;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];

    protected OpenGLRenderer(MainInterface mainInterface) {
        this.log = Messenger.getInstance();
        this.mainInterface = mainInterface;
        this.toRender = new ArrayList<Trackable>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 0f, 0f, 0.5f);
    }

    // TODO: Check call to OpenGL ES API with no current context (logged once
    // per thread)call to OpenGL ES API with no current context (logged once
    // per thread)
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
            // Set the camera position (View matrix)
            Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
            for (Trackable trackable : toRender) {
                trackable.draw(mMVPMatrix);
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

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Messenger.getInstance().log("OpenGLRenderer",
                    glOperation + ": glError" + " " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
