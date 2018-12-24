package com.libyanelite.libyaalwatanyaradio;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity /* implements GetFetchedURL */{



    private ImageButton bPlay;
    private MediaPlayer mPlayer;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private AudioManager mAudioManager;
    private String streamURL;
    private TextView status;
    private ConstraintLayout mTopLayout;
    // private NotificationManagerCompat notificationManager;
    private ProgressBar progressBar;
    private TextView localTime;
    private TextView mPlayer_info;
    private TextView debug_info;
    private ShareActionProvider mShareActionProvider;

    final static  String TAG = "OSAMA";



    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
           // Log.d(this.getClass().getName(), "back button pressed");

          keyCode = KeyEvent.KEYCODE_HOME;
           moveTaskToBack(true);
        }
        return super.onKeyUp(keyCode, event);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/* Crash Test Button !      S T A R T

        Button crashButton = new Button(this);
        crashButton.setText("Crash!");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Crashlytics.getInstance().crash(); // Force a crash
            }
        });

        addContentView(crashButton, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


      Crash Test Button !     E N D  */


        status = findViewById(R.id.tvStatus);
        mPlayer_info = findViewById(R.id.mPlayer_info);
        debug_info = findViewById(R.id.debug_info);
        mTopLayout = findViewById(R.id.topLayout);
        progressBar = findViewById(R.id.pbLoading);
        bPlay = findViewById(R.id.bPlay);
        localTime = findViewById(R.id.Local_Time);


        // Display Local Time ----------------- S T A R T

        final  Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TimeZone tz = TimeZone.getTimeZone("GMT+02");
                Calendar c = Calendar.getInstance(tz);
                String time = String.format("%02d" , c.get(Calendar.HOUR_OF_DAY))+":"+
                        String.format("%02d" , c.get(Calendar.MINUTE));

                localTime.setText(getString(R.string.Local_Time)+time);


                handler.postDelayed(this, 1000);
            }
        }, 10);

        // Display Local Time ----------------- E N D


        final ImageButton bStop = findViewById(R.id.bStop);
        bStop.setEnabled(false);
        final ImageButton bClose = findViewById(R.id.bClose);
        // notificationManager = NotificationManagerCompat.from(this);
        // createNotification();
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (inetConnection()) {
            mTopLayout.setBackgroundResource(R.drawable.libya_watanya_benghazi_lighton);
            GetURL getURL = new GetURL();
            getURL.execute();

        } else {
            Toast.makeText(this, R.string.NoInternet, Toast.LENGTH_SHORT).show();
            mTopLayout.setBackgroundResource(R.drawable.libya_watanya_benghazi);

        }


// Hook to audio service ----------------------------------------------------------------
        // mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initControls();


//--------------------------------------------------------------------------------------

// Listen to focus change and act accordingly ------------------------------------------

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // Log.i(TAG, "AUDIOFOCUS_GAIN");
                        debug_info.setText("AUDIOFOCUS_GAIN");
                        if (mPlayer != null) {
                            mPlayer.start();
                        }
                        //  else {
                        //      play();
                        //  }

                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        //  Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        debug_info.setText("AUDIOFOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        //   Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        debug_info.setText("AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        //   Log.i(TAG, "AUDIOFOCUS_LOSS");
                        debug_info.setText("AUDIOFOCUS_LOSS stop()");
                        stop();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        //   Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        debug_info.setText("AUDIOFOCUS_LOSS_TRANSIEN pause()");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        //    Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        debug_info.setText("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK pause()");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        //   Log.i(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                        debug_info.setText("AUDIOFOCUS_REQUEST_FAILED stop()");
                        stop();
                        break;
                    default:
                        //
                }
            }
        };
//-----------------------------------------------------------------------------------------


        //mPlayer = new MediaPlayer();    //Moved to bPlay on Click
        //mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  //Moved to bPlay on Click


        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer_info.setText("");
                initialize_and_play();
                bStop.setEnabled(true);
            }
        });


        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer_info.setText("");
                stop();
            }
        });

        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Standard practice to initialize media player (copied from internet) --> START
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.stop();
                    }
                    mPlayer.reset();
                    mPlayer.release();
                    mPlayer = null;


                }
                // Standard practice to initialize media player (copied from internet) --> END

                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                handler.removeCallbacksAndMessages(null);
                // notificationManager.cancel(1);

                // System.exit(0);
                MainActivity.this.finish();
            }
        });

    }


    //////////////////  Process Actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share_app:
                doShare();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Intent doShare() {
        // populate the share intent with data
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.ShareTheApp)+getString(R.string.AppDownloadURL));
        startActivity(Intent.createChooser(intent, getString(R.string.share_the_app)));
        return intent;
    }

    /////////////////////////////// Proccess Actionbar E N D

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void stop() {
       // Log.i(TAG, "stop() Method started");
        progressBar.setVisibility(View.INVISIBLE);
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
                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                // mPlayer_info.setText("");

            }
            // Standard practice to initialize media player (copied from internet) --> END
            bPlay.setEnabled(true);
            status.setText(R.string.Stopped);
          //  mPlayer_info.setText("info");



    }

    private void pause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }

        }
    }

    private void play() {

        if ((mPlayer != null) && (streamURL != null) ) {
            if (!mPlayer.isPlaying()) {

                bPlay.setEnabled(false);
                bPlay.setImageResource(R.drawable.play_pressed);
                status.setText(R.string.Starting);

                // mPlayer.reset(); // Not Needed after adding the standard practice code in bPlay

                try {
                    //String STREAM_URL = "http://bbcwssc.ic.llnwd.net/stream/bbcwssc_mp1_ws-araba"; // Use the static address in case there is a probloem with the site.
                   // final String streamAddress = streamURL;


                    mPlayer.setDataSource(streamURL /* streamAddress */);
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
                            Toast.makeText(MainActivity.this, R.string.Connected, Toast.LENGTH_SHORT).show();
                            status.setText(R.string.Connected);

                            // This was Added to prevent radio from playing while user is in-call session.
                            // This caused by the delay in connection while receiving a call, then when in call if the connection is
                            // done the radio will play while the user is still in-call.
                            //===================================================================================
                            int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                                    // Hint: the music stream.
                                    AudioManager.STREAM_MUSIC,
                                    // Request permanent focus.
                                    AudioManager.AUDIOFOCUS_GAIN);

                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                                mediaPlayer.start();
                            }
                            else {
                                pause();
                            }
                            //====================================================================================
                        }
                    });

                    mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            debug_info.setText("what : " + what + " extra : " + extra);
                            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START ){
                                status.setText(R.string.Buffering);
                                mPlayer_info.setText("");
                            }

                            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END ){
                                status.setText(R.string.Connected);
                                mPlayer_info.setText("");
                            }



                            return false;
                        }
                    });

                    mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            debug_info.setText("Error What: " + what + " Error extra: " + extra);
                            if (what == 1 && extra == -2147483648){
                                mPlayer_info.setText(R.string.Server_Down);
                            } else {
                                mPlayer_info.setText("");
                            }
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

        /*
        if (streamURL == null ) {

            Toast.makeText(this, "Internet slow or not connected \n Reconnect then restart App.", Toast.LENGTH_SHORT).show();
            stop();



        }else if (!streamURL.contains("http")) {
            Toast.makeText(this, "Internet slow or not connected", Toast.LENGTH_SHORT).show();
            stop();
        } */



    }

    private void reinitialize() {
        stop();
        initialize_and_play();
    }

    private void initialize_and_play() {

        if (inetConnection()) {
        mTopLayout.setBackgroundResource(R.drawable.libya_watanya_benghazi_lighton);


            Log.i(TAG, "initialize_and_play: Done Getting URL : " + streamURL);



            // if (!doneFetchingURL || streamURL == null) {

            GetURL getURL = new GetURL();
            //execute the async task
            getURL.execute();

     //   }


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

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            play();
        }
    }else {
            Toast.makeText(this, "Internet is not available", Toast.LENGTH_SHORT).show();
            mTopLayout.setBackgroundResource(R.drawable.libya_watanya_benghazi);
        }
    }





    private boolean inetConnection () {

        ConnectivityManager cm =
                (ConnectivityManager)MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork;
        assert cm != null;
        activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null) &&
                activeNetwork.isConnectedOrConnecting();
    }

     private  class GetURL extends AsyncTask<Void,Integer,String>  {

       // private final static String TAG = "OSAMA MNEINA";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected String doInBackground(Void... voids) {


            // Fetch URL Remotely ------------------------------START

            String data = getString(R.string.FailedToGetURL);
            try {
                // Create a URL for the desired page
                URL url = new URL("http://radio.libyanelite.ly/Alwatanya.html");

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                data = in.readLine();
                in.close();

            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground - Malformed URL Exception :" + e.toString(), e);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground - IO Exception :" + e.toString(), e);
            }
            // Fetch URL Remotely ------------------------------END

            return data;
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            streamURL = o;
            progressBar.setVisibility(View.INVISIBLE);

            Log.i(TAG, "onPostExecute: Done Fething URL : "+streamURL);
            debug_info.setText("onPostExecute: Done Fething URL : "+streamURL);

            if(streamURL == getString(R.string.FailedToGetURL)){
                mPlayer_info.setText(getString(R.string.FailedToGetURL));
            }else {
                mPlayer_info.setText("");
            }

          //  Toast.makeText(MainActivity.this, "Done Fetching URL" + o, Toast.LENGTH_SHORT).show();

        }





    }
    /* Suspend notification - causes some delay in some phones
private void createNotification(){

    // This intent was used because the standard intent syntax will make new instance of MainActivity each time the notification is clicked
    final Intent intent = new Intent(this, MainActivity.class);
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


    PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);


    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.libya_alwatanya_radio_round);

    Notification notification  = new NotificationCompat.Builder(this,CHANNEL_ID)
            .setLargeIcon(bm)
            .setSmallIcon(R.drawable.ic_radio)
            .setContentTitle(getString(R.string.notification_Title))
            .setContentText(getString(R.string.notification_Description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .build();
    notification.flags |= Notification.FLAG_NO_CLEAR;


    notificationManager.notify(1,notification);

}
    */

    private void initControls()
    {
        try
        {
            // private static final String TAG = "Osama Mneina";
            SeekBar volumeSeekbar = findViewById(R.id.volSeekbar);
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(mAudioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(mAudioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e)
        {

        }
    }


}







