/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package com.mansurishahrukh007.trackattendance.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mansurishahrukh007.trackattendance.app.AppConfig;
import com.mansurishahrukh007.trackattendance.app.AppController;
import com.mansurishahrukh007.trackattendance.helper.ConnectionDetector;
import com.mansurishahrukh007.trackattendance.helper.SQLiteHandler;
import com.mansurishahrukh007.trackattendance.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.androidhive.loginandregistration.R;

public class LoginActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private Button btnForgotPassword;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    public LoginActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnForgotPassword = (Button) findViewById(R.id.btnForgot);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                if (isValidEmail(email) && !password.isEmpty()) {
                    final ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
                    if (connectionDetector.isConnectingToInternet() == true) {
                        checkLogin(email, password);
                    } else {
                        Snackbar.make(view, "No Internet Connection!!!", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(getCurrentFocus(), "Please enter valid credentials!", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "Please enter valid credentials!", Toast.LENGTH_LONG).show();
                }
            }

        });

        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.enter_email, null);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LoginActivity.this);
                alertBuilder.setView(view);
                final EditText email = (EditText) view.findViewById(R.id.etEmail);
                alertBuilder.setCancelable(true).setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ConnectionDetector connectionDetector = new ConnectionDetector(getApplicationContext());
                        if (connectionDetector.isConnectingToInternet() == true) {
                            sendPassword(email.getText().toString());
                        } else {
                            Snackbar.make(view, "No Internet Connection!!!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                alertBuilder.setTitle("Enter Email To get Password");
                Dialog dialog = alertBuilder.create();
                dialog.show();
            }
        });
    }

    private void sendPassword(final String email) {
        StringRequest stringRequest = new StringRequest(Method.POST, AppConfig.URL_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String errMessage = jsonObject.getString("error_msg");
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        //Toast.makeText(getApplicationContext(), "Password is sent to " + email, Toast.LENGTH_SHORT).show();
                        Snackbar.make(getCurrentFocus(),  "Password is sent to " + email, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(getCurrentFocus(),  errMessage, Snackbar.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Snackbar.make(getCurrentFocus(),  error.getMessage(), Snackbar.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void checkLogin(final String email, final String password) {
        String tag_string_req = "req_login";
        pDialog.setMessage("Logging in ...");
        showDialog();
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        session.setLogin(true);
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user.getString("created_at");

                        db.addUser(name, email, uid, created_at);

                        //adding server attendance to local db
                        db.getAllAttendance(db.getUniqueId());
                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Snackbar.make(getCurrentFocus(),  errorMsg, Snackbar.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Snackbar.make(getCurrentFocus(),  "Json error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Snackbar.make(getCurrentFocus(),  error.getMessage(), Snackbar.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,3})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
