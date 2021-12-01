package com.github.qingmei2.opengl_demo.d_video;

import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.github.qingmei2.opengl_demo.LoadShaderKt;
import com.github.qingmei2.opengl_demo.R;
import com.github.qingmei2.opengl_demo.d_video.utils.OpenglTextUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class D01VideoRenderer implements GLSurfaceView.Renderer {

    private static final String LRC = "雨下整夜, 我的爱溢出就像雨水";

    private final float[] mVertexData = new float[]{
            -1.0f, 1.0f,        // 左上
            -1.0f, -1.0f,       // 左下
            1.0f, 1.0f,         // 右上
            1.0f, -1.0f,        // 右下
    };

    private final float[] mTextureData = new float[]{
            0.0f, 0.0f,        // 左下
            0.0f, 1.0f,        // 左上
            1.0f, 0.0f,        // 右下
            1.0f, 1.0f,        // 右上
    };

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mTextureBuffer;

    private final Context mContext;
    private final MediaPlayer mMediaPlayer;
    private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener;

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private float[] mProjectM = new float[16];

    private int mBackgroundProgram = 0;
    private int mBackgroundPosition = 0;
    private int mBackgroundCoor = 0;
    private int mBackgroundMatrix = 0;
    private float[] mBackgroundM = new float[16];
    private int mBackgroundTextureId = 0;
    private int mBackgroundTextureHandler = 0;

    private int mLrcProgram = 0;
    private int mLrcVPosition = 0;
    private int mLrcVCoordinate = 0;
    private int mLrcAAlphaHandle = 0;
    private int mLrcMatrix = 0;
    private int mLrcTextureId = 0;
    private float[] mLrcM = new float[16];

    private float mVideoWidth;
    private float mVideoHeight;
    private int mViewportWidth;
    private int mViewportHeight;

    private String mCurLrc;
    // 歌词透明度
    private float mLrcAlpha = 1f;
    // 歌词开始时间
    private long mLrcStartTime = 0L;

    public D01VideoRenderer(Context context,
                            MediaPlayer mediaPlayer,
                            SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener) {
        mContext = context;
        mOnFrameAvailableListener = onFrameAvailableListener;
        mMediaPlayer = mediaPlayer;

        mVertexBuffer = ByteBuffer.allocateDirect(mVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexData);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(mTextureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mTextureData);
        mTextureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 初始化歌词程序
        mLrcProgram = LoadShaderKt.loadShaderWithResource(
                mContext,
                R.raw.d_effect_texture_vertex_shader,
                R.raw.d_effect_texture_fragment_shader
        );
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.mViewportWidth = width;
        this.mViewportHeight = height;

        // 计算宽高比
        boolean isVertical = width < height;
        float ratio = (float) width / (float) height;

        // 根据横竖屏，设置场景(全屏)
        if (isVertical) {
            Matrix.orthoM(mProjectM, 0, -1f, 1f, -1f / ratio, 1f / ratio, -1f, 1f);
        } else {
            Matrix.orthoM(mProjectM, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
        }

        this.mVideoHeight = 360f;
        this.mVideoWidth = 640f;

        this.initBackgroundMatrix(isVertical, ratio, this.mVideoWidth, this.mVideoHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清屏，清理掉颜色的缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //设置清屏的颜色，这里是float颜色的取值范围的[0,1]
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        this.refreshLrcPorperties();
        this.initLrcBitmapMatrix(this.mViewportWidth, this.mViewportHeight, this.mVideoWidth, this.mVideoHeight);

        this.drawBackground();
        this.drawLrcText();
    }

    private void initBackgroundMatrix(boolean isVertical, float ratio, float videoW, float videoH) {
        float[] backgroundS = new float[16];
        Matrix.setIdentityM(backgroundS, 0);
        if (isVertical) {
            Matrix.scaleM(backgroundS, 0, 1f, videoH / videoW, 1f);
        } else {
            Matrix.scaleM(backgroundS, 0, ratio, 1f, 1f);
        }
        Matrix.multiplyMM(mBackgroundM, 0, mProjectM, 0, backgroundS, 0);
    }

    private boolean isVideoAvailable = false;

    public void setVideoAvailable(boolean videoAvailable) {
        this.isVideoAvailable = videoAvailable;
    }

    private void drawBackground() {
        if (mBackgroundProgram <= 0) {
            // 初始化背景程序
            mBackgroundProgram = LoadShaderKt.loadShaderWithResource(
                    mContext,
                    R.raw.d_effect_video_vertex_shader,
                    R.raw.d_effect_video_fragment_shader
            );

            mBackgroundPosition = GLES20.glGetAttribLocation(mBackgroundProgram, "aPosition");
            mBackgroundCoor = GLES20.glGetAttribLocation(mBackgroundProgram, "aCoordinate");
            mBackgroundTextureHandler = GLES20.glGetUniformLocation(mBackgroundProgram, "uTexture");
            mBackgroundMatrix = GLES20.glGetUniformLocation(mBackgroundProgram, "uMatrix");

            // 生成纹理
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            if (textureIds[0] == 0) {
                return;
            }
            mBackgroundTextureId = textureIds[0];

            mSurfaceTexture = new SurfaceTexture(mBackgroundTextureId);

            mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
            mSurface = new Surface(mSurfaceTexture);

            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.start();

            mSurface.release();
        }

        if (isVideoAvailable) {
            GLES20.glUseProgram(mBackgroundProgram);

            //激活指定纹理单元
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //绑定纹理ID到纹理单元
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mBackgroundTextureId);
            //将激活的纹理单元传递到着色器里面
            GLES20.glUniform1i(mBackgroundTextureHandler, 0);
            //配置边缘过渡参数
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            //绑定图片到纹理单元
            mSurfaceTexture.updateTexImage();

            GLES20.glUniformMatrix4fv(mBackgroundMatrix, 1, false, mBackgroundM, 0);

            //启用顶点的句柄
            GLES20.glEnableVertexAttribArray(mBackgroundPosition);
            GLES20.glVertexAttribPointer(mBackgroundPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
            GLES20.glEnableVertexAttribArray(mBackgroundCoor);
            GLES20.glVertexAttribPointer(mBackgroundCoor, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

            //开始绘制
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            checkGlError("draw video");
        }
    }

    private void drawLrcText() {
        GLES20.glUseProgram(mLrcProgram);

        mLrcMatrix = GLES20.glGetUniformLocation(mLrcProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(mLrcMatrix, 1, false, mLrcM, 0);

        // 顶点坐标
        mLrcVPosition = GLES20.glGetAttribLocation(mLrcProgram, "vPosition");
        // 纹理坐标
        mLrcVCoordinate = GLES20.glGetAttribLocation(mLrcProgram, "vCoordinate");
        // 透明度
        mLrcAAlphaHandle = GLES20.glGetUniformLocation(mLrcProgram, "aAlpha");
        GLES20.glUniform1f(mLrcAAlphaHandle, mLrcAlpha);

        //设置为可用的状态
        GLES20.glEnableVertexAttribArray(mLrcVPosition);
        GLES20.glVertexAttribPointer(mLrcVPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mLrcVCoordinate);
        GLES20.glVertexAttribPointer(mLrcVCoordinate, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        checkGlError("draw text");
    }

    private void initLrcBitmapMatrix(int width, int height, float videoW, float videoH) {
        // 计算宽高比
        boolean isVertical = width < height;
        float ratio = (float) width / (float) height;

        // 生成纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return;
        }
        mLrcTextureId = textureIds[0];
        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLrcTextureId);
        // 环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        // 过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        final Bitmap bitmap = OpenglTextUtils.createFontBitmap(mCurLrc, 16, width / 2);

        if (bitmap != null) {
            final int bitmapH = bitmap.getHeight();
            final int bitmapW = bitmap.getWidth();
            final float bitmapRatio = (float) bitmapW / (float) bitmapH;
            final float lrcWidthPercent = 0.7f;
            final float lrcHeightPercent = lrcWidthPercent / bitmapRatio;

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();

            float percentH = ratio * (videoH / videoW);

            float[] lrcTS = new float[16];
            Matrix.setIdentityM(lrcTS, 0);
            Matrix.translateM(lrcTS, 0, 0f, -percentH * 1.7f, 0f);          // 2.平移
            Matrix.scaleM(lrcTS, 0, lrcWidthPercent, lrcHeightPercent, 1f);       // 1.缩放
            Matrix.multiplyMM(mLrcM, 0, mProjectM, 0, lrcTS, 0);  // 3.正交投影
        }
    }

    private void refreshLrcPorperties() {
        if (mLrcStartTime <= 0L) {
            mLrcStartTime = System.currentTimeMillis();
            mLrcAlpha = 1.0f;
        }

        final long turnTime = 5000L;
        // 第几句歌词
        final long curTurnNumber = (System.currentTimeMillis() - mLrcStartTime) / turnTime;
        // 当前歌词的状态
        final long curTurnTime = (System.currentTimeMillis() - mLrcStartTime) % turnTime;

        this.mCurLrc = LRC + curTurnNumber;
        if (curTurnTime >= 0L && curTurnTime < 1000L) {
            // 第1秒，从隐藏到展示（0f -> 1f)
            this.mLrcAlpha = (float) curTurnTime / 1000f;
        } else if (curTurnTime >= 1000L && curTurnTime < 4000) {
            // 第2-4秒，持续展示
            this.mLrcAlpha = 1.0f;
        } else {
            // 第5秒，从展示到隐藏（1f -> 0f, 0f 持续 500ms）
            if (curTurnTime >= 4000L && curTurnTime < 4500L) {
                // 第4-4.5秒，从展示到隐藏（1f -> 0f)
                this.mLrcAlpha = 1f - ((float) (curTurnTime - 4000) / 500f);
            } else {
                // 第4.5-5秒，持续隐藏
                this.mLrcAlpha = 0f;
            }
        }
    }

    public void checkGlError(String operation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("GlUtils", operation + ": glError " + error);
            throw new RuntimeException(operation + ": glError " + error);
        }
    }
}
