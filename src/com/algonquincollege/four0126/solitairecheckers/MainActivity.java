/*******************************************************************/
/**                                                               **/
/**                                                               **/
/**    Student Name                :  Alex Fournier               **/
/**    EMail Address               :  four0126@algonquinlive.com  **/
/**    Student Number              :  040 597 732                 **/
/**    Student User ID             :  four0126                    **/
/**    Course Number               :  CST 9132 XOR NET 2013       **/
/**    Lab Section Number          :  your lab section(010,011...)**/
/**    Professor Name              :  Gerry Hurdle                **/
/**    Assignment Name/Number/Date :  SolitaireCheckers/2/10-30-13**/
/**    Optional Comments           :                              **/
/**                                                               **/
/**                                                               **/
/*******************************************************************/
package com.algonquincollege.four0126.solitairecheckers;

import java.util.Observable;
import java.util.Observer;

import model.SolitaireCheckersModel;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Model the game of solitaire checkers (also known as Hi-Q).
 * 
 * @author Alex Fournier - four0126@algonquinlive.com
 * @version 1.0
 */
public class MainActivity extends Activity implements Observer,
		OnItemSelectedListener {

	private static final int INFO_DIALOG = 10;

	// INSTANCE VARIABLES

	private Spinner configuration;
	private Dialog infoDialog;
	private SolitaireCheckersModel model;
	private ProgressBar progressBar;
	private ImageView imageView;

	// CONSTROCTORS

	/**
	 * setting the buttons and view items.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// register this activity as an Observer of the model
		model = new SolitaireCheckersModel();
		model.addObserver(this);

		infoDialog = onCreateDialog(INFO_DIALOG);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		configuration = (Spinner) findViewById(R.id.alt_boards);

		// register the configuration to be handled by this activity
		configuration.setOnItemSelectedListener(this);

		progressBar.setMax(model.getNumberOfPegs());
		// synch the view with the model
		this.updateView();
	}

	/**
	 * checking for action options.
	 * 
	 * @param Menu
	 *            menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// INSTANCE METHODS

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

		switch (id) {
		// Create About dialog
		case INFO_DIALOG:
			dialogBuilder.setCancelable(false);
			dialogBuilder.setTitle(R.string.dialogGameOver);
			// THIS IS YOUR FIRST TIME SEEING AN ANONYMOUS INNER CLASS
			// TAKE THE TIME TO THOROUGHLY UNDERSTAND THIS CODE
			dialogBuilder.setPositiveButton(R.string.ok_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int button) {
							dialog.dismiss();
						}
					});
		}
		return dialogBuilder.create();
	}

	/**
	 * Event handling for Pegs
	 * 
	 * @param view
	 *            v
	 */
	public void handlePeg(View v) {
		String name = getResources().getResourceEntryName(v.getId());

		// GET which view object was clicked
		int r = name.charAt(1) - '0';
		int c = name.charAt(3) - '0';

		if (model.isPegAt(r, c)) {
			model.jumpPegAt(r, c);
			System.out.println("Row: " + r + " Column: " + c);
		} else {
			model.moveLastPegTo(r, c);
			System.out.println("Row: " + r + " Column: " + c);
		}
	}

	/**
	 * Resetting the configuration
	 * 
	 * @param MenuIten
	 *            item
	 */
	public void handleReset(MenuItem item) {
		model.reset();
	}

	/**
	 * Set board configuration
	 * 
	 * @param parent
	 * @param View
	 *            view
	 * @param pos
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Toast.makeText(
				parent.getContext(),
				"onItemSelecetedListener : "
						+ parent.getItemAtPosition(pos).toString(),
				Toast.LENGTH_SHORT).show();

		model.setConfiguration(parent.getItemAtPosition(pos).toString()
				.replaceAll("\\s", ""));
	}

	/**
	 * No configuration selected ( NOOP )
	 * 
	 * @param parent
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// NOOP
	}

	/**
	 * Option Menu Selection
	 * 
	 * @param MenuItem
	 *            item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		// GET from the view which menu item was selected
		case R.id.action_reset:
			model.reset();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Update view
	 * 
	 * @param obsevable
	 * @param data
	 */
	@Override
	public void update(Observable observable, Object object) {
		this.updateView();
	}

	/**
	 * Loop and update board. Change image state. Option for multiple jumps
	 */
	private void updateBoard() {
		int row;
		int col;

		int id;
		imageView = null;
		Resources res = getResources();

		for (int i = 0; i < model.getBoardLength(); i++) {
			for (int j = 0; j < model.getBoardLengthAt(i); j++) {

				String StringId = "r" + i + "c" + j;

				id = res.getIdentifier(StringId, "id", getBaseContext()
						.getPackageName());

				imageView = (ImageView) findViewById(id);
				imageView.setBackgroundResource(R.color.background);

				// imageView.setClickable(false);
				imageView.setEnabled(false);

				if (model.isPegAt(i, j) == true) {
					imageView.setEnabled(true);
					imageView.setImageResource(R.drawable.peg);

				} else {
					imageView.setImageResource(R.drawable.nopeg);
				}
			}
		}

		// special update condition: multiple jumps
		if (model.hasMultipleJumps() == true) {
			int[][] possibleJumps;
			possibleJumps = model.getPossibleJumps();

			for (int i = 0; i < possibleJumps.length; i++) {
				if (possibleJumps[i][SolitaireCheckersModel.ROW] != SolitaireCheckersModel.ILLEGAL_JUMP
						&& possibleJumps[i][SolitaireCheckersModel.COLUMN] != SolitaireCheckersModel.ILLEGAL_JUMP) {

					row = possibleJumps[i][SolitaireCheckersModel.ROW];

					col = possibleJumps[i][SolitaireCheckersModel.COLUMN];

					id = getResources().getIdentifier("r" + row + "c" + col,
							"id", getBaseContext().getPackageName());

					imageView = (ImageView) findViewById(id);
					imageView.setBackgroundResource(R.color.multipleJumps);
					imageView.setEnabled(true);
					imageView.setClickable(true);
				}
			}
		}
	}

	/**
	 * Update progress bar
	 */
	private void updateProgress() {
		int numberOfPegs = model.getNumberOfStartingPegs();
		progressBar.setMax(numberOfPegs);

		int remainingPegs = model.getNumberOfPegs();
		progressBar.setProgress(remainingPegs);
	}

	/**
	 * updateView: board, progressBar, win or loss
	 */
	public void updateView() {
		this.updateBoard();
		this.updateProgress();
		this.updateWinOrLoss();
	}

	/**
	 * dialog checker: win ideal, win, loss cast setmessage show infodialog
	 */
	private void updateWinOrLoss() {
		if (model.isWonIdeal() == true) {
			((AlertDialog) infoDialog)
					.setMessage(SolitaireCheckersModel.DEFAULT_MESSAGE_WON_PERFECT);
			infoDialog.show();
		}
		if (model.isWon() == true) {
			((AlertDialog) infoDialog)
					.setMessage(SolitaireCheckersModel.DEFAULT_MESSAGE_WON);
			infoDialog.show();
		}
		if (model.isLost() == true) {
			((AlertDialog) infoDialog)
					.setMessage(SolitaireCheckersModel.DEFAULT_MESSAGE_LOST);
			infoDialog.show();
		}
	}
}
