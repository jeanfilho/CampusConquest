package de.tum.socialcomp.android.ui;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.json.JSONObject;
import org.json.JSONArray;

import de.tum.socialcomp.android.Configuration;
import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpGetter;
import de.tum.socialcomp.android.webservices.util.HttpPoster;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;


/**
 * This class represents the faculty selection fragment.
 */
public class FacultySectionFragment extends Fragment {

    /* Essentially what this whole fragment does is let the user choose their faculty.
    upon confirmation, sends that choice to the server and switches to the map fragment
    * */

     View rootView;

    private int selectedFacultyID = -1; // ID of the currently picked faculty
    private MainActivity mainAct;
    private String facebookID;
    private ImageView[] facultySymbols = new ImageView[5];
    private Drawable facultyDrawables[] = new Drawable[5];
    Faculty fac;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.faculty_paul, container, false);

        mainAct = MainActivity.getInstance();
        facebookID = mainAct.getFacebookID(getActivity());

        //section faculty-Buttons
        /*
        * FacultyID definition in server code:
        * 0 Informatik
        * 1 Chemie
        * 2 Maschinenbau
        * 3 Physik
        * 4 Mathematik
        * */

        //Informatik
        fac = new Faculty();
        facultySymbols[0] = (ImageView) rootView.findViewById(R.id.in_icon);
        facultySymbols[1] = (ImageView) rootView.findViewById(R.id.ch_icon);
        facultySymbols[2] = (ImageView) rootView.findViewById(R.id.mw_icon);
        facultySymbols[3] = (ImageView) rootView.findViewById(R.id.ph_icon);
        facultySymbols[4] = (ImageView) rootView.findViewById(R.id.ma_icon);
        for(int i =0; i<5; i++)
        {
            facultyDrawables[i]=getResources().getDrawable(fac.getEnemyDrawable(i));
        }

        rootView.findViewById(R.id.radioButton0).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedFacultyID = 0;
                        resetSymbols(0);
                    }
                });

        //Maschinenwesen
        rootView.findViewById(R.id.radioButton1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedFacultyID = 2;
                        resetSymbols(2);
                    }
                });

        //Chemie
        rootView.findViewById(R.id.radioButton2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedFacultyID = 1;
                        resetSymbols(1);
                    }
                });

        // Mathematik
        rootView.findViewById(R.id.radioButton3).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedFacultyID = 4;
                        resetSymbols(4);
                    }
                });

        //Physik
        rootView.findViewById(R.id.radioButton4).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedFacultyID = 3;
                        resetSymbols(3);
                    }
                });
        //end section faculty Buttons

        rootView.findViewById(R.id.confirmFacultyBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmChoice(selectedFacultyID, view);
                    }
                });
        MainActivity.menu.clear();
        mainAct.getMenuInflater().inflate(R.menu.misc, MainActivity.menu);
        return rootView;
    }

    /**
     *
     * @param facultyID
     * @param v
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void confirmChoice(int facultyID, View v) {
        /*Here, set as player's faculty (get userID, then POST enterFacultyIfNone)
        * lock Confirm button so no other changes are possible anymore*/
        ((Button) rootView.findViewById(R.id.confirmFacultyBtn)).setEnabled(false);

        if(v.getId() == R.id.confirmFacultyBtn) {
            // This was only needed for debugging purposes.
            //((Button) rootView.findViewById(R.id.confirmFacultyBtn)).setText("Choice: " + facultyID);

            // Save faculty to Server
            if(facultyID != -1){
                new HttpPoster().execute(new String[] {
                    "users", this.facebookID,
                    facultyID + "", "enterFacultyIfNone" });
            }
            else{
                ((Button) rootView.findViewById(R.id.confirmFacultyBtn)).setText("You are a feggit.");
            }
            MainActivity.facultyID = facultyID;

            /*if failure, let them retry? or throw error?*/
            Log.i(this.getClass().getName(), "Switching to class fragment...");
            //getChildFragmentManager().beginTransaction().replace(R.id.MainFrameLayout, MainActivity.getInstance().mapsectionfrag);
            MainActivity.getInstance().getActionBar().setTitle("Choose your Class");
            MainActivity.getInstance().switchToFragment(MainActivity.getInstance().classsectionfrag);
        }

    }

    /**
     *
     * @param facID
     */
    private void resetSymbols(int facID)
    {
        for(int i =0; i<5; i++) {
            facultySymbols[i].setImageDrawable(facultyDrawables[i]);
        }
        facultySymbols[facID].setImageResource(fac.getMyDrawable(facID));
    }
}