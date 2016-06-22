package com.example.carl.basketballshotlog2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import Helpers.AdsHelper;
import Helpers.DateConverter;


public class GraphActivity extends AppCompatActivity {
    SQLiteDatabase db;

    ArrayList<Date> dates;
    private int spid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_progress);

        Intent recievedIntent = getIntent();
        spid = recievedIntent.getIntExtra("spid", 0);
        //this.setUpAds();
        ShotDBHelper dbHelper = new ShotDBHelper(getBaseContext());
        this.db = dbHelper.getReadableDatabase();

        ArrayList<Integer> shots = null;
        try {
            shots = runSQLQuery();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("PARSE ERROR", "NIGGA FUCKED UP");
        }

        DateConverter dc = new DateConverter();
        DataPoint[] dps = createDataPoints(shots);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dps);
        if (shots.size()>0){
            this.createGraph(series, shots.get(0));
        }

        this.runAds();
    }

    private ArrayList<Integer> runSQLQuery() throws ParseException {
        String sql = "SELECT made, attempts, date FROM Shots WHERE spid = "+ spid + " ORDER BY date";
        Cursor c = db.rawQuery(sql, null);
        ArrayList<Integer> shots = new ArrayList<Integer>();
        dates = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        c.moveToFirst();
        while (c.isAfterLast() == false){
            double p = (c.getInt(0)+0.0)/c.getInt(1)*100;
            int percent = (int) Math.round(p);
            shots.add(percent);
            dates.add(dateFormat.parse(c.getString(2)));
            c.moveToNext();
        }
        return shots;
    }

    private DataPoint[] createDataPoints(ArrayList<Integer> shots){
        DataPoint[] dps = new DataPoint[shots.size()];//maxes.size()
        for(int i = 0; i<shots.size(); i++){//maxes.size()
            Log.i("Date", dates.get(i)+"");
            dps[i] = new DataPoint(dates.get(i), shots.get(i));
        }
        return dps;
    }
    private void createGraph(LineGraphSeries<DataPoint> series, int maxIfOne){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        Log.i("Dates Size: ", dates.size() + "");
        if(dates.size()!= 0){
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
            if(dates.size() == 1) {
                PointsGraphSeries<DataPoint> series3 = new PointsGraphSeries<DataPoint>(new DataPoint[]{
                        new DataPoint(dates.get(0), maxIfOne),
                });

                graph.addSeries(series3);
                series3.setShape(PointsGraphSeries.Shape.TRIANGLE);

                graph.getViewport().setMinX(dates.get(0).getTime() - 1 * 24 * 60 * 60 * 1000);
                graph.getViewport().setMaxX(dates.get(dates.size() - 1).getTime() + 1 * 24 * 60 * 60 * 1000);

                Log.i("PERCENT", maxIfOne+"");
                graph.getViewport().setMinY(maxIfOne - 10);
                graph.getViewport().setMaxY(maxIfOne + 10);
                graph.setTitle("Shooting Percent");
            }else{
                graph.getGridLabelRenderer().setNumHorizontalLabels(3);

                graph.getViewport().setMinX(dates.get(0).getTime() - .25 * 24 * 60 * 60 * 1000);
                graph.getViewport().setMaxX(dates.get(dates.size()-1).getTime()+.25*24*60*60*1000);
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(100);
                graph.setTitle("Shooting Percent Over Time");

                series.setDrawDataPoints(true);
                series.setDataPointsRadius(10);
                series.setThickness(4);

                graph.getViewport().setScalable(true);
                graph.getViewport().setScrollable(true);
                graph.addSeries(series);
            }
        }else{
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(100);
        }


    }


    private AdsHelper adsHelper;

    private void runAds(){
        adsHelper =  new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.banner_ad), this);

        adsHelper.setUpAds();
        int delay = 1000; // delay for 1 sec.
        int period = getResources().getInteger(R.integer.refresh_rate);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                adsHelper.refreshAd();  // display the data
            }
        }, delay, period);
    }
}
