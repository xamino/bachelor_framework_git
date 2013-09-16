package eu.imagine.framework.opencv;

import eu.imagine.framework.MainInterface;
import eu.imagine.framework.messenger.Messenger;
import eu.imagine.framework.messenger.TimerResult;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Tamino Hartmann
 * Date: 9/9/13
 * Time: 3:54 PM
 */

/**
 * Class encapsulates the main work method, allowing a simpler view from
 * outside.
 */
public class Detector {

    private Messenger log;
    private final String TAG = "Detector";

    // FLAGS (compositeFrameOut order!)
    public boolean USE_CANNY = false;
    public boolean DEBUG_PREP_FRAME = false;
    public boolean DEBUG_CONTOURS = false;
    public boolean DEBUG_POLY = true;
    public boolean DEBUG_DRAW_MARKERS = true;
    public boolean DEBUG_DRAW_MARKER_ID = false;
    public boolean DEBUG_DRAW_SAMPLING = false;

    // Important numbers
    private final int MARKER_GRID = 6;
    private final int MARKER_SQUARE = 3;
    private final int MARKER_SIZE = MARKER_GRID * MARKER_SQUARE;
    private final int RENDER_SCALE = 5;
    private final int SAMPLING_ERRORS = 4;
    private int step = MARKER_SIZE / MARKER_GRID;
    private int half = step / 2;

    // Important reused vars
    private Mat out, compositeFrameOut, tempPerspective;
    private ArrayList<MatOfPoint> contours, contoursAll;
    private ArrayList<Marker> markerCandidates;
    private MatOfPoint2f result, standardMarker;

    // Colors
    private final double[] GREEN = new double[]{0, 255, 0, 0};
    private final double[] RED = new double[]{255, 0, 0, 0};
    private final double[] WHITE = new double[]{255, 255, 255, 0};
    private final double[] BLACK = new double[]{0, 0, 0, 0};

    public Detector() {
        log = Messenger.getInstance();
        out = new Mat();
        compositeFrameOut = new Mat();
        contours = new ArrayList<MatOfPoint>();
        contoursAll = new ArrayList<MatOfPoint>();
        markerCandidates = new ArrayList<Marker>();
        result = new MatOfPoint2f();
        // Prepare standard marker:
        standardMarker = new MatOfPoint2f();
        standardMarker.fromArray(new Point(0, MARKER_SIZE),
                new Point(0, 0),
                new Point(MARKER_SIZE, 0), new Point(MARKER_SIZE, MARKER_SIZE));
    }

    public Mat detect(Mat gray, Mat rgba) {
        if (MainInterface.DEBUG_LOGGING)
            log.pushTimer(this, "frame");
        contours.clear();
        contoursAll.clear();
        markerCandidates.clear();

        if (USE_CANNY) {
            Imgproc.Canny(gray, out, 50, 150);
        } else {
            Imgproc.adaptiveThreshold(gray, out, 255,
                    Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY, 81, 7);
        }

        if (DEBUG_PREP_FRAME)
            return out;

        Imgproc.findContours(out, contoursAll, new Mat(),
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_NONE);
        // Remove too small contours:
        for (MatOfPoint contour : contoursAll) {
            if (contour.total() > 200)
                contours.add(contour);
        }

        compositeFrameOut = rgba;

        // DEBUG_LOGGING
        if (DEBUG_CONTOURS) {
            Imgproc.drawContours(compositeFrameOut, contours, -1, new Scalar(255, 0, 0), 2);
            return compositeFrameOut;
        }

        // Get all 4-vertice polygons from contours:
        for (MatOfPoint contour : contours) {
            MatOfPoint2f input = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(input, result, input.total() * 0.10,
                    true);
            // Only take contours with ==4 points
            if (result.total() != 4 || !Imgproc.isContourConvex(new
                    MatOfPoint(result.toArray()))) {
                continue;
            }
            // Calculate perspective transform
            tempPerspective = Imgproc.getPerspectiveTransform(result,
                    standardMarker);
            // Apply to get marker texture
            Imgproc.warpPerspective(rgba, out, tempPerspective,
                    new Size(MARKER_SIZE, MARKER_SIZE));
            // Check if marker
            Marker mark = isMarker(result, tempPerspective, out);
            if (mark == null)
                continue;
            // Save marker candidate
            markerCandidates.add(mark);
        }

        if (DEBUG_POLY) {
            ArrayList<MatOfPoint> temp = new ArrayList<MatOfPoint>();
            for (Marker mark : markerCandidates) temp.add(mark.getMOPCorners());
            Core.polylines(compositeFrameOut, temp, true, new Scalar(255, 0, 0), 2);
        }

        if ((DEBUG_DRAW_MARKERS || DEBUG_DRAW_MARKER_ID) && !markerCandidates
                .isEmpty()) {
            if (DEBUG_DRAW_MARKERS) {
                int count = 0;
                for (Marker mark : markerCandidates) {
                    Mat tempText = mark.texture;
                    int xoffset = RENDER_SCALE * MARKER_SIZE * count;
                    count++;
                    for (int i = 0; i < RENDER_SCALE * MARKER_SIZE; i++)
                        for (int j = 0; j < RENDER_SCALE * MARKER_SIZE; j++) {
                            compositeFrameOut.put(i, xoffset + j,
                                    tempText.get(i / RENDER_SCALE,
                                            j / RENDER_SCALE));
                        }
                }
            }
            if (DEBUG_DRAW_MARKER_ID) {
                int count = 0;
                for (Marker mark : markerCandidates) {
                    boolean[][] tempBool = mark.getPattern();
                    int yoffset = RENDER_SCALE * MARKER_SIZE *
                            (DEBUG_DRAW_MARKERS ? 1 : 0);
                    int xoffset = RENDER_SCALE * MARKER_SIZE * count;
                    count++;
                    for (int i = 0; i < RENDER_SCALE * MARKER_SIZE; i++)
                        for (int j = 0; j < RENDER_SCALE * MARKER_SIZE; j++) {
                            int x = i / MARKER_SQUARE / RENDER_SCALE,
                                    y = j / MARKER_SQUARE / RENDER_SCALE;
                            if (x == 0 || y == 0 || x == MARKER_GRID - 1 || y ==
                                    MARKER_GRID - 1)
                                compositeFrameOut.put(yoffset + i, xoffset + j,
                                        BLACK);
                            else
                                compositeFrameOut.put(yoffset + i, xoffset + j,
                                        tempBool[x - 1][y - 1] ?
                                                WHITE : BLACK);
                        }
                }
            }
        }

        if (MainInterface.DEBUG_LOGGING) {
            TimerResult timer = log.popTimer(this);
            log.debug(TAG, "Detected " + markerCandidates.size() + " markers " +
                    "in " + timer.time + "ms.");
        }

        /*
        try {
            MainInterface.detectedMarkers.put(markerCandidates);
        } catch (InterruptedException e) {
            log.log(TAG, "Error placing markers in queue!");
            e.printStackTrace();
        }
        */

        return compositeFrameOut;
    }

    private Marker isMarker(MatOfPoint2f result, Mat tempPerspective, Mat texture) {
        boolean[][] pattern = new boolean[4][4];
        Imgproc.cvtColor(texture, texture, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.threshold(texture, texture, 40, 255, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(texture, texture, Imgproc.COLOR_GRAY2RGBA);

        // reset error allowance
        int errorAllowance = 0;
        // Check border:
        for (int i = 1; i < MARKER_GRID - 1; i++) {
            if (testSample(half + i * step, half, texture) > 0)
                errorAllowance++;
            if (testSample(half, half + i * step, texture) > 0)
                errorAllowance++;
            if (testSample(half + i * step, MARKER_SIZE - 1 - half, texture) > 0)
                errorAllowance++;
            if (testSample(MARKER_SIZE - 1 - half, half + i * step, texture) > 0)
                errorAllowance++;
        }

        if (errorAllowance > SAMPLING_ERRORS)
            return null;
        // Now ID it:
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                pattern[i][j] = (testSample(half + (i + 1) * step,
                        half + (j + 1) * step, texture) > 0);
            }

        // Check corners & get rotation:
        int angle;
        if (!pattern[0][0] && pattern[0][3] && pattern[3][0] && pattern[3][3]) {
            angle = -90;
        } else if (pattern[0][0] && !pattern[0][3] &&
                pattern[0][3] && pattern[3][3]) {
            angle = 0;
        } else if (pattern[0][0] && pattern[0][3] &&
                !pattern[3][0] && pattern[3][3]) {
            angle = 180;
        } else if (pattern[0][0] && pattern[0][3] &&
                pattern[3][0] && !pattern[3][3]) {
            angle = 90;
        } else {
            return null;
        }
        int id = -1;
        // TODO ID THE MARKER!
        return new Marker(result, tempPerspective, angle, id, pattern,
                texture);
    }

    /**
     * @param x       X-Coordinate.
     * @param y       Y-Coordinate.
     * @param texture Reference to texture.
     * @return Negative for black, positive for white
     */
    private int testSample(int x, int y, Mat texture) {
        int countBlack = 0;
        for (int i = -MARKER_SQUARE / 2; i <= MARKER_SQUARE / 2; i++)
            for (int j = -MARKER_SQUARE / 2; j <= MARKER_SQUARE / 2; j++) {
                if (texture.get(x + i, y + j)[0] == 0d)
                    countBlack++;
            }
        if (countBlack > SAMPLING_ERRORS) {
            if (DEBUG_DRAW_SAMPLING)
                texture.put(x, y, GREEN);
            return -1;
        } else {
            if (DEBUG_DRAW_SAMPLING)
                texture.put(x, y, RED);
            return 1;
        }
    }
}
