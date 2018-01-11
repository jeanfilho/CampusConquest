package de.tum.socialcomp.android.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
/**
 * This class represents (or, as of 15/07/15, represented) the Settings fragment, where a player could configure upload and download frequency as well as active/inactive state
 * Frequency is now set constant and active/inactive should not be in player control
 *
 * Created by xXxJonasxXx on 18/05/2015.
 */
public class SettingsSectionFragment extends Fragment {

    View rootView;
    private static int barVal_refreshrate_game = 0;
    private static int barVal_refreshrate_stats = 0;
    private static int game_refreshrate = 10;
    private static int stats_refreshrate = 10;
    private static final int minrefreshrate = 1;
    private boolean online;
    private Settings settings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.settings_jonas, container, false);
        settings = Settings.getInstance();

        /* removed settings because they arent used right now
        * Lets keep the code in case we need it in the future*/
        /*
        updateGameRefreshrateText();
        updateStatsRefreshrateText();


        //gamerefreshrate
        ((SeekBar)rootView.findViewById(R.id.seekBar_game)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                barVal_refreshrate_game = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateGameRefreshrateText();
            }
        });

        //statsrefresh
        ((SeekBar)rootView.findViewById(R.id.seekBar_stats)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                barVal_refreshrate_stats = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateStatsRefreshrateText();
            }
        });

        //"save" new settings into settings object
        rootView.findViewById(R.id.settings_apply).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        applySettings();

                    }
                });

        ((ToggleButton)rootView.findViewById(R.id.toggleButton)).setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                online = isChecked;
            }
        });
        */

        MainActivity.menu.clear();
        MainActivity.getInstance().getMenuInflater().inflate(R.menu.misc, MainActivity.menu);
        return rootView;
    }

    /*
    private void updateGameRefreshrateText()
    {
        game_refreshrate = (int)(minrefreshrate + barVal_refreshrate_game);
        if(game_refreshrate <= 0)
            ((TextView) rootView.findViewById(R.id.refresh_num_game)).setText("Manual");
        else
            ((TextView) rootView.findViewById(R.id.refresh_num_game)).setText("" + game_refreshrate+" Htz");
    }

    //
    private void updateStatsRefreshrateText()
    {
        stats_refreshrate = (int)(minrefreshrate + barVal_refreshrate_stats);
        if(stats_refreshrate <= 0)
            ((TextView) rootView.findViewById(R.id.refresh_num_stats)).setText("Manual");
        else
            ((TextView) rootView.findViewById(R.id.refresh_num_stats)).setText("" + stats_refreshrate+" Htz");
    }


    //"save" new settings into settings object and update refresh var in main activity (for use in up/download threads)
    private void applySettings()
    {
        settings.setOnline(online);
        settings.setRefreshdate_game(game_refreshrate);
        settings.setRefreshrate_stats(stats_refreshrate);

        MainActivity.game_refreshRate = game_refreshrate;
    }
    */
}
