package com.connect4.utils;

import com.connect4.utils.IJoinServiceCallback;

interface IJoinService {

	void registerJoinActivity(IJoinServiceCallback joinObject);
}