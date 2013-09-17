package eu.imagine.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import eu.imagine.R;
import eu.imagine.framework.MainInterface;

public class MyActivity extends Activity {

    /**
     * Stores instance of framework for access.
     */
    private MainInterface framework;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        framework = new MainInterface(this, (ViewGroup) findViewById(R.id.group));
        framework.onCreate();
    }

    public void onResume() {
        super.onResume();
        framework.onResume();
    }

    public void onPause() {
        super.onPause();
        framework.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        framework.onDestroy();
    }
}
