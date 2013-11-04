package model;

/**
 * Constants for Solitaire Checkers.
 * 
 * @author Gerald.Hurdle@AlgonquinCollege.com
 * @version 1.0
 */
public interface SolitaireCheckersConstants {

	// model
	// view
	// application
	// applet
	public static final SolitaireCheckersConfigurations DEFAULT_CONFIGURATION = SolitaireCheckersConfigurations.Solitaire;

	public static final String DEFAULT_MESSAGE_LOST = new String(
			"No more jumps!");
	public static final String DEFAULT_MESSAGE_WON = new String("I'm a winner");
	public static final String DEFAULT_MESSAGE_WON_PERFECT = new String(
			"I'm a perfect winner!");

	// view
	// application
	public static final boolean IS_ASSIGNMENT = true; // default :: false
}