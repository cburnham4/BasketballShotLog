package Helpers;

import java.util.HashMap;

/**
 * Created by cvburnha on 3/5/2015.
 */
public class DateConverter {
    HashMap<String,String> months;
    public DateConverter(){
        months = new HashMap<String, String>();
        months.put("01", "Jan.");
        months.put("02", "Feb.");
        months.put("03", "March");
        months.put("04", "April");
        months.put("05", "May");
        months.put("06", "June");
        months.put("07", "July");
        months.put("08", "August");
        months.put("09", "Sep.");
        months.put("10", "Oct.");
        months.put("11", "Nov.");
        months.put("12", "Dec.");
    }

    public String convertDateToText(String d){
        String date;
        String year = d.substring(0,4);
        String month = months.get(d.substring(5,7));
        String day = d.substring(8,10);
        date = month + " " + day+", " +year;
        return date;

    }
}
