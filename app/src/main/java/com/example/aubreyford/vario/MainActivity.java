package com.example.aubreyford.vario;

import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import io.sule.gaugelibrary.GaugeView;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private static final String TAG = "AltitudeActivity";
    private static final int TIMEOUT = 200; // waaaas 1 second
    private static final long NS_TO_MS_CONVERSION = (long) 1E6;

    // System services
    private SensorManager sensorManager;
    private LocationManager locationManager;

    // UI Views
    private TextView barometerAltitudeView;
    private TextView barometerRelativeAltitude;


    // Member state
    private long lastBarometerAltitudeTimestamp = -1;
    private float bestLocationAccuracy = -1;
    private float currentBarometerValue;
    private float lastBarometerValue;
    private boolean webServiceFetching;
    private Float mslp;

    private VarioData mVarioData = new VarioData();
    private GaugeView mGaugeView;
    private float lastMpS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGaugeView = (GaugeView) findViewById(R.id.gauge_view);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        barometerAltitudeView = (TextView) findViewById(R.id.barometerAltitude);
        barometerRelativeAltitude = (TextView) findViewById(R.id.barometerRelativeAltitude);


        webServiceFetching = false;

    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> enabledProviders = locationManager.getProviders(true);

        if (!enabledProviders.contains(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS Not Enabled", Toast.LENGTH_LONG).show();
        } else if (!enabledProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Please change location mode to High Accuracy", Toast.LENGTH_LONG).show();
        } else {
            for (String provider : enabledProviders) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(this, "Please grant location permission!", Toast.LENGTH_LONG).show();
                    return;
                }
                locationManager.requestLocationUpdates(provider,
                        6000,  // minimum time of 60000 ms (1 minute)
                        0,      // Minimum distance of 0
                        this);
            }
        }

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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Please grant location permission!", Toast.LENGTH_LONG).show();
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
            if (mslp != null)
            {
                altitude = SensorManager.getAltitude(mslp, currentBarometerValue);
                barometerAltitudeView.setText(String.valueOf(altitude));
            }else{
                altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentBarometerValue);
                barometerAltitudeView.setText(String.valueOf(altitude));
            }

            lastBarometerAltitudeTimestamp = (long)currentTimestamp;
            AltitudeEntry newEntry = new AltitudeEntry(altitude, currentTimestamp);


            mVarioData.addEntry(newEntry);
            float MpS = mVarioData.getCurrentMpS();
            mGaugeView.setTargetValue(MpS);

            if( MpS > 0 && lastMpS > 0){
                //play sound
            }

            lastMpS = MpS;
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



        float accuracy = location.getAccuracy();
        boolean betterAccuracy = accuracy < bestLocationAccuracy;
        if (mslp == null  || (bestLocationAccuracy > -1 && betterAccuracy))
        {
            bestLocationAccuracy = accuracy;



        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Log.i("***disabled***", provider);
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        Log.i("***enabled***", provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // no-op
    }

}
