package com.example.OpenCV;

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
     * The Matrix containing the original rgba texture of the marker.
     */
    private Mat markerTexture;
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
     * @param originalCorners
     * @param markerPerspective
     * @param markerTexture
     */
    // TODO: Consider which values will really be needed on the OpenGL side
    // and remove all others, as they only decrease speed!
    public Marker(MatOfPoint2f originalCorners, Mat markerPerspective, Mat markerTexture
    ) {
        // MUST COPY ALL VALUES!
        this.originalCorners = new MatOfPoint2f(originalCorners.toArray());
        this.markerPerspective = markerPerspective.clone();
        this.markerTexture = markerTexture.clone();
    }

    /**
     * Function for easy access to MatOfPoint representation of corner points.
     * @return
     */
    public MatOfPoint getMOPCorners() {
        return new MatOfPoint(originalCorners.toArray());
    }

    /**
     * Returns reference to original marker texture. WARNING; changes will be
     * done on the original!
     * @return
     */
    public Mat getMarkerTextureReference() {
        return markerTexture;
    }
}
