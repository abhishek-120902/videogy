/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.download.list;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompletedVideos implements Serializable {
    private final List<String> videos;

    public CompletedVideos() {
        videos = new ArrayList<>();
    }

    public static CompletedVideos load(Context context) {
        CompletedVideos completedVideos = new CompletedVideos();
        File file = new File(context.getFilesDir(), "completed.dat");
        Log.d("surabhi", "load: " + context.getFilesDir());
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.close();
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                objectInputStream.close();
                completedVideos = (CompletedVideos) objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                //nada
            }
        }
        return completedVideos;
    }

    public void addVideo(Context context, String name) {
        videos.add(0, name);
        save(context);
    }

    public List<String> getVideos() {
        return videos;
    }

    public void save(Context context) {
        try {
            File file = new File(context.getFilesDir(), "completed.dat");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.close();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.close();
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            //nada
        }
    }
}
