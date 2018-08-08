package com.libyanelite.libyaalwatanyaradio;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

class FetchURL extends AsyncTask<Void,Integer,String>  {

    private final static String TAG = "OSAMA";






    private GetFetchedURL resultURL;


    FetchURL(GetFetchedURL activityContext) {
        this.resultURL = activityContext;
    }



    @Override
    protected String doInBackground(Void... voids) {

        // Fetch URL Remotely ------------------------------START

        String data = "Failed to get Stream. Internet maybe slow or disconnected.";
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
        resultURL.getFetchedURL(o);



        Log.i(TAG, "onPostExecute: readLine isL" + o); // for debugging.


    }





}