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
class Rectangle2 : Shape {

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
            -1f,
            1f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA,     // 左上
            -1f,
            -1f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA,     // 左下
            1f,
            1f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA,     // 右上
            1f,
            -1f,
            0.0f,
            watermarkColorR,
            watermarkColorG,
            watermarkColorB,
            watermarkColorA      // 右下
        )

        // 歌词矩形区域
        private val lyricCoords = floatArrayOf(
            -1f,
            1f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA,     // 左上
            -1f,
            -1f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA,     // 左下
            1f,
            1f,
            0.0f,
            lyricColorR,
            lyricColorG,
            lyricColorB,
            lyricColorA,     // 右上
            1f,
            -1f,
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

    private var mBackgroundM = FloatArray(16)
    private var mWatermarkM = FloatArray(16)
    private var mLyricM = FloatArray(16)

    private var mMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private lateinit var backgroundVertexBuffer: FloatBuffer        // 背景色
    private lateinit var mWatermarkVertexBuffer: FloatBuffer         // 水印
    private lateinit var mLyricVertexBuffer: FloatBuffer             // 歌词

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //计算宽高比
        val isVertical = width < height
        val ratio = width.toFloat() / height.toFloat()
        // 设置背景的矩阵
        Matrix.orthoM(mBackgroundM, 0, -1f, 1f, -1f, 1f, -1f, 1f)

        // 设置水印的变换矩阵
        val watermarkProjectM = FloatArray(16)
        val watermarkScaleM = FloatArray(16)
        val watermarkTransM = FloatArray(16)
        val scratchScale = FloatArray(16)
        val scratchTrans = FloatArray(16)

        if (isVertical) {
            Matrix.orthoM(watermarkProjectM, 0, -1f, 1f, -1f / ratio, 1f / ratio, -1f, 1f)
        } else {
            Matrix.orthoM(watermarkProjectM, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        }

        Matrix.setIdentityM(watermarkScaleM, 0)
        Matrix.scaleM(watermarkScaleM, 0, 0.25f, 0.15f, 1f)
        Matrix.multiplyMM(scratchScale, 0, watermarkProjectM, 0, watermarkScaleM, 0)

        Matrix.setIdentityM(watermarkTransM, 0)
        Matrix.translateM(watermarkTransM, 0, 0.65f, 0.80f, 0f)
        Matrix.multiplyMM(scratchTrans, 0, watermarkTransM,0, scratchScale, 0)

        mWatermarkM = scratchTrans

        // 设置歌词的变换矩阵
        val lrcProjectM = FloatArray(16)
        val lrcScaleM = FloatArray(16)
        val lrcTransM = FloatArray(16)
        val lrcScale = FloatArray(16)
        val lrcTrans = FloatArray(16)

        if (isVertical) {
            Matrix.orthoM(lrcProjectM, 0, -1f, 1f, -1f / ratio, 1f / ratio, -1f, 1f)
        } else {
            Matrix.orthoM(lrcProjectM, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        }

        Matrix.setIdentityM(lrcScaleM, 0)
        Matrix.scaleM(lrcScaleM, 0, 0.85f, 0.10f, 1f)
        Matrix.multiplyMM(lrcScale, 0, lrcProjectM, 0, lrcScaleM, 0)

        Matrix.setIdentityM(lrcTransM, 0)
        Matrix.translateM(lrcTransM, 0, 0f, -14f, 0f)
        Matrix.multiplyMM(lrcTrans, 0, lrcScale, 0, lrcTransM, 0)

        mLyricM = lrcTrans
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

        this.drawLayer(backgroundVertexBuffer, mBackgroundM)
        this.drawLayer(mWatermarkVertexBuffer, mWatermarkM)
        this.drawLayer(mLyricVertexBuffer, mLyricM)

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

    private fun drawLayer(floatBuffer: FloatBuffer, matrix: FloatArray) {
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, matrix, 0)

        floatBuffer.position(0)
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            floatBuffer
        )

        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle)
        floatBuffer.position(3)
        GLES20.glVertexAttribPointer(
            mColorHandle,
            4,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            floatBuffer
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun initWatermark() {
        val watermarkBuffer = ByteBuffer.allocateDirect(watermarkCoords.size * 4)
        watermarkBuffer.order(ByteOrder.nativeOrder())
        mWatermarkVertexBuffer = watermarkBuffer.asFloatBuffer().apply {
            put(watermarkCoords)
            position(0)
        }
    }

    private fun initLyric() {
        val lyricBuffer = ByteBuffer.allocateDirect(lyricCoords.size * 4)
        lyricBuffer.order(ByteOrder.nativeOrder())
        mLyricVertexBuffer = lyricBuffer.asFloatBuffer().apply {
            put(lyricCoords)
            position(0)
        }
    }
}
