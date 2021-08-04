package com.github.qingmei2.opengl_demo.a_shape.shapes

import android.opengl.GLES20
import android.opengl.Matrix
import com.github.qingmei2.opengl_demo.a_shape.Shape
import com.github.qingmei2.opengl_demo.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Square2 : Shape {

    private val squareCoords = floatArrayOf(
        -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,      // 左上
        -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,     // 左下
        0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,       // 右上
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f       // 右下
    )

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

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    private var mMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private lateinit var vertexBuffer: FloatBuffer

    //******************************* 方式二相关 ↓ *******************************
    private lateinit var indexBuffer: ShortBuffer

    //顶点个数
    private val vertexCount: Int = squareCoords.size / COORDS_PER_VERTEX

    //索引法绘制顺序，严格按照右手坐标系绘制2个三角形
    private val index = shortArrayOf(0, 1, 2, 2, 1, 3) // order to draw vertices
    //******************************* 方式二相关 ↑ *******************************

    override fun onDrawFrame(gl: GL10?) {
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram)

        //获取变换矩阵vMatrix成员句柄
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0)

        //顶点着色器vPosition句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        vertexBuffer.position(0)
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle)
        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(
            mColorHandle,
            4,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        //绘制正方形
        // 方式1
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        // 方式2
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            index.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //计算宽高比
        val ratio = width.toFloat() / height.toFloat()
        //设置投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply {
            put(squareCoords)
            position(0)
        }

        val cc = ByteBuffer.allocateDirect(index.size * 2)
        cc.order(ByteOrder.nativeOrder())
        indexBuffer = cc.asShortBuffer().apply {
            put(index)
            position(0)
        }

        val vertexShader = loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader = loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentShaderCode
        )

        mProgram = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }
    }
}
