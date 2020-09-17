package com.example.maker.recordapplication.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.example.maker.recordapplication.R;
import com.example.maker.recordapplication.tool.UserAction;

public class VideoPlayActivity extends Activity {
    private VideoView videoView;;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_play_layout);
        initView();
        initData();
    }

    private void initData() {
        MediaController mc = new MediaController(VideoPlayActivity.this);//Video是我类名，是你当前的类
        videoView.setMediaController(mc);//设置VedioView与MediaController相关联
        videoView.setVideoPath(UserAction.selectedVideoPath);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });
    }

    private void initView() {
       videoView =findViewById(R.id.video);
    }
}
