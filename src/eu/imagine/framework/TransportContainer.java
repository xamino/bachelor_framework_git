package eu.imagine.framework;

import org.opencv.core.Mat;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/9/13
 * Time: 4:37 PM
 */
class TransportContainer {

    protected Mat gray, rgba;

    protected TransportContainer(Mat gray, Mat rgba) {
        this.gray = gray;
        this.rgba = rgba;
    }
}
