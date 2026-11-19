package com.example.carl.basketballshotlog2;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TabHost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import Helpers.EdgeToEdgeHelper;


public class TabShotActivity extends AppCompatActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        EdgeToEdgeHelper.apply(this, findViewById(android.R.id.content));

        Intent recievedIntent = getIntent();
        int spid = recievedIntent.getIntExtra("spid", 0);
        String spot = recievedIntent.getStringExtra("spot");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setTitle(spot);

        TabHost tabHost = findViewById(R.id.tabHost);
        LocalActivityManager mLocalActivityManager = new LocalActivityManager(this, false);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        tabHost.setup(mLocalActivityManager);

        TabHost.TabSpec spec1 = tabHost.newTabSpec("tab1");
        Intent intent1 = new Intent(this, InputShotsActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("spid", spid);
        spec1.setContent(intent1);
        spec1.setIndicator("Past Shots");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("tab2");
        Intent intent2 = new Intent(this, GraphActivity.class);
        intent2.putExtra("spid", spid);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec2.setIndicator("Graph");
        spec2.setContent(intent2);
        tabHost.addTab(spec2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
