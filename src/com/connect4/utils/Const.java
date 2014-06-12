package com.connect4.utils;


import android.os.Environment;

/**
 *
 * @author dev
 */
public class Const {
	
	public static final String LOG_TAG = "Connect4";
    
    public static final String GAME_PREFS = "com.connect4.utils.MySharedPrefs"; 
    
    public static final String GAMESTATUS= "gameStatus"; 
    public static final String GAMESTATUS_NOT_IN_GAME = "NOT in game"; 
    public static final String GAMESTATUS_IN_GAME = "IN game"; 
    
    public static final String CURRENT_PLAYER = "currentPlayer"; 
    public static final String PLAYER_ONE = "Player 1 (Host)"; 
    public static final String PLAYER_TWO = "Player 2 (Guest)"; 
    
    public static final int CREATE_ACTIVITY = 1;
    public static final int JOIN_ACTIVITY = 2;
    public static final int GL_ACTIVITY = 3;
    
    public static final int gridWidth = 7;
    public static final int gridHeight = 6;
    
    
    
    public static final int COMMAND_CONNECTED_TO_SERVER = 10;
    public static final int COMMAND_START_GAME = 11;
    public static final String COMMAND_OPPONENT_COLUMN = "opponentColumnIs_";

}
