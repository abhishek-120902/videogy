/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.download.frag;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.VDApp;
import com.abhishek.Videogy.adapter.SavedAdapter;
import com.abhishek.Videogy.download.list.CompletedVideos;
import com.abhishek.Videogy.model.VDFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownloadsCompleted extends VDFragment implements DownloadsInProgress.OnAddDownloadedVideoToCompletedListener {

    private View view;
    private RecyclerView downloadsList;
    private List<String> videos;
    private CompletedVideos completedVideos;

    private OnNumDownloadsCompletedChangeListener onNumDownloadsCompletedChangeListener;
    private SavedAdapter adapter;
    private LinearLayout empty;

    public SavedAdapter getAdapter() {
        return adapter;
    }

    public void setOnNumDownloadsCompletedChangeListener(OnNumDownloadsCompletedChangeListener
                                                                 onNumDownloadsCompletedChangeListener) {
        this.onNumDownloadsCompletedChangeListener = onNumDownloadsCompletedChangeListener;
    }

    public int getNumDownloadsCompleted() {
        return videos.size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        videos = new ArrayList<>();
        completedVideos = CompletedVideos.load(getActivity());
        videos = completedVideos.getVideos();

        if (view == null) {
            view = inflater.inflate(R.layout.downloads_completed, container, false);

            downloadsList = view.findViewById(R.id.downloadsCompletedList);
            Button clearAllFinishedButton = view.findViewById(R.id.clearAllFinishedButton);
            Button goToFolderButton = view.findViewById(R.id.goToFolder);

            empty = view.findViewById(R.id.empty_download);

            Dexter.withContext(getContext())
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {

                            File videoFile = new File(Environment.getExternalStoragePublicDirectory(getContext().getResources().getString(R.string.app_name)).getAbsolutePath());

                            if (videoFile.exists()) {

                                List<File> nonExistentFiles = new ArrayList<>(Arrays.asList(videoFile.listFiles()));

                                if (!nonExistentFiles.isEmpty()) {
                                    empty.setVisibility(View.INVISIBLE);
                                    adapter = new SavedAdapter(nonExistentFiles, getContext());
                                    downloadsList.setAdapter(adapter);
                                    downloadsList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                                    downloadsList.setHasFixedSize(true);
                                } else {
                                    empty.setVisibility(View.VISIBLE);
                                }

                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            //nada
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();


            clearAllFinishedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(getResources().getString(R.string.empty_download_list))
                            .setPositiveButton("Yes", (dialog, which) -> {
                                int length = videos.size();
                                videos.clear();
                                if (completedVideos != null) {
                                    completedVideos.save(getVDActivity());
                                }
                                if (getAdapter() != null) {
                                    getAdapter().notifyItemRangeRemoved(0, length);
                                }
                                onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            });

            goToFolderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File path = Environment.getExternalStoragePublicDirectory(getString(R.string.app_name));
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setDataAndType(Uri.fromFile(path), "video/*");
                    galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(galleryIntent);
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        completedVideos.save(VDApp.getInstance().getApplicationContext());
        onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
    }

    @Override
    public void onAddDownloadedVideoToCompleted(final String name, final String type) {
        if (completedVideos == null) {
            completedVideos = new CompletedVideos();
        }
        completedVideos.addVideo(getActivity(), name + "." + type);
        videos = completedVideos.getVideos();
        if (getAdapter() != null) {
            getAdapter().notifyItemInserted(0);
        }
        onNumDownloadsCompletedChangeListener.onNumDownloadsCompletedChange();
    }

    public interface OnNumDownloadsCompletedChangeListener {
        void onNumDownloadsCompletedChange();
    }


}
