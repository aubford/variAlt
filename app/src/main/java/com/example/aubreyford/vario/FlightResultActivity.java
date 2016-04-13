package com.example.aubreyford.vario;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FlightResultActivity extends Activity {

    TextView mFlightTime;
    TextView mStartingAlt;
    TextView mEndingAlt;
    TextView mAltDif;
    TextView mAscendingTime;
    TextView mDistance;
    Bundle bundle;
    Button mMap;
    Button mSave;
    dbHandler database;
    long flightTimeMillis;
    ArrayList<AltitudeEntry> altitudeEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_flight_result);

        bundle = getIntent().getExtras();
        altitudeEntries = (ArrayList<AltitudeEntry>)bundle.getSerializable("altitudeEntries");

        mFlightTime = (TextView) findViewById(R.id.result_flight_time);
        mStartingAlt = (TextView) findViewById(R.id.result_starting_altitude);
        mEndingAlt = (TextView) findViewById(R.id.result_ending_altitude);
        mAltDif = (TextView) findViewById(R.id.result_difference);
        mAscendingTime = (TextView) findViewById(R.id.result_ascending);
        mDistance = (TextView) findViewById(R.id.results_distance);
        mMap = (Button) findViewById(R.id.result_map);
        mSave = (Button) findViewById(R.id.result_save);

        flightTimeMillis = bundle.getLong("flightTime");
        String totalTime  = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(flightTimeMillis),
                TimeUnit.MILLISECONDS.toMinutes(flightTimeMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(flightTimeMillis)),
                TimeUnit.MILLISECONDS.toSeconds(flightTimeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(flightTimeMillis)));
        mFlightTime.setText(totalTime);


        final double millisDouble = bundle.getDouble("ascendingTime");
        long millis = (long)millisDouble;
        String ascendingTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        mAscendingTime.setText(String.valueOf(ascendingTime));

        float startingAlt = altitudeEntries.get(0).altitude;
        mStartingAlt.setText(String.valueOf(startingAlt) + " m");

        float endingAlt = altitudeEntries.get(altitudeEntries.size()-1).altitude;
        mEndingAlt.setText(String.valueOf(endingAlt) + " m");

        mAltDif.setText(String.valueOf(startingAlt-endingAlt) + " m");

        Location locationA = new Location("point A");
        locationA.setLatitude(altitudeEntries.get(0).getLattitude());
        locationA.setLongitude(altitudeEntries.get(0).getLongitude());
        Location locationB = new Location("point B");
        locationB.setLatitude(altitudeEntries.get(altitudeEntries.size()-1).getLattitude());
        locationB.setLongitude(altitudeEntries.get(altitudeEntries.size()-1).getLongitude());
        float distanceFloat = locationA.distanceTo(locationB);
        String distance = String.format("%.2f", distanceFloat) + " m";
        mDistance.setText(String.valueOf(distance));

        database = new dbHandler(FlightResultActivity.this, null, null, 2);

        mMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FlightResultActivity.this, MapsActivity.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });



        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(FlightResultActivity.this);
                builder.setTitle("Enter A Name");

                final EditText input = new EditText(FlightResultActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();

                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        String formattedDate = dateFormat.format(date);

                        database.addFlight(name, formattedDate, flightTimeMillis, millisDouble, altitudeEntries);

                        Toast.makeText(FlightResultActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
    }
}
