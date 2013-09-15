package eu.imagine.framework.renderer;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.View;
import eu.imagine.framework.messenger.Messenger;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:05 PM
 */
public class RenderInterface{

    private Messenger log;
    private final String TAG = "RenderInterface";
    private GLSurfaceView mGLView;

    public RenderInterface() {
        log = Messenger.getInstance();
    }

    public void onCreate(Activity mainActivity, View renderView) {
        mGLView = new OpenGLSurface(mainActivity.getApplicationContext());
        renderView = mGLView;
        log.log(TAG, "Created.");
    }

    public void onResume() {
        mGLView.onResume();
    }

    public void onPause() {
        mGLView.onPause();
    }
}
