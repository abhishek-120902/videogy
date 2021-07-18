/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.model.VDFragment;

public class Settings extends VDFragment implements MainActivity.OnBackPressedListener, View.OnClickListener {
    private View view;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.settings, container, false);

            getVDActivity().setOnBackPressedListener(this);
            final SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);

            //Back
            ImageView btnSettingsBack = view.findViewById(R.id.btn_settings_back);
            btnSettingsBack.setOnClickListener(this);

            // Switch vibrate switch
            Switch vibrateSwitch = view.findViewById(R.id.vibrateSwitch);
            boolean vibrateON = prefs.getBoolean(getString(R.string.vibrateON), true);
            vibrateSwitch.setChecked(vibrateON);
            vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean(getString(R.string.vibrateON), isChecked).commit();
                }
            });

            // Switch sound switch
            Switch soundSwitch = view.findViewById(R.id.soundSwitch);
            boolean soundON = prefs.getBoolean(getString(R.string.soundON), true);
            soundSwitch.setChecked(soundON);
            soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean(getString(R.string.soundON), isChecked).commit();
                }
            });

            // Switch ad blocker switch
            Switch adBlockerSwitch = view.findViewById(R.id.adBlockerSwitch);
            boolean adBlockOn = prefs.getBoolean(getString(R.string.adBlockON), true);
            adBlockerSwitch.setChecked(adBlockOn);
            adBlockerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean(getString(R.string.adBlockON), isChecked).commit();
                }
            });

            view.findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getVDActivity(), HistoryActivity.class);
                    startActivity(intent);
                }
            });

            //Rate app
            TextView btnRateApp = view.findViewById(R.id.rateApp);
            btnRateApp.setOnClickListener(this);

            //Share app
            TextView btnShareApp = view.findViewById(R.id.shareApp);
            btnShareApp.setOnClickListener(this);

            //More app
            TextView btnMoreApp = view.findViewById(R.id.moreApp);
            btnMoreApp.setOnClickListener(this);

            //Privacy Policy
            TextView btnPrivacyPolicy = view.findViewById(R.id.privacyPolicy);
            btnPrivacyPolicy.setOnClickListener(this);

        }
        return view;
    }

    @Override
    public void onBackpressed() {
        getVDActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_settings_back:
                getActivity().onBackPressed();
                break;
            case R.id.rateApp:
                rateApp();
                break;
            case R.id.shareApp:
                shareApp();
                break;
            case R.id.moreApp:
                Toast.makeText(getContext(), "Nothing yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.privacyPolicy:
                privacyPolicyClicked();
                break;
            default:
                break;
        }
    }

    private void privacyPolicyClicked() {
        Toast.makeText(getContext(), "Nothing yet", Toast.LENGTH_SHORT).show();
    }

    public void shareApp() {
        StringBuilder msg = new StringBuilder();
        msg.append(getString(R.string.share_msg));
        msg.append("\n");
        msg.append("https://play.google.com/store/apps/details?id=com.videodownloader.whatsappstatussaver");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getActivity().getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        else
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        intent.addFlags(flags);
        return intent;
    }
}
