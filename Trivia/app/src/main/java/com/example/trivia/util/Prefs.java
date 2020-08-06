package com.example.trivia.util;

import android.app.Activity;
import android.content.SharedPreferences;

public class Prefs {

    private SharedPreferences prefrence;

    public Prefs(Activity activity) {
        this.prefrence = activity.getPreferences(activity.MODE_PRIVATE);
    }

    public void saveHighScore(int currScore) {
        int prevHighScore = prefrence.getInt("score", 0);

        if(currScore > prevHighScore) {
            prefrence.edit().putInt("score", currScore).apply();
        }
    }

    public int getHighScore(){
        return prefrence.getInt("score", 0);
    }
}
