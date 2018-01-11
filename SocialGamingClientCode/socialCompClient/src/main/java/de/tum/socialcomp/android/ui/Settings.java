package de.tum.socialcomp.android.ui;

/**
 * This class served as a container for settings in the Settings Section Fragment
 *
 * Created by xXxJonasxXx on 21/05/2015.
 */
public class Settings {

    private static Settings Instance;

    private Settings()
    {
        refreshdate_game =0;
        refreshrate_stats =0;
        online = true;
    }

    public static Settings getInstance()
    {
        if(Instance == null)
        {
            Instance = new Settings();
        }
        return Instance;
    }



    public int getRefreshdate_game() {
        return refreshdate_game;
    }

    public void setRefreshdate_game(int refreshdate_game) {
        this.refreshdate_game = refreshdate_game;
    }

    private int refreshdate_game;

    public int getRefreshrate_stats() {
        return refreshrate_stats;
    }

    public void setRefreshrate_stats(int refreshrate_stats) {
        this.refreshrate_stats = refreshrate_stats;
    }

    private int refreshrate_stats;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    private boolean online;
}
