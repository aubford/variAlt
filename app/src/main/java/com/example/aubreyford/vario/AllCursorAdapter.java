package com.example.aubreyford.vario;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AllCursorAdapter extends CursorAdapter {

    final Context context;

    public AllCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.show_all_row, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameView = (TextView) view.findViewById(R.id.all_name);
        TextView dateView = (TextView) view.findViewById(R.id.all_date);
        Button delete = (Button) view.findViewById(R.id.all_delete);
        Button select = (Button) view.findViewById(R.id.all_select);

        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String altitudeEntriesString =  cursor.getString(cursor.getColumnIndexOrThrow("altitude_entries"));
        double ascending_time =  cursor.getDouble(cursor.getColumnIndexOrThrow("ascending_time"));
        long flight_time = cursor.getLong(cursor.getColumnIndexOrThrow("flight_time"));


        Type listType = new TypeToken<ArrayList<AltitudeEntry>>() {}.getType();
        ArrayList<AltitudeEntry> altitudeEntries = new Gson().fromJson(altitudeEntriesString, listType);

        nameView.setText(name);
        dateView.setText(date);
        delete.setTag(id);
        select.setTag(R.string.one, altitudeEntries);
        select.setTag(R.string.two, ascending_time);
        select.setTag(R.string.three, flight_time);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context clickContext = v.getContext();

                Intent i = new Intent(clickContext, FlightResultActivity.class);
                i.putExtra("altitudeEntries", (ArrayList<AltitudeEntry>) v.getTag(R.string.one));
                i.putExtra("ascendingTime", (double) v.getTag(R.string.two));
                i.putExtra("flightTime", (long) v.getTag(R.string.three));
                clickContext.startActivity(i);


            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbHandler database = new dbHandler(v.getContext(), null, null, 1);
                int deleteID = (int)v.getTag();

                database.deleteFlight(deleteID);

                Cursor cursor = database.getDatabase();
                AllCursorAdapter.this.swapCursor(cursor);




            }
        });


    }
}