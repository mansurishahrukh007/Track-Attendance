package com.mansurishahrukh007.trackattendance.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.mansurishahrukh007.trackattendance.helper.Attendance;
import com.mansurishahrukh007.trackattendance.helper.RecyclerAdapter;
import com.mansurishahrukh007.trackattendance.helper.SQLiteHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;

import info.androidhive.loginandregistration.R;

public class AllAttendanceActivity extends AppCompatActivity {

    private SQLiteHandler db;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private ArrayList<Attendance> attendanceArrayList;

    private TextView textTotalAttendance;
    private TextView textPercentageAttendance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_attendance);
        attendanceArrayList = new ArrayList<>();
        db = new SQLiteHandler(this);

        textPercentageAttendance = (TextView) findViewById(R.id.tvPercentageAttendance);
        textTotalAttendance = (TextView) findViewById(R.id.tvTotalAttendance);

        recyclerView = (RecyclerView) findViewById(R.id.recyle_view);
        recyclerView.setHasFixedSize(true);

        setTextAttendance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        setRecyclerViewData();
        adapter = new RecyclerAdapter(attendanceArrayList, this);
        recyclerView.setAdapter(adapter);

    }

    private void setRecyclerViewData(){

        ArrayList<String[]> attendance = db.getTotalAttendance();
        String[] date = attendance.get(0);
        String[] attended = attendance.get(1);
        String[] total = attendance.get(2);
        String[] added_at = attendance.get(3);
        String[] modified_at = attendance.get(4);

        for (int i = 0; i < date.length; i++) {
            attendanceArrayList.add(new Attendance(date[i], attended[i]+"/"+total[i], added_at[i], modified_at[i]));
        }
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
}
