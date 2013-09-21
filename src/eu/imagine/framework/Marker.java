package eu.imagine.framework;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

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
    protected Mat grayTexture, rgbaTexture;
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
                  int angle, int id, boolean[][] pattern, Mat grayTexture) {
        this.id = id;
        this.angle = angle;
        this.pattern = pattern;
        // MUST COPY ALL VALUES!
        this.originalCorners = new MatOfPoint2f(originalCorners.toArray());
        this.markerPerspective = markerPerspective.clone();
        this.grayTexture = grayTexture.clone();
        this.rgbaTexture = new Mat();
        Imgproc.cvtColor(this.grayTexture, this.rgbaTexture ,
                Imgproc.COLOR_GRAY2RGBA);
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
        this.grayTexture = null;
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
        return "ID: "+id+" | angle: "+angle+"°";
    }

    protected int getID() {
        return id;
    }

    public Mat getPerspective() {
        return markerPerspective;
    }
}
