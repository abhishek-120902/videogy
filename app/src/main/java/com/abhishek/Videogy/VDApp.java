/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy;

import android.app.Application;
import android.content.Intent;

import com.abhishek.Videogy.activity.MainActivity;
import com.abhishek.Videogy.download.DownloadManager;
import com.google.android.gms.ads.MobileAds;

public class VDApp extends Application {
    private static VDApp instance;
    private Intent downloadService;
    private MainActivity.OnBackPressedListener onBackPressedListener;

    public static VDApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MobileAds.initialize(this);
        downloadService = new Intent(getApplicationContext(), DownloadManager.class);
    }

    public Intent getDownloadService() {
        return downloadService;
    }

    public MainActivity.OnBackPressedListener getOnBackPressedListener() {
        return onBackPressedListener;
    }

    public void setOnBackPressedListener(MainActivity.OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }
}
