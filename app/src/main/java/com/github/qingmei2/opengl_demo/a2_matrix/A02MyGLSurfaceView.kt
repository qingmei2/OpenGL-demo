package com.github.qingmei2.opengl_demo.a2_matrix

import android.content.Context
import android.opengl.GLSurfaceView
import com.github.qingmei2.opengl_demo.a2_matrix.A02MyGLRenderer

class A02MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: A02MyGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = A02MyGLRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}