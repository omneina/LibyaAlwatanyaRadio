package com.libyanelite.libyaalwatanyaradio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;



public class MainActivity extends AppCompatActivity {

    private ImageButton bPlay;
    private ImageButton bStop;
    private String STREAM_URL = "http://audio1.meway.tv:8082/";
    private MediaPlayer mPlayer;
    private ImageButton bClose;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private static final String TAG = "Radio Elite";
    private boolean mAudioFocusGranted = false;
    private AudioManager mAudioManager;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            keyCode = KeyEvent.KEYCODE_HOME;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Hook to audio service ----------------------------------------------------------------
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


//--------------------------------------------------------------------------------------

// Listen to focus change and act accordingly ------------------------------------------

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
                        play();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                        break;
                    default:
                        //
                }
            }
        };
//-----------------------------------------------------------------------------------------

        bPlay =  findViewById(R.id.bPlay);
        bStop =  findViewById(R.id.bStop);
        bClose = findViewById(R.id.bClose);
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

// Request Audio Focus ---------------------------------------------------------------------
                int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                        // Hint: the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN);

//-------------------------------------------------------------------------------------------

                if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    play();
                }
            }
        });


        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bPlay.setImageResource(R.drawable.play); // Set play button image to normal.
                //bStop.setImageResource(R.drawable.stop_pressed); // Set stop button image to pressed.

                mPlayer.stop();
                bPlay.setEnabled(true);

            }
        });

        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.stop();
                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                mPlayer.release();
                mPlayer = null;
                // System.exit(0);
                MainActivity.this.finish();
            }
        });
    }

    private void pause() {
        if (mPlayer != null){
            mPlayer.pause();
        }

    }

    private void play() {
        if (!mPlayer.isPlaying()) {
            bPlay.setEnabled(false);
            bPlay.setImageResource(R.drawable.play_pressed);


            mPlayer.reset();
            try {
                mPlayer.setDataSource(STREAM_URL);
                mPlayer.prepareAsync();
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }




}
