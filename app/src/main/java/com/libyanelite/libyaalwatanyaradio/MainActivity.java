package com.libyanelite.libyaalwatanyaradio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements GetFetchedURL{



    private ImageButton bPlay;
    private MediaPlayer mPlayer;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private static final String TAG = "Radio Elite";
    private AudioManager mAudioManager;
    private String streamURL;
    private String streamAddress;


    @Override
    public void getFetchedURL(String result) {
        streamURL = result;
        Toast.makeText(this, "URL Fetched!! = " + streamURL, Toast.LENGTH_SHORT).show();
        if (streamURL == "Failed to get Stream. Internet maybe slow or disconnected."){
            TextView tvTemp = MainActivity.this.findViewById(R.id.tvStatus);
            tvTemp.setText(streamURL);
        }

    }


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
         TextView status = findViewById(R.id.tvStatus);


        FetchURL fetchURL = new FetchURL(MainActivity.this);


        //execute the async task
        fetchURL.execute();




         String test = streamURL;
         //
        Toast.makeText(this, "URL is : " + streamURL, Toast.LENGTH_LONG).show();
       // streamURL = fetchURL.getMyURL();
/*
        // Fetch URL Remotely ------------------------------START
        // Set Thread for URL Fetch ------- START
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Set Thread for URL Fetch ------- END
       try {
            // Create a URL for the desired page
            URL url = new URL("http://radio.libyanelite.ly/Alwatanya.html");

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            streamURL = in.readLine();
            in.close();

        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malform URL",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "IO Exception" ,
                    Toast.LENGTH_LONG).show();

        }
        // Fetch URL Remotely ------------------------------END
*/
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

        bPlay =  findViewById(R.id.bPlay);
        ImageButton bStop = findViewById(R.id.bStop);
        ImageButton bClose = findViewById(R.id.bClose);
        //mPlayer = new MediaPlayer();    //Moved to bPlay on Click
        //mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  //Moved to bPlay on Click


        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // mPlayer initialization is moved here because setting mPlayer to null in bStop then pressing bPlay caused Fatal Error. ---START
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        });


        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                }
                // Standard practice to initialize media player (copied from internet) --> END
                bPlay.setEnabled(true);

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
                }
                // Standard practice to initialize media player (copied from internet) --> END

                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);

                // System.exit(0);
                MainActivity.this.finish();
            }
        });
    }

    private void pause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }

        }
    }

    private void play() {

        if (mPlayer != null) {
            if (!mPlayer.isPlaying()) {
                bPlay.setEnabled(false);
                bPlay.setImageResource(R.drawable.play_pressed);
                // mPlayer.reset(); // Not Needed after adding the standard practice code in bPlay

                try {
                    //String STREAM_URL = "http://bbcwssc.ic.llnwd.net/stream/bbcwssc_mp1_ws-araba"; // Use the static address in case there is a probloem with the site.
                    streamAddress = streamURL; // trim(): Returns a string whose value is this string, with any leading and trailing whitespace removed.

                    Toast.makeText(this, "URL is : " + streamAddress,
                            Toast.LENGTH_LONG).show();
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
                            mediaPlayer.start();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error " + e, Toast.LENGTH_LONG).show();
                }
            }
        }
    }



}







