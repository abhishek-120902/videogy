/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.adapter.GuideAdapter;
import com.abhishek.Videogy.model.GuideModel;
import com.sinaseyfi.advancedcardview.AdvancedCardView;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;


public class GuideActivity extends AppCompatActivity {

    private ViewPager2 pager;
    private AdvancedCardView back;
    private Button next;
    ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            switch (position) {
                case 0:
                    back.setVisibility(View.GONE);
                    next.setText(getResources().getString(R.string.next));
                    break;
                case 2:
                    next.setText(getResources().getString(R.string.got_it));
                    break;
                default:
                    back.setVisibility(View.VISIBLE);
                    next.setText(getResources().getString(R.string.next));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide);

        back = findViewById(R.id.info_back);
        next = findViewById(R.id.info_next);
        ImageView close = findViewById(R.id.close);

        pager = findViewById(R.id.guide_pager);
        DotsIndicator indicator = findViewById(R.id.dots_indicator);

        GuideAdapter adapter = new GuideAdapter(this, getList());
        pager.setAdapter(adapter);

        indicator.setViewPager2(pager);
        pager.registerOnPageChangeCallback(callback);

        back.setOnClickListener(view -> pager.setCurrentItem(pager.getCurrentItem() - 1));

        next.setOnClickListener(view -> {
            if (pager.getCurrentItem() != 2) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                onBackPressed();
            }
        });

        close.setOnClickListener(view -> onBackPressed());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pager.unregisterOnPageChangeCallback(callback);
    }

    private List<GuideModel> getList() {
        List<GuideModel> list = new ArrayList<>();
        list.add(new GuideModel("1", "Go to website", R.drawable.info_one));
        list.add(new GuideModel("2", "Play the video", R.drawable.info_two));
        list.add(new GuideModel("3", "Click the download button", R.drawable.info_three));
        return list;
    }

}