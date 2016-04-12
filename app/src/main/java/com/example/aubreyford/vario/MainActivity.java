package com.example.aubreyford.vario;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
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

    SoundPool soundPool;
    int soundOne;
    int soundTwo;
    int soundThree;
    int soundFour;
    boolean playedOne = false;
    boolean playedTwo = false;
    int streamOne;
    int streamTwo;
    int streamThree;


    private int[] incrementTest = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,5,5,2,2,5,2,2,5,2,2,5,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,999999999};
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
                Log.i("*****ONLOADCOMPLETE**", String.valueOf(sampleId));
            }

        });

        soundOne = soundPool.load(MainActivity.this, R.raw.one, 1);
        soundTwo = soundPool.load(MainActivity.this, R.raw.two, 1);
        soundThree = soundPool.load(MainActivity.this, R.raw.three, 1);




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

            double rando = (Math.random() * 7) + .5;
            float randy = (float) rando;

            playBeepUpdate(incrementTest[incrementer], incrementTest[incrementer]);

            incrementer++;

            lastMpS = MpS;

        }
    }

    private void playBeepUpdate(float mps, float lastmps){

            if( mps > 4 ){

                if(playedTwo) {

                    soundPool.resume(streamTwo);

                    playedTwo=true;

                }else{

                    streamTwo =  soundPool.play(soundOne, 1, 1, 0, -1, 2);

                }

           }else if( mps > .5 ){

                if(playedOne) {

                    soundPool.resume(streamOne);

                    playedOne=true;

                }else{

                    streamOne =  soundPool.play(soundOne, 1, 1, 0, -1, 1);

                }

            }else{

                soundPool.autoPause();

            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }

}
