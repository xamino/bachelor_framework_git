package eu.imagine.framework.opencv;

import org.opencv.core.Mat;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/9/13
 * Time: 4:37 PM
 */
public class TransportContainer {

    public Mat gray, rgba;

    public TransportContainer(Mat gray, Mat rgba) {
        this.gray = gray;
        this.rgba = rgba;
    }
}
