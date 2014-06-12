package com.connect4.game;

import com.connect4.utils.Const;

public class Gameplay {
	
	private static Grid board;
	public static int currentLine, currentColumn;
	public static String myPlayer;

	public static void startGame(String player)
	{
		board = new Grid();
		myPlayer = player;
	}
	
	public static boolean placeDisc(int col)
	{
		currentColumn = col;
		currentLine = board.placeDisc(col);
		
		if (-1 == currentLine)
		{
			return false;
		}
		return true;
	}
	
	public static boolean isWinner()
	{
		int i, j;
		
		/* Look on | , direction DOWN*/
		for (i=1; (i < 4) && (currentLine+i < Const.gridHeight); i++)
		{
			if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine+i][currentColumn])) )
			{
				break;
			}
		}
		if (i == 4)
		{
			return true;
		}
		
		
		/* Look on --- , direction LEFT */
		for (i=1; (i < 4) && (currentColumn-i >= 0); i++)
		{
			if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine][currentColumn-i])) )
			{
				break;
			}
		}
		if (i == 4)
		{
			return true;
		}
		else
		{
			/* Look on --- , direction RIGHT*/
			for (j=1; (i + j < 5) && (currentColumn+j < Const.gridWidth); j++)
			{
				if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine][currentColumn+j])) )
				{
					break;
				}
			}
			if ((i + j) == 5)
			{
				return true;
			}
		}
		

		/* Look on \ , direction UP */
		for (i=1; (i < 4) && (currentLine-i >= 0) && (currentColumn-i >= 0); i++)
		{
			if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine-i][currentColumn-i])) )
			{
				break;
			}
		}
		if (i == 4)
		{
			return true;
		}
		else
		{
			/* Look on \ , direction UP */
			for (j=1; (i + j < 5) && (currentLine+j < Const.gridHeight) && (currentColumn+j < Const.gridWidth); j++)
			{
				if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine+j][currentColumn+j])) )
				{
					break;
				}
			}
			if ((i + j) == 5)
			{
				return true;
			}
		}
		
		
		/* Look on / , direction UP */
		for (i=1; (i < 4) && (currentLine-i >= 0) && (currentColumn+i < Const.gridWidth); i++)
		{
			if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine-i][currentColumn+i])) )
			{
				break;
			}
		}
		if (i == 4)
		{
			return true;
		}
		else
		{
			/* Look on / , direction UP */
			for (j=1; (i + j < 5) && (currentLine+j < Const.gridHeight) && (currentColumn-j >= 0); j++)
			{
				if ( !(board.grid[currentLine][currentColumn].equals(board.grid[currentLine+j][currentColumn+j])) )
				{
					break;
				}
			}
			if ((i + j) == 5)
			{
				return true;
			}
		}
		

		return false;
	}
	
	public static boolean isDraw()
	{
		for (int i=0; i < Const.gridWidth; i++)
		{
			if (board.grid[0][i] == null)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static void changeTurn()
	{
		if (board.currentPlayer.equals(Const.PLAYER_ONE))
		{
			board.currentPlayer = Const.PLAYER_TWO;
		} 
		else 
		{
			board.currentPlayer = Const.PLAYER_ONE;
		}
	}
	
	public static boolean isMyTurn()
	{
		if (myPlayer.equals(board.currentPlayer))
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
}
