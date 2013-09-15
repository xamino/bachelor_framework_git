package eu.imagine.app;

import android.app.Activity;
import android.os.Bundle;
import eu.imagine.framework.controller.MainInterface;

public class MyActivity extends Activity {

    /**
     * Stores instance of framework for access.
     */
    private MainInterface framework;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        framework = new MainInterface();
        framework.onCreate(savedInstanceState, this);
    }

    public void onResume() {
        super.onResume();
        framework.onResume(this);
    }

    public void onPause() {
        super.onPause();
        framework.onPause(this);
    }

    public void onDestroy() {
        super.onDestroy();
        framework.onDestroy(this);
    }
}
