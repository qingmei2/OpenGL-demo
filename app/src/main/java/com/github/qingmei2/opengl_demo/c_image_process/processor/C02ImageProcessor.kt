package com.github.qingmei2.opengl_demo.c_image_process.processor

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.github.qingmei2.opengl_demo.R
import com.github.qingmei2.opengl_demo.c_image_process.ImageProcessor
import com.github.qingmei2.opengl_demo.loadShaderWithResource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 通过调整窗口大小，适配图片
 */
class C02ImageProcessor(private val mContext: Context) : ImageProcessor {

    // 绘制坐标范围
    private val vertexData = floatArrayOf(
        1.0f, 1.0f,         // 右上
        1.0f, -1.0f,        // 右下
        -1.0f, 1.0f,        // 左上
        -1.0f, -1.0f        // 左下
    )

    // 纹理坐标需要和顶点坐标相反
    // https://blog.csdn.net/zhangpengzp/article/details/89543108
    private val textureData = floatArrayOf(
        1.0f, 0.0f,         // 右下
        1.0f, 1.0f,         // 右上
        0.0f, 0.0f,         // 左下
        0.0f, 1.0f          // 左上
    )

    private val mVertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(vertexData)

    private val mTextureBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(textureData)

    // gl程序句柄
    private var mProgram: Int = 0

    // 顶点坐标句柄
    private var avPosition = 0

    // 纹理坐标句柄
    private var afPosition = 0

    // 纹理ID
    private var textureId = 0

    private var mBitmapW: Int = 0
    private var mBitmapH: Int = 0

    init {
        //初始化buffer
        mVertexBuffer.position(0)
        mTextureBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mProgram = loadShaderWithResource(
            mContext,
            R.raw.viewport_vertex_shader,
            R.raw.viewport_fragment_shader
        )

        // 顶点坐标句柄
        avPosition = GLES20.glGetAttribLocation(mProgram, "av_Position")
        // 纹理坐标句柄
        afPosition = GLES20.glGetAttribLocation(mProgram, "af_Position")

        // 生成纹理
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            return
        }
        textureId = textureIds[0]

        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        // 环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        // 过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)


        val bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.women_h)     // 横图
//        val bitmap = BitmapFactory.decodeResource(mResource, R.drawable.women_v)            // 竖图
        mBitmapW = bitmap.width / 3
        mBitmapH = bitmap.height / 3

        // 加载纹理到GPU
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        // 回收Bitmap
        bitmap.recycle()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // 设置大小位置
        val screenRatio = width.toFloat() / height.toFloat()
        val bitmapRatio = mBitmapW.toFloat() / mBitmapH.toFloat()

        if (screenRatio < bitmapRatio) {        // 横屏显示，上下留黑边
            val h = (width / bitmapRatio).toInt()
            val y = (height - h) / 2
            val w = width
            val x = 0
            GLES20.glViewport(x, y, w, h)
        } else {                                // 竖屏显示，左右留黑边
            val w = (height * bitmapRatio).toInt()
            val x = (width - w) / 2
            val h = height
            val y = 0
            GLES20.glViewport(x, y, w, h)
        }
    }

    override fun onDrawFrame(gl: GL10) {
        //清屏，清理掉颜色的缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //设置清屏的颜色，这里是float颜色的取值范围的[0,1]
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        //使用program
        GLES20.glUseProgram(mProgram)

        //设置为可用的状态
        //设置顶点
        GLES20.glEnableVertexAttribArray(avPosition)
        //size 指定每个顶点属性的组件数量。必须为1、2、3或者4。初始值为4。（如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
        //stride 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。
        //size 2 代表(x,y)，stride 8 代表跨度 （2个点为一组，2个float有8个字节）
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer)

        //设置片元
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer)

        //执行绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}
