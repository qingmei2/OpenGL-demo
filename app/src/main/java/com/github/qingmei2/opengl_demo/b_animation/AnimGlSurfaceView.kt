package com.github.qingmei2.opengl_demo.b_animation

import android.content.Context
import android.opengl.GLSurfaceView

class AnimGlSurfaceView(
    context: Context,
    animation: Animation
) : GLSurfaceView(context) {

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(animation)

//        启用连续渲染
//        https://developer.android.google.cn/training/graphics/opengl/motion?hl=zh_cn
//        如果您认真遵循了此类中针对这一点的示例代码，请确保对将渲染模式设置为仅在脏时才进行绘制的行取消备注，
//        否则 OpenGL 仅按一个增量旋转形状，然后就等待从 GLSurfaceView 容器调用 requestRender()

//        renderMode = RENDERMODE_WHEN_DIRTY
    }
}