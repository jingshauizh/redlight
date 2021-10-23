package com.example.opencvt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    AssetManager assetManager;
    MediaPlayer player = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera_view_start).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CameraActivityNew.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.red_audio_test).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                try {
                    if(player.isPlaying()){
                        player.stop();
                        player.release();
                        player = new MediaPlayer();
                    }
                    AssetFileDescriptor fileDescriptor = assetManager.openFd("red.mp3");
                    player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        player = new MediaPlayer();
        assetManager = getResources().getAssets();

        findViewById(R.id.green_audio_test).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                try {
                    if(player.isPlaying()){
                        player.stop();
                        player.release();
                        player = new MediaPlayer();
                    }
                    AssetFileDescriptor fileDescriptor = assetManager.openFd("green.mp3");
                    player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
