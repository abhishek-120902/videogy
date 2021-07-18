/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.model;

import android.util.Patterns;
import android.widget.EditText;

import com.abhishek.Videogy.activity.MainActivity;

public class WebConnect {
    private final EditText textBox;
    private final MainActivity activity;

    public WebConnect(EditText textBox, MainActivity activity) {
        this.textBox = textBox;
        this.activity = activity;
    }

    public void connect() {
        String text = textBox.getText().toString();
        if (Patterns.WEB_URL.matcher(text).matches()) {
            if (!text.startsWith("http")) {
                text = "http://" + text;
            }
            activity.getBrowserManager().newWindow(text);
        } else {
            text = "https://google.com/search?q=" + text;
            activity.getBrowserManager().newWindow(text);
        }
    }
}
