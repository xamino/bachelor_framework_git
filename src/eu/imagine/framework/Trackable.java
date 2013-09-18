package eu.imagine.framework;

import org.opencv.core.Mat;

/**
 * Class that holds the relationship between marker and object to be rendered.
 */
class Trackable {

    final int ID;
    final String OBJECT;
    final Mat PERSPECTIVE;

    public Trackable(final int ID, final String OBJECT,
                     final Mat PERSPECTIVE) {
        this.ID = ID;
        this.OBJECT = OBJECT;
        this.PERSPECTIVE = PERSPECTIVE;
    }

    public String toString() {
        return "Trackable | ID:" + ID + " | " + OBJECT;
    }
}
