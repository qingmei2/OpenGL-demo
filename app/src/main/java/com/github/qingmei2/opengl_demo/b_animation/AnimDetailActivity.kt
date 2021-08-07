package com.github.qingmei2.opengl_demo.b_animation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.b_animation.animation.*

class AnimDetailActivity : AppCompatActivity() {

    companion object {

        private val ANIM_KEY = "ANIM_KEY"

        const val ANIM_ROTATE_Z = "ANIM_ROTATE_Z"
        const val ANIM_ROTATE_Y = "ANIM_ROTATE_Y"
        const val ANIM_TRANSLATE_X = "ANIM_TRANSLATE_X"
        const val ANIM_SCALE_X = "ANIM_SCALE_X"
        const val ANIM_ALL = "ANIM_ALL"
        const val ANIM_ALL_SNOW_EFFECT = "ANIM_ALL_SNOW_EFFECT"

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
            ANIM_ROTATE_Z -> TriangleRotateZ()
            ANIM_ROTATE_Y -> TriangleRotateY()
            ANIM_TRANSLATE_X -> TriangleTranslateX()
            ANIM_SCALE_X -> TriangleScaleX()
            ANIM_ALL -> TriangleTransAndScaleAndRotateX()
            ANIM_ALL_SNOW_EFFECT -> TriangleTransAndScaleAndRotateX()
            else -> throw IllegalArgumentException("错误的参数 = $animation")
        }
    }
}
