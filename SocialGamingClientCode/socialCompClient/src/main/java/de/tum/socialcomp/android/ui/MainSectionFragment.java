package de.tum.socialcomp.android.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpPoster;

/**
 * This Fragment is used to start the game, it simply
 * shows one button that triggers a request at the 
 * webservice to start new game.
 *  
 * @author Niklas Klügel
 *
 */

public class MainSectionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

        return rootView;
    }
}

