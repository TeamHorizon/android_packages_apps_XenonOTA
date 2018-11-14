/*
 * Copyright (C) 2017 Team Horizon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xenonota;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.xenonota.adapters.ViewPagerAdapter;
import com.xenonota.fragments.Fragment_Gapps;
import com.xenonota.fragments.Fragment_OTA;
import com.xenonota.fragments.Fragment_Settings;
import com.xenonota.tasks.BootCompletedReceiver;
import com.xenonota.xml.OTADevice;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navigation;
    ViewPager viewPager;
    MenuItem prevMenuItem;

    private static final int STORAGE_PERMISSION_CODE = 200;

    Fragment_OTA fragment_ota;
    Fragment_Gapps fragment_gapps;
    Fragment_Settings fragment_settings;

    OTADevice deviceFromExtras = null;

    @ColorInt int colorAccent;
    @ColorInt int colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupIndent(getIntent());
        setupTheme();
        setContentView(R.layout.activity_main);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager = findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(mOnPageChangeListener);
        setupViewPager(viewPager);
        checkStoragePermissions();
        initChannels(this);
        final ComponentName onBootReceiver = new ComponentName(getApplication().getPackageName(), BootCompletedReceiver.class.getName());
        if(getPackageManager().getComponentEnabledSetting(onBootReceiver) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            getPackageManager().setComponentEnabledSetting(onBootReceiver,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onNewIntent(Intent intent){
        setupIndent(intent);
        if (viewPager != null && fragment_ota != null) {viewPager.setCurrentItem(0); fragment_ota.deviceFromExtras = deviceFromExtras; fragment_ota.checkDeviceUpdates();}
    }

    @Override public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (name.equals("android.support.v7.view.menu.ListMenuItemView") &&
                parent.getParent() instanceof FrameLayout) {
            ((View) parent.getParent()).setBackgroundColor(colorPrimary);
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    private void setupIndent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null) {
            OTADevice device = (OTADevice) extras.getSerializable("OTADevice");
            if (device != null) deviceFromExtras = device;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);
            }
            else
            {
                navigation.getMenu().getItem(0).setChecked(false);
            }

            switch(position){
                case 0:{
                    fragment_ota.setHasOptionsMenu(true);
                    break;
                }
                case 1:{
                    fragment_gapps.setHasOptionsMenu(true);
                    break;
                }
                case 2:{
                    fragment_settings.setHasOptionsMenu(false);
                    break;
                }
            }

            invalidateOptionsMenu();

            navigation.getMenu().getItem(position).setChecked(true);
            prevMenuItem = navigation.getMenu().getItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void setupTheme() {
        setTheme(android.R.style.Theme_DeviceDefault_Settings);

        TypedValue typedValue_accent = new TypedValue();
        TypedValue typedValue_primary = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue_accent, true);
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue_primary, true);
        colorAccent = typedValue_accent.data;
        colorPrimary = typedValue_primary.data;

        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (getActionBar() != null) getActionBar().hide();

            supportActionBar.setBackgroundDrawable(new ColorDrawable(colorPrimary));
            supportActionBar.getThemedContext().setTheme(android.R.style.Theme_DeviceDefault_Settings);
            Spannable text = new SpannableString(supportActionBar.getTitle());
            text.setSpan(new ForegroundColorSpan(colorAccent), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            supportActionBar.setTitle(text);

            final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, null);
            upArrow.setColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP);
            supportActionBar.setHomeAsUpIndicator(upArrow);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_rom:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.navigation_gapps:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.navigation_settings:
                    viewPager.setCurrentItem(2);
                    break;
            }
            return true;
        }
    };

    private void checkStoragePermissions() {
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(MainActivity.this,  R.string.permission_not_enabled, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }

    public void initChannels(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("xenonota",
                "Downloads",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setDescription("Downloads");
        notificationManager.createNotificationChannel(channel);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        fragment_ota = Fragment_OTA.newInstance();
        fragment_gapps = Fragment_Gapps.newInstance();
        fragment_settings = Fragment_Settings.newInstance();
        adapter.addFragment(fragment_ota);
        adapter.addFragment(fragment_gapps);
        adapter.addFragment(fragment_settings);
        fragment_ota.setHasOptionsMenu(true);
        fragment_ota.deviceFromExtras = deviceFromExtras;
        viewPager.setAdapter(adapter);
        invalidateOptionsMenu();
    }

}
