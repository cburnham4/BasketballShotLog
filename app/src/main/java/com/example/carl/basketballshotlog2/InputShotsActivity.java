package com.example.carl.basketballshotlog2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import Helpers.AdsHelper;
import ListViewHelpers.Shot;
import ListViewHelpers.ShotAdapter;


public class InputShotsActivity extends Activity  {

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
        setContentView(R.layout.activity_input_shot);

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

        this.runAds();


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
