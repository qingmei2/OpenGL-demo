package com.github.qingmei2.opengl_demo.c_image_process.processor;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static com.github.qingmei2.opengl_demo.LoadShaderKt.loadShaderWithResource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import androidx.annotation.DrawableRes;
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

    @NonNull
    private final Context mContext;

    //顶点坐标
    private final float[] vertexData = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    // 背景层-纹理坐标
    private final float[] backTextureData = new float[]{
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    @NonNull
    private final FloatBuffer mVertexBuffer;
    @NonNull
    private final FloatBuffer mBackTextureBuffer;

    private int mProgram;
    private int avPosition;
    private int afPosition;
    private int uMatrixLocation;

    private int mBackTextureId;
    private int mMidTextureId;
    private int mFrontTextureId;

    public C06Image3DProcessor(@NonNull Context context) {
        this.mContext = context;
        //初始化buffer
        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        mVertexBuffer.position(0);

        mBackTextureBuffer = ByteBuffer.allocateDirect(backTextureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(backTextureData);
        mBackTextureBuffer.position(0);
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
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(mProgram);

        this.drawLayerInner(mBackTextureId);
        this.drawLayerInner(mMidTextureId);
        this.drawLayerInner(mFrontTextureId);
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

    private void drawLayerInner(int textureId) {
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        final float[] projection = new float[16];
        Matrix.setIdentityM(projection, 0);
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projection, 0);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, mBackTextureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
