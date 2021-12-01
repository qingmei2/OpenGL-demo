package com.github.qingmei2.opengl_demo.d_video;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.qingmei2.opengl_demo.R;

public class D01VideoPlayerActivity extends AppCompatActivity {

    public static void launch(Context context) {
        context.startActivity(new Intent(context, D01VideoPlayerActivity.class));
    }

    private GLSurfaceView mGLSurfaceView;
    private D01VideoRenderer mRenderer;

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_01);

        mGLSurfaceView = findViewById(R.id.surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);

        mMediaPlayer = MediaPlayer.create(this, R.raw.d_video_640_360);

        mRenderer = new D01VideoRenderer(this, mMediaPlayer, surfaceTexture -> {
            mRenderer.setVideoAvailable(true);
            mGLSurfaceView.requestRender();
        });

        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetVideo();
    }

    private void resetVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }
}
