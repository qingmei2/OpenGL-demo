package com.github.qingmei2.opengl_demo.a_shape.shapes

import android.opengl.GLES20
import android.opengl.Matrix
import com.github.qingmei2.opengl_demo.a_shape.Shape
import com.github.qingmei2.opengl_demo.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 多个矩形
 */
class Rectangle : Shape {

    companion object {
        private const val backgroundColorR: Float = 255f / 255f
        private const val backgroundColorG: Float = 152f / 255f
        private const val backgroundColorB: Float = 0f / 255f
        private const val backgroundColorA: Float = 1f

        private const val watermarkColorR: Float = 255f / 255f
        private const val watermarkColorG: Float = 201f / 255f
        private const val watermarkColorB: Float = 71f / 255f
        private const val watermarkColorA: Float = 1f

        private const val lyricColorR: Float = 198f / 255f
        private const val lyricColorG: Float = 105f / 255f
        private const val lyricColorB: Float = 0f / 255f
        private const val lyricColorA: Float = 1f

        // 背景矩形区域
        private val backgroundCoords = floatArrayOf(
            -1f,
            1f,
            0.0f,
            backgroundColorR,
            backgroundColorG,
            backgroundColorB,
            backgroundColorA,     // 左上
            -1f,
            -1f,
            0.0f,
            backgroundColorR,
            backgroundColorG,
            backgroundColorB,
            backgroundColorA,     // 左下
            1f,
            1f,
            0.0f,
            backgroundColorR,
            backgroundColorG,
            backgroundColorB,
            backgroundColorA,     // 右上
            1f,
            -1f,
            0.0f,
            backgroundColorR,
            backgroundColorG,
            backgroundColorB,
            backgroundColorA      // 右下
        )

        // 水印矩形区域
        private val watermarkCoords = floatArrayOf(
            0.2f,
            0.9f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA,     // 左上
            0.2f,
            0.7f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA,     // 左下
            0.8f,
            0.9f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA,     // 右上
            0.8f,
            0.7f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA      // 右下
        )

        // 歌词矩形区域
        private val lyricCoords = floatArrayOf(
            -0.7f,
            -0.75f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA,     // 左上
            -0.7f,
            -0.85f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA,     // 左下
            0.7f,
            -0.75f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA,     // 右上
            0.7f,
            -0.85f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA      // 右下
        )
    }

    private val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "uniform mat4 vMatrix;" +
                "varying  vec4 vColor;" +
                "attribute vec4 aColor;" +
                "void main() {" +
                "  gl_Position = vMatrix * vPosition;" +
                "  vColor=aColor;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private var mProgram = 0

    private val COORDS_PER_VERTEX = 7

    //顶点之间的偏移量
    private val vertexStride: Int = COORDS_PER_VERTEX * 4   // 每个顶点四个字节左下

    private val mProjectMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    private var mMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private lateinit var backgroundVertexBuffer: FloatBuffer        // 背景色
    private lateinit var watermarkVertexBuffer: FloatBuffer         // 水印
    private lateinit var lyricVertexBuffer: FloatBuffer             // 歌词

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //计算宽高比
        val ratio = width.toFloat() / height.toFloat()
        //设置投影
        Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
        //设置相机
//        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1f, 1f, 1f, 1f)

        val vertexShader = loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader = loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentShaderCode
        )

        this.initBackground()
        this.initWatermark()
        this.initLyric()

        mProgram = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram)

        //获取变换矩阵vMatrix成员句柄
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mProjectMatrix, 0)

        //顶点着色器vPosition句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        this.drawBackground()
        this.drawWatermark()
        this.drawLyric()

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    private fun initBackground() {
        val backgroundBuffer = ByteBuffer.allocateDirect(backgroundCoords.size * 4)
        backgroundBuffer.order(ByteOrder.nativeOrder())
        backgroundVertexBuffer = backgroundBuffer.asFloatBuffer().apply {
            put(backgroundCoords)
            position(0)
        }
    }

    private fun drawBackground() {
        backgroundVertexBuffer.position(0)
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            backgroundVertexBuffer
        )

        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle)
        backgroundVertexBuffer.position(3)
        GLES20.glVertexAttribPointer(
            mColorHandle,
            4,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            backgroundVertexBuffer
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun initWatermark() {
        val watermarkBuffer = ByteBuffer.allocateDirect(watermarkCoords.size * 4)
        watermarkBuffer.order(ByteOrder.nativeOrder())
        watermarkVertexBuffer = watermarkBuffer.asFloatBuffer().apply {
            put(watermarkCoords)
            position(0)
        }
    }

    private fun drawWatermark() {
        watermarkVertexBuffer.position(0)
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            watermarkVertexBuffer
        )

        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle)
        watermarkVertexBuffer.position(3)
        GLES20.glVertexAttribPointer(
            mColorHandle,
            4,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            watermarkVertexBuffer
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun initLyric() {
        val lyricBuffer = ByteBuffer.allocateDirect(lyricCoords.size * 4)
        lyricBuffer.order(ByteOrder.nativeOrder())
        lyricVertexBuffer = lyricBuffer.asFloatBuffer().apply {
            put(lyricCoords)
            position(0)
        }
    }

    private fun drawLyric() {
        lyricVertexBuffer.position(0)
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            lyricVertexBuffer
        )

        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle)
        lyricVertexBuffer.position(3)
        GLES20.glVertexAttribPointer(
            mColorHandle,
            4,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            lyricVertexBuffer
        )

        //绘制正方形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}
