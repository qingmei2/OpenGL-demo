package com.github.qingmei2.opengl_demo.a1_hello_world

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.a2_matrix.A02MyGLSurfaceView

/**
 * 1、定义基本的三角形
 *
 * [定义形状](https://developer.android.google.cn/training/graphics/opengl/shapes)
 * [绘制形状](https://developer.android.google.cn/training/graphics/opengl/draw?hl=zh_cn)
 */
class A01Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val glSurfaceView = A02MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }
}