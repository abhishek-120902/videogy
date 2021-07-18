/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.VDApp;
import com.abhishek.Videogy.browser.BrowserManager;
import com.abhishek.Videogy.download.frag.Downloads;
import com.abhishek.Videogy.download.frag.DownloadsC;
import com.abhishek.Videogy.model.WebConnect;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String DOWNLOAD = "Downloads";
    private static final String HISTORY = "History";
    private static final String SETTING = "Settings";
    private EditText searchTextBar;
    private ImageView btnSearchCancel;
    private BrowserManager browserManager;
    private Uri appLinkData;
    private FragmentManager manager;
    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                homeClicked();
                return true;
            case R.id.navigation_downloads:
                downloadClicked();
                return true;
            case R.id.navigation_history:
                historyClicked();
                return true;
            default:
                break;
        }
        return false;
    };
    private BottomNavigationView navView;
    private ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = this.getSupportFragmentManager();
        // This is for creating browser manager fragment
        if ((browserManager = (BrowserManager) this.getSupportFragmentManager().findFragmentByTag("BM")) == null) {
            browserManager = new BrowserManager(MainActivity.this);

            manager.beginTransaction()
                    .add(browserManager, "BM").commit();
        }

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        appLinkData = appLinkIntent.getData();

        // Bottom navigation
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        ImageView instagram = findViewById(R.id.instagram_btn);
        ImageView facebook = findViewById(R.id.fb_btn);
        ImageView youtube = findViewById(R.id.yt_btn);
        ImageView twitter = findViewById(R.id.twitter_btn);
        ImageView reddit = findViewById(R.id.reddit_btn);
        ImageView tumblr = findViewById(R.id.tumblr_btn);
        ImageView tiktok = findViewById(R.id.tiktok_btn);


        instagram.setOnClickListener(view -> browserManager.newWindow("https://www.instagram.com/"));

        facebook.setOnClickListener(view -> browserManager.newWindow("https://www.facebook.com/"));

        youtube.setOnClickListener(view -> browserManager.newWindow("https://www.youtube.com/"));

        twitter.setOnClickListener(view -> browserManager.newWindow("https://www.twitter.com/"));

        reddit.setOnClickListener(view -> browserManager.newWindow("https://www.reddit.com/"));

        tumblr.setOnClickListener(view -> browserManager.newWindow("https://www.tumblr.com/"));

        tiktok.setOnClickListener(view -> browserManager.newWindow("https://www.tiktok.com/"));

        Button guide = findViewById(R.id.start_guide);
        guide.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, GuideActivity.class);
            startActivity(intent);
        });

        setUPBrowserToolbarView();
    }

    private void setUPBrowserToolbarView() {

        // Toolbar search
        btnSearchCancel = findViewById(R.id.btn_search_cancel);
        btnSearchCancel.setOnClickListener(this);
        ImageView btnSearch = findViewById(R.id.btn_search);
        searchTextBar = findViewById(R.id.et_search_bar);

        /*hide/show clear button in search view*/
        TextWatcher searchViewTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    btnSearchCancel.setVisibility(View.GONE);
                } else {
                    btnSearchCancel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nada
            }

            @Override
            public void afterTextChanged(Editable s) {
                //nada
            }
        };
        searchTextBar.addTextChangedListener(searchViewTextWatcher);
        searchTextBar.setOnEditorActionListener(this);
        btnSearch.setOnClickListener(this);

        //Toolbar home button
        ImageView toolbarHome = findViewById(R.id.btn_home);
        toolbarHome.setOnClickListener(this);

        //Settings
        ImageView settings = findViewById(R.id.btn_settings);
        settings.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_search_cancel:
                searchTextBar.getText().clear();
                break;
            case R.id.btn_home:
                searchTextBar.getText().clear();
                getBrowserManager().closeAllWindow();
                break;
            case R.id.btn_settings:
                settingsClicked();
                break;
            case R.id.btn_search:
                new WebConnect(searchTextBar, this).connect();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_GO) {
            new WebConnect(searchTextBar, this).connect();
        }
        return handled;
    }

    @Override
    public void onBackPressed() {
        if (manager.findFragmentByTag(DOWNLOAD) != null ||
                manager.findFragmentByTag(HISTORY) != null) {
            VDApp.getInstance().getOnBackPressedListener().onBackpressed();
            browserManager.resumeCurrentWindow();
            navView.setSelectedItemId(R.id.navigation_home);
        } else if (manager.findFragmentByTag(SETTING) != null) {
            VDApp.getInstance().getOnBackPressedListener().onBackpressed();
            browserManager.resumeCurrentWindow();
            navView.setVisibility(View.VISIBLE);
        } else if (VDApp.getInstance().getOnBackPressedListener() != null) {
            VDApp.getInstance().getOnBackPressedListener().onBackpressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("YES", (dialog, which) -> MainActivity.super.onBackPressed())
                    .setNegativeButton("NO", (dialog, which) -> {
                        //nada
                    })
                    .create()
                    .show();
        }
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        VDApp.getInstance().setOnBackPressedListener(onBackPressedListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appLinkData != null) {
            browserManager.newWindow(appLinkData.toString());
        }
    }

    public void browserClicked() {
        browserManager.unhideCurrentWindow();
    }

    public void downloadClicked() {
        closeHistory();
        if (manager.findFragmentByTag(DOWNLOAD) == null) {
            browserManager.hideCurrentWindow();
            browserManager.pauseCurrentWindow();
            manager.beginTransaction().add(R.id.main_content, new DownloadsC(), DOWNLOAD).commit();
        }
    }


    public void historyClicked() {
        closeDownloads();
        if (manager.findFragmentByTag(HISTORY) == null) {
            browserManager.hideCurrentWindow();
            browserManager.pauseCurrentWindow();
            manager.beginTransaction().add(R.id.main_content, new Downloads(MainActivity.this), HISTORY).commit();
        }
    }

    private void settingsClicked() {
        if (manager.findFragmentByTag(SETTING) == null) {
            browserManager.hideCurrentWindow();
            browserManager.pauseCurrentWindow();
            navView.setVisibility(View.GONE);
            manager.beginTransaction().add(R.id.main_content, new Settings(), SETTING).commit();
        }
    }

    public void homeClicked() {
        browserManager.unhideCurrentWindow();
        browserManager.resumeCurrentWindow();
        closeDownloads();
        closeHistory();
    }

    private void closeDownloads() {
        Fragment fragment = manager.findFragmentByTag(DOWNLOAD);
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
    }

    private void closeHistory() {
        Fragment fragment = manager.findFragmentByTag(HISTORY);
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }

    public void setOnRequestPermissionsResultListener(ActivityCompat
                                                              .OnRequestPermissionsResultCallback
                                                              onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }

    public interface OnBackPressedListener {
        void onBackpressed();
    }

}
