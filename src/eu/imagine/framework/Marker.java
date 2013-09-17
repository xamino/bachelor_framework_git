package eu.imagine.framework;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/7/13
 * Time: 2:08 PM
 */
public class Marker {

    /**
     * Stores bool representation of pattern
     */
    private boolean[][] pattern;
    protected Mat texture;
    /**
     * Contains angle.
     */
    private int angle;
    /**
     * Contains the perspective transform.
     */
    private Mat markerPerspective;
    /**
     * Contains the original corner coordinates.
     */
    private MatOfPoint2f originalCorners;
    /**
     * The ID of the marker. Default is -1, which signifies that no ID has
     * been assigned yet.
     */
    private int id = -1;

    /**
     * Constructor. COPIES all values!
     *
     * @param originalCorners
     * @param markerPerspective
     */
    // TODO: Consider which values will really be needed on the OpenGL side
    // and remove all others, as they only decrease speed!
    public Marker(MatOfPoint2f originalCorners, Mat markerPerspective,
                  int angle, int id, boolean[][] pattern, Mat texture) {
        this.id = id;
        this.angle = angle;
        this.pattern = pattern;
        // MUST COPY ALL VALUES!
        this.originalCorners = new MatOfPoint2f(originalCorners.toArray());
        this.markerPerspective = markerPerspective.clone();
        this.texture = texture.clone();
    }

    /**
     * Faster constructor – WARNING, NULL fields possible as some are unused
     * here!
     *
     * @param markerPerspective
     * @param angle
     * @param id
     */
    public Marker(Mat markerPerspective,
                  int angle, int id) {
        this.id = id;
        this.angle = angle;
        this.markerPerspective = markerPerspective.clone();
        this.pattern = null;
        this.texture = null;
        this.originalCorners = null;
    }

    /**
     * Function for easy access to MatOfPoint representation of corner points.
     *
     * @return
     */
    protected MatOfPoint getMOPCorners() {
        if (originalCorners == null)
            return null;
        else
            return new MatOfPoint(originalCorners.toArray());
    }

    protected boolean[][] getPattern() {
        return this.pattern;
    }

    public String toString() {
        return "" + angle + "°|id:" + id + "\n" + pattern[0][0] + "|" + pattern[0][1
                ] + "|" + pattern[0][2] + "|" + pattern[0][3] + "\n"
                + pattern[1][0] + "|" + pattern[1][1
                ] + "|" + pattern[1][2] + "|" + pattern[1][3] + "\n"
                + pattern[2][0] + "|" + pattern[2][1
                ] + "|" + pattern[2][2] + "|" + pattern[2][3] + "\n"
                + pattern[3][0] + "|" + pattern[3][1
                ] + "|" + pattern[3][2] + "|" + pattern[3][3] + "\n";
    }

    protected int getID() {
        return id;
    }
}
