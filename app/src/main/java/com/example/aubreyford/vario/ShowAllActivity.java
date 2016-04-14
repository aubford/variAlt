package com.example.aubreyford.vario;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import java.util.ArrayList;

public class ShowAllActivity extends Activity {

    dbHandler database;
    long flight_time;
    double ascending_time;
    ArrayList<AltitudeEntry> altitude_entries;
    String altitude_entries_gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_all);

        database = new dbHandler(this, null, null, 1);
        Cursor cursor = database.getDatabase();

        ListView lvItems = (ListView) findViewById(R.id.all_listView);
        AllCursorAdapter adapter = new AllCursorAdapter(ShowAllActivity.this, cursor, 0);
        lvItems.setAdapter(adapter);

    }
    public void refresh(){
        finish();
        startActivity(getIntent());
    }
}


