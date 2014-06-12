package com.connect4.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.connect4.utils.Const;
import com.connect4.utils.IJoinService;
import com.connect4.utils.IJoinServiceCallback;
import com.example.connect4.R;

public class JoinGameActivity extends Activity {

	TextView messageView;
	
	boolean serviceBounded;
	
	IJoinService joinService = null;
	
	
	
    private IJoinServiceCallback joinCallback = new IJoinServiceCallback.Stub() {
    	
    	@Override
		public void foundOpponent(String ipOpponent) {
    		if (ipOpponent == null) {
    			messageView.setText("Searching for games...");
    		} else {
    			messageView.setText("Found opponent: " + ipOpponent);
    		}
    	}
    	
    	@Override
    	public void startGame()
    	{
    		Intent intent = new Intent(JoinGameActivity.this, GL2JNIActivity.class);
    		intent.putExtra("player", Const.PLAYER_TWO);
			startActivity(intent);
    	}
    };
	
    
	public ServiceConnection serviceConnection = new ServiceConnection () {

		@Override
		public void onServiceConnected(ComponentName className, IBinder serviceBinder) {

			Log.v("app_join", "Connected to service");
			serviceBounded = true;

			joinService = IJoinService.Stub.asInterface(serviceBinder);

			try {
				joinService.registerJoinActivity(joinCallback);
			} catch (RemoteException e) {e.printStackTrace();}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v("app_join", "onServiceDisconnected: Disconnected from service ");
			serviceBounded = false;
			joinService = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		messageView = (TextView) findViewById(R.id.messageView);
		
		serviceBounded = false;
		
	}
	
	
	@Override
	protected void onResume() {
		Log.v("app_join","on RESUME");
		bindToService();
	}
	
	@Override
    protected void onPause() {
    	super.onPause();
    	Log.v("app_join","on PAUSE");
    	/*
    	try {
    		unbindFromService();
    		serviceBounded = false;
    		Log.v("app_join", "Disconnected from service due to app closed");
    	} catch(Exception e) {Log.v("app", "Exception onStop"); serviceBounded = false;}
    	*/
    }
	
	
	
	void bindToService() {
		Log.v("app_create","Trying to BIND to service");
		if (serviceBounded == false) {
			Intent i = new Intent();
			i.setClassName("com.connect4.utils", "com.connect4.utils.c4_Service");
			i.putExtra("sentFrom", Const.JOIN_ACTIVITY);
			bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	void unbindFromService() {
		if (serviceBounded) {
			unbindService(serviceConnection);
			serviceBounded = false;
			Log.v("app_join","Service successfully UNBINDED");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

}
