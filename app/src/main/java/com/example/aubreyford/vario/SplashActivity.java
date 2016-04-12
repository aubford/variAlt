package com.example.aubreyford.vario;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SplashActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    private ImageButton startFlight;
    private Button enterLanding;
    private Button autoLanding;
    private Button myFlights;
    private TextView mLanding;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SensorManager sensorManager;


    private float landingZoneAltitude;
    private Float mslp;
    private String landingString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        landingString = getResources().getString(R.string.landing_zone_altitude);

        startFlight = (ImageButton) findViewById(R.id.spl_startFlight);
        enterLanding = (Button) findViewById(R.id.spl_enterLanding);
        autoLanding = (Button) findViewById(R.id.spl_autoLanding);
        myFlights = (Button) findViewById(R.id.spl_myFlights);
        mLanding = (TextView) findViewById(R.id.spl_landing);

        mslp = (float) 0;
        landingZoneAltitude = 0;


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setListeners();
    }


    private void setListeners() {



        startFlight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                i.putExtra("mslp", mslp);
                i.putExtra("landingZoneAltitude", landingZoneAltitude);
                startActivity(i);
            }
        });



        enterLanding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle("Enter Altitude in Meters");

                final EditText input = new EditText(SplashActivity.this);

                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_QWERTY);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        landingZoneAltitude = Float.valueOf(m_Text);
                        String newLandingText = landingString + " " + String.format("%.2f", landingZoneAltitude) + "m";
                        mLanding.setText(newLandingText);

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

////////////////////
            }
        });


        autoLanding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                // Only make registration call if device has a pressure sensor
                if (sensor != null) {
                    sensorManager.registerListener(SplashActivity.this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                    Toast.makeText(SplashActivity.this, "You do not have a pressure sensor!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mslp != null)
        {
            landingZoneAltitude = SensorManager.getAltitude(mslp, event.values[0]);
            String newLandingText = landingString + " " + String.format("%.2f", landingZoneAltitude) + "m";
            mLanding.setText(newLandingText);
        }else{
            landingZoneAltitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0]);
            String newLandingText = landingString + " " + String.format("%.2f", landingZoneAltitude) + "m";
            mLanding.setText(newLandingText);
        }
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor mBarometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        new MetarAsyncTask().execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                Toast.makeText(SplashActivity.this, "No weather station data available at this location.  Absolute altitude data will be less accurate.", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(SplashActivity.this, "Calibration Successful.", Toast.LENGTH_LONG).show();
                mslp = result;
            }
        }
    }
}
