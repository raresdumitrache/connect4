package com.connect4.nsd;

import java.util.ArrayList;

import android.content.Context;

import com.connect4.nsd.NetworkDiscoveryHelper;
import com.connect4.nsd.NetworkPeer;

public class NetworkDiscovery {
	
	NetworkDiscoveryHelper mNsdHelper;
	public static final String TAG = "appShuttle";
	Context context;
	
	public NetworkDiscovery(Context context) {
		this.context = context;
		this.start();
		this.discover();
	}
	
	public void start() {
		mNsdHelper = new NetworkDiscoveryHelper(context);
        mNsdHelper.initializeNsd();
        mNsdHelper.registerService(2020); //TODO - make dynamic port allocation
		
	}
	
	public void discover() {
        mNsdHelper.discoverServices();
    }
	
	public ArrayList<NetworkPeer> getNetworkPeers() {
		return mNsdHelper.getNetworkPeers();
	}
	

}
