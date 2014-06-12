package com.connect4.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {
	
	/*
	 * Getter/Setter for GAME STATUS
	 */
	
	public static String getGameStatus(Context context) {
		
		SharedPreferences prefs = context.getSharedPreferences(Const.GAME_PREFS, 0);
		
		return prefs.getString(Const.GAMESTATUS, Const.GAMESTATUS_NOT_IN_GAME);
	}
	
	public static void setGameStatus(Context context, String gameStatus) {
		SharedPreferences prefs = context.getSharedPreferences(Const.GAME_PREFS, 0);
		SharedPreferences.Editor edit = prefs.edit();
		        
		edit.putString(Const.GAMESTATUS, gameStatus);
		edit.commit();
	}
	
	/*
	 * Getter/Setter PLAYERN TURN
	 */
	
	public static String getCurrentPlayer(Context context) {
		
		SharedPreferences prefs = context.getSharedPreferences(Const.GAME_PREFS, 0);
		
		return prefs.getString(Const.CURRENT_PLAYER, Const.CURRENT_PLAYER);
	}
	
	public static void setCurrentPlayer(Context context, String player) {
		SharedPreferences prefs = context.getSharedPreferences(Const.GAME_PREFS, 0);
		SharedPreferences.Editor edit = prefs.edit();
		        
		edit.putString(Const.CURRENT_PLAYER, player);
		edit.commit();
	}
	
}
	