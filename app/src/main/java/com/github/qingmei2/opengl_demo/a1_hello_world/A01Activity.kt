package com.github.qingmei2.opengl_demo.a1_hello_world

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class A01Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }
}