/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.download.frag;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.activity.MainActivity;
import com.abhishek.Videogy.download.Tracking;
import com.abhishek.Videogy.model.VDFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Downloads extends VDFragment implements MainActivity.OnBackPressedListener, Tracking, DownloadsInProgress.OnNumDownloadsInProgressChangeListener,
        DownloadsCompleted.OnNumDownloadsCompletedChangeListener, DownloadsInProgress.OnAddDownloadedVideoToCompletedListener {
    private static final String PROGRESS = "downloadsInProgress";
    private final Activity activity;
    private View view;
    private Handler mainHandler;
    private Tracking tracking;
    private ViewPager pager;
    private DownloadsInProgress downloadsInProgress;

    public Downloads(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onDestroy() {
        Fragment fragment;
        if ((fragment = getFragmentManager().findFragmentByTag(PROGRESS)) != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onDestroy();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.downloads, container, false);

            AdView mAdView = view.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            getVDActivity().setOnBackPressedListener(this);

            mainHandler = new Handler(Looper.getMainLooper());
            tracking = new Tracking();

            pager = view.findViewById(R.id.progress_pager);
            pager.setAdapter(new PagerAdapter());

            downloadsInProgress = new DownloadsInProgress(activity);

            downloadsInProgress.setOnNumDownloadsInProgressChangeListener(this);

            getFragmentManager().beginTransaction().add(pager.getId(), downloadsInProgress,
                    PROGRESS).commit();

            downloadsInProgress.setTracking(this);

            downloadsInProgress.setOnAddDownloadedVideoToCompletedListener(this);

        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pager.setCurrentItem(0);
    }

    @Override
    public void onBackpressed() {
        getVDActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onNumDownloadsInProgressChange() {
        //nada
    }

    @Override
    public void onNumDownloadsCompletedChange() {
        //nada
    }

    @Override
    public void onAddDownloadedVideoToCompleted(String name, String type) {
        //nada
    }

    public void startTracking() {
        getActivity().runOnUiThread(tracking);
    }

    public void stopTracking() {
        mainHandler.removeCallbacks(tracking);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getFragmentManager().findFragmentByTag(PROGRESS) != null) {
                    downloadsInProgress.updateDownloadItem();
                }
            }
        });
    }

    class Tracking implements Runnable {

        @Override
        public void run() {

            if (getFragmentManager() != null && getFragmentManager().findFragmentByTag
                    (PROGRESS) != null) {
                downloadsInProgress.updateDownloadItem();
            }
            mainHandler.postDelayed(this, 1000);
        }
    }

    class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return downloadsInProgress;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //nada
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }
    }
}
