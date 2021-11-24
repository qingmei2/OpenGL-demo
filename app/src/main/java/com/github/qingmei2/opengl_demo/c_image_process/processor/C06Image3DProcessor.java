package com.github.qingmei2.opengl_demo.c_image_process.processor;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.qingmei2.opengl_demo.c_image_process.ImageProcessor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 裸眼3D效果
 * <p>
 * https://juejin.cn/post/6991409083765129229
 */
public class C06Image3DProcessor implements ImageProcessor {

    @NonNull
    private Context mContext;

    public C06Image3DProcessor(@NonNull Context context) {
        this.mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
