package com.vilisvit.things;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private final Context context;
    private static final String DATABASE_NAME = "ThingsDatabase.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "things";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATETIME_ADDED = "_datetime_added";
    public static final String COLUMN_TITLE = "thing_title";
    public static final String COLUMN_DESCRIPTION = "thing_description";
    public static final String COLUMN_DATETIME = "thing_datetime";
    public static final String COLUMN_NOTIFICATION_TIME = "notification_time";
    public static final String COLUMN_STATUS = "thing_status";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATETIME_ADDED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_TITLE + " TINYTEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATETIME + " DATETIME, " +
                COLUMN_NOTIFICATION_TIME + " TINYTEXT, " +
                COLUMN_STATUS + " BOOL);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addThing (String title, String description, String datetime, String notificationTime, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_DATETIME, datetime);
        cv.put(COLUMN_NOTIFICATION_TIME, notificationTime);
        cv.put(COLUMN_STATUS, status);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Unable to add element", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public void updateData (String row_id, String title, String description, String datetime, String notificationTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_DATETIME, datetime);
        cv.put(COLUMN_NOTIFICATION_TIME, notificationTime);
        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});
        if (result == -1) {
            Toast.makeText(context, "Unable to update", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateStatus(String row_id, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_STATUS, status);

        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});

        if (result == -1) {
            Toast.makeText(context, "Unable to update", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteOneRow(String row_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "_id=?", new String[]{row_id});
        if (result == -1) {
            Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show();
        }
    }
    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    public Cursor readAllData () {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}
