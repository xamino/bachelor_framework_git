package eu.imagine.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import eu.imagine.R;
import eu.imagine.framework.Flags;
import eu.imagine.framework.MainInterface;

public class MyActivity extends Activity {

    /**
     * Stores instance of framework for access.
     */
    private MainInterface framework;

    /**
     * Example object to render. Format is 3 coords followed by 4 colors.
     */
    private float[] oneData = new float[]{
            -0.5f, -0.5f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, -0.5f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.5f, 0.5f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f
    };

    // Camera matrix (here determined ahead of time)
    private float[][] cameraMatrix = new float[][]{
            new float[]{1279.170989993096f, 0f, 639.5f},
            new float[]{0f, 1279.170989993096f, 359.5f},
            new float[]{0f, 0f, 1f}
    };
    // Distortion coefficients:
    private float[] distortionCoefficients = new float[]{
            0.3226026655144f, -2.722492888428328f, 0f, 0f, 5.676717782925402f
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Construct framework. This includes passing a reference to the
        // activity (here this), the viewgroup where it'll construct its
        // views, and the camera and distortioncoefficients.
        framework = new MainInterface(this, (ViewGroup) findViewById(R.id
                .group), cameraMatrix, distortionCoefficients);
        // Set some debugging flags:
        // framework.allowUncertainHamming();
        framework.setDebugFlag(Flags.DEBUG_LOGGING);
        framework.setDebugFlag(Flags.DEBUG_FRAME);
        framework.setDebugFlag(Flags.USE_CANNY);
        // framework.setDebugFlag(Flags.DEBUG_CONTOURS);
        // framework.setDebugFlag(Flags.DEBUG_POLY);
        framework.setDebugFlag(Flags.DEBUG_DRAW_MARKERS);
        framework.setDebugFlag(Flags.DEBUG_DRAW_MARKER_ID);
        framework.setDebugFlag(Flags.DEBUG_DRAW_SAMPLING);
        // Add some test entities:
        // Tracking one = new Tracking(242, true, oneData);
        // Tracking two = new Tracking(9, true, oneData);
        // framework.registerEntity(one);
        // framework.registerEntity(two);
        // Call on create:
        framework.onCreate();
    }

    public void onResume() {
        super.onResume();
        // Call on resume:
        framework.onResume();
    }

    public void onPause() {
        super.onPause();
        // Call on pause:
        framework.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        // Call on destroy:
        framework.onDestroy();
    }
}
