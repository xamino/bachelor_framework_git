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
            0, 0, 0,
            1.0f, 0.0f, 0.0f, 1.0f, // red

            -0.5f, 0.5f, 0,
            0.0f, 1.0f, 0.0f, 1.0f,  // green

            0.5f, 0.5f, 0,
            0.0f, 0.0f, 1.0f, 1.0f, // blue

            0, 0, 0,
            1.0f, 0.0f, 0.0f, 0.5f, // red

            -0.5f, -0.5f, 0,
            0.0f, 1.0f, 0.0f, 0.5f,  // green

            0.5f, -0.5f, 0,
            0.0f, 0.0f, 1.0f, 0.5f // blue
    };

    private float[] twoData = new float[]{
            0, 0, 0,
            1.0f, 0.0f, 0.0f, 1.0f, // red

            -0.5f, 0.5f, 0,
            0.0f, 0.0f, 1.0f, 0.8f,  // blue

            0.5f, 0.5f, 0,
            0.0f, 0.0f, 1.0f, 0.8f // blue
    };

    /*
    Orig:
    private float[][] cameraMatrix = new float[][]{
            new float[]{1279.170989993096f, 0f, 639.5f},
            new float[]{0f, 1279.170989993096f, 359.5f},
            new float[]{0f, 0f, 1f}
    };
    private float[] distortionCoefficients = new float[]{
            0.3226026655144f, -2.722492888428328f, 0f, 0f, 5.676717782925402f
    };
     */

    // Camera matrix (here determined ahead of time)
    private float[][] cameraMatrix = new float[][]{
            new float[]{1280, 0, 640},
            new float[]{0, 1280, 360},
            new float[]{0, 0, 1}
    };
    // Distortion coefficients:
    private float[] distortionCoefficients = new float[]{
            0.3f, -2.7f, 0f, 0f, 5.7f
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagine);
        // Construct framework. This includes passing a reference to the
        // activity (here this), the viewgroup where it'll construct its
        // views, and the camera and distortioncoefficients.
        framework = new MainInterface(this, cameraMatrix, distortionCoefficients);
        // Set some debugging flags:
        // framework.allowUncertainHamming();
        framework.setFlag(Flags.ALLOW_DUPLICATE_MARKERS, true);
        // framework.setFlag(Flags.DEBUG_LOGGING, true);
        // framework.setFlag(Flags.DEBUG_FRAME, true);
        // framework.setFlag(Flags.DEBUG_POLY, true);
        // framework.setFlag(Flags.DEBUG_DRAW_MARKERS);
        // framework.setFlag(Flags.DEBUG_DRAW_MARKER_ID);
        // framework.setFlag(Flags.DEBUG_DRAW_SAMPLING);
        // Add some test entities:
        Tracking one = new Tracking(42, true, oneData);
        Tracking two = new Tracking(234, true, twoData);
        framework.registerEntity(one);
        framework.registerEntity(two);
        // Call on create:
        framework.onCreate((ViewGroup) findViewById(R.id
                .group));
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
