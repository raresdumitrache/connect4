package com.connect4.main;

import com.connect4.utils.Const;
import com.connect4.utils.Utils;

import android.R;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity {

	Button createGameButton, joinGameButton, statisticsButton, resumeGameButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		
		createGameButton = (Button) findViewById(R.id.createGame_button);
		joinGameButton = (Button) findViewById(R.id.joinGame_button);
		statisticsButton = (Button) findViewById(R.id.statistics_button);
		resumeGameButton = (Button) findViewById(R.id.resumeGame_button);

	
		createGameButton.setOnClickListener(new OnClickListener() {
	    	
			@Override
			public void onClick(View v) {
				
				
			}
	    });
		
		joinGameButton.setOnClickListener(new OnClickListener() {
			    	
			@Override
			public void onClick(View v) {
				
				
			}
	    });
		
		statisticsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
			}
		});
		
		resumeGameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
			}
		});
		
	    
		 /*
         * Start the service
         */
        try {
        	
        	Intent i = new Intent();
        	i.setClassName("com.connect4.utils", "com.connect4.utils.c4_Service");
        	startService(i);
        	Log.v("menu", "Service started");
        	
        } catch (Exception e) {
        	e.printStackTrace();
        	Log.v("menu","Cannot start the service");
        }
        
        
		
	}
	
	
	@Override
	protected void onResume() {
		
		if (Utils.getGameStatus(getBaseContext()).equals(Const.GAMESTATUS_NOT_IN_GAME)) {
			
			createGameButton.setVisibility(View.VISIBLE);
			joinGameButton.setVisibility(View.VISIBLE);
			statisticsButton.setVisibility(View.VISIBLE);
			
			resumeGameButton.setVisibility(View.GONE);
			
		} else {
			
			resumeGameButton.setVisibility(View.VISIBLE);
			
			createGameButton.setVisibility(View.GONE);
			joinGameButton.setVisibility(View.GONE);
			statisticsButton.setVisibility(View.GONE);
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

}
