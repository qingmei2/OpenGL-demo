package com.github.qingmei2.opengl_demo.c_image_process.processor

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.github.qingmei2.opengl_demo.R
import com.github.qingmei2.opengl_demo.c_image_process.ImageProcessor
import com.github.qingmei2.opengl_demo.createProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NoneImageProcessor(private val mResource: Resources) : ImageProcessor {

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

    // 不相反就会出错 ↓
//    private val textureData = floatArrayOf(
//        0.0f, 0.0f,
//        1.0f, 0.0f,
//        0.0f, 1.0f,
//        1.0f, 1.0f
//    )

    private val mVertexBuffer: FloatBuffer
    private val mTextureBuffer: FloatBuffer

    private var mProgram: Int = 0

    private var avPosition = 0
    private var afPosition = 0
    private var textureId = 0

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
        mProgram = createProgram(
            mResource,
            "c_image_processor/viewport_vertex_shader.sh",
            "c_image_processor/viewport_fragment_shader.sh"
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

        val bitmap = BitmapFactory.decodeResource(mResource, R.drawable.women_h)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        //设置大小位置
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        //清屏，清理掉颜色的缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //设置清屏的颜色，这里是float颜色的取值范围的[0,1]
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        //使用program
        GLES20.glUseProgram(mProgram)

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
