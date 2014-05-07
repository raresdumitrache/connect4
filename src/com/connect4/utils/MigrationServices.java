package com.connect4.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MigrationServices extends Activity {
	
	public boolean serviceBounded;
	
	IServiceManager serviceManager = null;
	IServiceVm serviceVm = null;
	
	Button startAppButton, stopAppButton;
    TextView infoTextView;
    EditText urlEditText;
    
    

    private IServiceManagerCallback managerCallback = new IServiceManagerCallback.Stub() {
    	
    	@Override
		public void setRunningApps(int x) {
    		infoTextView.setText("App state: " + x );
    	}
    };
    
    
	public ServiceConnection serviceConnection = new ServiceConnection () {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder serviceBinder) {

			Log.v("app", "Connected to service");
			serviceBounded = true;
			
			serviceManager = IServiceManager.Stub.asInterface(serviceBinder);
			
			try {
				
				serviceManager.registerManager(managerCallback);
				
				serviceManager.getRunningApps();
				
			} catch (RemoteException e) {e.printStackTrace();}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

			Log.v("app", "onServiceDisconnected: Disconnected from service ");
			serviceBounded = false;
			
			serviceManager = null;
		}
    };
    
    
    
    
    
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration_services);
        
        startAppButton = (Button) findViewById(R.id.startAppButton);
        stopAppButton = (Button) findViewById(R.id.stopAppButton);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        urlEditText = (EditText) findViewById(R.id.urlEditText);
        
        serviceBounded = false;
        /*
         * Start the service
         */
        try {
        	
        	Intent i = new Intent();
        	i.setClassName("com.example.migrationservices", "com.example.migrationservices.migrationServicesS");
        	startService(i);
        	Log.v("app", "Service started");
        	
        } catch (Exception e) {
        	e.printStackTrace();
        	Log.v("app","Cannot start the service");
        }

        /*
         * Start the application: tell service to start th vm
         */
        startAppButton.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				
				Log.v("app", "Start app");
				
				if (serviceBounded == true) {
					
					if (TextUtils.isEmpty(urlEditText.getText().toString())) {
						Toast.makeText(getBaseContext(), "Please enter an URL!", Toast.LENGTH_LONG).show();
					} else {
						try {
							
							//serviceManager.getRunningApps();
							serviceManager.startApp(urlEditText.getText().toString());
							
						} catch (RemoteException e) {e.printStackTrace();}
					}
						
				} else {
					bindToService();
				}
				
			}
        });
        
        /*
         * Stop the application: tell service to start th vm
         */
        stopAppButton.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				
				Log.v("app", "serviceBounded = " + serviceBounded);
				
				if (serviceBounded == true) {
					
						try {
							
							serviceManager.stopApp();
							
						} catch (RemoteException e) {e.printStackTrace();}
						
				} else {
					bindToService();
				}
				
			}
        });

    }
    
    
    
    @Override
    protected void onResume() {
		super.onResume();
		Log.v("app","on RESUME");
		
    	bindToService();
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.v("app","on PAUSE");
    	
    	try {
    		
    		unbindFromService();
    		serviceBounded = false;
    		Log.v("app", "Disconnected from service due to app closed");
    		
    	} catch(Exception e) {Log.v("app", "Exception onStop"); serviceBounded = false;}
    	
    }
  
    
    void bindToService() {
    	
    	Log.v("app","Trying to BIND to service");
    	
    	if (serviceBounded == false) {
    		Intent i = new Intent();
        	i.setClassName("com.example.migrationservices", "com.example.migrationservices.migrationServicesS");
        	i.putExtra("sentFrom", 0);
    		bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    	}
    	
    }
    
    void unbindFromService() {
    	
    	if (serviceBounded) {
    		
    		unbindService(serviceConnection);
			serviceBounded = false;
			Log.v("app","Service successfully UNBINDED");
			
    	}
    }
    
}
