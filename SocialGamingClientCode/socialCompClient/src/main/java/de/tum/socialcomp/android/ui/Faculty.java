package de.tum.socialcomp.android.ui;

import android.graphics.Color;

import de.tum.socialcomp.android.R;

/**
 * Created by Jonas on 25/06/2015.
 */
public class Faculty {

    /*
    * Class basically only there to get color and strings values for a certain faculty ID
    * */

    int[] enemyDrawables = new int[6];
    int[] teamDrawables = new int[6];
    int[] myDrawables = new int[6];

    public Faculty()
    {
        enemyDrawables[0] =R.drawable.in_enemy;
        enemyDrawables[1] =R.drawable.ch_enemy;
        enemyDrawables[2] = R.drawable.mw_enemy;
        enemyDrawables[3] = R.drawable.ph_enemy;
        enemyDrawables[4] =R.drawable.ma_enemy;

        teamDrawables[0] =R.drawable.in_team;
        teamDrawables[1] =R.drawable.ch_team;
        teamDrawables[2] = R.drawable.mw_team;
        teamDrawables[3] = R.drawable.ph_team;
        teamDrawables[4] =R.drawable.ma_team;

        myDrawables[0] =R.drawable.in_player;
        myDrawables[1] =R.drawable.ch_player;
        myDrawables[2] = R.drawable.mw_player;
        myDrawables[3] = R.drawable.ph_player;
        myDrawables[4] =R.drawable.ma_player;
    }
    //TODO: Suggestion: make these methods static, since the class is not used to actually represent a faculty, just to grab info from essentially somewhere else

    /**
     *
     * @param facID
     * @return
     */
    public int getFacColor(int facID)
    {
        int color;
        switch(facID) {
			/*FacultyID definition in server code:
			* 0 Informatik
			* 1 Chemie
			* 2 Maschinenbau
			* 3 Physik
			* 4 Mathematik
			* */
            //if -1, means make it free/unoccupied
            case -1:
                color = R.color.TUM_blue_light_trans;
                break;
            case 0:
                color = R.color.in_shit;
                break;
            case 1:
                color = R.color.ch_green;
                break;
            case 2:
                color = R.color.mw_gray;
                break;
            case 3:
                color = R.color.ph_orange;
                break;
            case 4:
                color = R.color.ma_green;
                break;
            default:
                color = Color.RED;
                break;
        }
        return color;
    }

    /**
     *
     * @param facID
     * @return
     */
    public int getFacColor_trans(int facID)
    {
        int color;
        switch(facID) {
			/*FacultyID definition in server code:
			* 0 Informatik
			* 1 Chemie
			* 2 Maschinenbau
			* 3 Physik
			* 4 Mathematik
			* */
            //if -1, means make it free/unoccupied
            case -1:
                color = R.color.neutral_white;
                break;
            case 0:
                color = R.color.in_shit_trans;
                break;
            case 1:
                color = R.color.ch_green_trans;
                break;
            case 2:
                color = R.color.mw_gray_trans;
                break;
            case 3:
                color = R.color.ph_orange_trans;
                break;
            case 4:
                color = R.color.ma_green_trans;
                break;
            default:
                color = Color.RED;
                break;
        }
        return color;
    }

    /**
     *
     * @param facID
     * @return
     */
    public String getName(int facID)
    {
        String name;
        switch(facID) {
			/*FacultyID definition in server code:
			* 0 Informatik
			* 1 Chemie
			* 2 Maschinenbau
			* 3 Physik
			* 4 Mathematik
			* */
            //if -1, means make it free/unoccupied
            case -1:
                name = "Free";
                break;
            case 0:
                name = "Informatik";
                break;
            case 1:
                name = "Chemie";
                break;
            case 2:
                name = "Maschinenbau";
                break;
            case 3:
                name = "Physik";
                break;
            case 4:
                name = "Mathematik";
                break;
            default:
                name = "Error";
                break;
        }
        return name;
    }

    /**
     *
     * @param facID
     * @return
     */
    public int getMyDrawable(int facID)
    {
        if(facID>=0 && facID <=5)
            return myDrawables[facID];
        else
            return -1;
    }

    /**
     *
     * @param facID
     * @return
     */
    public int getTeamDrawable(int facID)
    {
        if(facID>=0 && facID <=5)
            return teamDrawables[facID];
        else
            return -1;
    }

    /**
     *
     * @param facID
     * @return
     */
    public int getEnemyDrawable(int facID)
    {
        if(facID>=0 && facID <=5)
            return enemyDrawables[facID];
        else
            return -1;
    }

}
