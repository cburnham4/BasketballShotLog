package com.example.carl.basketballshotlog2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ListViewHelpers.Shot;
import ListViewHelpers.ShotAdapter;
import ListViewHelpers.SpotAdapter;


public class ShotTracker extends Activity implements AdListener {

    private ShotDBHelper dbHelper;
    private ArrayList<Shot> list_shots;

    private ShotAdapter shotAdapter;
    private int spid;

    ListView listView;

    private ViewGroup adViewContainer;
    private com.amazon.device.ads.AdLayout amazonAdView;
    private com.google.android.gms.ads.AdView admobAdView;
    private boolean amazonAdEnabled;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_tracker);
        this.setUpAds();

        dbHelper = new ShotDBHelper(getBaseContext());
        this.db = dbHelper.getReadableDatabase();
        this.listView = (ListView) findViewById(R.id.lv_shots);
        Intent recievedIntent = getIntent();
        spid = recievedIntent.getIntExtra("spid", 0);
        loadCurrentDay();
        //Shots = new ArrayList<>();


        Button btn_add_made =(Button)findViewById(R.id.btn_add1_made);
        Button btn_sub_made = (Button) findViewById(R.id.btn_sub1_made);
        Button btn_sub_attempt = (Button) findViewById(R.id.btn_sub1_attempts);
        Button btn_add_attempt = (Button) findViewById(R.id.btn_add1_attempt);
        final Button addShot = (Button) findViewById(R.id.btn_add_shot);

        final EditText ed_made = (EditText) findViewById(R.id.ed_made);
        final EditText ed_attempt =(EditText) findViewById(R.id.ed_attempts);

        btn_add_made.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    int rep = Integer.parseInt(ed_made.getText().toString());
                    ed_made.setText(rep + 1 + "");
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_sub_made.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    int rep = Integer.parseInt(ed_made.getText().toString());
                    ed_made.setText(rep - 1 + "");
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_add_attempt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int rep = Integer.parseInt(ed_attempt.getText().toString());
                    ed_attempt.setText(rep + 1 + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_sub_attempt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int weight = Integer.parseInt(ed_attempt.getText().toString());
                ed_attempt.setText(weight - 1 + "");
            }
        });

//CHANGE BACK
        addShot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (ed_made.getText().toString() != "" && ed_attempt.getText().toString() != "") {
                        int attempts = Integer.parseInt(ed_attempt.getText().toString());
                        int made = Integer.parseInt(ed_made.getText().toString());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        //PUT SET INTO SETS
                        String d = dateFormat.format(date);
                        ContentValues values = new ContentValues();
                        values.put("attempts", attempts);
                        values.put("made", made);
                        values.put("date", d);
                        values.put("spid", spid);
                        db.insert("Shots", null, values);
                        //-----------------------
                        list_shots.add(new Shot(made, attempts, d, spid));
                        shotAdapter.notifyDataSetChanged();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.i("HERE", "We tried");
                }

            }
        });
        int delay = 1000; // delay for 1 sec.
        int period = 10000; // repeat every 4 sec.
        Timer timer = new Timer();
        final Context context= this;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                refreshAd();
            }
        }, delay, period);


    }

    private void loadCurrentDay(){
        list_shots = new ArrayList<Shot>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql  = "SELECT sid, made, attempts, date FROM Shots WHERE spid = "+ spid + " ";//ADD place id to the check in the future
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            Log.i("SETSANDREPS", "Weight: " + c.getString(0) + " Reps: " + c.getString(1) + " sets: " + c.getInt(2));
            Shot shot = new Shot(c.getInt(1),c.getInt(2), c.getString(3),c.getInt(0));
            list_shots.add(shot);
            c.moveToNext();
        }
        shotAdapter = new ShotAdapter(this, list_shots);
        listView.setAdapter(shotAdapter);
        registerForContextMenu(listView);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_shot_tracker, menu);
    }
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.deleteCurrent:
                deleteFromDatabase(shotAdapter.getItem(info.position));
                break;
        }
        return true;
    }

    private void deleteFromDatabase(Shot shot){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int sid = shot.getSid();
        db.delete("Shots","sid = "+sid,null);
        shotAdapter.remove(shot);
        shotAdapter.notifyDataSetChanged();
    }
    private void setUpAds(){
        AdRegistration.setAppKey("15aa1494c2d14f298d7a257f4809cdce");
        amazonAdView = new com.amazon.device.ads.AdLayout(this, com.amazon.device.ads.AdSize.SIZE_320x50);
        amazonAdView.setListener(this);
        //AdRegistration.enableTesting(true);
        admobAdView = new com.google.android.gms.ads.AdView(this);
        admobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        admobAdView.setAdUnitId(getString(R.string.banner_ad));

        // Initialize view container
        adViewContainer = (ViewGroup)findViewById(R.id.al_shots);
        amazonAdEnabled = true;
        adViewContainer.addView(amazonAdView);

        amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());
    }


    public void refreshAd()
    {
        amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());
    }

    @Override
    public void onAdLoaded(Ad ad, AdProperties adProperties) {
        if (!amazonAdEnabled)
        {
            amazonAdEnabled = true;
            adViewContainer.removeView(admobAdView);
            adViewContainer.addView(amazonAdView);
        }
    }

    @Override
    public void onAdFailedToLoad(Ad ad, AdError adError) {
        // Call AdMob SDK for backfill
        if (amazonAdEnabled)
        {
            amazonAdEnabled = false;
            adViewContainer.removeView(amazonAdView);
            adViewContainer.addView(admobAdView);
        }
//        AdRequest.Builder.addTestDevice("04CD51A7A1F806B7F55CADD6A3B84E92");
        admobAdView.loadAd((new com.google.android.gms.ads.AdRequest.Builder()).build());
    }

    @Override
    public void onAdExpanded(Ad ad) {

    }

    @Override
    public void onAdCollapsed(Ad ad) {

    }

    @Override
    public void onAdDismissed(Ad ad) {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        this.amazonAdView.destroy();
    }

    public void onPause(){
        super.onPause();
        this.amazonAdView.destroy();
    }

    public void onResume(){
        super.onResume();
        this.setUpAds();
    }

}
