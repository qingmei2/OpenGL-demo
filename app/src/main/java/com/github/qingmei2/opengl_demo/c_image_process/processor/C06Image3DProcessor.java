package com.github.qingmei2.opengl_demo.c_image_process.processor;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static com.github.qingmei2.opengl_demo.LoadShaderKt.loadShaderWithResource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.github.qingmei2.opengl_demo.R;
import com.github.qingmei2.opengl_demo.c_image_process.ImageProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 裸眼3D效果
 * <p>
 * https://juejin.cn/post/6991409083765129229
 */
@SuppressWarnings("FieldCanBeLocal")
public class C06Image3DProcessor implements ImageProcessor {

    private static final float SCALE_BACK_GROUND = 1.1f;    // 背景缩放
    private static final float SCALE_MID_GROUND = 1.0f;     // 中景不变
    private static final float SCALE_FORE_GROUND = 1.06f;    // 前景缩放

    private static final float MAX_VISIBLE_SIDE_FOREGROUND = 1.04f;
    private static final float MAX_VISIBLE_SIDE_BACKGROUND = 1.06f;

    private static final float USER_X_AXIS_STANDARD = -45f;
    private static final float USER_Y_AXIS_STANDARD = 0f;
    private static final float MAX_TRANS_DEGREE_X = 25f;   // XY轴最大旋转角度
    private static final float MAX_TRANS_DEGREE_Y = 45f;   // XY轴最大旋转角度

    @NonNull
    private final Context mContext;

    //顶点坐标
    private final float[] vertexData = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    //纹理坐标
    private final float[] mTextureData = new float[]{
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    @NonNull
    private final FloatBuffer mVertexBuffer;
    @NonNull
    private final FloatBuffer mTextureBuffer;

    private float[] mProjectionMatrix = new float[16];
    private float[] mBackMatrix = new float[16];
    private float[] mMidMatrix = new float[16];
    private float[] mFrontMatrix = new float[16];

    private int mProgram;
    private int avPosition;
    private int afPosition;
    private int uMatrixLocation;

    private int mBackTextureId;
    private int mMidTextureId;
    private int mFrontTextureId;

    private final SensorManager mSensorManager;
    private final Sensor mAcceleSensor;
    private final Sensor mMagneticSensor;

    private float[] mAcceleValues = new float[16];
    private float[] mMageneticValues = new float[16];

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAcceleValues = lowPass(event.values.clone(), mAcceleValues);
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mMageneticValues = lowPass(event.values.clone(), mMageneticValues);
            }

            float[] values = new float[3];
            float[] R = new float[9];
            SensorManager.getRotationMatrix(R, null, mAcceleValues, mMageneticValues);
            SensorManager.getOrientation(R, values);
            // x轴的偏转角度
            float degreeX = (float) Math.toDegrees(values[1]);
            // y轴的偏转角度
            float degreeY = (float) Math.toDegrees(values[2]);
            // z轴的偏转角度
            float degreeZ = (float) Math.toDegrees(values[0]);

            Log.d("qingmei2", "x轴偏转角度 = " + degreeX + " , y轴偏转角度 = " + degreeY + ", z轴 = " + degreeZ);

            updateMatrix(degreeX, degreeY);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public C06Image3DProcessor(@NonNull Context context) {
        this.mContext = context;
        //初始化buffer
        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(mTextureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mTextureData);
        mTextureBuffer.position(0);

        // 注册传感器
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(mSensorEventListener, mAcceleSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorEventListener, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgram = loadShaderWithResource(
                mContext,
                R.raw.projection_vertex_shader,
                R.raw.projection_fragment_shader
        );

        avPosition = GLES20.glGetAttribLocation(mProgram, "av_Position");
        afPosition = GLES20.glGetAttribLocation(mProgram, "af_Position");
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_Matrix");

        int[] textureIds = new int[3];
        GLES20.glGenTextures(3, textureIds, 0);
        if (textureIds[0] == 0 || textureIds[1] == 0 || textureIds[2] == 0) {
            return;
        }
        mBackTextureId = textureIds[0];
        mMidTextureId = textureIds[1];
        mFrontTextureId = textureIds[2];

        this.texImageInner(R.drawable.bg_3d_back, mBackTextureId);
        this.texImageInner(R.drawable.bg_3d_mid, mMidTextureId);
        this.texImageInner(R.drawable.bg_3d_fore, mFrontTextureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置大小位置
        GLES20.glViewport(0, 0, width, height);

        Matrix.setIdentityM(mProjectionMatrix, 0);

//        // 计算宽高比
//        boolean isVertical = width < height;
//        float ratio = (float) width / (float) height;
//
//        // 根据横竖屏，设置场景(全屏)
//        if (isVertical) {
//            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f / ratio, 1f / ratio, -1f, 1f);
//        } else {
//            Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
//        }
    }

    /**
     * 陀螺仪数据回调，更新各个层级的变换矩阵.
     *
     * @param degreeX x轴旋转角度，图片应该上下移动
     * @param degreeY y轴旋转角度，图片应该左右移动
     */
    private void updateMatrix(@FloatRange(from = -180.0f, to = 180.0f) float degreeX,
                              @FloatRange(from = -180.0f, to = 180.0f) float degreeY) {
        // 用户的使用习惯, Y轴一般是 0f，X轴一般是 -45f
        degreeX -= USER_X_AXIS_STANDARD;
        degreeY -= USER_Y_AXIS_STANDARD;

        if (degreeX > MAX_TRANS_DEGREE_X) {
            degreeX = MAX_TRANS_DEGREE_X;
        }
        if (degreeX < -MAX_TRANS_DEGREE_X) {
            degreeX = -MAX_TRANS_DEGREE_X;
        }
        if (degreeY > MAX_TRANS_DEGREE_Y) {
            degreeY = MAX_TRANS_DEGREE_Y;
        }
        if (degreeY < -MAX_TRANS_DEGREE_Y) {
            degreeY = -MAX_TRANS_DEGREE_Y;
        }

        // 背景变换
//        Matrix.setIdentityM(mBackMatrix, 0);

        // 1.最大位移量
        float maxTransXY = MAX_VISIBLE_SIDE_BACKGROUND - 1f;
        // 2.本次的位移量
        float transX = ((maxTransXY) / MAX_TRANS_DEGREE_Y) * -degreeY;
        float transY = ((maxTransXY) / MAX_TRANS_DEGREE_X) * -degreeX;
        float[] backMatrix = new float[16];
        Matrix.setIdentityM(backMatrix, 0);
        Matrix.translateM(backMatrix, 0, transX, transY, 0f);                    // 2.平移
        Matrix.scaleM(backMatrix, 0, SCALE_BACK_GROUND, SCALE_BACK_GROUND, 1f);  // 1.缩放
        Matrix.multiplyMM(mBackMatrix, 0, mProjectionMatrix, 0, backMatrix, 0);  // 3.正交投影

        // 中景变换
        Matrix.setIdentityM(mMidMatrix, 0);

        // 前景变换
//        Matrix.setIdentityM(mFrontMatrix, 0);

        // 1.最大位移量
        maxTransXY = MAX_VISIBLE_SIDE_FOREGROUND - 1f;
        // 2.本次的位移量
        transX = ((maxTransXY) / MAX_TRANS_DEGREE_Y) * -degreeY;
        transY = ((maxTransXY) / MAX_TRANS_DEGREE_X) * -degreeX;
        float[] frontMatrix = new float[16];
        Matrix.setIdentityM(frontMatrix, 0);
        Matrix.translateM(frontMatrix, 0, -transX, -transY - 0.10f, 0f);         // 2.平移
        Matrix.scaleM(frontMatrix, 0, SCALE_FORE_GROUND, SCALE_FORE_GROUND, 1f);    // 1.缩放
        Matrix.multiplyMM(mFrontMatrix, 0, mProjectionMatrix, 0, frontMatrix, 0);  // 3.正交投影
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(mProgram);

        this.drawLayerInner(mBackTextureId, mTextureBuffer, mBackMatrix);
        this.drawLayerInner(mMidTextureId, mTextureBuffer, mMidMatrix);
        this.drawLayerInner(mFrontTextureId, mTextureBuffer, mFrontMatrix);
    }

    private void texImageInner(@DrawableRes int drawableRes, int textureId) {
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableRes);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    private void drawLayerInner(int textureId, FloatBuffer textureBuffer, float[] matrix) {
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    static final float ALPHA = 0.25f;

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
