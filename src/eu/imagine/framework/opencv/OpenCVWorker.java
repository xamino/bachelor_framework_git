package eu.imagine.framework.opencv;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/2/13
 * Time: 2:09 PM
 */
public class OpenCVWorker extends Thread {

    private final String TAG;
    private TransportContainer in;
    private Detector det;

    public OpenCVWorker(String tag) {
        super();
        super.setPriority(Thread.MAX_PRIORITY);
        this.TAG = tag;
        this.det = new Detector();
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                in = OpenCVInterface.workerFeeder.take();
            } catch (InterruptedException e) {
                Log.e(TAG, "[FAIL] Error taking Mat from queue!");
                e.printStackTrace();
                continue;
            }
            det.detect(in.gray, in.rgba);
        }
    }
}
