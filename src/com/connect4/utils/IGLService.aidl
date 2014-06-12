package com.connect4.utils;

import com.connect4.utils.IGLServiceCallback;

interface IGLService {

	void registerGLActivity(IGLServiceCallback GLObject);
	
	void sendMove(int col);
}