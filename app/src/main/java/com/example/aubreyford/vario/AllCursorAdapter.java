package com.example.aubreyford.vario;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AllCursorAdapter extends CursorAdapter {



    public AllCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.show_all_row, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameView = (TextView) view.findViewById(R.id.all_name);
        TextView dateView = (TextView) view.findViewById(R.id.all_date);
        TextView delete = (TextView) view.findViewById(R.id.all_delete);

       String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
       String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
       int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

        nameView.setText(name);
        dateView.setText(date);
        delete.setTag(id);


    }
}