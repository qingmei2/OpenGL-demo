package com.github.qingmei2.opengl_demo.c_image_process.processor

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix.orthoM
import com.github.qingmei2.opengl_demo.R
import com.github.qingmei2.opengl_demo.c_image_process.ImageProcessor
import com.github.qingmei2.opengl_demo.loadShaderWithResource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class C05ImageProcessor(private val mContext: Context) : ImageProcessor {

    private val U_MATRIX = "u_Matrix"

    //顶点坐标
    private val vertexData = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )

    // 纹理坐标需要和顶点坐标相反
    // https://blog.csdn.net/zhangpengzp/article/details/89543108
    private val textureData = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    private val mVertexBuffer: FloatBuffer
    private val mTextureBuffer: FloatBuffer

    private var mProgram: Int = 0

    private var avPosition = 0
    private var afPosition = 0
    private var textureId = 0

    private val projectionMatrix = FloatArray(16)
    private var uMatrixLocation = 0

    private var mBitmapWidth = 0
    private var mBitmapHeight = 0

    init {
        //初始化buffer
        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        mVertexBuffer.position(0)

        mTextureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureData)
        mTextureBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mProgram = loadShaderWithResource(
            mContext,
            R.raw.projection_vertex_shader,
            R.raw.projection_fragment_shader
        )

        avPosition = GLES20.glGetAttribLocation(mProgram, "av_Position")
        afPosition = GLES20.glGetAttribLocation(mProgram, "af_Position")

        //生成纹理
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            return
        }
        textureId = textureIds[0]
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        //环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        //过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.women_h)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        mBitmapWidth = bitmap.width
        mBitmapHeight = bitmap.height

        bitmap.recycle()

        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        //设置大小位置
        GLES20.glViewport(0, 0, width, height)

        val isHorizontalScreen = width > height

        // 设置大小位置
        val bitmapRatio = mBitmapWidth.toFloat() / mBitmapHeight.toFloat()

        val hRatio = height / (width / bitmapRatio)
        val wRatio = width / (height * bitmapRatio)

        // 1. 矩阵数组
        // 2. 结果矩阵起始的偏移量
        // 3. left：x的最小值
        // 4. right：x的最大值
        // 5. bottom：y的最小值
        // 6. top：y的最大值
        // 7. near：z的最小值
        // 8. far：z的最大值
        if (isHorizontalScreen) {
            orthoM(projectionMatrix, 0, -wRatio, wRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -hRatio, hRatio, -1f, 1f);
        }
    }

    override fun onDrawFrame(gl: GL10) {
        //清屏，清理掉颜色的缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //设置清屏的颜色，这里是float颜色的取值范围的[0,1]
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        //使用program
        GLES20.glUseProgram(mProgram)

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        //设置为可用的状态
        GLES20.glEnableVertexAttribArray(avPosition)
        //size 指定每个顶点属性的组件数量。必须为1、2、3或者4。初始值为4。（如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
        //stride 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。
        //size 2 代表(x,y)，stride 8 代表跨度 （2个点为一组，2个float有8个字节）
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer)

        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
