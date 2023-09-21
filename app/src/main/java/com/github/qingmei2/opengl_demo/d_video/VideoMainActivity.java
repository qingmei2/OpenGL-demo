package com.github.qingmei2.opengl_demo.d_video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.qingmei2.opengl_demo.R;

public class VideoMainActivity extends AppCompatActivity {

    public static void launch(Context context) {
        context.startActivity(new Intent(context, VideoMainActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_main);

        findViewById(R.id.btn_01).setOnClickListener(__ -> {
            D01VideoPlayerActivity.launch(this);
        });
    }
}
