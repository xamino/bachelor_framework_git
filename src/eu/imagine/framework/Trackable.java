package eu.imagine.framework;

import java.nio.FloatBuffer;

/**
 * Class that holds the relationship between marker and object to be rendered.
 */
class Trackable {

    private FloatBuffer FLOATBUFFER;
    private int ID;
    private float[] TRANSLATION, INVERSE;

    public Trackable(final int ID,
                     float[] translation, final FloatBuffer FLOATBUFFER) {
        this.ID = ID;
        this.FLOATBUFFER = FLOATBUFFER;
        this.TRANSLATION = translation;
    }

    public String toString() {
        return "Trackable | ID:" + ID;
    }

    public FloatBuffer getFloatbuffer() {
        return this.FLOATBUFFER;
    }

    public float[] getTRANSLATION() {
        return this.TRANSLATION;
    }

    public float[] getINVERSE() {
        return INVERSE;
    }

    public void setInverse(float[] inverse) {
        this.INVERSE = inverse;
    }
}
