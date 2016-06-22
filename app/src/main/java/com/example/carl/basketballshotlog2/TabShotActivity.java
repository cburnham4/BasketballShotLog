package com.example.carl.basketballshotlog2;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TabHost;


public class TabShotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Intent recievedIntent = getIntent();
        int spid = recievedIntent.getIntExtra("spid", 0);
        String spot = recievedIntent.getStringExtra("spot");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        LocalActivityManager mLocalActivityManager = new LocalActivityManager(this, false);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        tabHost.setup(mLocalActivityManager);

        TabHost.TabSpec spec1 =tabHost.newTabSpec("tab1");
        Intent intent1 = new Intent(this, InputShotsActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("spid", spid);
        spec1.setContent(intent1);//
        spec1.setIndicator("Past Shots");

        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 =tabHost.newTabSpec("tab2");
        Intent intent2 = new Intent(this, GraphActivity.class);
        intent2.putExtra("spid", spid);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec2.setIndicator("Graph");
        spec2.setContent(intent2);//
        tabHost.addTab(spec2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
