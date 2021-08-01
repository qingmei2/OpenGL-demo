package com.github.qingmei2.opengl_demo.a_shape

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.a_shape.shapes.Triangle
import com.github.qingmei2.opengl_demo.a_shape.shapes.CameraTriangle
import com.github.qingmei2.opengl_demo.a_shape.shapes.ColorfulTriangle
import com.github.qingmei2.opengl_demo.a_shape.shapes.Square

/**
 * 绘制颜色渐变的三角形
 */
class ShapeDetailActivity : AppCompatActivity() {

    companion object {

        private val SHAPE_KEY = "SHAPE_KEY"

        val SHAPE_TRIANGLE = "SHAPE_TRIANGLE"
        val SHAPE_TRIANGLE_CAMERA = "SHAPE_TRIANGLE_CAMERA"
        val SHAPE_TRIANGLE_COLORFUL = "SHAPE_TRIANGLE_COLORFUL"
        val SHAPE_SQUARE = "SHAPE_SQUARE"

        fun launch(context: Context, shape: String) {
            val intent = Intent(context, ShapeDetailActivity::class.java)
            intent.putExtra(SHAPE_KEY, shape)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shape = getShape()
        val glSurfaceView = ShapeGlSurfaceView(this, shape)
        setContentView(glSurfaceView)
    }

    private fun getShape(): Shape {
        val shape = intent.getStringExtra(SHAPE_KEY)
        return when (shape) {
            SHAPE_TRIANGLE -> Triangle()
            SHAPE_TRIANGLE_CAMERA -> CameraTriangle()
            SHAPE_TRIANGLE_COLORFUL -> ColorfulTriangle()
            SHAPE_SQUARE -> Square()
            else -> throw IllegalArgumentException("错误的参数 = $shape")
        }
    }
}