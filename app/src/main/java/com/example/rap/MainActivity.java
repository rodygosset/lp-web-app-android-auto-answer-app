package com.example.rap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    public boolean isPermissionGranted() {
        // Return true if user has given his permission to read incoming message
        return (
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        );
    }

    public void requestSMSPermission() {
        // Ask the user for necessary permissions
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_SMS)) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_SMS }, 0);
        }
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.SEND_SMS }, 0);;
        }
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_CONTACTS }, 0);
        }
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.RECEIVE_SMS }, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If permissions are not already granted, ask the user for permission to read/receive messages

        if(!isPermissionGranted()) {
            requestSMSPermission();
        }

        ViewPager viewPager = findViewById(R.id.activity_main_viewpager);

        // Instanciate the adapter

        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        // Bind the Adapter to the viewPager

        viewPager.setAdapter(pagerAdapter);

        // Bind the Tab bar to the viewpager

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }
}