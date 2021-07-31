package com.github.qingmei2.opengl_demo.a2_matrix

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.github.qingmei2.opengl_demo.shape.Square
import com.github.qingmei2.opengl_demo.shape.Triangle2
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class A02MyGLRenderer : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle2
    private lateinit var mSquare: Square

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // initialize a triangle
        mTriangle = Triangle2()
        // initialize a square
        mSquare = Square()
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        mTriangle.draw(vPMatrix)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}