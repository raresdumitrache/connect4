package com.connect4.game;

public class Disc {

	public String player;
	
	public Disc(String player)
	{
		this.player = player;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final Disc other = (Disc) obj;
	    if ((this.player == null) ? (other.player != null) : !this.player.equals(other.player)) {
	        return false;
	    }
	    
	    return true;
	}
}
