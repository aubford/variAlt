package com.example.aubreyford.vario;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SplashActivity extends AppCompatActivity {

    private Float mslp;
    private ImageButton startFlight;
    private Button enterLanding;
    private Button autoLanding;
    private Button calibrateAltitude;
    private Button myFlights;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startFlight = (ImageButton) findViewById(R.id.spl_startFlight);
        enterLanding = (Button) findViewById(R.id.spl_enterLanding);
        autoLanding = (Button) findViewById(R.id.spl_autoLanding);
        calibrateAltitude = (Button) findViewById(R.id.spl_calibrateAltitude);
        myFlights = (Button) findViewById(R.id.spl_myFlights);

        setListeners();
    }



    private void setListeners(){

        calibrateAltitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ////get the location

                new MetarAsyncTask().execute(location.getLatitude(), location.getLongitude());
            }
        });
    }




    private class MetarAsyncTask extends AsyncTask<Number, Void, Float> {
        private static final String WS_URL = "http://ws.geonames.org/findNearByWeatherJSON";
        private static final String SLP_STRING = "slp";


        @Override
        protected Float doInBackground(Number... params)
        {
            HttpURLConnection urlConnection = null;
            Float newMslp = null;

            try {
                // Generate URL with parameters for web service
                Uri uri = Uri.parse(WS_URL)
                                .buildUpon()
                                .appendQueryParameter("lat", String.valueOf(params[0]))
                                .appendQueryParameter("lng", String.valueOf(params[1]))
                                .appendQueryParameter("username", "aubford")
                                .build();

                // Connect to web service
                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                // Read web service response and convert to a string
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                // Convert InputStream to String using a Scanner
                Scanner inputStreamScanner = new Scanner(inputStream).useDelimiter("\\A");
                String response = inputStreamScanner.next();
                inputStreamScanner.close();

                Log.d("Web Service Response", response);

                JSONObject json = new JSONObject(response);

                String observation = json.getJSONObject("weatherObservation").getString("observation");

                // Split on whitespace
                String[] values = observation.split("\\s");

                // Iterate of METAR string until SLP string is found
                String slpString = null;
                for (int i = 1; i < values.length; i++) {
                    String value = values[i];

                    if (value.startsWith(SLP_STRING.toLowerCase()) || value.startsWith(SLP_STRING.toUpperCase()))
                    {
                        slpString = value.substring(SLP_STRING.length());
                        break;
                    }
                }

                // Decode SLP string into numerical representation
                StringBuffer sb = new StringBuffer(slpString);

                sb.insert(sb.length() - 1, ".");

                float val1 = Float.parseFloat("10" + sb);
                float val2 = Float.parseFloat("09" + sb);

                newMslp = (Math.abs((1000 - val1)) < Math.abs((1000 - val2))) ? val1 : val2;
            } catch (Exception e) {
                Log.e("error", "Can't com w/ websrvc", e);
            } finally {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
            }
            return newMslp;
        }

        @Override
        protected void onPostExecute(Float result)
        {
            if (result == null) {

                Toast.makeText(SplashActivity.this, "No sea level data available at this location.", Toast.LENGTH_LONG).show();
            }else{
                mslp = result;
            }
        }
    }
}
