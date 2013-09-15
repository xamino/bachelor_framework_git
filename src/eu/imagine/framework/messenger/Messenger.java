package eu.imagine.framework.messenger;

import android.util.Log;
import eu.imagine.framework.controller.MainInterface;

import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 1:09 PM
 */
public class Messenger {

    /**
     * Variable that stores singleton instance.
     */
    private static Messenger INSTANCE;
    /**
     * The stack with which the TimerResult objects are managed.
     */
    private Stack<TimerResult> timerStack;

    /**
     * Method to return the singleton instance of Messenger.
     *
     * @return
     */
    public static Messenger getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Messenger();
        return INSTANCE;
    }

    /**
     * Private constructor that prepares class. To get an instance of
     * Messenger, ust the getInstance Method.
     */
    private Messenger() {
        this.timerStack = new Stack<TimerResult>();
    }

    /**
     * Method for logging a message to the Android log.
     *
     * @param tag     The tag used when logging after the internal tag.
     * @param content The content of the message to log.
     */
    public void log(final String tag, final String content) {
        Log.i("Messenger|" + tag, content);
    }

    /**
     * Method for logging only debugging content. DEBUG is set in
     * MainInterface.
     *
     * @param tag     The tag used after the internal tag.
     * @param content The content of the message to log.
     */
    public void debug(final String tag, final String content) {
        if (MainInterface.DEBUG)
            Log.d("Messenger|" + tag, content);
    }

    /**
     * Method for placing a timer object on the stack with the given label.
     *
     * @param label The label to remember for the timer.
     */
    public void pushTimer(final String label) {
        this.timerStack.push(new TimerResult(System.currentTimeMillis(),
                label));
    }

    /**
     * Method for reading a timer object. Returns a TimerResult object which
     * contains the time spent between push and pop,
     * and the label placed originally.
     *
     * @return The TimerResult object containing the time difference and the
     *         label. If the stack is empty (meaning more pops than pushes
     *         were done), an object with time "0" and label "EMPTY STACK" are
     *         returned.
     */
    public TimerResult popTimer() {
        if (this.timerStack.isEmpty())
            return new TimerResult(0, "EMPTY STACK");
        TimerResult poped = this.timerStack.pop();
        poped.time = System.currentTimeMillis() - poped.time;
        return poped;
    }
}
