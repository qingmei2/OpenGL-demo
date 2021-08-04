package com.github.qingmei2.opengl_demo.c_image_process

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.c_image_process.processor.CameraImageProcessor
import com.github.qingmei2.opengl_demo.c_image_process.processor.NoneImageProcessor

class ImageProcessDetailActivity : AppCompatActivity() {

    companion object {

        private val IMAGE_PROCESSOR_KEY = "IMAGE_PROCESSOR_KEY"

        const val IMAGE_PROCESSOR_NONE = "IMAGE_PROCESSOR_NONE"
        const val IMAGE_PROCESSOR_CAMERA = "IMAGE_PROCESSOR_CAMERA"
        const val ANIM_TRANSLATE_X = "ANIM_TRANSLATE_X"
        const val ANIM_SCALE_X = "ANIM_SCALE_X"
        const val ANIM_ALL = "ANIM_ALL"

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
            IMAGE_PROCESSOR_NONE -> NoneImageProcessor(resources)
            IMAGE_PROCESSOR_CAMERA -> CameraImageProcessor(resources)
            else -> throw IllegalArgumentException("错误的参数 = $imageProcessor")
        }
    }
}