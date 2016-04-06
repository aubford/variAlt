package com.example.aubreyford.vario;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import io.sule.gaugelibrary.GaugeView;

public class MainActivity extends AppCompatActivity {

    private GaugeView mGaugeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGaugeView = (GaugeView) findViewById(R.id.gauge_view);
        mGaugeView.setTargetValue(-1000);
    }


}
