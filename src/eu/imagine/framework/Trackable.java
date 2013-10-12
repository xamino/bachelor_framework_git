package eu.imagine.framework;

import java.nio.FloatBuffer;

/**
 * Class that holds the relationship between marker and object to be rendered.
 */
class Trackable {

    private FloatBuffer FLOATBUFFER;
    private int ID;
    private float[] TRANSLATION;

    Trackable(final int ID,
                     float[] translation, final FloatBuffer FLOATBUFFER) {
        this.ID = ID;
        this.FLOATBUFFER = FLOATBUFFER;
        this.TRANSLATION = translation;
    }

    public String toString() {
        return "Trackable | ID:" + ID;
    }

    FloatBuffer getFloatbuffer() {
        return this.FLOATBUFFER;
    }

    float[] getTRANSLATION() {
        return this.TRANSLATION;
    }
}
