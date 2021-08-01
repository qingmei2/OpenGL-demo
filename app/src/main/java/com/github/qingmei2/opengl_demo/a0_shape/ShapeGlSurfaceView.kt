package com.github.qingmei2.opengl_demo.a0_shape

import android.content.Context
import android.opengl.GLSurfaceView

class ShapeGlSurfaceView(
    context: Context,
    shape: Shape
) : GLSurfaceView(context) {

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(shape)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}