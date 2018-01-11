package de.tum.socialcomp.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import de.tum.socialcomp.android.R;

/**
 * This technically represents the Splash/Loading screen
 * Obviously does nothing logic wise
 *
 * Created by Jonas on 04/07/2015.
 */

public class SplashSectionFragment extends Fragment {
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.splash, container, false);
        return rootView;
    }
}
