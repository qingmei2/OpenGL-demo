package com.github.qingmei2.opengl_demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.qingmei2.opengl_demo.a1_hello_world.A01Activity
import com.github.qingmei2.opengl_demo.a2_matrix.A02Activity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_hello_world).setOnClickListener {
            navigation(A01Activity::class.java)
        }
        findViewById<View>(R.id.btn_02).setOnClickListener {
            navigation(A02Activity::class.java)
        }
    }

    private fun <T : Activity> navigation(activityClazz: Class<T>) {
        Intent(this, activityClazz).apply {
            startActivity(this)
        }
    }
}