package com.example.aubreyford.vario;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.sule.gaugelibrary.GaugeView;


public class MainActivity extends Activity implements SensorEventListener {
    private static final int TIMEOUT = 350; // waaaas 1 second
    private static final long NS_TO_MS_CONVERSION = (long) 1E6;
    // System services
    private SensorManager sensorManager;
    // UI Views
    private TextView barometerAltitudeView;
    private TextView relativeAltitude;
    private TextView flightTime;
    private Button endFlight;
    // Member state
    private long lastBarometerAltitudeTimestamp = -1;
    private float currentBarometerValue;
    // Me
    private Float mslp;
    private VarioData mVarioData = new VarioData();
    private GaugeView mGaugeView;
    private float lastMpS = 0;
    private float landingZoneAltitude;
    private boolean beep = false;
    private int playBeep = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGaugeView = (GaugeView) findViewById(R.id.gauge_view);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        barometerAltitudeView = (TextView) findViewById(R.id.main_barometerAltitude);
        relativeAltitude = (TextView) findViewById(R.id.main_relativeAltitude);
        flightTime = (TextView) findViewById(R.id.main_flightTime);
        endFlight = (Button) findViewById(R.id.main_endFlight);

        Bundle bundle = getIntent().getExtras();

        mslp = bundle.getFloat("mslp");
        landingZoneAltitude = bundle.getFloat("landingZoneAltitude");

        Toast.makeText(MainActivity.this, String.valueOf(landingZoneAltitude), Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this, String.valueOf(mslp), Toast.LENGTH_SHORT).show();

    }

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


        while(beep){
            Toast.makeText(MainActivity.this, "Thermal", Toast.LENGTH_SHORT).show();
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
            relativeAltitude.setText(String.valueOf(altitude - landingZoneAltitude));
            playBeepUpdate(1, 1);

            lastMpS = MpS;

        }
    }

    private void playBeepUpdate(float mps, float lastmps){


        if(mps > 4 && lastmps > 4){
            MediaPlayer mpFour= MediaPlayer.create(MainActivity.this, R.raw.four);
//            mpFour.setLooping(true);
            mpFour.start();

            playBeep = 3;
        }else if(mps > 2 && lastmps > 2){

            MediaPlayer mpThree= MediaPlayer.create(MainActivity.this, R.raw.three);
//            mpThree.setLooping(true);
            mpThree.start();

            playBeep = 2;
        }else if(mps > .5 && lastmps > .5){

            MediaPlayer mpTwo= MediaPlayer.create(MainActivity.this, R.raw.two);
//            mpTwo.setLooping(true);
            mpTwo.start();

            playBeep = 1;
        }else{

//            mpFour.stop();
//            mpThree.stop();
//            mpTwo.stop();

            playBeep = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }

}
