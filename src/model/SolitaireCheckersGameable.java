package model;

import model.Gameable;

/**
 * <<interface>> SolitaireCheckersGameable
 * 
 * Declares the various methods that solitaire checkers gameable objects must
 * implement.
 * 
 * @author Gerald.Hurdle@AlgonquinCollege.com
 * @version 1.0
 */
public interface SolitaireCheckersGameable extends Gameable {

	/* Answers whether or not the game is ideally won. */
	/* An ideal win occurs when the last remaining peg */
	/* is in the center of the board. */
	public boolean isWonIdeal();
}