package com.github.qingmei2.opengl_demo.b_animation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AnimDetailActivity : AppCompatActivity() {

    companion object {

        private val ANIM_KEY = "ANIM_KEY"

        const val ANIM_ROTATE = "ANIM_ROTATE"

        fun launch(context: Context, anim: String) {
            val intent = Intent(context, AnimDetailActivity::class.java)
            intent.putExtra(ANIM_KEY, anim)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = getAnimation()
        val glSurfaceView = AnimGlSurfaceView(this, animation)
        setContentView(glSurfaceView)
    }

    private fun getAnimation(): Animation {
        val animation = intent.getStringExtra(ANIM_KEY)
        return when (animation) {
            ANIM_ROTATE -> TriangleForAnim()
            else -> throw IllegalArgumentException("错误的参数 = $animation")
        }
    }
}