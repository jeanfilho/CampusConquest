package de.tum.socialcomp.android.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Formatter;
import java.util.Locale;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpGetter;

/**
 * This class represents the (personal) per-match statistics page
 * Contains stats such as Time Played, Match Score, Kills, Deaths, etc
 *
 * Created by xXxJonasxXx on 21/05/2015.
 */
public class StatisticsSectionFragment extends Fragment{
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_section_statistics, container, false);

        String[] stats = new String[7];

        //dummy parser
        for(int i =0; i<7;i++)
        {
            stats[i]="Dis is da row #"+i+"!";
        }

        //formatting string from: http://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);

        //grab this user's stats from the server, format if necessary and paste into stats array
        try {
            HttpGetter request = new HttpGetter();
            request.execute(new String[]{"users", MainActivity.facebookID, "getPlayerStats"});
            String requestResult = request.get();
            if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
                JSONObject jsonStats = new JSONObject(requestResult);
                JSONObject playtime = jsonStats.getJSONObject("Playtime");
                formatter.format("%d:%02d:%02d",playtime.getInt("Hours"),
                        playtime.getInt("Minutes"),
                        playtime.getInt("Seconds"));
                stats[0] = sb.toString();
                stats[1] = "" + jsonStats.getInt("score");
                stats[2] = "" + jsonStats.getInt("kills");
                stats[3] = "" + jsonStats.getInt("deaths");
                stats[4] = "" + jsonStats.getInt("captures");
                int facID = jsonStats.getInt("faculty");
                if(facID >= 0 && facID < MainActivity.numberOfFaculties){
                    stats[5] = MainActivity.allFaculties[facID];
                }else{
                    stats[5] = "None";
                }
                stats[6] = "" + jsonStats.getInt("polygons");
            }
        }catch(Exception e){
            Log.e("PlayerStats Exception: ", e.getMessage());
        }

        //put each stat into the respective text view on the fragment
        ((TextView) rootView.findViewById(R.id.playtime_val)).setText(stats[0]);
        ((TextView) rootView.findViewById(R.id.score_val)).setText(stats[1]);
        ((TextView) rootView.findViewById(R.id.kills_val)).setText(stats[2]);
        ((TextView) rootView.findViewById(R.id.deaths_val)).setText(stats[3]);
        ((TextView) rootView.findViewById(R.id.captured_val)).setText(stats[4]);
        ((TextView) rootView.findViewById(R.id.faculty_val)).setText(stats[5]);
        ((TextView) rootView.findViewById(R.id.poligons_val)).setText(stats[6]);
        MainActivity.menu.clear();
        MainActivity.getInstance().getMenuInflater().inflate(R.menu.misc, MainActivity.menu);
        return rootView;
    }

}
