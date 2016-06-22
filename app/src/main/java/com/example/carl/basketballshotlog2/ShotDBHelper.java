package com.example.carl.basketballshotlog2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Carl on 6/25/2015.
 */
public class ShotDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private  static final String DATABASE_NAME ="BShotsDatabase2.db";
    //private static final String DICTIONARY_TABLE_NAME = "dictionary";
    private static final String SHOTS_TABLE_CREATE =
            "CREATE TABLE Shots("
                    + "sid integer primary key AUTOINCREMENT, " +
                    "spid integer, "
                    + "date TEXT, "
                    + "made integer," +
                    "attempts integer"
                    + ")";

    private static final String SPOT_TABLE_CREATE =
            "CREATE TABLE Spots("
            + "spid integer primary key AUTOINCREMENT, " //spotid
            + "spot TEXT "
            + ")";

    private final Context myContext;
    public SQLiteDatabase dbSqlite;

    public ShotDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        //if(checkDataBase()){
        db.execSQL(SHOTS_TABLE_CREATE);
        db.execSQL(SPOT_TABLE_CREATE);
        //
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // If you need to add a new column
        if (newVersion > oldVersion) {
        }
    }
}