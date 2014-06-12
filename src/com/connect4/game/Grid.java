package com.connect4.game;

import com.connect4.utils.Const;
import com.connect4.utils.Utils;

public class Grid {

	public Disc [][] grid = new Disc[Const.gridWidth][Const.gridHeight];
	public String currentPlayer;
	
	
	public Grid() {
		this.currentPlayer = Const.PLAYER_ONE;
	}
	
	/*
	 * Tries to put a disc on the grid at the given column.
	 * 
	 * Return:  -1, 		if the coloumn is full;
	 * 			integer, 	the line number.
	 */
	public int placeDisc(int col)
	{
		/* The column is full */
		if (null != grid[0][col])
		{
			return -1;
		}
		
		for (int i=(Const.gridHeight - 1); i >= 0; i--)
		{
			if (null == grid[i][col])
			{
				grid[i][col] = new Disc(currentPlayer);
				
				return i;
			}
		}
		
		return -1;
		
	}	
	
}
