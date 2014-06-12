package com.connect4.utils;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.connect4.communication.NetworkCommunication;
import com.connect4.communication.NsdHelper;


public class c4_Service extends Service {
	
	NsdHelper mNsdHelper;
	NetworkCommunication mCommunication;
    private Handler mUpdateHandler;
    
    boolean foundOpponent = false;
    NsdServiceInfo service;
	
	ICreateServiceCallback createCallback;
	IJoinServiceCallback joinCallback;
	IGLServiceCallback glCallback;
	
	private final ICreateService.Stub createBinder = new ICreateService.Stub() {

		@Override
		public void registerCreateActivity(ICreateServiceCallback createObject)
				throws RemoteException {
			
			createCallback = createObject;
			
			advertiseService();
			
		}

		@Override
		public void startGame() throws RemoteException {
			
			sendNetworkCommand(Const.COMMAND_START_GAME+"");
		}
	    
	};
	
	private final IJoinService.Stub joinBinder = new IJoinService.Stub() {

		@Override
		public void registerJoinActivity(IJoinServiceCallback joinObject)
				throws RemoteException {
			
			joinCallback = joinObject;

			advertiseService();
			discoverService();
			
			final Timer mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					
					foundOpponent = connectToOpponent();
					
					if (foundOpponent) {
						try {
							joinCallback.foundOpponent(null);
						} catch (RemoteException e) {e.printStackTrace();}
					} else {
						try {
							
							joinCallback.foundOpponent(service.getHost().getHostAddress());
							sendNetworkCommand(""+Const.COMMAND_CONNECTED_TO_SERVER);
							
							stopDiscovery();
							
							mTimer.cancel();
						} catch (RemoteException e) {e.printStackTrace();}
					}
				}
			}, 0, 5000);
			
		}
	    
	};
	
	private final IGLService.Stub glBinder = new IGLService.Stub() {

		@Override
		public void registerGLActivity(IGLServiceCallback GLObject)
				throws RemoteException {
			
			glCallback = GLObject;
		}

		@Override
		public void sendMove(int col) throws RemoteException {
			sendNetworkCommand(Const.COMMAND_OPPONENT_COLUMN + col);	
		}
	    
	};
	
	@Override
	public void onCreate() {
		
		Log.v("service", "Service has been CREATED");
		
		 mUpdateHandler = new Handler() {
             @Override
         public void handleMessage(Message msg) {
            	 
             String command = msg.getData().getString("msg");
             try {
				processMessage(command);
			} catch (RemoteException e) {e.printStackTrace();}
             
         }
     };

     mCommunication = new NetworkCommunication(mUpdateHandler);

     mNsdHelper = new NsdHelper(this);
     mNsdHelper.initializeNsd();
	}
	
	@Override
	public void onDestroy() {
		
		Log.v("service","Service has been DESTROYED");
		
		mNsdHelper.tearDown();
        mCommunication.tearDown();
        super.onDestroy();
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		
		Log.v("service", "Service STARTED");
		
	    return Service.START_STICKY;

	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		
		Log.v("service","on BINDER");
		
		switch ((Integer)intent.getExtras().get("sentFrom")) {
		
		case Const.CREATE_ACTIVITY: return createBinder;
		case Const.JOIN_ACTIVITY: return joinBinder;
		case Const.GL_ACTIVITY: return glBinder;
		
		default: return null;
	}
	}
	
	
	@Override
	public boolean onUnbind(Intent arg0) {
		Log.v("service","on UNBIND");
		
		mNsdHelper.tearDown();
        mCommunication.tearDown();
		
		// return true   =  the next time an activity calls bind()
		// 				    the onRebind() method in the service will be called;
		// return false  =  call the same onBind() method in the service.
		return false;
	}
	
	
	//|-------------------------------------------|
	//| 	AVAILABLE COMMANDS AND PROCESSING 	  |
	//|------------------------------------------ |
	
	public void processMessage(String command) throws RemoteException
	{
		if (command.contains(Const.COMMAND_OPPONENT_COLUMN)) {
			
			int opponentColumn = Integer.parseInt(command.split("_")[1]);
			glCallback.opponentPlacedDiscOnColumn(opponentColumn);
			
		} else {
			
			switch (Integer.parseInt(command)) {
				case Const.COMMAND_CONNECTED_TO_SERVER: {
					createCallback.foundOpponent(mCommunication.getSocket().getInetAddress().getHostAddress());
				}
				case Const.COMMAND_START_GAME: {
					joinCallback.startGame();
				}
				default: return;
			}
			
		}
		
	}
	
	//|-------------------------------------------|
	//| NETWORK COMMUNICATION FUNCTIONS - HELPERS |
	//|------------------------------------------ |
	
	public void advertiseService() {
        // Register service
        if(mCommunication.getLocalPort() > -1) {
            mNsdHelper.registerService(mCommunication.getLocalPort());
        } else {
            Log.d(Const.LOG_TAG, "ServerSocket isn't bound.");
        }
    }

    public void discoverService() {
        mNsdHelper.discoverServices();
    }
    
    public void stopDiscovery() {
    	if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
    }
    
    public boolean connectToOpponent() {
        service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(Const.LOG_TAG, "Connecting.");
            mCommunication.connectToServer(service.getHost(),
                    service.getPort());
            return true;
        } else {
            Log.d(Const.LOG_TAG, "No service to connect to!");
            return false;
        }
    }

    public void sendNetworkCommand(String command) {
            mCommunication.sendMessage(command);
    }

	
}
