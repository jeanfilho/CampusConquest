package de.tum.socialcomp.android.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpGetter;

/**
 * This is class represents the Team Scoreboard fragment
 *
 * @author Michael Sailer
 * @author Jonas Mayer
 * @author Paul Preissner
 */

public class TeamSectionFragment extends Fragment {

    View rootView;
    Faculty fac;
    private MainActivity mainAct = MainActivity.getInstance();
    private TableRow rows[] = new TableRow[4];
    private ImageView[] facultySymbols = new ImageView[5];
    private ImageView[] facultySymbols2 = new ImageView[5];

    private TableRow top_player_rows[] = new TableRow[10];

    // faculty bitmaps for topPlayers
    private Drawable facultyDrawables[] = new Drawable[6];
    private int sizeOfFacultyBitmaps = 30;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.team_paul, container, false);
        fac = new Faculty();

        //all text views for one "category" are in one row, and without any empty or non text fields in between each other
        //thus, when parsing/pasting the JSON array, we can access each next text view through an iterative loop
        rows[0] = (TableRow) rootView.findViewById(R.id.score_row);
        rows[1] = (TableRow) rootView.findViewById(R.id.reg_row);
        rows[2] = (TableRow) rootView.findViewById(R.id.act_row);
        rows[3] = (TableRow) rootView.findViewById(R.id.ded_row);

        // get top_player_rows;
        top_player_rows[0] = (TableRow) rootView.findViewById(R.id.top_players_row_1);
        top_player_rows[1] = (TableRow) rootView.findViewById(R.id.top_players_row_2);
        top_player_rows[2] = (TableRow) rootView.findViewById(R.id.top_players_row_3);
        top_player_rows[3] = (TableRow) rootView.findViewById(R.id.top_players_row_4);
        top_player_rows[4] = (TableRow) rootView.findViewById(R.id.top_players_row_5);
        top_player_rows[5] = (TableRow) rootView.findViewById(R.id.top_players_row_6);
        top_player_rows[6] = (TableRow) rootView.findViewById(R.id.top_players_row_7);
        top_player_rows[7] = (TableRow) rootView.findViewById(R.id.top_players_row_8);
        top_player_rows[8] = (TableRow) rootView.findViewById(R.id.top_players_row_9);
        top_player_rows[9] = (TableRow) rootView.findViewById(R.id.top_players_row_10);

        facultySymbols[0] = (ImageView) rootView.findViewById(R.id.in_icon);
        facultySymbols[1] = (ImageView) rootView.findViewById(R.id.ch_icon);
        facultySymbols[2] = (ImageView) rootView.findViewById(R.id.mw_icon);
        facultySymbols[3] = (ImageView) rootView.findViewById(R.id.ph_icon);
        facultySymbols[4] = (ImageView) rootView.findViewById(R.id.ma_icon);

        facultySymbols2[0] = (ImageView) rootView.findViewById(R.id.in_icon2);
        facultySymbols2[1] = (ImageView) rootView.findViewById(R.id.ch_icon2);
        facultySymbols2[2] = (ImageView) rootView.findViewById(R.id.mw_icon2);
        facultySymbols2[3] = (ImageView) rootView.findViewById(R.id.ph_icon2);
        facultySymbols2[4] = (ImageView) rootView.findViewById(R.id.ma_icon2);

        /*Initialize faculty bitmaps*/
        for(int i =0; i<5; i++)
        {
            facultyDrawables[i]=getResources().getDrawable(fac.getEnemyDrawable(i));
        }
        facultyDrawables[5] = getResources().getDrawable(R.drawable.feggit);
        int facID = MainActivity.getInstance().facultyID;
        if(facID>=0 && facID<=5)
        {
            facultyDrawables[facID]=getResources().getDrawable(fac.getTeamDrawable(facID));
            facultySymbols[facID].setImageDrawable(getResources().getDrawable(fac.getMyDrawable(facID)));
            facultySymbols2[facID].setImageDrawable(getResources().getDrawable(fac.getMyDrawable(facID)));
        }

        /*GET getFacultyStats,
        * then interpret the JSON object,
        * i.e. readout of player count data etc per faculty etc etc
        * then enter said data to  R.id.the different textViews*/
        Log.i(this.getClass().getName(), "Trying to GET TeamScoreboard...");
        HttpGetter request = new HttpGetter();
        request.execute(new String[]{"game", "getTeamScoreboard"});
        try {
            String requestResult = request.get();
            // if we just received an empty json, ignore
            if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
                JSONObject jsonTeamScoreboard = new JSONObject(requestResult);
                // get faculty stats
                JSONArray jsonFacultyStats = jsonTeamScoreboard.getJSONArray("Faculties");
                for(int i = 0; i < jsonFacultyStats.length(); i++){
                    JSONObject faculty = jsonFacultyStats.getJSONObject(i);
                    /*need to define column cause the elements of
                    * the jsonArray arent given given in the right order*/
                    int id = faculty.getInt("id");
                    int column;
                    switch(id){
                        case 0: column = 1;
                            break;
                        case 1: column = 3;
                            break;
                        case 2: column = 2;
                            break;
                        case 3: column = 5;
                            break;
                        case 4: column = 4;
                            break;
                        // the default case is primarily for the players without faculty
                        default: column = -1;
                            break;
                    }
                    if(column != -1) {
                        ((TextView) rows[0].getChildAt(column)).setText(faculty.getInt("score") + ""); //score
                        ((TextView) rows[1].getChildAt(column)).setText(faculty.getInt("players_registered") + ""); //registered players
                        ((TextView) rows[2].getChildAt(column)).setText(faculty.getInt("players_active") + ""); //active players
                        ((TextView) rows[3].getChildAt(column)).setText(faculty.getInt("players_dead") + ""); //dead players
                    }
                }
                // get top players
                JSONObject jsonTopPlayers = jsonTeamScoreboard.getJSONObject("TopPlayers");
                JSONArray  jsonScoreboard = jsonTopPlayers.getJSONArray("scoreboard");
                JSONObject topPlayer;
                int numberOpTopPlayers = jsonTopPlayers.getInt("numberOfTopPlayers");
                for(int i = 0; i < numberOpTopPlayers; i++){
                    topPlayer = jsonScoreboard.getJSONObject(i);
                    ((TextView) top_player_rows[i].getChildAt(1)).setText(topPlayer.getString("name"));
                    int score = topPlayer.getInt("score");
                    ((TextView) top_player_rows[i].getChildAt(2)).setText("   " + String.valueOf(score) + "   ");
                    int facultyID = topPlayer.getInt("facultyID");
                    if(facultyID > -1 && facultyID < 5){
                        ((ImageView) top_player_rows[i].getChildAt(3)).setImageDrawable(facultyDrawables[facultyID]);
                    }else{
                        ((ImageView) top_player_rows[i].getChildAt(3)).setImageDrawable(facultyDrawables[5]);
                    }
                }
                // remove text from unused rows
                for(int i = numberOpTopPlayers; i < 10; i++){
                    ((TextView) top_player_rows[i].getChildAt(1)).setText("");
                    ((TextView) top_player_rows[i].getChildAt(2)).setText("");
                }
            }
        } catch (Exception e) {
            // various Exceptions can be
            // thrown in the process, for
            // brevity we do a 'catch all'
            Log.e("FacStats Exception: ", e.getMessage());
        }
        MainActivity.menu.clear();
        MainActivity.getInstance().getMenuInflater().inflate(R.menu.misc, MainActivity.menu);
        return rootView;
    }
}