package com.github.qingmei2.opengl_demo.c_image_process

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.c_image_process.processor.*

class ImageProcessDetailActivity : AppCompatActivity() {

    companion object {

        private val IMAGE_PROCESSOR_KEY = "IMAGE_PROCESSOR_KEY"

        const val IMAGE_PROCESSOR_NONE = "IMAGE_PROCESSOR_NONE"
        const val IMAGE_PROCESSOR_VIEWPORT = "IMAGE_PROCESSOR_VIEWPORT"
        const val IMAGE_PROCESSOR_VIEWPORT_MATRIX = "IMAGE_PROCESSOR_VIEWPORT_MATRIX"
        const val IMAGE_PROCESSOR_ROTATE = "IMAGE_PROCESSOR_ROTATE"
        const val IMAGE_PROCESSOR_ROTATE_MATRIX = "IMAGE_PROCESSOR_ROTATE_MATRIX"
        const val IMAGE_PROCESSOR_3D = "IMAGE_PROCESSOR_3D"

        fun launch(context: Context, anim: String) {
            val intent = Intent(context, ImageProcessDetailActivity::class.java)
            intent.putExtra(IMAGE_PROCESSOR_KEY, anim)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageProcessor = getImageProcessor()
        val glSurfaceView = ImageProcessGlSurfaceView(this, imageProcessor)
        setContentView(glSurfaceView)
    }

    private fun getImageProcessor(): ImageProcessor {
        val imageProcessor = intent.getStringExtra(IMAGE_PROCESSOR_KEY)
        return when (imageProcessor) {
            IMAGE_PROCESSOR_NONE -> C01ImageProcessor(this)
            IMAGE_PROCESSOR_VIEWPORT -> C02ImageProcessor(this)
            IMAGE_PROCESSOR_ROTATE -> C03ImageProcessor(this)
            IMAGE_PROCESSOR_ROTATE_MATRIX -> C04ImageProcessor(this)
            IMAGE_PROCESSOR_VIEWPORT_MATRIX -> C05ImageProcessor(this)
            IMAGE_PROCESSOR_3D -> C06Image3DProcessor(this)
            else -> throw IllegalArgumentException("错误的参数 = $imageProcessor")
        }
    }
}
