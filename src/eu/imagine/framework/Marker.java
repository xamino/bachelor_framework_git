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
    /**
     *
     */
    protected Mat grayTexture;
    /**
     * Contains angle.
     */
    private int angle;
    /**
     * The ID of the marker. Default is -1, which signifies that no ID has
     * been assigned yet.
     */
    private int id;
    /**
     * Contains the original corner coordinates.
     */
    private MatOfPoint2f originalCorners;

    private float[][] rotation;
    private float[] translation;
    private float[][] combined;

    /**
     * Constructor – WARNING, NULL fields possible as some values are not set
     * . Use setDebugParameters to set pattern and texture.
     *
     * @param angle
     * @param id
     */
    public Marker(MatOfPoint2f corners, int angle, int id) {
        this.id = id;
        this.angle = angle;
        this.originalCorners = new MatOfPoint2f(corners.toArray());
    }

    protected void setDebugParameters(boolean[][] pattern, Mat grayTexture) {
        this.grayTexture = grayTexture.clone();
        this.pattern = pattern;
    }

    /**
     * Function for easy access to MatOfPoint representation of corner points.
     *
     * @return
     */
    protected MatOfPoint getMOPCorners() {
        return new MatOfPoint(originalCorners.toArray());
    }

    protected boolean[][] getPattern() {
        return this.pattern;
    }

    public String toString() {
        return "ID: " + id + " | angle: " + angle + "°";
    }

    protected int getID() {
        return id;
    }

    protected MatOfPoint2f getCorners() {
        return originalCorners;
    }

    protected void setRotTranslation(float[][] rotMat, float[] transVec) {
        this.rotation = rotMat;
        this.translation = transVec;
    }

    protected float[][] getTranslation() {
        // If hasn't been calculated yet, do so:
        if (combined == null) {
            combined = new float[3][4];
            for (int row = 0; row < 3; row++) {
                // Copy rotation in:
                for (int column = 0; column < 3; column++)
                    combined[row][column] = rotation[row][column];
                // Add translation on end:
                combined[row][3] = translation[row];
            }
        }
        return combined;
    }
}
