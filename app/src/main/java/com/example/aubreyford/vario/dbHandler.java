package com.example.aubreyford.vario;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class dbHandler extends SQLiteOpenHelper {

    //We need to pass database information along to superclass
    public dbHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "flightdb", factory, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + "flights" + "(" +
                "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " + "name" + " TEXT," +
                "date" + " TEXT, " + "flight_time" + " INTEGER," + "ascending_time" + " REAL," + "altitude_entries" + " TEXT" +
                ");";

        Log.i("***********", "CREATE DATABASE");
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "flights");
        onCreate(db);
        Log.i("***********", "UPGRADE DATABASE");
    }

    public void addFlight(String name, String date, long flight_time, double ascending_time, ArrayList<AltitudeEntry> altitude_entries){
        ContentValues values = new ContentValues();

        Gson gson = new Gson();
        String altitude_entries_string = gson.toJson(altitude_entries);

        values.put("name", name);
        values.put("date", date);
        values.put("flight_time", flight_time);
        values.put("ascending_time", ascending_time);
        values.put("altitude_entries", altitude_entries_string);

        SQLiteDatabase db = getWritableDatabase();
        db.insert("flights", null, values);
        db.close();
    }

    //Delete a product from the database
    public void deleteFlight(int id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + "flights" + " WHERE " + "_id=" + id);
    }

    public Cursor getDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + "flights" + " WHERE 1";

        Cursor cursory = db.rawQuery(query, null);

        return cursory;
    }

}
