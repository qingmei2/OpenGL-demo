package com.github.qingmei2.opengl_demo.b_animation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.R

class AnimMainActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, AnimMainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim_main)

        findViewById<View>(R.id.btn_01).setOnClickListener {
            AnimDetailActivity.launch(this, AnimDetailActivity.ANIM_ROTATE_Z)
        }
        findViewById<View>(R.id.btn_02).setOnClickListener {
            AnimDetailActivity.launch(this, AnimDetailActivity.ANIM_ROTATE_Y)
        }
    }
}