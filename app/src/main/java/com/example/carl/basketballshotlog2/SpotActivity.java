package com.example.carl.basketballshotlog2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Helpers.AdsHelper;
import Helpers.EdgeToEdgeHelper;
import ListViewHelpers.Spot;
import ListViewHelpers.SpotAdapter;


public class SpotActivity extends AppCompatActivity {
    private ArrayList<Spot> list_spots;
    private SpotAdapter spotsAdapter;
    ShotDBHelper dbHelper;
    ListView listView;

    private AdsHelper adsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spots);
        EdgeToEdgeHelper.apply(this, findViewById(android.R.id.content));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Shot Locations");

        list_spots = new ArrayList<>();
        dbHelper = new ShotDBHelper(getBaseContext());
        final SQLiteDatabase write_db = dbHelper.getWritableDatabase();

        this.setTitle("Shot Locations");
        this.listView = findViewById(R.id.lv_shot_location);
        this.setUpListView();

        final TextView et_input_spot = findViewById(R.id.et_input_spot);
        Button btn_addSpot = findViewById(R.id.btn_add_Spot);
        btn_addSpot.setOnClickListener(v -> {
            if (!et_input_spot.getText().toString().equals("")) {
                String spot = et_input_spot.getText().toString();
                ContentValues values = new ContentValues();
                values.put("spot", spot);
                write_db.insert("Spots", null, values);
                list_spots.add(new Spot(getLastSpid(), spot));
                spotsAdapter.notifyDataSetChanged();
                et_input_spot.setText("");
                setUpListView();
            }
        });

        listView.setOnItemClickListener((a, v, position, id) -> {
            Intent intent = new Intent(SpotActivity.this, TabShotActivity.class);
            Spot spot = spotsAdapter.getItem(position);
            intent.putExtra("spid", spot.getSpid());
            intent.putExtra("spot", spot.getSpot());
            startActivity(intent);
        });

        this.runAds();
    }

    private int getLastSpid() {
        list_spots = new ArrayList<>();
        SQLiteDatabase read_db = dbHelper.getReadableDatabase();
        String sql = "SELECT Max(spid) FROM Spots";
        Cursor c = read_db.rawQuery(sql, null);
        c.moveToFirst();
        int spid = c.getInt(0);
        c.close();
        return spid;
    }

    private void setUpListView() {
        SQLiteDatabase read_db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Spots";
        Cursor c = read_db.rawQuery(sql, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Spot spot = new Spot(c.getInt(0), c.getString(1));
            list_spots.add(spot);
            c.moveToNext();
        }
        c.close();

        spotsAdapter = new SpotAdapter(this, list_spots);
        listView.setAdapter(spotsAdapter);
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_show_list_view, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }
        Spot spot = spotsAdapter.getItem(info.position);
        int itemId = item.getItemId();
        if (itemId == R.id.item_edit) {
            this.CreateDialog(spot);
            return true;
        } else if (itemId == R.id.item_delete) {
            deleteFromDatabase(spot);
            Toast.makeText(this, "Deleted: " + spot.getSpot(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFromDatabase(Spot spot) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int spid = spot.getSpid();
        db.delete("Spots", "spid = ?", new String[]{String.valueOf(spid)});
        spotsAdapter.remove(spot);
        spotsAdapter.notifyDataSetChanged();
    }

    public void CreateDialog(Spot spot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Spot Name");
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_editspot, null);
        final Spot s = spot;

        final EditText editText = view.findViewById(R.id.et_edit_spot);
        editText.setText(s.getSpot());
        builder.setView(view)
                .setPositiveButton("Finish", (dialog, id) -> {
                    String new_spot = editText.getText().toString();
                    if (!new_spot.isEmpty()) {
                        edit_spot(s, new_spot);
                    }
                })
                .setNegativeButton("Cancel", (DialogInterface dialog, int id) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void edit_spot(Spot spot, String spot_string) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        spot.setSpot(spot_string);
        spotsAdapter.notifyDataSetChanged();

        ContentValues newValues = new ContentValues();
        newValues.put("spot", spot_string);
        db.update("Spots", newValues, "spid= ?", new String[]{String.valueOf(spot.getSpid())});
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
