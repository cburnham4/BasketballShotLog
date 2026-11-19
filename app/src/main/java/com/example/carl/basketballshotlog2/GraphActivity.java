package com.example.carl.basketballshotlog2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import Helpers.EdgeToEdgeHelper;


public class GraphActivity extends AppCompatActivity {
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

    private AdsHelper adsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_progress);
        EdgeToEdgeHelper.apply(this, findViewById(android.R.id.content));

        Intent recievedIntent = getIntent();
        spid = recievedIntent.getIntExtra("spid", 0);

        this.setupDateSelections();

        shotDBHelper = new ShotDBHelper(this);

        graph = findViewById(R.id.graph);
        rel_graph = findViewById(R.id.rel_graph);
        tvNoData = findViewById(R.id.tv_noData);

        try {
            getExistingData();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("PARSE ERROR", "Failed to parse shot dates");
        }

        this.createGraph();
        this.runAds();
    }

    public void setupDateSelections() {
        final TextView tv_1m = findViewById(R.id.tv_1m);
        final TextView tv_3m = findViewById(R.id.tv_3m);
        final TextView tv_6m = findViewById(R.id.tv_6m);
        final TextView tv_1y = findViewById(R.id.tv_1y);
        final TextView tv_all = findViewById(R.id.tv_all);

        tvDateSelections = new TextView[]{tv_1m, tv_3m, tv_6m, tv_1y, tv_all};

        /* Milliseconds in a day */
        double timeInDay = 24 * 60 * 60 * 1000;
        final double timeInMonth = timeInDay * 30.5;
        final double timeIn3Month = timeInMonth * 3;
        final double timeIn6Month = timeIn3Month * 2;
        final double timeInYear = timeIn6Month * 2;

        tv_1m.setOnClickListener(new OnDateRangeSelection(timeInMonth, 0));
        tv_3m.setOnClickListener(new OnDateRangeSelection(timeIn3Month, 1));
        tv_6m.setOnClickListener(new OnDateRangeSelection(timeIn6Month, 2));
        tv_1y.setOnClickListener(new OnDateRangeSelection(timeInYear, 3));
        tv_all.setOnClickListener(new OnDateRangeSelection(Double.MAX_VALUE, 4));
    }

    public class OnDateRangeSelection implements View.OnClickListener {
        private final double timeLimit;
        private final int tvIndex;

        public OnDateRangeSelection(double timeLimit, int tvIndex) {
            this.timeLimit = timeLimit;
            this.tvIndex = tvIndex;
        }

        @Override
        public void onClick(View v) {
            Date currentDate = new Date();
            final double currentTime = currentDate.getTime();

            ArrayList<DataPoint> dataPointsLocal = new ArrayList<>();
            for (DataPoint dataPoint : dataPoints) {
                if (currentTime - dataPoint.getX() <= timeLimit) {
                    dataPointsLocal.add(dataPoint);
                }
            }

            DataPoint[] dataPoints1 = dataPointsLocal.toArray(new DataPoint[0]);
            lineGraphSeries.resetData(dataPoints1);
            Viewport viewport = graph.getViewport();
            viewport.setMinX(lineGraphSeries.getLowestValueX() - 5 * 24 * 60 * 60 * 1000);
            viewport.setMaxX(lineGraphSeries.getHighestValueX() + 5 * 24 * 60 * 60 * 1000);
            viewport.setMinY(lineGraphSeries.getLowestValueY() - 5);
            viewport.setMaxY(lineGraphSeries.getHighestValueY() + 5);
            for (TextView tv : tvDateSelections) {
                tv.setBackgroundColor(0);
            }
            tvDateSelections[tvIndex].setBackgroundColor(ContextCompat.getColor(GraphActivity.this, R.color.divider));
        }
    }

    private void getExistingData() throws ParseException {
        SQLiteDatabase db = shotDBHelper.getReadableDatabase();
        dataPoints = new ArrayList<>();
        lineGraphSeries = new LineGraphSeries<>();

        String sql = "SELECT made, attempts, date FROM Shots WHERE spid = " + spid + " ORDER BY date";
        Cursor c = db.rawQuery(sql, null);
        dates = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            double p = (c.getInt(0) + 0.0) / c.getInt(1) * 100;
            int percent = (int) Math.round(p);
            Date date = dateFormat.parse(c.getString(2));

            DataPoint dataPoint = new DataPoint(date, percent);
            dataPoints.add(dataPoint);

            lineGraphSeries.appendData(dataPoint, true, c.getCount());
            c.moveToNext();
        }

        if (dataPoints.size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            rel_graph.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rel_graph.setVisibility(View.VISIBLE);
        }

        c.close();
        db.close();
    }

    private void createGraph() {
        graph.setTitle("Shots Percent Made");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        if (!lineGraphSeries.isEmpty()) {
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            if (dataPoints.size() == 1) {
                DataPoint dataPoint = dataPoints.get(0);
                PointsGraphSeries<DataPoint> seriesSingle = new PointsGraphSeries<>(new DataPoint[]{
                        dataPoint
                });

                graph.addSeries(seriesSingle);
                seriesSingle.setShape(PointsGraphSeries.Shape.TRIANGLE);

                Viewport viewport = graph.getViewport();
                viewport.setMinX(dataPoint.getX() - 5 * 24 * 60 * 60 * 1000);
                viewport.setMaxX(dataPoint.getX() + 5 * 24 * 60 * 60 * 1000);

                viewport.setMinY(dataPoint.getY() - 10);
                viewport.setMaxY(dataPoint.getY() + 10);

            } else {
                Viewport viewport = graph.getViewport();
                viewport.setMinX(lineGraphSeries.getLowestValueX() - 5 * 24 * 60 * 60 * 1000);
                viewport.setMaxX(lineGraphSeries.getHighestValueX() + 5 * 24 * 60 * 60 * 1000);
                viewport.setMinY(lineGraphSeries.getLowestValueY() - 5);
                viewport.setMaxY(lineGraphSeries.getHighestValueY() + 5);

                lineGraphSeries.setDrawDataPoints(true);
                lineGraphSeries.setDataPointsRadius(10);
                lineGraphSeries.setThickness(4);

                graph.addSeries(lineGraphSeries);
            }
        }
    }

    private void runAds() {
        adsHelper = new AdsHelper(getWindow().getDecorView(), getString(R.string.admob_ad_id), this);
        adsHelper.setUpAds();
        int delay = 1000;
        int period = getResources().getInteger(R.integer.ad_refresh_rate);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                adsHelper.refreshAd();
            }
        }, delay, period);
    }

    @Override
    protected void onPause() {
        if (adsHelper != null) {
            adsHelper.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adsHelper != null) {
            adsHelper.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adsHelper != null) {
            adsHelper.onDestroy();
        }
        super.onDestroy();
    }
}
