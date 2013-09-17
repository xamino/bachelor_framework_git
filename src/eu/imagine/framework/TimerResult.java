package eu.imagine.framework;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 1:22 PM
 */
class TimerResult {
    protected long time;
    protected String label;

    protected TimerResult(long time, String label) {
        this.time = time;
        this.label = label;
    }
}
