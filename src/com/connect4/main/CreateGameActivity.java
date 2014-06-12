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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.connect4.utils.Const;
import com.connect4.utils.ICreateService;
import com.connect4.utils.ICreateServiceCallback;
import com.example.connect4.R;

public class CreateGameActivity extends Activity {

	Button startGameButton;
	TextView messageView;
	
	boolean gameReady;
	boolean serviceBounded;
	
	ICreateService createService = null;
	
	
	
    private ICreateServiceCallback createCallback = new ICreateServiceCallback.Stub() {
    	
    	@Override
		public void foundOpponent(String ipOpponent) {
    		if (ipOpponent == null) {
    			messageView.setText("Waiting for players...");
    		} else {
    			messageView.setText("Found opponent: " + ipOpponent);
    			gameReady = true;
    		}
    	}
    };
	
    
	public ServiceConnection serviceConnection = new ServiceConnection () {

		@Override
		public void onServiceConnected(ComponentName className, IBinder serviceBinder) {

			Log.v("app_create", "Connected to service");
			serviceBounded = true;

			createService = ICreateService.Stub.asInterface(serviceBinder);

			try {
				createService.registerCreateActivity(createCallback);
			} catch (RemoteException e) {e.printStackTrace();}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v("app_create", "onServiceDisconnected: Disconnected from service ");
			serviceBounded = false;
			createService = null;
		}
	};
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		
		startGameButton = (Button) findViewById(R.id.startGame_button);
		messageView = (TextView) findViewById(R.id.messageView);
		
		gameReady = false;
		serviceBounded = false;
		
		startGameButton.setOnClickListener(new OnClickListener() {
	    	
			@Override
			public void onClick(View v) {
				
				if (gameReady)
				{
					try {
						createService.startGame();
					} catch (RemoteException e) {e.printStackTrace();}
					
					Intent intent = new Intent(CreateGameActivity.this, GL2JNIActivity.class);
					intent.putExtra("player", Const.PLAYER_ONE);
					startActivity(intent);
				} else {
					Toast.makeText(getBaseContext(), "Not connected to any user.", Toast.LENGTH_LONG).show();
				}
			}
	    });
	}
	
	@Override
	protected void onResume() {
		Log.v("app_create","on RESUME");
		bindToService();
	}
	
	@Override
    protected void onPause() {
    	super.onPause();
    	Log.v("app_create","on PAUSE");
    	/*
    	try {
    		unbindFromService();
    		serviceBounded = false;
    		Log.v("app_create", "Disconnected from service due to app closed");
    	} catch(Exception e) {Log.v("app", "Exception onStop"); serviceBounded = false;}
    	*/
    }
	
	
	
	void bindToService() {
		Log.v("app_create","Trying to BIND to service");
		if (serviceBounded == false) {
			Intent i = new Intent();
			i.setClassName("com.connect4.utils", "com.connect4.utils.c4_Service");
			i.putExtra("sentFrom", Const.CREATE_ACTIVITY);
			bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	void unbindFromService() {
		if (serviceBounded) {
			unbindService(serviceConnection);
			serviceBounded = false;
			Log.v("app_create","Service successfully UNBINDED");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

}
