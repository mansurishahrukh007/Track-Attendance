package com.mansurishahrukh007.trackattendance.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mansurishahrukh007.trackattendance.helper.ConnectionDetector;
import com.mansurishahrukh007.trackattendance.helper.PDFGenerator;
import com.mansurishahrukh007.trackattendance.helper.SQLiteHandler;
import com.mansurishahrukh007.trackattendance.helper.ServerConnection;
import com.mansurishahrukh007.trackattendance.helper.SessionManager;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import info.androidhive.loginandregistration.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DIALOG_ID = 0;
    private TextView textDate, textTotalAttendance, textPercentageAttendance;
    private EditText edtAttended, edtTotal;
    private Button btnAdd, btnDelete, btnModify;
    private int year1, month, day;
    private SQLiteHandler db;
    private SessionManager session;
    private ServerConnection serverConnection;
    private Toolbar toolbar;
    private Handler handler;
    String uid, date, attended, total;

    Runnable timeTask = new Runnable() {
        @Override
        public void run() {
            ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
            if (!cd.isConnectingToInternet()) {
                buttonsDisabled();
            } else {
                buttonsEnabled();
            }
            setTextAttendance();
            handler.postDelayed(timeTask, 1000);
        }
    };

    private void Initialization() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        edtAttended = (EditText) findViewById(R.id.etAttended);
        edtTotal = (EditText) findViewById(R.id.etTotal);
        final Calendar cal = Calendar.getInstance();
        year1 = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        textDate = (TextView) findViewById(R.id.tvDate);
        textDate.setText(day + "-" + (month + 1) + "-" + year1);
        showDialogPicker();
        btnAdd = (Button) findViewById(R.id.btnAddAttendance);
        btnDelete = (Button) findViewById(R.id.btnDeleteAttendance);
        btnModify = (Button) findViewById(R.id.btnModifyAttendance);
        textTotalAttendance = (TextView) findViewById(R.id.tvTotalAttendance);
        textPercentageAttendance = (TextView) findViewById(R.id.tvPercentageAttendance);
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        handler = new Handler();
        handler.post(timeTask);
        serverConnection = new ServerConnection(MainActivity.this);

    }

    private void showDialogPicker() {
        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID)
            return new DatePickerDialog(this, dpickerListener, year1, month, day);
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            year1 = year;
            month = monthOfYear + 1;
            day = dayOfMonth;
            textDate.setText(day + "-" + month + "-" + year1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialization();
        setSupportActionBar(toolbar);

        HashMap<String, String> user = db.getUserDetails();
        final String name = user.get("name");
        final String email = user.get("email");

        //for navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                TextView txtname = (TextView) findViewById(R.id.nav_title);
                TextView txtEmail = (TextView) findViewById(R.id.nav_email);
                txtEmail.setText(email);
                txtname.setText(name);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getAttended().isEmpty() && !getTotal().isEmpty()) {
                    if (!isTrueInput()) {
                        Toast.makeText(getApplicationContext(), "Total can't be less than Attended", Toast.LENGTH_LONG).show();
                    } else {
                        serverConnection.addAttendance(getUid(), getDate(), getAttended(), getTotal());
                    }
                } else {
                    if (getAttended().isEmpty()) {
                        edtAttended.setError("Required");
                    }
                    if (getTotal().isEmpty()) {
                        edtTotal.setError("Required");
                    }
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverConnection.deleteAttendance(getUid(), getDate());
            }
        });

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getAttended().isEmpty() && !getTotal().isEmpty()) {
                    if (!isTrueInput()) {
                        Toast.makeText(getApplicationContext(), "Total can't be less than Attended", Toast.LENGTH_LONG).show();
                    } else {
                        serverConnection.modifyAttendance(getUid(), getDate(), getAttended(), getTotal());
                    }
                } else {
                    if (getAttended().isEmpty()) {
                        edtAttended.setError("Required");
                    }
                    if (getTotal().isEmpty()) {
                        edtTotal.setError("Required");
                    }
                }
            }
        });
    }

    public void setTextAttendance() {
        int total = db.getAttendanceTotal();
        int attended = db.getAttendanceAttended();
        DecimalFormat df = new DecimalFormat("00.00");
        String percentage;
        if (total == 0 || attended == 0) {
            percentage = "00.00%";
        } else {
            percentage = df.format(((float) attended / (float) total) * 100) + "%";
        }
        textTotalAttendance.setText(attended + "/" + total);
        textPercentageAttendance.setText(percentage);
        Log.d("Getting data from IDB", attended + "/" + total);
    }

    private void clearAttendance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        ConnectionDetector connectionDetector = new ConnectionDetector(this);
        if (connectionDetector.isConnectingToInternet()) {
            builder.setTitle("Clear All Attendance ???").setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    serverConnection.allDeleteAttendance(db.getUniqueId());
                }
            }).setNegativeButton("NO", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection.", Toast.LENGTH_LONG).show();
        }
    }

    private void logoutUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Really want to Logout ???").setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.setLogin(false);
                db.deleteUsersDB();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("NO", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void buttonsEnabled() {
        btnAdd.setEnabled(true);
        btnDelete.setEnabled(true);
        btnModify.setEnabled(true);
    }

    private void buttonsDisabled() {
        btnAdd.setEnabled(false);
        btnDelete.setEnabled(false);
        btnModify.setEnabled(false);
    }

    private boolean isTrueInput() {
        if (Integer.parseInt(getAttended().toString()) <= Integer.parseInt(getTotal().toString())) {
            return true;
        }
        return false;
    }

    public String getUid() {
        return uid = db.getUniqueId();
    }

    public String getAttended() {
        return attended = edtAttended.getText().toString();
    }

    public String getTotal() {
        return total = edtTotal.getText().toString();
    }

    public String getDate() {
        return date = textDate.getText().toString();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_exit:
                finish();
                break;
            case R.id.nav_all_attendance:
                Intent newIntent = new Intent(getApplicationContext(), AllAttendanceActivity.class);
                startActivity(newIntent);
                break;
            case R.id.nav_pdf:
                PDFGenerator pdf = new PDFGenerator(this);
                pdf.createPDF();
                break;
            case R.id.nav_clear:
                clearAttendance();
                break;
            case R.id.nav_logout:
                logoutUser();
                break;
            case R.id.nav_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Track Attendance");
                intent.putExtra(Intent.EXTRA_TEXT, "link");
                startActivity(Intent.createChooser(intent, "Share Using"));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
