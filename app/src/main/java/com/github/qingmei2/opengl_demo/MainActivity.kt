package com.github.qingmei2.opengl_demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.a0_shape.ShapeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_hello_world).setOnClickListener {
            ShapeActivity.launch(this, ShapeActivity.SHAPE_TRIANGLE)
        }
        findViewById<View>(R.id.btn_02).setOnClickListener {
            ShapeActivity.launch(this, ShapeActivity.SHAPE_TRIANGLE_CAMERA)
        }
        findViewById<View>(R.id.btn_03).setOnClickListener {
            ShapeActivity.launch(this, ShapeActivity.SHAPE_TRIANGLE_COLORFUL)
        }
    }
}