package com.example.nikita.forecastapp.model.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Nikita on 17.06.2018.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "forecastdb";
    public static final String DB_TABLE = "cities";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_LATITUDE = "lat";
    public static final String COLUMN_LONGITUDE = "lng";
    public static final String COLUMN_PLACE_ID = "placeId";
    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_CITY + " text unique, " +
                    COLUMN_LATITUDE + " real, " +
                    COLUMN_LONGITUDE + " real, " +
                    COLUMN_PLACE_ID + " text" +
                    ");";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
