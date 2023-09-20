package com.github.qingmei2.opengl_demo.d_zhongqiu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.qingmei2.opengl_demo.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player


/**
 * 中秋节主题的音乐播放器.
 */
class ZQMediaPlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ZQMediaPlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player_main)

        player = ExoPlayer.Builder(this).build()

        val uri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.nocturne)
        val mediaItem: MediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
