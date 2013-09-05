package com.example.OpenCV;

import android.util.Log;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/2/13
 * Time: 2:09 PM
 */
public class OpenCVWorker extends Thread {

    private final String TAG;
    private long time;

    Mat in, out, outMarker;
    ArrayList<MatOfPoint> contours, sub;
    MatOfPoint2f result;
    MatOfPoint2f vecOut;

    public OpenCVWorker(String tag) {
        super();
        super.setPriority(Thread.MAX_PRIORITY);
        this.TAG = tag;

        out = new Mat();
        in = new Mat();
        outMarker = new Mat();
        contours = new ArrayList<MatOfPoint>();
        sub = new ArrayList<MatOfPoint>();
        result = new MatOfPoint2f();
        vecOut = new MatOfPoint2f();

        // Set plane orienatation for perspective transform
        vecOut.fromArray(new Point[]{new Point(0f, 0f), new Point(0f, 1f),
                new Point(1f, 0f), new Point(1f, 1f)});

        Log.v(TAG, "[READY] Ready for task");
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                in = MyActivity.workerFeeder.take();
            } catch (InterruptedException e) {
                Log.e(TAG, "[FAIL] Error taking Mat from queue!");
                e.printStackTrace();
                continue;
            }
            Log.v(TAG, "[START] Doing task");
            time = System.currentTimeMillis();

            Imgproc.Canny(in, out, 150, 220);

            // Otherwise leak!
            contours.clear();
            sub.clear();

            Imgproc.findContours(out.clone(), contours, new Mat(), Imgproc.RETR_CCOMP,
                    Imgproc.CHAIN_APPROX_NONE);
            // Get all 4-vertice polygons from contours:
            for (int i = 0; i < contours.size(); i++) {
                MatOfPoint2f input = new MatOfPoint2f(contours.get(i).toArray());
                // Check area:
                if (128 > (int) input.size().area()) {
                    continue;
                }
                Imgproc.approxPolyDP(input, result, input.total() * 0.04,
                        true); // TODO THRESHOLD!
                // Only take contours with ==4 points
                if (result.total() == 4 && Imgproc.isContourConvex(new
                        MatOfPoint(result.toArray()))) {
                    // If okay, normalize
                    Core.normalize(result, result);
                    // Detect & correct orientation, id marker
                    Mat perspective = Imgproc.getPerspectiveTransform
                            (result, vecOut);
                    sub.add(contours.get(i));
                    Core.perspectiveTransform(in, outMarker, perspective);
                }
            }

            // Draw masked squares
            Mat mask = new Mat(in.rows(), in.cols(), in.type());
            Imgproc.drawContours(mask, sub, -1,
                    new Scalar(255), -1);
            Mat crop = new Mat(in.rows(), in.cols(), in.type());
            crop.setTo(new Scalar(0));
            in.copyTo(crop, mask);
            try {
                MyActivity.resultFeeder.put(crop);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Log.v(TAG, "[DONE] Finished task in " + (System.currentTimeMillis()
                    - time) + "ms – FOUND: " + sub.size());
        }
    }
}
