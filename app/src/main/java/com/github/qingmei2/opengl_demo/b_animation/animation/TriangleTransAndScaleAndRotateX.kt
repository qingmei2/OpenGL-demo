package com.github.qingmei2.opengl_demo.b_animation.animation

import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import com.github.qingmei2.opengl_demo.a_shape.shapes.COORDS_PER_VERTEX
import com.github.qingmei2.opengl_demo.a_shape.shapes.triangleCoords
import com.github.qingmei2.opengl_demo.b_animation.Animation
import com.github.qingmei2.opengl_demo.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 三角形
 */
class TriangleTransAndScaleAndRotateX : Animation {

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val translateMatrix = FloatArray(16)
    private val scaleMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var mProgram: Int = 0

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }
            // get handle to shape's transformation matrix
            vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 设置相机矩阵
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // 计算变换矩阵
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        Matrix.setIdentityM(translateMatrix, 0)

        // 设置旋转矩阵
        val time = SystemClock.uptimeMillis() % 10000L
        val angle = 0.090f * time.toInt()       // 4000 * 0.09 = 360度，每4秒旋转一周
        Matrix.setRotateM(rotationMatrix, 0, 0f, 0f, 0f, 1.0f)
        // 打开下面代码，增加旋转效果，但效果不佳
//        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, 1.0f)

        // 计算变换矩阵
        val scratch1 = FloatArray(16)
        Matrix.multiplyMM(scratch1, 0, vPMatrix, 0, rotationMatrix, 0)

        // 设置平移矩阵
        val transX = 0.0001f * time.toInt()
        Matrix.translateM(translateMatrix, 0, transX, 0f, 0f)

        // 计算变换矩阵
        val scratch2 = FloatArray(16)
        Matrix.multiplyMM(scratch2, 0, scratch1, 0, translateMatrix, 0)

        // 设置缩放矩阵
        val baseMatrix = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        val scaleX = 1f - 0.0001f * time.toInt()
        Matrix.scaleM(scaleMatrix, 0, baseMatrix, 0, scaleX, 1f, 1f)

        // 计算变换矩阵
        val scratch3 = FloatArray(16)
        Matrix.multiplyMM(scratch3, 0, scratch2, 0, scaleMatrix, 0)
        this.draw(scratch3)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val ratio: Float = width.toFloat() / height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader: Int = loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader: Int =
            loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode
            )

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }
}
