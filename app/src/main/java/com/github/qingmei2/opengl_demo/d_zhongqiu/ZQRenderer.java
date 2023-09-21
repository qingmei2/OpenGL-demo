package com.github.qingmei2.opengl_demo.d_zhongqiu;

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
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.github.qingmei2.opengl_demo.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 中秋render
 */
@SuppressWarnings("FieldCanBeLocal")
public class ZQRenderer implements GLSurfaceView.Renderer {

    private static final float SCALE_BACK_GROUND = 1.1f;    // 背景缩放
    private static final float SCALE_MID_GROUND = 1.0f;     // 中景不变
    private static final float SCALE_FORE_GROUND = 1.06f;   // 前景缩放

    private static final float MAX_VISIBLE_SIDE_FOREGROUND = 1.04f;
    private static final float MAX_VISIBLE_SIDE_BACKGROUND = 1.06f;

    private static final float USER_X_AXIS_STANDARD = -45f;
    private static final float USER_Y_AXIS_STANDARD = 0f;
    private static final float MAX_TRANS_DEGREE_X = 25f;   // X轴最大旋转角度
    private static final float MAX_TRANS_DEGREE_Y = 45f;   // Y轴最大旋转角度

    @NonNull
    private final Context mContext;

    //顶点坐标
    private final float[] vertexData = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};

    //纹理坐标
    private final float[] mTextureData = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    @NonNull
    private final FloatBuffer mVertexBuffer;
    @NonNull
    private final FloatBuffer mTextureBuffer;

    private float[] mBgProjectionMatrix = new float[16];
    private float[] mMoonProjectionMatrix = new float[16];
    private float[] mCoverProjectionMatrix = new float[16];

    private float[] mBackMatrix = new float[16];
    private float[] mMoonMatrix = new float[16];
    private float[] mCoverMatrix = new float[16];
    private float[] mFrontMatrix = new float[16];

    private float mCurDegreeX;
    private float mCurDegreeY;

    private int mProgram;
    private int avPosition;
    private int afPosition;
    private int uMatrixLocation;

    // 底层-蓝色背景纹理
    private int mBackTextureId;
    // 下层-月亮图片纹理
    private int mMidTextureId;
    // 中层-音乐封面纹理
    private int mCoverTextureId;
    // 上层-装饰&文字纹理
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

            updateDegreeValue(degreeX, degreeY);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public ZQRenderer(@NonNull Context context) {
        this.mContext = context;
        //初始化buffer
        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(mTextureData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mTextureData);
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
        mProgram = loadShaderWithResource(mContext, R.raw.projection_vertex_shader, R.raw.projection_fragment_shader);

        avPosition = GLES20.glGetAttribLocation(mProgram, "av_Position");
        afPosition = GLES20.glGetAttribLocation(mProgram, "af_Position");
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_Matrix");

        int[] textureIds = new int[4];
        GLES20.glGenTextures(4, textureIds, 0);
        if (textureIds[0] == 0 || textureIds[1] == 0 || textureIds[2] == 0 || textureIds[3] == 0) {
            return;
        }
        mBackTextureId = textureIds[0];
        mMidTextureId = textureIds[1];
        mCoverTextureId = textureIds[2];
        mFrontTextureId = textureIds[3];

        this.texImageInner(R.drawable.icon_player_bg, mBackTextureId);
        this.texImageInner(R.drawable.icon_player_moon, mMidTextureId);
        this.texImageInner(R.drawable.icon_album_cover_nocturne, mCoverTextureId);
        this.texImageInner(R.drawable.icon_player_text, mFrontTextureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置大小位置
        GLES20.glViewport(0, 0, width, height);

        Matrix.setIdentityM(mBgProjectionMatrix, 0);
        Matrix.setIdentityM(mMoonProjectionMatrix, 0);
        Matrix.setIdentityM(mCoverProjectionMatrix, 0);

        // 计算宽高比
        boolean isVertical = width < height;
        float screenRatio = (float) width / (float) height;

        // 设置投影矩阵

        // 1.背景图投影矩阵，只需要铺全屏

        // 2.月亮和装饰图投影矩阵
        float ratio = (float) 1080 / (float) 1528;
        if (isVertical) {
            Matrix.orthoM(mMoonProjectionMatrix, 0, -1f, 1f, -1f / ratio, 1f / ratio, -1f, 1f);
        } else {
            Matrix.orthoM(mMoonProjectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
        }

        // 3.歌曲封面图投影矩阵
        if (isVertical) {
            Matrix.orthoM(mCoverProjectionMatrix, 0, -1f, 1f, -1f / screenRatio, 1f / screenRatio, -1f, 1f);
        } else {
            Matrix.orthoM(mCoverProjectionMatrix, 0, -screenRatio, screenRatio, -1f, 1f, -1f, 1f);
        }
    }

    /**
     * 陀螺仪数据回调，更新各个层级的变换矩阵.
     *
     * @param degreeX x轴旋转角度，图片应该上下移动
     * @param degreeY y轴旋转角度，图片应该左右移动
     */
    private void updateDegreeValue(@FloatRange(from = -180.0f, to = 180.0f) float degreeX, @FloatRange(from = -180.0f, to = 180.0f) float degreeY) {
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

        mCurDegreeX = degreeX;
        mCurDegreeY = degreeY;
    }

    private void updateMatrix() {
        //  ----------  背景-蓝色底图  ----------
        Matrix.setIdentityM(mBackMatrix, 0);
        // 1.最大位移量
        float maxTransXY = MAX_VISIBLE_SIDE_BACKGROUND - 1f;
        // 2.本次的位移量
        float transX = ((maxTransXY) / MAX_TRANS_DEGREE_Y) * -mCurDegreeY;
        float transY = ((maxTransXY) / MAX_TRANS_DEGREE_X) * -mCurDegreeX;
        float[] backMatrix = new float[16];
        // 蓝色底图的投影矩阵，需要铺展全屏.
        Matrix.setIdentityM(mBgProjectionMatrix, 0);
        Matrix.setIdentityM(backMatrix, 0);
        Matrix.translateM(backMatrix, 0, transX, transY, 0f);                    // 2.平移
        Matrix.scaleM(backMatrix, 0, SCALE_BACK_GROUND, SCALE_BACK_GROUND, 1f);  // 1.缩放
        Matrix.multiplyMM(mBackMatrix, 0, mBgProjectionMatrix, 0, backMatrix, 0);  // 3.正交投影

        //  ----------  中景-月亮  ----------
        Matrix.setIdentityM(mMoonMatrix, 0);
        float[] midMatrix = new float[16];
        Matrix.setIdentityM(midMatrix, 0);
        Matrix.scaleM(midMatrix, 0, 1.0f, 1.0f, 1.0f);  // 1.缩放
        Matrix.multiplyMM(mMoonMatrix, 0, mMoonProjectionMatrix, 0, midMatrix, 0);  // 2.正交投影

        // ---------  中景-歌曲封面  ----------
        Matrix.setIdentityM(mCoverMatrix, 0);
        float[] coverMatrix = new float[16];
        Matrix.setIdentityM(coverMatrix, 0);
        Matrix.translateM(coverMatrix, 0, 0.03f, 0.81f, 0f);    // 2.平移,这里的位移参数是开发时，即时调整的，保证歌曲封面和月亮的center位置在一起
        Matrix.scaleM(coverMatrix, 0, 0.58f, 0.58f, 1.0f);      // 1.缩放,这里的缩放参数是开发时，即时调整的，保证歌曲封面和月亮的大小一致
        Matrix.multiplyMM(mCoverMatrix, 0, mCoverProjectionMatrix, 0, coverMatrix, 0);  // 2.正交投影

        //  ----------  前景-装饰  ----------
        Matrix.setIdentityM(mFrontMatrix, 0);
        // 1.最大位移量
        maxTransXY = MAX_VISIBLE_SIDE_FOREGROUND - 1f;
        // 2.本次的位移量
        transX = ((maxTransXY) / MAX_TRANS_DEGREE_Y) * -mCurDegreeY;
        transY = ((maxTransXY) / MAX_TRANS_DEGREE_X) * -mCurDegreeX;
        float[] frontMatrix = new float[16];
        Matrix.setIdentityM(frontMatrix, 0);
        Matrix.translateM(frontMatrix, 0, -transX, -transY - 0.10f, 0f);         // 2.平移
        Matrix.scaleM(frontMatrix, 0, SCALE_FORE_GROUND, SCALE_FORE_GROUND, 1f);    // 1.缩放
        Matrix.multiplyMM(mFrontMatrix, 0, mMoonProjectionMatrix, 0, frontMatrix, 0);  // 3.正交投影
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(mProgram);

        this.updateMatrix();

        this.drawLayerInner(mBackTextureId, mTextureBuffer, mBackMatrix);
        this.drawLayerInner(mCoverTextureId, mTextureBuffer, mCoverMatrix);
        this.drawLayerInner(mMidTextureId, mTextureBuffer, mMoonMatrix);
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
