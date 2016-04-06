package com.example.aubreyford.vario;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import io.sule.gaugelibrary.GaugeView;

/**
 * Displays the current altitude
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class DetermineAltitudeActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private static final String TAG = "AltitudeActivity";
    private static final int TIMEOUT = 1000; //1 second
    private static final long NS_TO_MS_CONVERSION = (long) 1E6;

    // System services
    private SensorManager sensorManager;
    private LocationManager locationManager;

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
        setContentView(R.layout.determine_altitude);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        gpsAltitudeView = (TextView) findViewById(R.id.gpsAltitude);

        gpsRelativeAltitude =
                (TextView) findViewById(R.id.gpsRelativeAltitude);

        barometerAltitudeView = (TextView) findViewById(R.id.barometerAltitude);
        barometerRelativeAltitude =
                (TextView) findViewById(R.id.barometerRelativeAltitude);
        mslpBarometerAltitudeView =
                (TextView) findViewById(R.id.mslpBarometerAltitude);
        mslpBarometerRelativeAltitude =
                (TextView) findViewById(R.id.mslpBarometerRelativeAltitude);
        mslpView = (TextView) findViewById(R.id.mslp);

        webServiceFetching = false;

        TextView standardPressure =
                (TextView) findViewById(R.id.standardPressure);
        String standardPressureString =
                String.valueOf(SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
        standardPressure.setText(standardPressureString);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> enabledProviders = locationManager.getProviders(true);

        if (enabledProviders.isEmpty()
                || !enabledProviders.contains(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this,
                    R.string.gpsNotEnabledMessage,
                    Toast.LENGTH_LONG).show();
        } else {
            // Register every location provider returned from LocationManager
            for (String provider : enabledProviders) {


                // Register for updates every minute
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(provider,
                        60000,  // minimum time of 60000 ms (1 minute)
                        0,      // Minimum distance of 0
                        this,
                        null);
            }
        }

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        // Only make registration call if device has a pressure sensor
        if (sensor != null) {
            sensorManager.registerListener(this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
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
            altitude =
                    SensorManager
                        .getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                            currentBarometerValue);
            barometerAltitudeView.setText(String.valueOf(altitude));

            if (mslp != null)
            {
                altitude = SensorManager.getAltitude(mslp,
                        currentBarometerValue);
                mslpBarometerAltitudeView.setText(String.valueOf(altitude));
                mslpView.setText(String.valueOf(mslp));
            }

            lastBarometerAltitudeTimestamp = (long)currentTimestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())
                && (lastGpsAltitudeTimestamp == -1
                    || location.getTime() - lastGpsAltitudeTimestamp > TIMEOUT))
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

    @Override
    public void onProviderDisabled(String provider)
    {
        // no-op
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        // no-op
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // no-op
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
                Toast.makeText(DetermineAltitudeActivity.this,
                        R.string.webServiceConnectionFailureMessage,
                        Toast.LENGTH_LONG).show();

                lastErrorMessageTimestamp = uptime;
            }
            else
            {
                DetermineAltitudeActivity.this.mslp = result;
            }

            DetermineAltitudeActivity.this.webServiceFetching = false;
        }
    }
}
