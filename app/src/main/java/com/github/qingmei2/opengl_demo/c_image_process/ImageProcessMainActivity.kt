package com.github.qingmei2.opengl_demo.c_image_process

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.R

class ImageProcessMainActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ImageProcessMainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_process_main)

        findViewById<View>(R.id.btn_01).setOnClickListener {
            ImageProcessDetailActivity.launch(this, ImageProcessDetailActivity.IMAGE_PROCESSOR_NONE)
        }
        findViewById<View>(R.id.btn_02).setOnClickListener {
            ImageProcessDetailActivity.launch(this, ImageProcessDetailActivity.IMAGE_PROCESSOR_VIEWPORT)
        }
        findViewById<View>(R.id.btn_03).setOnClickListener {
            ImageProcessDetailActivity.launch(this, ImageProcessDetailActivity.IMAGE_PROCESSOR_ROTATE)
        }
        findViewById<View>(R.id.btn_04).setOnClickListener {
            ImageProcessDetailActivity.launch(this, ImageProcessDetailActivity.IMAGE_PROCESSOR_ROTATE_MATRIX)
        }
    }
}
