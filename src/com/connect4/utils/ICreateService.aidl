package com.connect4.utils;

import com.connect4.utils.ICreateServiceCallback;

interface ICreateService {

	void registerCreateActivity(ICreateServiceCallback createObject);
 	
 	void startGame();
}