package com.connect4.nsd;
import java.net.Socket;
import java.util.ArrayList;

public class NetworkPeer {
	Socket socket;
	String info;
	
	public NetworkPeer() {
		socket = new Socket();
		info = new String();
	}
	
	public NetworkPeer(Socket socket, String info) {
		this.socket = socket;
		this.info = info;
	}
	
	public Socket getSocket() {
		return null; // TODO : Get socket
	}
	
	public String getInfo() {
		return this.info;
	}
	
	public boolean equals(Object ob) {
		return this.getInfo() == ((NetworkPeer) ob).getInfo();
	}
}
