/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.connect4.game.Gameplay;
import com.connect4.utils.Const;
import com.connect4.utils.IGLService;
import com.connect4.utils.IGLServiceCallback;


public class GL2JNIActivity extends Activity {

    GL2JNIView mView;
    
    boolean serviceBounded;
    
    IGLService glService = null;
    
    private IGLServiceCallback createCallback = new IGLServiceCallback.Stub() {

		@Override
		public void opponentPlacedDiscOnColumn(int column)
				throws RemoteException {
			
		}
    	
    };
    
    
    public ServiceConnection serviceConnection = new ServiceConnection () {

		@Override
		public void onServiceConnected(ComponentName className, IBinder serviceBinder) {

			Log.v("app_gl", "Connected to service");
			serviceBounded = true;

			glService = IGLService.Stub.asInterface(serviceBinder);

			try {
				glService.registerGLActivity(createCallback);
			} catch (RemoteException e) {e.printStackTrace();}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v("app_gl", "onServiceDisconnected: Disconnected from service ");
			serviceBounded = false;
			glService = null;
		}
	};
    
    

    @Override protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Intent intent = getIntent();
        Gameplay.startGame(intent.getStringExtra("player"));
        
        serviceBounded = false;
        
        mView = new GL2JNIView(getApplication());
        setContentView(mView);
    }
    
    @Override protected void onPause() {
        super.onPause();
        mView.onPause();
        /*
    	try {
    		unbindFromService();
    		serviceBounded = false;
    		Log.v("app_create", "Disconnected from service due to app closed");
    	} catch(Exception e) {Log.v("app", "Exception onStop"); serviceBounded = false;}
    	*/
    }

    @Override protected void onResume() {
        super.onResume();
        bindToService();
        mView.onResume();
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
}
