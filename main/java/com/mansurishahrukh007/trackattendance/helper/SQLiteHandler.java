package com.mansurishahrukh007.trackattendance.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mansurishahrukh007.trackattendance.app.AppConfig;
import com.mansurishahrukh007.trackattendance.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";

    // Attendance table name
    private static final String TABLE_ATTENDANCE = "attendance";


    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";

    private static final String KEY_ADDED_AT = "added_at";
    private static final String KEY_MODIFIED_AT = "modified_at";

    private static final String KEY_DATE = "date";
    private static final String KEY_ATTENDED = "attended";
    private static final String KEY_TOTAL = "total";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //login table
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";

        //attendance table
        String CREATE_ATTENDANCE_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE + "("
                + KEY_DATE + " TEXT," + KEY_ATTENDED + " INTEGER," + KEY_TOTAL + " INTEGER,"
                + KEY_ADDED_AT + " TEXT," + KEY_MODIFIED_AT + " TEXT" + ")";

        db.execSQL(CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_ATTENDANCE_TABLE);
        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        onCreate(db);
    }


    /**
     * Storing user details in database
     */
    public void addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); //Name
        values.put(KEY_EMAIL, email); //Email
        values.put(KEY_UID, uid);
        values.put(KEY_CREATED_AT, created_at); //Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);

    }

    public void deleteAttendance(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] s = {date};
        db.delete(TABLE_ATTENDANCE, "date = ?", s);
        db.close();
        Log.d(TAG, "Deleted Attendance");
    }

    public void allDeleteAttendance() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ATTENDANCE, null, null);
        db.close();
        Log.d(TAG, "Deleted Attendance");
    }

    public String getUniqueId() {

        String query = "SELECT uid FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        HashMap<String, String> user = new HashMap<>();
        if (cursor.getCount() > 0) {
            user.put("uid", cursor.getString(0));
        }
        cursor.close();
        db.close();
        return user.get("uid");
    }

    //add data into attendance
    public void addAttendance(String date, int attended, int total) {
        DateAndTimeProvider dt = new DateAndTimeProvider();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_ATTENDED, attended);
        values.put(KEY_TOTAL, total);
        values.put(KEY_ADDED_AT, dt.getDateAndTime());
        values.put(KEY_MODIFIED_AT, "Not Updated.");

        // Inserting Row
        long id = db.insert(TABLE_ATTENDANCE, null, values);
        Log.d(TAG, "New entry of attendance " + id);
    }

    public void modifyAttendance(String date, int attended, int total) {
        DateAndTimeProvider dt = new DateAndTimeProvider();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_ATTENDED, attended);
        values.put(KEY_TOTAL, total);
        values.put(KEY_MODIFIED_AT, dt.getDateAndTime());
        String[] s = {date};
        long id = db.update(TABLE_ATTENDANCE, values, "date = ?", s);
        Log.d(TAG, "New entry of attendance " + id);
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }


    public void getAllAttendance(final String uid) {

        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_GET_ATTENDANCE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String a = "";
                    JSONObject root = new JSONObject(response);
                    JSONObject attendance = root.getJSONObject("attendance");
                    Iterator<String> keys = attendance.keys();
                    ContentValues values = new ContentValues();
                    SQLiteDatabase db = getWritableDatabase();
                    while (keys.hasNext()) {
                        String date = keys.next();
                        JSONObject JSONdate = attendance.getJSONObject(date);
                        values.put(KEY_DATE, date);
                        values.put(KEY_ATTENDED, Integer.parseInt(JSONdate.getString("attended")));
                        values.put(KEY_TOTAL, Integer.parseInt(JSONdate.getString("total")));
                        values.put(KEY_ADDED_AT, JSONdate.getString("added_at"));
                        values.put(KEY_MODIFIED_AT, JSONdate.getString("modified_at"));

                        // Inserting Row
                        db.insert(TABLE_ATTENDANCE, null, values);
                        a += date + " " + JSONdate.getString("attended") + " " + JSONdate.getString("total") + "\n";
                    }
                    Log.d(TAG, a);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unique_id", uid);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "hello");
    }

    //getting all attendance
    public String getAttendance() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_ATTENDANCE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String result = "";
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                result += "Date : " + cursor.getString(0) + " Attendance : " + cursor.getString(1) + "/" + cursor.getString(2) + "\n\n";
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.d(TAG, result);
        return result;
    }

    public ArrayList<String[]> getTotalAttendance() {
        ArrayList<String[]> attendance = new ArrayList<String[]>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_ATTENDANCE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String[] date = new String[cursor.getCount()];
        String[] attended = new String[cursor.getCount()];
        String[] total = new String[cursor.getCount()];
        String[] added_at = new String[cursor.getCount()];
        String[] modified_at = new String[cursor.getCount()];
        String s = "";

        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                date[i] = cursor.getString(0);
                attended[i] = cursor.getString(1);
                total[i] = cursor.getString(2);
                added_at[i] = cursor.getString(3);
                modified_at[i] = cursor.getString(4);
                cursor.moveToNext();
            }
        }
        //adding to the array List
        attendance.add(date);
        attendance.add(attended);
        attendance.add(total);
        attendance.add(added_at);
        attendance.add(modified_at);

        cursor.close();
        db.close();
        return attendance;
    }

    public void deleteUsersDB() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER, null, null);
        db.delete(TABLE_ATTENDANCE, null, null);
        db.close();
        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public int getAttendanceAttended() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_ATTENDANCE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int attended = 0;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                attended += cursor.getInt(1);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.d(TAG, "get total of attendance");
        return attended;
    }

    public int getAttendanceTotal() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_ATTENDANCE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int total = 0;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                total += cursor.getInt(2);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.d(TAG, total + "");
        return total;
    }
}
