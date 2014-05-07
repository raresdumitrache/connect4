package com.example.migrationservices;

import com.example.migrationservices.IServiceManagerCallback;

interface IServiceManager {

	void registerManager(IServiceManagerCallback managerObject);
	
 	void getRunningApps();
 
	void startApp(String url);
	
	void stopApp();
}