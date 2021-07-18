/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.model;

import androidx.fragment.app.Fragment;

import com.abhishek.Videogy.VDApp;
import com.abhishek.Videogy.activity.MainActivity;

public class VDFragment extends Fragment {

    public MainActivity getVDActivity() {
        return (MainActivity) getActivity();
    }

    public VDApp getVDApp() {
        return (VDApp) getActivity().getApplication();
    }
}