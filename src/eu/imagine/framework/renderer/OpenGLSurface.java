package eu.imagine.framework.renderer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:26 PM
 */
public class OpenGLSurface extends GLSurfaceView {
    public OpenGLSurface(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // CALL LAST:
        setRenderer(new OpenGLRenderer());
    }
}
