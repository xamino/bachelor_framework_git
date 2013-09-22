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
    private float[] triangle = new float[]{
            -0.5f, -0.25f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Construct framework and pass a suitable viewgroup where we want
        // the results to show.
        framework = new MainInterface(this, (ViewGroup) findViewById(R.id.group));
        // Set some debugging flags:
        framework.setDebugFlag(Flags.DEBUG_LOGGING);
        framework.setDebugFlag(Flags.DEBUG_FRAME);
        framework.setDebugFlag(Flags.DEBUG_POLY);
        framework.setDebugFlag(Flags.DEBUG_DRAW_SAMPLING);
        framework.setDebugFlag(Flags.DEBUG_DRAW_MARKERS);
        // framework.setDebugFlag(Flags.DEBUG_DRAW_MARKER_ID);
        // Add some test entities:
        Tracking one = new Tracking(242, true, triangle);
        // Tracking two = new Tracking(234);
        framework.registerEntity(one);
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
