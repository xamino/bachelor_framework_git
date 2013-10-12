package eu.imagine.framework;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

/**
 * Simple interface for handling the OpenGL surface and renderer.
 */
@SuppressWarnings("FieldCanBeLocal")
class RenderInterface {

    private final MainInterface mainInterface;
    private Messenger log;
    private final String TAG = "RenderInterface";
    private GLSurfaceView mGLView;

    protected RenderInterface(MainInterface mainInterface) {
        log = Messenger.getInstance();
        this.mainInterface = mainInterface;
    }

    /**
     * Method for creating the surface where the OpenGL drawing will happen.
     * Notably we set the transparent options here.
     *
     * @param renderView The view to use.
     */
    protected void onCreate(GLSurfaceView renderView) {
        mGLView = renderView;
        mGLView.setEGLContextClientVersion(2);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.setRenderer(new OpenGLRenderer(mainInterface));
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLView.setZOrderMediaOverlay(true);

        log.log(TAG, "Created.");
    }

    protected void onResume() {
        mGLView.onResume();
    }

    protected void onPause() {
        mGLView.onPause();
    }
}
