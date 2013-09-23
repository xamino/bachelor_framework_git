package eu.imagine.framework;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/23/13
 * Time: 2:20 PM
 */
public class ConvertHelper {

    private static ConvertHelper INSTANCE;

    public static ConvertHelper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ConvertHelper();
        return INSTANCE;
    }

    private ConvertHelper() {
    }

    /**
     * @param array
     * @return
     */
    public Mat floatToMat(final float[][] array) {
        Mat retMat = new Mat(array.length, array[0].length, CvType.CV_32F);
        for (int row = 0; row < array.length; row++)
            for (int column = 0; column < array[row].length; column++) {
                retMat.put(row, column, array[row][column]);
            }
        return retMat;
    }

    /**
     * @param array
     * @return
     */
    public Mat floatToMat(final float[] array) {
        Mat retMat = new Mat(1, array.length, CvType.CV_32F);
        for (int i = 0; i < array.length; i++)
            retMat.put(0, i, array[i]);
        return retMat;
    }
}
