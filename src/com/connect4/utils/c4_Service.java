package com.connect4.utils;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class c4_Service extends Service {
	
	
	
	@Override
	public void onCreate() {
		
		Log.v("service", "Service has been CREATED");
	}
	
	@Override
	public void onDestroy() {
		
		Log.v("service","Service has been DESTROYED");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		
		Log.v("service", "Service STARTED");
		
	    return Service.START_STICKY;

	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		
		Log.v("service","on BINDER");
		
		return null;
	}
	
	
	@Override
	public boolean onUnbind(Intent arg0) {
		Log.v("service","on UNBIND");
		
		// return true   =  the next time an activity calls bind()
		// 				    the onRebind() method in the service will be called;
		// return false  =  call the same onBind() method in the service.
		return false;
	}

	
}
