package com.mansurishahrukh007.trackattendance.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shahrukh Mansuri on 4/1/2016.
 */
public class DateAndTimeProvider {
    public String getDateAndTime(){
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        return df.format(date);
    }

}
