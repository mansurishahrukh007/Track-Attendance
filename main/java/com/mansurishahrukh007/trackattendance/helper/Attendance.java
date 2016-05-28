package com.mansurishahrukh007.trackattendance.helper;

/**
 * Created by Shahrukh Mansuri on 4/12/2016.
 */
public class Attendance {
    private String date;
    private String attendance;
    private String addedAt;
    private String updatedAt;

    public Attendance(String date, String attendance, String addedAt, String updatedAt) {
        this.date = date;
        this.attendance = attendance;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
    }

    public String getDate() {
        return date;
    }

    public String getAttendance() {
        return attendance;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
