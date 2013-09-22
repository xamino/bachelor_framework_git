package eu.imagine.framework;

import org.opencv.core.Mat;

import java.nio.FloatBuffer;

/**
 * Class that holds the relationship between marker and object to be rendered.
 */
class Trackable {

    private final FloatBuffer FLOATBUFFER;
    private final int ID;
    private final Mat PERSPECTIVE;

    public Trackable(final int ID, final Mat PERSPECTIVE,
                     final FloatBuffer FLOATBUFFER) {
        this.ID = ID;
        this.PERSPECTIVE = PERSPECTIVE;
        this.FLOATBUFFER = FLOATBUFFER;
    }

    public String toString() {
        return "Trackable | ID:" + ID;
    }

    public FloatBuffer getFloatbuffer() {
        return this.FLOATBUFFER;
    }

    public Mat getPerspective() {
        return PERSPECTIVE;
    }
}
