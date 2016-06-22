package com.example.carl.basketballshotlog2;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ListViewHelpers.Spot;
import ListViewHelpers.SpotAdapter;


public class Spot_ListView extends AppCompatActivity implements AdListener {
    private ArrayList<Spot> list_spots;
    private SpotAdapter spotsAdapter;
    ShotDBHelper dbHelper;
    ListView listView;

    private ViewGroup adViewContainer;
    private com.amazon.device.ads.AdLayout amazonAdView;
    private com.google.android.gms.ads.AdView admobAdView;
    private boolean amazonAdEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list_view);
        list_spots = new ArrayList<Spot>();
        dbHelper = new ShotDBHelper(getBaseContext());
        final SQLiteDatabase write_db = dbHelper.getWritableDatabase();
        //this.getActionBar().setTitle("Shot spotsss");
        this.setTitle("Shot Locations");
        this.listView = (ListView) findViewById(R.id.lv_shot_location);
        this.setUpListView();
        this.setUpAds();
        final TextView et_input_spot = (TextView) findViewById(R.id.et_input_spot);
        Button btn_addSpot = (Button) findViewById(R.id.btn_add_Spot);
        btn_addSpot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((!et_input_spot.getText().toString().equals(""))) {
                    String spot = et_input_spot.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put("spot", spot);
                    write_db.insert("Spots", null, values);
                    list_spots.add(new Spot(getLastSpid(), spot));
                    spotsAdapter.notifyDataSetChanged();
                    et_input_spot.setText("");
                    setUpListView();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent(Spot_ListView.this, TabShotActivity.class);
                Spot spot = spotsAdapter.getItem(position);
                intent.putExtra("spid", spot.getSpid());
                intent.putExtra("spot", spot.getSpot());
                startActivity(intent);
            }
        });

        int delay = 1000; // delay for 1 sec.
        int period = getResources().getInteger(R.integer.refresh_rate); // repeat every 10 sec.
        Timer timer = new Timer();
        final Context context= this;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                refreshAd();
            }
        }, delay, period);



    }

    private int getLastSpid(){
        list_spots = new ArrayList<Spot>();
        SQLiteDatabase read_db = dbHelper.getReadableDatabase();
        String sql = "SELECT Max(spid) FROM Spots";//ADD place id to the check in the future
        Cursor c = read_db.rawQuery(sql, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    private void setUpListView(){

        SQLiteDatabase read_db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Spots";//ADD place id to the check in the future
        Cursor c = read_db.rawQuery(sql, null);
        c.moveToFirst();
        while(c.isAfterLast()==false)

        {
            Spot spot = new Spot(c.getInt(0), c.getString(1));
            list_spots.add(spot);
            c.moveToNext();
        }

        spotsAdapter = new SpotAdapter(this, list_spots);
        listView.setAdapter(spotsAdapter);
        registerForContextMenu(listView);
    }



    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_show_list_view, menu);
    }
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Spot spot = spotsAdapter.getItem(info.position);
        switch(item.getItemId()){

            case R.id.item_edit:
                //TextView tv = (TextView) findViewById(R.id.inputLift);
                //tv.setText(adapter.getItem(info.position));
                //Toast.makeText(this, "Edit : " + adapter.getItem(info.position), Toast.LENGTH_SHORT).show();
                this.CreateDialog(spot);
                break;
            case R.id.item_delete:
                deleteFromDatabase(spot);
                Toast.makeText(this, "Deleted: " + spot.getSpot(), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void deleteFromDatabase(Spot spot){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int spid = spot.getSpid();
        db.delete("Spots", "spid = " + spid, null);
        spotsAdapter.remove(spot);
        spotsAdapter.notifyDataSetChanged();
    }


    public void CreateDialog(Spot spot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater

        builder.setTitle("Edit Spot Name");
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_editspot, null);
        final Spot s = spot;

        final EditText editText = (EditText) view.findViewById(R.id.et_edit_spot);
        editText.setText(s.getSpot());
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //change the spot string
                        String new_spot = editText.getText().toString();
                        if ((!new_spot.equals("")) || !new_spot.isEmpty()) {
                            edit_spot(s, new_spot);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void edit_spot(Spot spot, String spot_string){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        spot.setSpot(spot_string);
        spotsAdapter.notifyDataSetChanged();

        ContentValues newValues = new ContentValues();
        newValues.put("spot", spot_string);
        db.update("Spots", newValues, "spid= "+spot.getSpid(),null);

    }
    private void setUpAds(){
        AdRegistration.setAppKey(getString(R.string.amazon_id));
        amazonAdView = new com.amazon.device.ads.AdLayout(this, com.amazon.device.ads.AdSize.SIZE_320x50);
        amazonAdView.setListener(this);
        //AdRegistration.enableTesting(true);
        admobAdView = new com.google.android.gms.ads.AdView(this);
        admobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        admobAdView.setAdUnitId(getString(R.string.banner_ad));

        // Initialize view container
        adViewContainer = (ViewGroup)findViewById(R.id.al_spots);
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
