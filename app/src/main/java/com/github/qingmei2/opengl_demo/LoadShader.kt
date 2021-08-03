package com.github.qingmei2.opengl_demo

import android.content.res.Resources
import android.opengl.GLES20

fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}

fun createProgram(
    res: Resources,
    vertexRes: String,
    fragmentRes: String
): Int {
    return loadAssetsShader(
        loadFromAssetsFile(vertexRes, res)!!,
        loadFromAssetsFile(fragmentRes, res)!!
    )
}

fun loadAssetsShader(vertexSource: String, fragmentSource: String): Int {
    val vertex: Int =
        loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
    if (vertex == 0) return 0
    val fragment: Int =
        loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
    if (fragment == 0) return 0
    var program = GLES20.glCreateProgram()
    if (program != 0) {
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
    }
    return program
}

private fun loadFromAssetsFile(fname: String, res: Resources): String? {
    val result = StringBuilder()
    try {
        val `is` = res.assets.open(fname)
        var ch: Int
        val buffer = ByteArray(1024)
        while (-1 != `is`.read(buffer).also { ch = it }) {
            result.append(String(buffer, 0, ch))
        }
    } catch (e: Exception) {
        return null
    }
    return result.toString().replace("\\r\\n".toRegex(), "\n")
}