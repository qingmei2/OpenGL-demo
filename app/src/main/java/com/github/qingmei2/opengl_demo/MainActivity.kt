package com.github.qingmei2.opengl_demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.a_shape.ShapeMainActivity
import com.github.qingmei2.opengl_demo.b_animation.AnimMainActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_01).setOnClickListener {
            ShapeMainActivity.launch(this)
        }
        findViewById<View>(R.id.btn_02).setOnClickListener {
            AnimMainActivity.launch(this)
        }
    }
}