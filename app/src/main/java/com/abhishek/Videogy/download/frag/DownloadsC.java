/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.download.frag;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.activity.MainActivity;
import com.abhishek.Videogy.model.VDFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class DownloadsC extends VDFragment implements MainActivity.OnBackPressedListener, DownloadsInProgress.OnNumDownloadsInProgressChangeListener,
        DownloadsCompleted.OnNumDownloadsCompletedChangeListener, DownloadsInProgress.OnAddDownloadedVideoToCompletedListener {
    private static final String COMPLETE = "downloadsCompleted";
    private View view;

    private ViewPager pager;
    private DownloadsCompleted downloadsComplete;

    @Override
    public void onDestroy() {
        Fragment fragment;
        if ((fragment = getFragmentManager().findFragmentByTag(COMPLETE)) != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onDestroy();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.downloads_c, container, false);

            AdView mAdView = view.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            getVDActivity().setOnBackPressedListener(this);

            pager = view.findViewById(R.id.progress_pager);
            pager.setAdapter(new PagerAdapter());

            downloadsComplete = new DownloadsCompleted();

            downloadsComplete.setOnNumDownloadsCompletedChangeListener(this);

            Toolbar toolbar = view.findViewById(R.id.download_toolbar);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_all_action) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(getResources().getString(R.string.empty_download_list))
                            .setNegativeButton("Ok", (dialog, which) -> {
                                deleteAll();
                                downloadsComplete.getAdapter().updateList();
                            })
                            .create()
                            .show();
                }
                return true;
            });

            getFragmentManager().beginTransaction().add(pager.getId(), downloadsComplete,
                    COMPLETE).commit();

        }

        return view;
    }

    private void deleteAll() {
        File dir = Environment.getExternalStoragePublicDirectory(getString(R.string.app_name));
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (new File(dir, child).delete()) {
                    //nada
                }

            }
        }
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
        Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
    }


    class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return downloadsComplete;
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
