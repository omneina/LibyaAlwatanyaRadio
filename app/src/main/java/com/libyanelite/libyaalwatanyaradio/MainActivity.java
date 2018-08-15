package com.libyanelite.libyaalwatanyaradio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements GetFetchedURL{



    private ImageButton bPlay;
    private MediaPlayer mPlayer;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private static final String TAG = "Radio Elite";
    private AudioManager mAudioManager;
    private String streamURL;
    private TextView status;
    private TextView mPlyer_info;
    private WifiManager.WifiLock wifiLock;


    // This is the interface method that passes the fetched stream URL to Main Activity. ===== START
    @Override
    public void getFetchedURL(String result) {
        streamURL = result;
        /*
        Toast.makeText(this, "URL Fetched!! = " + streamURL, Toast.LENGTH_SHORT).show();
        if (streamURL.equals("Failed to get Stream. Internet maybe slow or disconnected.") ){
            TextView tvTemp = MainActivity.this.findViewById(R.id.tvStatus);
            tvTemp.setText("Failed to get Stream.\n Internet maybe slow or disconnected.");

        }
        */
    }

    // This is the interface method that passes the fetched stream URL to Main Activity. ===== END

    //private TextView status = findViewById(R.id.tvStatus);


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



        FetchURL fetchURL = new FetchURL(MainActivity.this);


        //execute the async task
        fetchURL.execute();





         //


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
                        if (mPlayer != null) {
                            mPlayer.start();
                        }
                      //  else {
                      //      play();
                      //  }

                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.i(TAG, "AUDIOFOCUS_LOSS");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.i(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                        break;
                    default:
                        //
                }
            }
        };
//-----------------------------------------------------------------------------------------

       status = findViewById(R.id.tvStatus);
       mPlyer_info = findViewById(R.id.tvInfo);
        bPlay =  findViewById(R.id.bPlay);
        final ImageButton bStop = findViewById(R.id.bStop);bStop.setEnabled(false);
        final ImageButton bClose = findViewById(R.id.bClose);

        //mPlayer = new MediaPlayer();    //Moved to bPlay on Click
        //mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  //Moved to bPlay on Click


        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               initialize_and_play();
                bStop.setEnabled(true);
            }
        });


        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            stop();
            }
        });

        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Standard practice to initialize media player (copied from internet) --> START
                if(mPlayer!=null) {
                    if(mPlayer.isPlaying()) {
                        mPlayer.stop();
                    }
                    mPlayer.reset();
                    mPlayer.release();
                    mPlayer=null;
                    ReleaseWifiLock();
                }
                // Standard practice to initialize media player (copied from internet) --> END

                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);

                // System.exit(0);
                MainActivity.this.finish();
            }
        });
    }

    private void stop() {

            bPlay.setImageResource(R.drawable.play); // Set play button image to normal.
            //bStop.setImageResource(R.drawable.stop_pressed); // Set stop button image to pressed.
            // Standard practice to initialize media player (copied from internet) --> START
            if(mPlayer!=null) {
                if(mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                mPlayer.reset();
                mPlayer.release();
                mPlayer=null;
                ReleaseWifiLock();
            }
            // Standard practice to initialize media player (copied from internet) --> END
            bPlay.setEnabled(true);
            status.setText("Stopped");



    }

    private void pause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }

        }
    }

    private void play() {

        if ((mPlayer != null) && (streamURL != null)) {
            if (!mPlayer.isPlaying()) {
                //mPlayer.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);
                bPlay.setEnabled(false);
                bPlay.setImageResource(R.drawable.play_pressed);
                status.setText("Starting...");
                // mPlayer.reset(); // Not Needed after adding the standard practice code in bPlay

                try {
                    //String STREAM_URL = "http://bbcwssc.ic.llnwd.net/stream/bbcwssc_mp1_ws-araba"; // Use the static address in case there is a probloem with the site.
                    final String streamAddress = streamURL;


                    mPlayer.setDataSource(streamAddress);
                    mPlayer.prepareAsync();

                /*
                //######################################################

                mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        status.setText(percent);
                    }
                });

                //######################################################
                */
                    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                            status.setText("Connected");
                            mediaPlayer.start();
                        }
                    });

                    mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            mPlyer_info.setText("what : " + what + " extra : " + extra);
                            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START )
                                status.setText("Buffering...");
                            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END )
                                status.setText("Connected");

                            return false;
                        }
                    });

                    mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            mPlyer_info.setText("Error What: " + what + " Error extra: " + extra);
                           reinitialize();
                            return false;
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "play: " + e);
                }

            }
        }

        if (streamURL == null ) {

            Toast.makeText(this, "Internet slow or not connected \n Reconect then restart App.", Toast.LENGTH_SHORT).show();
            stop();



        }else if (!streamURL.contains("http")) {
            Toast.makeText(this, "Internet slow or not connected", Toast.LENGTH_SHORT).show();
            stop();
        }



    }

    private void reinitialize() {
        stop();
        initialize_and_play();
    }

    private void initialize_and_play() {

        // mPlayer initialization is moved here because setting mPlayer to null in bStop then pressing bPlay caused Fatal Error. ---START
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        AcquireWifiLock();

        // mPlayer initialization is moved here because setting mPlayer to null in bStop then pressing bPlay caused Fatal Error. ---END

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


    /// Lock the wifi so we can still stream under lock screen
    private void AcquireWifiLock()
    {

        wifiLock = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "Osama WiFi Lock");

        wifiLock.acquire();
    }

    /// This will release the wifi lock if it is no longer needed
    private void ReleaseWifiLock()
    {
        wifiLock.release();
    }

}







