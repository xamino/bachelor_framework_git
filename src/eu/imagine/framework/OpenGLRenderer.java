package eu.imagine.framework;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 3:25 PM
 */
public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final MainInterface mainInterface;
    private Messenger log;
    private final String TAG = "OpenGLRenderer";

    private ArrayList<Trackable> toRender;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;
    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;
    private final int mBytesPerFloat = 4;
    private final int mStrideBytes = 7 * mBytesPerFloat;

    protected OpenGLRenderer(MainInterface mainInterface) {
        this.log = Messenger.getInstance();
        this.mainInterface = mainInterface;
        this.toRender = new ArrayList<Trackable>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0f, 0f, 0.1f);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;
        // Set our up vector.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix => camera position
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // Create shaders (voodoo!):
        createShaders();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (MainInterface.DEBUG_FRAME_LOGGING)
            log.pushTimer(this, "opengl frame");

        // Clear:
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // If new markerlist, get:
        if (mainInterface.getListUpdateStatus()) {
            this.toRender = mainInterface.getList();
            if (toRender == null) {
                log.log(TAG, "Error getting list!");
                toRender = new ArrayList<Trackable>();
            }
        }
        // ------------------------ RENDER ------------------------
        if (!toRender.isEmpty()) {
            for (Trackable trackable : toRender) {
                Matrix.setIdentityM(mModelMatrix, 0);
                drawObject(trackable.getFloatbuffer(), trackable.getTRANSLATION());
            }
        }
        if (MainInterface.DEBUG_FRAME_LOGGING) {
            log.debug(TAG, "OpenGL rendered frame in " + log.popTimer(this).time
                    + "ms.");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    /**
     * Method for painting any given object.
     *
     * @param data        The vertice data containing coordinates and colors to draw.
     * @param translation
     */
    private void drawObject(final FloatBuffer data, float[][] translation) {

        /*
        String out = "\n[";
        for (int row = 0; row < translation.length; row++) {
            for (int column = 0; column < translation[row].length; column++) {
                out += translation[row][column] + ", ";
            }
            out += ";\n";
        }
        log.log(TAG, "Rendering:" + out + "]\n");
        */

        // Pass in the position information
        data.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, data);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        data.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, data);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    private void createShaders() {
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"        // A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"        // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"        // Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"        // This will be passed into the fragment shader.

                        + "void main()                    \n"        // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"        // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n"    // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"        // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"        // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"        // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"        // Pass the color directly through the pipeline.
                        + "}                              \n";

        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }
}
