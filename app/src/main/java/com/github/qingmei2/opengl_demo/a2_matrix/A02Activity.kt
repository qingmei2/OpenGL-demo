package com.github.qingmei2.opengl_demo.a2_matrix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * 2、应用投影和相机视图
 *
 * [应用投影和相机视图](https://developer.android.google.cn/training/graphics/opengl/projection?hl=zh_cn)
 */
class A02Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val glSurfaceView = A02MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }
}