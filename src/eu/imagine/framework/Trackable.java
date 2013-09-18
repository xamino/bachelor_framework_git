package eu.imagine.framework;

import org.opencv.core.Mat;

/**
 * Class that holds the relationship between marker and object to be rendered.
 */
class Trackable {

    final int ID;
    final Mat PERSPECTIVE;
    final OpenGLDraw DRAW;

    public Trackable(final int ID, final Mat PERSPECTIVE,
                     final OpenGLDraw DRAW) {
        this.ID = ID;
        this.PERSPECTIVE = PERSPECTIVE;
        this.DRAW = DRAW;
    }

    public String toString() {
        return "Trackable | ID:" + ID;
    }

    public void draw() {
        DRAW.draw();
    }
}
