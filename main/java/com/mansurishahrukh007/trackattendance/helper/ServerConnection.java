package com.mansurishahrukh007.trackattendance.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mansurishahrukh007.trackattendance.app.AppConfig;
import com.mansurishahrukh007.trackattendance.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shahrukh Mansuri on 3/23/2016.
 */
public class ServerConnection {

    private static final String TAG = ServerConnection.class.getSimpleName();
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private Context context;
    private DateAndTimeProvider dateAndTimeProvider;


    public ServerConnection(Context context) {
        this.context = context;
        db = new SQLiteHandler(context);
        dateAndTimeProvider = new DateAndTimeProvider();
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
    }

    public void addAttendance(final String uid, final String date, final String attended, final String total) {
        String tag_string_req = "req_add_attendance";
        pDialog.setMessage("Adding to Server....");
        showDialog();
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_ADD_ATTENDANCE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Attendance Response: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errMessage = jsonObject.getString("error");
                            String attendance = jsonObject.getString("attendance");
                            if (errMessage == "false") {
                                db.addAttendance(date, Integer.parseInt(attended), Integer.parseInt(total));
                                Toast.makeText(context, attendance, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, errMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Attendance Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unique_id", uid);
                params.put("Adate", date);
                params.put("attended", attended);
                params.put("total", total);
                params.put("added_at", dateAndTimeProvider.getDateAndTime());
                params.put("modified_at", "Not Updated.");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void deleteAttendance(final String uid, final String date) {
        String tag_string_req = "req_delete_attendance";
        pDialog.setMessage("Deleting from the server...");
        showDialog();
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_DELETE_ATTENDANCE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Attendance Response: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errMessage = jsonObject.getString("error");
                            String attendance = jsonObject.getString("attendance");
                            if (errMessage == "false") {
                                db.deleteAttendance(date);
                                Toast.makeText(context, attendance, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, errMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Attendance Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unique_id", uid);
                params.put("Adate", date);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void modifyAttendance(final String uid, final String date, final String attended, final String total) {
        String tag_string_req = "req_modify_attendance";
        pDialog.setMessage("Updating to Server....");
        showDialog();
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_MODIFY_ATTENDANCE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Attendance Response: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errMessage = jsonObject.getString("error");
                            String attendance = jsonObject.getString("attendance");
                            if (errMessage == "false") {
                                db.modifyAttendance(date, Integer.parseInt(attended), Integer.parseInt(total));
                                Toast.makeText(context, attendance, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, errMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Attendance Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unique_id", uid);
                params.put("Adate", date);
                params.put("attended", attended);
                params.put("total", total);
                params.put("modified_at", dateAndTimeProvider.getDateAndTime());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void allDeleteAttendance(final String uid) {
        String tag_string_req = "req_delete_attendance";
        pDialog.setMessage("Deleting from the server...");
        showDialog();
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_All_DELETE_ATTENDANCE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Attendance Response: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String errMessage = jsonObject.getString("error");
                            String attendance = jsonObject.getString("attendance");
                            if (errMessage == "false") {
                                db.allDeleteAttendance();
                                Toast.makeText(context, attendance, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, errMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Attendance Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unique_id", uid);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
