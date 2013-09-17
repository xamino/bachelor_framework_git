package eu.imagine.framework;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:05 PM
 */
class RenderInterface {

    private final MainInterface mainInterface;
    private Messenger log;
    private final String TAG = "RenderInterface";
    private GLSurfaceView mGLView;

    protected RenderInterface(MainInterface mainInterface) {
        log = Messenger.getInstance();
        this.mainInterface = mainInterface;
    }

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
