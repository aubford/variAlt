package com.example.aubreyford.vario;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import io.sule.gaugelibrary.GaugeView;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener, LocationListener {
    private static final String TAG = "AltitudeActivity";
    private static final int TIMEOUT = 500; // waaaas 1 second
    private static final long NS_TO_MS_CONVERSION = (long) 1E6;

    // System services
    private SensorManager sensorManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // UI Views
    private TextView gpsAltitudeView;
    private TextView gpsRelativeAltitude;
    private TextView barometerAltitudeView;
    private TextView barometerRelativeAltitude;
    private TextView mslpBarometerAltitudeView;
    private TextView mslpBarometerRelativeAltitude;
    private TextView mslpView;

    // Member state
    private Float mslp;
    private long lastGpsAltitudeTimestamp = -1;
    private long lastBarometerAltitudeTimestamp = -1;
    private float bestLocationAccuracy = -1;
    private float currentBarometerValue;
    private float lastBarometerValue;
    private double lastGpsAltitude;
    private double currentGpsAltitude;
    private boolean webServiceFetching;
    private long lastErrorMessageTimestamp = -1;

    private GaugeView mGaugeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGaugeView = (GaugeView) findViewById(R.id.gauge_view);
        mGaugeView.setTargetValue(100);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        gpsAltitudeView = (TextView) findViewById(R.id.gpsAltitude);
        gpsRelativeAltitude = (TextView) findViewById(R.id.gpsRelativeAltitude);
        barometerAltitudeView = (TextView) findViewById(R.id.barometerAltitude);
        barometerRelativeAltitude = (TextView) findViewById(R.id.barometerRelativeAltitude);
        mslpBarometerAltitudeView = (TextView) findViewById(R.id.mslpBarometerAltitude);
        mslpBarometerRelativeAltitude = (TextView) findViewById(R.id.mslpBarometerRelativeAltitude);
        mslpView = (TextView) findViewById(R.id.mslp);

        webServiceFetching = false;
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {return;}

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {

        if (lastGpsAltitudeTimestamp == -1 || location.getTime() - lastGpsAltitudeTimestamp > TIMEOUT)
        {
            double altitude = location.getAltitude();
            gpsAltitudeView.setText(String.valueOf(altitude));
            lastGpsAltitudeTimestamp = location.getTime();
            currentGpsAltitude = altitude;
        }

        float accuracy = location.getAccuracy();
        boolean betterAccuracy = accuracy < bestLocationAccuracy;
        if (mslp == null  || (bestLocationAccuracy > -1 && betterAccuracy))
        {
            bestLocationAccuracy = accuracy;

            if (!webServiceFetching)
            {
                webServiceFetching = true;
                new MetarAsyncTask().execute(location.getLatitude(),
                        location.getLongitude());
            }
        }
    }


    ////////////////////////////////////////////BAROMETER///////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        // Only make registration call if device has a pressure sensor
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }else{
            Toast.makeText(this, "You do not have a pressure sensor!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float altitude;
        currentBarometerValue = event.values[0];

        double currentTimestamp = event.timestamp / NS_TO_MS_CONVERSION;
        double elapsedTime = currentTimestamp - lastBarometerAltitudeTimestamp;
        if (lastBarometerAltitudeTimestamp == -1 || elapsedTime > TIMEOUT)
        {

            if (mslp != null)
            {
                altitude = SensorManager.getAltitude(mslp, currentBarometerValue);
                mslpBarometerAltitudeView.setText(String.valueOf(altitude));
                mslpView.setText(String.valueOf(mslp));
                barometerAltitudeView.setText(String.valueOf(altitude));
            }else{
                altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentBarometerValue);
                barometerAltitudeView.setText(String.valueOf(altitude));
            }

            lastBarometerAltitudeTimestamp = (long)currentTimestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }

    private class MetarAsyncTask extends AsyncTask<Number, Void, Float>
    {
        private static final String WS_URL =
                "http://ws.geonames.org/findNearByWeatherJSON";
        private static final String SLP_STRING = "slp";

        @Override
        protected Float doInBackground(Number... params)
        {
            Float mslp = null;
            HttpURLConnection urlConnection = null;

            try
            {
                // Generate URL with parameters for web service
                Uri uri =
                        Uri.parse(WS_URL)
                        .buildUpon()
                        .appendQueryParameter("lat", String.valueOf(params[0]))
                        .appendQueryParameter("lng", String.valueOf(params[1]))
                        .appendQueryParameter("username", "aubford")
                        .build();

                // Connect to web service
                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                // Read web service response and convert to a string
                InputStream inputStream =
                        new BufferedInputStream(urlConnection.getInputStream());

                // Convert InputStream to String using a Scanner
                Scanner inputStreamScanner =
                        new Scanner(inputStream).useDelimiter("\\A");
                String response = inputStreamScanner.next();
                inputStreamScanner.close();

                Log.d(TAG, "Web Service Response -> " + response);

                JSONObject json = new JSONObject(response);

                String observation =
                        json
                            .getJSONObject("weatherObservation")
                            .getString("observation");

                // Split on whitespace
                String[] values = observation.split("\\s");

                // Iterate of METAR string until SLP string is found
                String slpString = null;
                for (int i = 1; i < values.length; i++)
                {
                    String value = values[i];

                    if (value.startsWith(SLP_STRING.toLowerCase())
                            || value.startsWith(SLP_STRING.toUpperCase()))
                    {
                        slpString =
                                value.substring(SLP_STRING.length());
                        break;
                    }
                }

                // Decode SLP string into numerical representation
                StringBuffer sb = new StringBuffer(slpString);

                sb.insert(sb.length() - 1, ".");

                float val1 = Float.parseFloat("10" + sb);
                float val2 = Float.parseFloat("09" + sb);

                mslp =
                        (Math.abs((1000 - val1)) < Math.abs((1000 - val2)))
                            ? val1
                            : val2;
            }
            catch (Exception e)
            {
                Log.e(TAG, "Can't com w/ websrvc", e);
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
            }

            return mslp;
        }

        @Override
        protected void onPostExecute(Float result)
        {
            long uptime = SystemClock.uptimeMillis();

            if (result == null
                    && (lastErrorMessageTimestamp == -1
                        || ((uptime - lastErrorMessageTimestamp) > 30000)))
            {
                Toast.makeText(MainActivity.this,
                        "No sea level data available at this location.",
                        Toast.LENGTH_LONG).show();

                lastErrorMessageTimestamp = uptime;
            }
            else
            {
                MainActivity.this.mslp = result;
            }

            MainActivity.this.webServiceFetching = false;
        }
    }


    public void onToggleClick(View view)
    {
        if (((ToggleButton)view).isChecked())
        {
            lastGpsAltitude = currentGpsAltitude;
            lastBarometerValue = currentBarometerValue;
            gpsRelativeAltitude.setVisibility(View.INVISIBLE);
            barometerRelativeAltitude.setVisibility(View.INVISIBLE);

            if (mslp != null)
            {
                mslpBarometerRelativeAltitude.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            double delta;

            delta = currentGpsAltitude - lastGpsAltitude;
            gpsRelativeAltitude.setText(String.valueOf(delta));
            gpsRelativeAltitude.setVisibility(View.VISIBLE);

            delta = SensorManager
                    .getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                            currentBarometerValue)
                    - SensorManager
                    .getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                            lastBarometerValue);

            barometerRelativeAltitude.setText(String.valueOf(delta));
            barometerRelativeAltitude.setVisibility(View.VISIBLE);

            if (mslp != null)
            {
                delta = SensorManager.getAltitude(mslp, currentBarometerValue)
                        - SensorManager.getAltitude(mslp, lastBarometerValue);
                mslpBarometerRelativeAltitude.setText(String.valueOf(delta));
                mslpBarometerRelativeAltitude.setVisibility(View.VISIBLE);
            }
        }
    }
}
