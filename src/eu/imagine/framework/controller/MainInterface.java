package eu.imagine.framework.controller;

import android.app.Activity;
import android.os.Bundle;
import eu.imagine.framework.messenger.Messenger;
import eu.imagine.framework.opencv.OpenCVInterface;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 2:02 PM
 */
public class MainInterface {

    public static final boolean DEBUG = true;

    // Linking variables to sub classes:
    private Messenger log;
    private OpenCVInterface opencv;

    public void onCreate(Bundle savedInstanceState, Activity mainActivity) {
        log = Messenger.getInstance();
        opencv = new OpenCVInterface(mainActivity);
        opencv.onCreate(savedInstanceState, mainActivity);
    }

    public void onResume(Activity mainActivity) {
        opencv.onResume(mainActivity);
    }

    public void onPause(Activity mainActivity) {
        opencv.onPause(mainActivity);
    }

    public void onDestroy(Activity mainActivity) {
        opencv.onDestroy(mainActivity);
    }
}
