package com.connect4.utils;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import com.connect4.nsd.NetworkDiscovery;
import com.example.utils.NetworkPeer;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class migrationServicesS extends Service {
	NetworkDiscovery nds;

	IServiceManagerCallback manager;
	
	ArrayList vmSlots = new ArrayList();
	
	/*
	 * Number to store binding connections
	 */
	
	private static int totalConnections = 0;
	
	
	
	private final IServiceManager.Stub managerBinder = new IServiceManager.Stub() {

		@Override
		public void registerManager(IServiceManagerCallback managerObject) {
			
			manager = managerObject;
		}
		
		@Override
		public void getRunningApps() throws RemoteException {
			
			manager.setRunningApps(++totalConnections);
		}

		@Override
		public void startApp(String url) throws RemoteException {

			Intent i;
			PackageManager manager = getPackageManager();
			try {
			    i = manager.getLaunchIntentForPackage("com.example.vmSlot");
			    if (i == null) {
			    	throw new PackageManager.NameNotFoundException();
			    }
			        
			    i.addCategory(Intent.CATEGORY_LAUNCHER);
			    
			    i.putExtra("url", url);
			    
			    startActivity(i);
			} catch (PackageManager.NameNotFoundException e) {

			}
			
		}

		@Override
		public void stopApp() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
	   
	};
	
	private final IServiceVm.Stub vmBinder = new IServiceVm.Stub() {
	    
	};

	

	
	@Override
	public void onCreate() {
		
		Log.v("app_service", "Service has been CREATED");
	}
	
	@Override
	public void onDestroy() {
		
		Log.v("app_service","Service has been DESTROYED");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		
		Log.v("app_service", "Service STARTED");

		nds = new NetworkDiscovery(getBaseContext());
		
		// Vad cati peeri am o data la N secunde
		// Debug
		Timer mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.i("appshuttle", "Am "+nds.getNetworkPeers().size()+" peeri");
				if(nds.getNetworkPeers().size() > 0) {
					for(NetworkPeer it : nds.getNetworkPeers()) {
						Log.i("appshuttle", "No "+ it.getInfo());
					}
				}
			}
		}, 0, 5000);
		
	    return Service.START_STICKY;

	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		
		Log.v("app_service","on BINDER");
		
		switch ((Integer)intent.getExtras().get("sentFrom")) {
		
			case 0: return managerBinder;
			case 1: return vmBinder;
			
			default: return null;
		}
	}
	
	
	@Override
	public boolean onUnbind(Intent arg0) {
		Log.v("app_service","on UNBIND");
		return false;
	}

	
}
