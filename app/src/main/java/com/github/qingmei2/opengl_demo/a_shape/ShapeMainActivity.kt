package com.github.qingmei2.opengl_demo.a_shape

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.R

class ShapeMainActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ShapeMainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shape_main)

        findViewById<View>(R.id.btn_01).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_TRIANGLE)
        }
        findViewById<View>(R.id.btn_02).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_TRIANGLE_CAMERA)
        }
        findViewById<View>(R.id.btn_03).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_TRIANGLE_COLORFUL)
        }
        findViewById<View>(R.id.btn_04).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_SQUARE)
        }
        findViewById<View>(R.id.btn_05).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_SQUARE_2)
        }
        findViewById<View>(R.id.btn_06).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_RECTANGLE)
        }
        findViewById<View>(R.id.btn_07).setOnClickListener {
            ShapeDetailActivity.launch(this, ShapeDetailActivity.SHAPE_RECTANGLE2)
        }
    }
}
