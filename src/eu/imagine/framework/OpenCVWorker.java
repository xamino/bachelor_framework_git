package eu.imagine.framework;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/2/13
 * Time: 2:09 PM
 */
class OpenCVWorker extends Thread {

    private final String TAG;
    private TransportContainer in;
    private OpenCVInterface cvInterface;
    private Detector det;
    private Messenger log;

    protected OpenCVWorker(OpenCVInterface cvInterface,
                           MainInterface mainInterface,
                           String tag) {
        super();
        super.setPriority(Thread.MAX_PRIORITY);
        this.TAG = tag;
        this.det = new Detector(mainInterface);
        this.cvInterface = cvInterface;
        log = Messenger.getInstance();
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            in = cvInterface.getTransport();
            if (in == null)
                continue;
            det.detect(in.gray, in.rgba);
        }
    }
}
