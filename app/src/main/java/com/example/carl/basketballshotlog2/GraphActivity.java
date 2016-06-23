package com.example.carl.basketballshotlog2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import Helpers.AdsHelper;
import Helpers.DateConverter;


public class GraphActivity extends AppCompatActivity {
    SQLiteDatabase db;


    /* DataPoints and series */
    private LineGraphSeries<DataPoint> lineGraphSeries;
    private ArrayList<DataPoint> dataPoints;

    /* Views */
    private GraphView graph;
    private RelativeLayout rel_graph;
    private TextView tvNoData;

    /* Database Helper */
    ShotDBHelper shotDBHelper;

    TextView[] tvDateSelections;

    ArrayList<Date> dates;
    private int spid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_progress);

        Intent recievedIntent = getIntent();
        spid = recievedIntent.getIntExtra("spid", 0);

        this.setupDateSelections();

        shotDBHelper = new ShotDBHelper(this);

        graph = (GraphView) findViewById(R.id.graph);
        rel_graph = (RelativeLayout) findViewById(R.id.rel_graph);
        tvNoData = (TextView) findViewById(R.id.tv_noData);

        try {
            getExistingData();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("PARSE ERROR", "NIGGA FUCKED UP");
        }

        /*
        DateConverter dc = new DateConverter();
        DataPoint[] dps = createDataPoints(shots);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dps);
        if (shots.size()>0){
            this.createGraph(series, shots.get(0));
        }
        */
        this.createGraph();
        this.runAds();
    }

    public void setupDateSelections(){
        final TextView tv_1m = (TextView) findViewById(R.id.tv_1m);
        final TextView tv_3m = (TextView) findViewById(R.id.tv_3m);
        final TextView tv_6m = (TextView) findViewById(R.id.tv_6m);
        final TextView tv_1y = (TextView) findViewById(R.id.tv_1y);
        final TextView tv_all = (TextView) findViewById(R.id.tv_all);

        tvDateSelections = new TextView[]{tv_1m, tv_3m, tv_6m, tv_1y, tv_all};

        Date currentDate = new Date();

        final double currentTime = currentDate.getTime();

        /* Milliseconds in a day */
        double timeInDay = 24 * 60 * 60 * 1000;
        final double timeInMonth = timeInDay * 30.5;
        final double timeIn3Month = timeInMonth *3;
        final double timeIn6Month = timeIn3Month * 2;
        final double timeInYear = timeIn6Month * 2;

        /* todo check if you can break if not true */
        tv_1m.setOnClickListener(new OnDateRangeSelection(timeInMonth, 0));
        tv_3m.setOnClickListener(new OnDateRangeSelection(timeIn3Month, 1));
        tv_6m.setOnClickListener(new OnDateRangeSelection(timeIn6Month, 2));
        tv_1y.setOnClickListener(new OnDateRangeSelection(timeInYear, 3));
        tv_all.setOnClickListener(new OnDateRangeSelection(Double.MAX_VALUE, 4));

    }

    public class OnDateRangeSelection implements View.OnClickListener{
        private double timeLimit;
        private int tvIndex;
        public OnDateRangeSelection(double timeLimit, int tvIndex) {
            this.timeLimit = timeLimit;
            this.tvIndex = tvIndex;
        }

        @Override
        public void onClick(View v) {
            Date currentDate = new Date();
            final double currentTime = currentDate.getTime();

            ArrayList<DataPoint> dataPointsLocal = new ArrayList<DataPoint>();
                /* Iterate though the global datapoints to get the ones that fall in a 1 month range */
            for (DataPoint dataPoint: dataPoints){
                if(currentTime-dataPoint.getX()<= timeLimit){
                    dataPointsLocal.add(dataPoint);
                }
            }

            DataPoint[] dataPoints1 = dataPointsLocal.toArray(new DataPoint[dataPointsLocal.size()]);
            lineGraphSeries.resetData(dataPoints1);
            Viewport viewport = graph.getViewport();
            viewport.setMinX(lineGraphSeries.getLowestValueX()-5*24*60*60*1000);
            viewport.setMaxX(lineGraphSeries.getHighestValueX()+5*24*60*60*1000);
            viewport.setMinY(lineGraphSeries.getLowestValueY()-5);
            viewport.setMaxY(lineGraphSeries.getHighestValueY()+5);
            /* Set all points textviews to null bg */
            for (TextView tv: tvDateSelections){
                tv.setBackgroundColor(0);
            }
            /* set background for selected item */
            tvDateSelections[tvIndex].setBackgroundColor(getResources().getColor(R.color.divider));
        }
    }

    private void getExistingData() throws ParseException {
        SQLiteDatabase db = shotDBHelper.getReadableDatabase();
        dataPoints = new ArrayList<>();
        lineGraphSeries = new LineGraphSeries<>();

        String sql = "SELECT made, attempts, date FROM Shots WHERE spid = "+ spid + " ORDER BY date";
        Cursor c = db.rawQuery(sql, null);
        ArrayList<Integer> shots = new ArrayList<Integer>();
        dates = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        c.moveToFirst();
        while (!c.isAfterLast()){
            double p = (c.getInt(0)+0.0)/c.getInt(1)*100;
            int percent = (int) Math.round(p);
            Date date = dateFormat.parse(c.getString(2));

            DataPoint dataPoint = new DataPoint(date, percent);
            dataPoints.add(dataPoint);

            lineGraphSeries.appendData(dataPoint, true, c.getCount());
            c.moveToNext();
        }

        if(dataPoints.size() == 0){
            tvNoData.setVisibility(View.VISIBLE);
            rel_graph.setVisibility(View.GONE);
        }else{
            tvNoData.setVisibility(View.GONE);
            rel_graph.setVisibility(View.VISIBLE);
        }

        /* close cursor and db */
        c.close();
        db.close();
    }

    private DataPoint[] createDataPoints(ArrayList<Integer> shots){
        DataPoint[] dps = new DataPoint[shots.size()];//maxes.size()
        for(int i = 0; i<shots.size(); i++){//maxes.size()
            Log.i("Date", dates.get(i)+"");
            dps[i] = new DataPoint(dates.get(i), shots.get(i));
        }
        return dps;
    }

    private void createGraph(){
        graph.setTitle("Shots Percent Made");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        if(!lineGraphSeries.isEmpty()){
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            if(dataPoints.size() == 1){
                DataPoint dataPoint = dataPoints.get(0);
                PointsGraphSeries<DataPoint> seriesSingle = new PointsGraphSeries<DataPoint>(new DataPoint[] {
                        dataPoint
                });

                graph.addSeries(seriesSingle);
                seriesSingle.setShape(PointsGraphSeries.Shape.TRIANGLE);

                Viewport viewport = graph.getViewport();
                viewport.setMinX(dataPoint.getX()-5*24*60*60*1000);
                viewport.setMaxX(dataPoint.getX()+5*24*60*60*1000);

                viewport.setMinY(dataPoint.getY()-10);
                viewport.setMaxY(dataPoint.getY() + 10);

            }else{
                Viewport viewport = graph.getViewport();
                viewport.setMinX(lineGraphSeries.getLowestValueX()-5*24*60*60*1000);
                viewport.setMaxX(lineGraphSeries.getHighestValueX()+5*24*60*60*1000);
                viewport.setMinY(lineGraphSeries.getLowestValueY()-5);
                viewport.setMaxY(lineGraphSeries.getHighestValueY()+5);

                lineGraphSeries.setDrawDataPoints(true);
                lineGraphSeries.setDataPointsRadius(10);
                lineGraphSeries.setThickness(4);

                //graph.getViewport().setScalable(true);
                //graph.getViewport().setScrollable(true);
                graph.addSeries(lineGraphSeries);
            }


        }

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
