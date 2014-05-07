package com.connect4.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {
	
	public static String getGameStatus(Context context) {
		
		SharedPreferences prefs = context.getSharedPreferences(Const.GAME_PREFS, 0);
		
		return prefs.getString("gameStatus", Const.GAMESTATUS_NOT_IN_GAME);
	}
	
	public static void setGameStatus(Context context, String gameStatus) {
		SharedPreferences prefs = context.getSharedPreferences(Const.GAME_PREFS, 0);
		SharedPreferences.Editor edit = prefs.edit();
		        
		edit.putString("gameStatus", gameStatus);
		edit.commit();
	}
}
	