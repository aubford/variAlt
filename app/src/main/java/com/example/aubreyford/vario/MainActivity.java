package com.example.aubreyford.vario;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
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

    private SoundPool soundPool;
    private int soundOne;
    private int streamOne;
    private boolean beep;


    Chronometer chronometer;



    private int[] incrementTest = {0,0,0,0,0,0,0,0,0,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,5,5,2,2,5,2,2,5,2,2,5,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,999999999};
    private int incrementer = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGaugeView = (GaugeView) findViewById(R.id.gauge_view);
        barometerAltitudeView = (TextView) findViewById(R.id.main_barometerAltitude);
        relativeAltitude = (TextView) findViewById(R.id.main_relativeAltitude);
        chronometer = (Chronometer) findViewById(R.id.main_chronometer);
        endFlight = (Button) findViewById(R.id.main_endFlight);

        Bundle bundle = getIntent().getExtras();

        mslp = bundle.getFloat("mslp");
        landingZoneAltitude = bundle.getFloat("landingZoneAltitude");


        endFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long flightTime = SystemClock.elapsedRealtime() - chronometer.getBase();


                Intent i = new Intent(MainActivity.this, FlightResultActivity.class);
                i.putExtra("mslp", mslp);
                i.putExtra("landingZoneAltitude", landingZoneAltitude);
                i.putExtra("flightTime", flightTime);
                startActivity(i);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();


        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();



        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                )
                .build();

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                streamOne =  soundPool.play(soundOne, 0, 0, 0, -1, 1);
                soundPool.pause(streamOne);
                soundPool.setVolume(streamOne, 1, 1);

            }

        });

        soundOne = soundPool.load(MainActivity.this, R.raw.beep, 1);


        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        // Only make registration call if device has a pressure sensor
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }else{
            Toast.makeText(this, "You do not have a pressure sensor! This app requires a pressure sensor.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);

        soundPool.release();
        finish();
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
                String altString = String.format("%.2f", altitude) + " m";
                barometerAltitudeView.setText(String.valueOf(altString));
            }else{
                altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentBarometerValue);
                String altString = String.format("%.2f", altitude) + " m";
                barometerAltitudeView.setText(String.valueOf(altString));
            }

            lastBarometerAltitudeTimestamp = (long)currentTimestamp;
            AltitudeEntry newEntry = new AltitudeEntry(altitude, currentTimestamp);


            mVarioData.addEntry(newEntry);
            float MpS = mVarioData.getCurrentMpS();

            mGaugeView.setTargetValue(MpS);

            float altDif = altitude - landingZoneAltitude;
            String altitudeFormatted = String.format("%.2f", altDif) + " m";
            relativeAltitude.setText(altitudeFormatted);

            playBeepUpdate(MpS);

            incrementer++;

            lastMpS = MpS;

        }
    }

    private void playBeepUpdate(float mps){

            if( mps > 4 ) {

                soundPool.setRate(streamOne, 2.0f);
                soundPool.resume(streamOne);
                beep = true;
//                Toast.makeText(MainActivity.this, "Thermal Heavy", Toast.LENGTH_SHORT).show();

            }else if(mps > 2){

                soundPool.setRate(streamOne, 1.8f);
                soundPool.resume(streamOne);
                beep=true;
//                Toast.makeText(MainActivity.this, "Thermal Medium", Toast.LENGTH_SHORT).show();
           }else if( mps > 1 ){

                soundPool.setRate(streamOne, 1.5f);
                soundPool.resume(streamOne);
                beep=true;
//                Toast.makeText(MainActivity.this, "Thermal Light", Toast.LENGTH_SHORT).show();

            }else{

                soundPool.autoPause();
                beep=false;

            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }

}
