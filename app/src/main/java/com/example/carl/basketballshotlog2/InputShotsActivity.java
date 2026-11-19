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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import Helpers.AdsHelper;
import Helpers.EdgeToEdgeHelper;
import ListViewHelpers.Shot;
import ListViewHelpers.ShotAdapter;


public class InputShotsActivity extends Activity {

    private ShotDBHelper dbHelper;
    private ArrayList<Shot> list_shots;

    private ShotAdapter shotAdapter;
    private int spid;

    ListView listView;
    SQLiteDatabase db;

    private AdsHelper adsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_shot);
        EdgeToEdgeHelper.apply(this, findViewById(android.R.id.content));

        dbHelper = new ShotDBHelper(getBaseContext());
        this.db = dbHelper.getReadableDatabase();
        this.listView = findViewById(R.id.lv_shots);
        Intent recievedIntent = getIntent();
        spid = recievedIntent.getIntExtra("spid", 0);
        loadCurrentDay();

        Button btn_add_made = findViewById(R.id.btn_add1_made);
        Button btn_sub_made = findViewById(R.id.btn_sub1_made);
        Button btn_sub_attempt = findViewById(R.id.btn_sub1_attempts);
        Button btn_add_attempt = findViewById(R.id.btn_add1_attempt);
        final Button addShot = findViewById(R.id.btn_add_shot);

        final EditText ed_made = findViewById(R.id.ed_made);
        final EditText et_missed = findViewById(R.id.et_missed);

        btn_add_made.setOnClickListener(v -> {
            try {
                int rep = Integer.parseInt(ed_made.getText().toString());
                ed_made.setText(String.valueOf(rep + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btn_sub_made.setOnClickListener(v -> {
            try {
                int rep = Integer.parseInt(ed_made.getText().toString());
                ed_made.setText(String.valueOf(rep - 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btn_add_attempt.setOnClickListener(v -> {
            try {
                int rep = Integer.parseInt(et_missed.getText().toString());
                et_missed.setText(String.valueOf(rep + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btn_sub_attempt.setOnClickListener(v -> {
            try {
                int weight = Integer.parseInt(et_missed.getText().toString());
                et_missed.setText(String.valueOf(weight - 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addShot.setOnClickListener(v -> {
            try {
                if (!ed_made.getText().toString().isEmpty() && !et_missed.getText().toString().isEmpty()) {
                    int misses = Integer.parseInt(et_missed.getText().toString());
                    int made = Integer.parseInt(ed_made.getText().toString());
                    int attempts = made + misses;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date date = new Date();
                    String d = dateFormat.format(date);
                    ContentValues values = new ContentValues();
                    values.put("attempts", attempts);
                    values.put("made", made);
                    values.put("date", d);
                    values.put("spid", spid);
                    db.insert("Shots", null, values);
                    list_shots.add(new Shot(made, misses, d, spid));
                    shotAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("HERE", "We tried");
            }
        });

        this.runAds();
    }

    private void loadCurrentDay() {
        list_shots = new ArrayList<>();
        SQLiteDatabase readableDb = dbHelper.getReadableDatabase();
        String sql = "SELECT sid, made, attempts, date FROM Shots WHERE spid = " + spid + " ";
        Cursor c = readableDb.rawQuery(sql, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Log.i("SETSANDREPS", "Weight: " + c.getString(0) + " Reps: " + c.getString(1) + " sets: " + c.getInt(2));
            int made = c.getInt(1);
            int attempts = c.getInt(2);
            int misses = attempts - made;
            Shot shot = new Shot(made, misses, c.getString(3), c.getInt(0));
            list_shots.add(shot);
            c.moveToNext();
        }
        c.close();
        shotAdapter = new ShotAdapter(this, list_shots);
        listView.setAdapter(shotAdapter);
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_shot_tracker, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }
        if (item.getItemId() == R.id.deleteCurrent) {
            deleteFromDatabase(shotAdapter.getItem(info.position));
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFromDatabase(Shot shot) {
        SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
        int sid = shot.getSid();
        writableDb.delete("Shots", "sid = ?", new String[]{String.valueOf(sid)});
        shotAdapter.remove(shot);
        shotAdapter.notifyDataSetChanged();
    }

    private void runAds() {
        adsHelper = new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_ad_id), this);
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
