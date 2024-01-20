package com.vilisvit.things;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private final Context context;
    private static final String DATABASE_NAME = "ThingsDatabase.db";
    private static final int DATABASE_VERSION = 7;

    public static final String TABLE_NAME = "things";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATETIME_ADDED = "_datetime_added";
    public static final String COLUMN_TITLE = "thing_title";
    public static final String COLUMN_DESCRIPTION = "thing_description";
    public static final String COLUMN_DATETIME = "thing_datetime";
    public static final String COLUMN_NOTIFICATION_TIME = "notification_time";
    public static final String COLUMN_PRIORITY = "thing_priority";
    public static final String COLUMN_STATUS = "thing_status";
    public static final String COLUMN_PARENT_ID = "parent_id";

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
                COLUMN_PRIORITY + " INTEGER, " +                 // 0-Low, 1-Medium, 2-High
                COLUMN_STATUS + " BOOL, " +
                COLUMN_PARENT_ID + " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addThing (String title, String description, String datetime, String notificationTime, int priority, boolean status, String parent_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_DATETIME, datetime);
        cv.put(COLUMN_NOTIFICATION_TIME, notificationTime);
        cv.put(COLUMN_PRIORITY, priority);
        cv.put(COLUMN_STATUS, status);
        cv.put(COLUMN_PARENT_ID, parent_id);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Unable to add element", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public void updateData (String row_id, String title, String description, String datetime, String notificationTime, int priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_DATETIME, datetime);
        cv.put(COLUMN_PRIORITY, priority);
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

    public void deleteMultipleRows(ArrayList<String> ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Constructing the IN clause for string IDs
        StringBuilder idList = new StringBuilder();
        for (String id : ids) {
            idList.append("'").append(id).append("',");
        }
        idList.deleteCharAt(idList.length() - 1); // Remove the trailing comma

        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " IN (" + idList.toString() + ")";

        db.execSQL(query);
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

    public int countChildren (String parent_id) {
        return getChildrenIds(parent_id).size();
    }

    public ArrayList<String> getChildrenIds(String parent_id) {
        ArrayList<String> children_ids = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String[] projection = {COLUMN_ID};

        String selection = "parent_id=?";
        String[] selectionArgs = {parent_id};

        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String child_id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                children_ids.add(child_id);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return children_ids;
    }

    public Cursor readDataForIds(ArrayList<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return null; // Return null or handle the case where the ArrayList is empty
        }

        // Constructing the IN clause for string IDs
        StringBuilder idList = new StringBuilder();
        for (String id : ids) {
            idList.append("'").append(id).append("',");
        }
        idList.deleteCharAt(idList.length() - 1); // Remove the trailing comma

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " IN (" + idList.toString() + ")";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    @SuppressLint("Range")
    public String getParentId (String row_id) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(row_id);
        Cursor cursor = readDataForIds(ids);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PARENT_ID));
    }

}
