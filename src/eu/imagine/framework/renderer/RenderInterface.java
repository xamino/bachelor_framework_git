package eu.imagine.framework.renderer;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import eu.imagine.framework.messenger.Messenger;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:05 PM
 */
public class RenderInterface {

    private Messenger log;
    private final String TAG = "RenderInterface";
    private GLSurfaceView mGLView;

    public RenderInterface() {
        log = Messenger.getInstance();
    }

    public void onCreate(GLSurfaceView renderView) {
        mGLView = renderView;
        mGLView.setEGLContextClientVersion(2);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.setRenderer(new OpenGLRenderer());
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLView.setZOrderOnTop(true);

        log.log(TAG, "Created.");
    }

    public void onResume() {
        mGLView.onResume();
    }

    public void onPause() {
        mGLView.onPause();
    }
}
