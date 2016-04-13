package com.example.aubreyford.vario;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle bundle = getIntent().getExtras();
        ArrayList<AltitudeEntry> altitudeEntries = (ArrayList<AltitudeEntry>)bundle.getSerializable("altitudeEntries");

        LatLng start = new LatLng(altitudeEntries.get(0).getLattitude(), altitudeEntries.get(0).getLongitude());
        LatLng ending = new LatLng(altitudeEntries.get(altitudeEntries.size()-1).getLattitude(), altitudeEntries.get(altitudeEntries.size()-1).getLongitude());
        mMap.addMarker(new MarkerOptions().position(start).title("Start"));
        mMap.addMarker(new MarkerOptions().position(ending).title("End"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));

        for(int i = 0 ; i < altitudeEntries.size()-1 ; i += 5 ){

            if(i + 5 >= altitudeEntries.size()-1){

                LatLng priorLatlng =  new LatLng(altitudeEntries.get(i).getLattitude(), altitudeEntries.get(i).getLongitude());
                mMap.addPolyline(new PolylineOptions().geodesic(true).add(priorLatlng).add(ending));

            }else {

                LatLng latlng = new LatLng(altitudeEntries.get(i).getLattitude(), altitudeEntries.get(i).getLongitude());
                LatLng latlngNext = new LatLng(altitudeEntries.get(i + 5).getLattitude(), altitudeEntries.get(i + 5).getLongitude());
                mMap.addPolyline(new PolylineOptions().geodesic(true).add(latlng).add(latlngNext));

            }
        }
    }
}
