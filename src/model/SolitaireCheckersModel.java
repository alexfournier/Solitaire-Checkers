package model;

import java.util.*;

/**
 * Model the game of solitaire checkers (also known as Hi-Q).
 *
 * @author Gerald.Hurdle@AlgonquinCollege.com
 * @version 1.0
 */
public class SolitaireCheckersModel extends Observable implements SolitaireCheckersConstants
                                                                , SolitaireCheckersGameable
{
    private static final int CENTER_COLUMN = 3;
    private static final int CENTER_ROW    = 3;
    private static final int DELTA         = 2;
    private static final int MAX_COLUMNS   = 7;
    private static final int MAX_ROWS      = 7;

    private static final int DOWN_OFFSET  = 1;
    private static final int LEFT_OFFSET  = -1;
    private static final int RIGHT_OFFSET = 1;
    private static final int UP_OFFSET    = -1;

    private static final int UP           = 0;
    private static final int RIGHT        = 1;
    private static final int DOWN         = 2;
    private static final int LEFT         = 3;
    public  static final int ROW          = 0;
    public  static final int COLUMN       = 1;
    public  static final int ILLEGAL_JUMP = -1;

    private static final int UNDEFINED    = -1;

    private boolean[][]                     board;
    private SolitaireCheckersConfigurations configuration;
    private int                             numberOfStartingPegs;
    private int                             lastColumn;
    private int                             lastRow;
    private int[][]                         possibleJumps;
    private String                          status;


    /**
     * Default constructor that creates a solitaire checkers board.
     */
    public SolitaireCheckersModel() {
        this( DEFAULT_CONFIGURATION );
    }

    /**
     * Creates a game in an initial configuration.
     * @param SolitaireCheckersConfiguration starting configuration
     */
    public SolitaireCheckersModel( SolitaireCheckersConfigurations configuration ) {
        super();

        board = new boolean[MAX_ROWS][];
        board[0] = new boolean[3];
        board[1] = new boolean[3];
        board[2] = new boolean[7];
        board[3] = new boolean[7];
        board[4] = new boolean[7];
        board[5] = new boolean[3];
        board[6] = new boolean[3];

        possibleJumps = new int[4][2];

        this.setConfiguration( configuration );

        this.reset();
    }

    /**
     * Put a peg to each cell on the board.
     */
    private void addAllPegs() {
        for( int i = 0; i < board.length; i++ ) {
            for( int j = 0; j < board[i].length; j++ ) {
                board[i][j] = true;
            }
        }
    }

    /**
     * Put a peg at position row, column
     * @param int row
     * @param int column
     */
    private void addPegAt( int row, int column ) {
        board[row][column] = true;
    }

    /**
     * Answer whether or not the peg at row, column can jump
     * an any direction.
     * @param int row
     * @param int column
     * @return boolean true if the peg can jump; otherwise, false
     */
    private boolean canPegAtJump( int row, int column ) {
        return (this.canPegAtJumpVertical(row, column, UP_OFFSET) ||
                this.canPegAtJumpVertical(row, column, DOWN_OFFSET) ||
                this.canPegAtJumpHorizontal(row, column, LEFT_OFFSET) ||
                this.canPegAtJumpHorizontal(row, column, RIGHT_OFFSET) );
    }

    /**
     * Answer whether or not the peg at row, column can jump
     * left or right.
     * @param int row
     * @param int column
     * @return boolean true if the peg can jump left or right; otherwise, false
     */
    private boolean canPegAtJumpHorizontal( int row, int column, int offset ) {
        int row1;
        int column1;
        int row2;
        int column2;
        int orientation;


        if ( offset > 0 )
            orientation = RIGHT;
        else
            orientation = LEFT;

        possibleJumps[orientation][ROW]    = ILLEGAL_JUMP;
        possibleJumps[orientation][COLUMN] = ILLEGAL_JUMP;

        row2 = row;
        column2 = column + (DELTA * offset);
        if ( this.isRowAndColumnValid(row2, column2) == false )
            return false;

        if ( this.isPegAt(row2, column2) )
            return false;

        row1    = row;
        column1 = column + offset;
        if ( this.isRowAndColumnValid(row1, column1) == false )
            return false;

        if ( this.isPegAt(row1, column1) ) {
            possibleJumps[orientation][ROW]    = row2;
            possibleJumps[orientation][COLUMN] = column2;
            return true;
        }

        return false;
    }

    /**
     * Answer whether or not the peg at row, column can jump
     * to multiple positions.
     * @param int row
     * @param int column
     * @return boolean true if the peg can jump to multiple positions; otherwise, false
     */
    private boolean canPegAtJumpMultiple( int row, int column ) {
        int count;

        count = 0;

        if ( this.canPegAtJumpVertical(row, column, UP_OFFSET) )
            count++;

        if ( this.canPegAtJumpVertical(row, column, DOWN_OFFSET) )
            count++;

        if ( this.canPegAtJumpHorizontal(row, column, LEFT_OFFSET) )
            count++;

        if ( this.canPegAtJumpHorizontal(row, column, RIGHT_OFFSET) )
            count++;

        return count > 1;
    }

    /**
     * Answer whether or not the peg at row, column can jump
     * up or down.
     * @param int row
     * @param int column
     * @return boolean true if the peg can jump up or down; otherwise, false
     */
    private boolean canPegAtJumpVertical( int row, int column, int offset ) {
        int row1;
        int column1;
        int row2;
        int column2;
        int orientation;


        if ( offset > 0 )
            orientation = DOWN;
        else
            orientation = UP;

        possibleJumps[orientation][ROW]    = ILLEGAL_JUMP;
        possibleJumps[orientation][COLUMN] = ILLEGAL_JUMP;

        row2 = row + (DELTA * offset);
        if ( this.isRowValid(row2) == false )
            return false;

        column2 = this.transposeColumn( row, column, row2 );
        if ( this.isRowAndColumnValid(row2, column2) == false )
            return false;

        if ( this.isPegAt(row2, column2) )
            return false;

        row1 = row + offset;
        if ( this.isRowValid(row1) == false )
            return false;

        column1 = this.transposeColumn( row, column, row1 );
        if ( this.isRowAndColumnValid(row1, column1) == false )
            return false;

        if ( this.isPegAt(row1, column1) ) {
            possibleJumps[orientation][ROW]    = row2;
            possibleJumps[orientation][COLUMN] = column2;
            return true;
        }

        return false;
    }

    /**
     * Clear all multiple jump positions.
     */
    private void clearPossibleJumps() {
        for( int i = 0; i < possibleJumps.length; i++ ) {
            possibleJumps[i][ROW]    = ILLEGAL_JUMP;
            possibleJumps[i][COLUMN] = ILLEGAL_JUMP;
        }
    }

    /**
     * Returns the board's number of columns at row.
     * @param int the row
     * @return int the number of columns
     */
    public int columnsAt( int row ) {
        return board[row].length;
    }

    /**
     * Set the board in (up) arrow configuration.
     */
    private void configureAsArrow() {

        this.removeAllPegs();

        this.addPegAt( 0, 1 );
        this.addPegAt( 1, 0 );
        this.addPegAt( 1, 1 );
        this.addPegAt( 1, 2 );
        this.addPegAt( 2, 1 );
        this.addPegAt( 2, 2 );
        this.addPegAt( 2, 3 );
        this.addPegAt( 2, 4 );
        this.addPegAt( 2, 5 );
        this.addPegAt( 3, 3 );
        this.addPegAt( 4, 3 );
        this.addPegAt( 5, 0 );
        this.addPegAt( 5, 1 );
        this.addPegAt( 5, 2 );
        this.addPegAt( 6, 0 );
        this.addPegAt( 6, 1 );
        this.addPegAt( 6, 2 );

        this.setNumberOfStartingPegs( 17 );
    }

    /**
     * Set the board in cross configuration.
     */
    private void configureAsCross() {

        this.removeAllPegs();

        this.addPegAt( 1, 1 );
        this.addPegAt( 2, 2 );
        this.addPegAt( 2, 3 );
        this.addPegAt( 2, 4 );
        this.addPegAt( 3, 3 );
        this.addPegAt( 4, 3 );

        this.setNumberOfStartingPegs( 6 );
    }

    /**
     * Set the board in diamond configuration.
     */
    private void configureAsDiamond() {

        this.addAllPegs();

        this.removePegAt( 0, 0 );
        this.removePegAt( 0, 2 );
        this.removePegAt( 2, 0 );
        this.removePegAt( 2, 6 );
        this.removePegAt( 3, 3 );
        this.removePegAt( 4, 0 );
        this.removePegAt( 4, 6 );
        this.removePegAt( 6, 0 );
        this.removePegAt( 6, 2 );

        this.setNumberOfStartingPegs( 8 );
    }

    /**
     * Set the board in double-arrow configuration.
     */
    private void configureAsDoubleArrow() {

        this.configureAsArrow();

        this.addPegAt( 3, 2 );
        this.addPegAt( 3, 4 );
        this.addPegAt( 4, 1 );
        this.addPegAt( 4, 2 );
        this.addPegAt( 4, 4 );
        this.addPegAt( 4, 5 );
        this.removePegAt( 6, 0 );
        this.removePegAt( 6, 2 );
    }

    /**
     * Set the board in fireplace configuration.
     */
    private void configureAsFireplace() {

        this.removeAllPegs();

        this.addPegAt( 0, 0 );
        this.addPegAt( 0, 1 );
        this.addPegAt( 0, 2 );
        this.addPegAt( 1, 0 );
        this.addPegAt( 1, 1 );
        this.addPegAt( 1, 2 );
        this.addPegAt( 2, 2 );
        this.addPegAt( 2, 3 );
        this.addPegAt( 2, 4 );
        this.addPegAt( 3, 2 );
        this.addPegAt( 3, 4 );

        this.setNumberOfStartingPegs( 11 );
    }

    /**
     * Set the board in plus configuration.
     */
    private void configureAsPlus() {

        this.removeAllPegs();

        this.addPegAt( 1, 1 );
        this.addPegAt( 2, 3 );
        this.addPegAt( 3, 1 );
        this.addPegAt( 3, 2 );
        this.addPegAt( 3, 3 );
        this.addPegAt( 3, 4 );
        this.addPegAt( 3, 5 );
        this.addPegAt( 4, 3 );
        this.addPegAt( 5, 1 );

        this.setNumberOfStartingPegs( 9 );
    }

    /**
     * Set the board in pyramid configuration.
     */
    private void configureAsPyramid() {

        this.removeAllPegs();

        this.addPegAt( 1, 1 );
        this.addPegAt( 2, 2 );
        this.addPegAt( 2, 3 );
        this.addPegAt( 2, 4 );
        this.addPegAt( 3, 1 );
        this.addPegAt( 3, 2 );
        this.addPegAt( 3, 3 );
        this.addPegAt( 3, 4 );
        this.addPegAt( 3, 5 );
        this.addPegAt( 4, 0 );
        this.addPegAt( 4, 1 );
        this.addPegAt( 4, 2 );
        this.addPegAt( 4, 3 );
        this.addPegAt( 4, 4 );
        this.addPegAt( 4, 5 );
        this.addPegAt( 4, 6 );

        this.setNumberOfStartingPegs( 16 );
    }

    /**
     * Set the board in solitaire configuration.
     */
    private void configureAsSolitaire() {

        this.addAllPegs();

        this.removePegAt( 3, 3 );

        this.setNumberOfStartingPegs( 32 );
    }

    /**
     * Display the board to standard out.
     * Debug method.
     */
    public void displayPossibleJumps() {
        for( int i = 0; i < possibleJumps.length; i++ ) {
            System.out.println( "possibleJumps[" + i + "] is " +
                possibleJumps[i][ROW] + ", " + possibleJumps[i][COLUMN] );
        }
    }

    public int getBoardLength() {
    	return board.length;
    }
    
    public int getBoardLengthAt( int row ) {
    	return board[row].length;
    }

    /**
     * Get this game's configuration.
     * @return SolitaireCheckersConfiguration the configuration
     */
    public SolitaireCheckersConfigurations getConfiguration() {
        return configuration;
    }

    /**
     * Count the number of pegs remaining on the board.
     * @return int the number of remaining pegs
     */
    public int getNumberOfPegs() {
        int count;

        count = 0;
        for( int i = 0; i < board.length; i++ ) {
            for( int j = 0; j < board[i].length; j++ ) {
                if ( board[i][j] )
                    count++;
            }
        }

        return count;
    }

    /**
     * Get the number of starting pegs for this configuration.
     * @return int the number of starting pegs
     */
    public int getNumberOfStartingPegs() {
        return numberOfStartingPegs;
    }

    /**
     * Get all known possible jump positions.
     * @return int[][] collection of rows and columns
     */
    public int[][] getPossibleJumps() {
        return possibleJumps;
    }

    /**
     * Get this game's status.
     * @return String the status of the game
     */
    public String getStatus() {
        return status.toString();
    }

    /**
     * Answer whether or not there is at least one peg that
     * can jump another peg.
     * @return boolean true if there is at least one peg that
     * can jump; otherwise, false
     */
    private boolean hasMoreJumps() {
        for( int i = 0; i < board.length; i++ ) {
            for( int j = 0; j < board[i].length; j++ ) {
                if ( board[i][j] ) {
                    if ( this.canPegAtJump(i, j) ) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Answer whether or not there is at least one peg that
     * can jump to multiple positions.
     * @return boolean true if there is at least one peg that
     * can jump to multiple positions; otherwise, false
     */
    public boolean hasMultipleJumps() {
        int count;

        count = 0;

        for( int i = 0; i < possibleJumps.length; i++ ) {
            if ( (possibleJumps[i][ROW]    != ILLEGAL_JUMP) &&
                 (possibleJumps[i][COLUMN] != ILLEGAL_JUMP) ) {
                    count++;
            }
        }

        return count > 1;
    }

    /**
     * Answer whether or not this game is lost.
     * The game is lost when:
     *  a) no peg can jump another peg, and
     *  b) the number of remaining pegs is greater than 1
     * @return boolean true if this game is lost; otherwise, false
     */
    @Override
    public boolean isLost() {
        if ( this.hasMoreJumps() )
            return false;

        return( this.getNumberOfPegs() > 1 );
    }

    /**
     * Answer whether or not there is a peg at position row, column.
     * @param int row
     * @param int column
     * @return boolean true if there is a peg at row, column; otherwise, false
     */
    public boolean isPegAt( int row, int column ) {
        if ( this.isRowAndColumnValid(row, column) )
            return board[row][column];

        return false;
    }

    /**
     * Answer whether or not there is a peg at position row, column.
     * @param int row
     * @param int column
     * @return boolean true if there is a peg at row, column; otherwise, false
     */
    private boolean isRowAndColumnValid( int row, int column ) {

        if ( this.isRowValid(row) == false )
            return false;

        return ( (column >= 0) && (column < board[row].length) );
    }

    /**
     * Answer whether or not row is in the range 0 to 7 (inclusive).
     * @param int row
     * @return boolean true if the row is valid; otherwise, false
     */
    private boolean isRowValid( int row ) {
        return( (row >= 0) && (row < MAX_ROWS) );
    }

    /**
     * Answer whether or not this game is won.
     * The game is won when:
     *  a) only one peg remains on the board, and
     *  b) the peg is NOT in the center of the board
     * @return boolean true if this game is lost; otherwise, false
     */
    @Override
    public boolean isWon() {
        return ( (this.getNumberOfPegs() == 1) && (this.isPegAt(CENTER_ROW, CENTER_COLUMN) == false) );
    }

    /**
     * Answer whether or not this game is perfectly won.
     * The game is perfectly won when:
     *  a) only one peg remains on the board, and
     *  b) the peg is in the center of the board
     * @return boolean true if this game is perfectly won; otherwise, false
     */
    @Override
    public boolean isWonIdeal() {
        return ( (this.getNumberOfPegs() == 1) && this.isPegAt(CENTER_ROW, CENTER_COLUMN) );
    }

    /**
     * Take the peg at position row, column and jump!
     * @param int row
     * @param int column
     */
    public void jumpPegAt( int row, int column ) {
        StringBuffer b = new StringBuffer();

        if ( this.isPegAt(row, column) == false )
            return;

        this.rememberPegAt( row, column );

        if ( this.canPegAtJumpMultiple(row, column) ) {
            b.append( "peg " );
            b.append( (row + 1) + ", " + (column + 1) );
            b.append( " has multiple jumps" );
            this.setStatus( b.toString() );
            this.updateObservers();
            return;
        }

        if ( this.canPegAtJump(row, column) ) {
            b.append( "peg " );
            b.append( (row + 1) + ", " + (column + 1) );
            b.append( " jumped" );
        }
        this.setStatus( b.toString() );

        // up
        if ( this.canPegAtJumpVertical(row, column, UP_OFFSET) ) {
            // DEBUG
            //System.out.println( "r " + row + " c " + column + " can jump UP" );
            this.jumpPegAtVertical( row, column, UP_OFFSET );
        }

        // down
        if ( this.canPegAtJumpVertical(row, column, DOWN_OFFSET) ) {
            // DEBUG
            //System.out.println( "r " + row + " c " + column + " can jump DOWN" );
            this.jumpPegAtVertical( row, column, DOWN_OFFSET );
        }

        // left
        if ( this.canPegAtJumpHorizontal(row, column, LEFT_OFFSET) ) {
            // DEBUG
            //System.out.println( "r " + row + " c " + column + " can jump LEFT" );
            this.jumpPegAtHorizontal( row, column, LEFT_OFFSET );
        }

        // right
        if ( this.canPegAtJumpHorizontal(row, column, RIGHT_OFFSET) ) {
            // DEBUG
            //System.out.println( "r " + row + " c " + column + " can jump RIGHT" );
            this.jumpPegAtHorizontal( row, column, RIGHT_OFFSET );
        }

        this.updateObservers();
    }

    /**
     * Take the peg at position row, column and jump left or right.
     * @param int row
     * @param int column
     */
    private void jumpPegAtHorizontal( int row, int column, int offset ) {
        int row2 = row;
        int column2 = column + (DELTA * offset);

        this.movePegFromTo( row, column, row2, column2 );

        column2 = column + offset;
        this.removePegAt( row2, column2 );
    }

    /**
     * Take the peg at position row, column and jump up or down.
     * @param int row
     * @param int column
     */
    private void jumpPegAtVertical( int row, int column, int offset ) {
        int row2 = row + (DELTA * offset);
        int column2 = this.transposeColumn( row, column, row2 );

        this.movePegFromTo( row, column, row2, column2 );

        row2 = row + offset;
        column2 = this.transposeColumn( row, column, row2 );
        this.removePegAt( row2, column2 );
    }

    /**
     * Take the peg's last row and column position, and
     * move to row2, column2.
     * @param int the row the peg will move to
     * @param int the column the peg will move to
     */
    public void moveLastPegTo( int row2, int column2 ) {
        StringBuffer b = new StringBuffer();

        this.clearPossibleJumps();
        //DEBUG
        //System.out.println( "lr lc r2 c2: " + lastRow + " " + lastColumn + " " + row2 + " " + column2 );

        if ( lastRow == row2 ) {
            if ( (lastColumn + DELTA) == column2 )
                this.jumpPegAtHorizontal( lastRow, lastColumn, RIGHT_OFFSET );
            else
                this.jumpPegAtHorizontal( lastRow, lastColumn, LEFT_OFFSET );
        } else {
            if ( lastRow > row2 )
                this.jumpPegAtVertical( lastRow, lastColumn, UP_OFFSET );
            else
                this.jumpPegAtVertical( lastRow, lastColumn, DOWN_OFFSET );
        }

        b.append( "you chose " );
        b.append( (row2 + 1) + ", " + (column2 + 1) );
        this.setStatus( b.toString() );

        this.updateObservers();
    }

    /**
     * Move the peg located at row1, column1 to row2, column2.
     * @param int row1 starting row
     * @param int column1 starting column
     * @param int row2 end row
     * @param int column2 end column
     */
    private void movePegFromTo( int row1, int column1, int row2, int column2 ) {
        this.removePegAt( row1, column1 );
        this.addPegAt( row2, column2 );
    }

    /**
     * Remember the peg located at row, column
     * @param int the row to remember
     * @param int the column to remember
     */
    private void rememberPegAt( int row, int column ) {
        this.lastRow    = row;
        this.lastColumn = column;
    }

    /**
     * Clear the board of all pegs.
     */
    private void removeAllPegs() {
        for( int i = 0; i < board.length; i++ ) {
            for( int j = 0; j < board[i].length; j++ ) {
                board[i][j] = false;
            }
        }
    }

    /**
     * Remove the peg at position row, column
     * @param int row
     * @param int column
     */
    private void removePegAt( int row, int column ) {
        board[row][column] = false;
    }

    /**
     * Reset the game according to the configuration.
     */
    @Override
    public void reset() {
        this.clearPossibleJumps();
        this.rememberPegAt( UNDEFINED, UNDEFINED );
        this.setConfiguration( this.getConfiguration() );
    }

    /**
     * Returns the board's number of rows.
     * @return int the number of rows
     */
    public int rows() {
        return board.length;
    }

    public void setConfiguration( String configuration )
        throws IllegalArgumentException
    {
//        this.setConfiguration( Enum.valueOf(SolitaireCheckersConfiguration.class, configuration) );
        this.setConfiguration( SolitaireCheckersConfigurations.valueOf(configuration) );
    }

    /**
     * Set this game's configuration.
     * @param SolitaireCheckersConfiguration new configuration
     */
    public void setConfiguration( SolitaireCheckersConfigurations configuration ) {
        this.configuration = configuration;

        switch( configuration ) {

            case Arrow:
                    this.configureAsArrow();
                    break;

            case Cross:
                    this.configureAsCross();
                    break;

            case Diamond:
                    this.configureAsDiamond();
                    break;

            case DoubleArrow:
                    this.configureAsDoubleArrow();
                    break;

            case Fireplace:
                    this.configureAsFireplace();
                    break;

            case Plus:
                    this.configureAsPlus();
                    break;

            case Pyramid:
                    this.configureAsPyramid();
                    break;

            case Solitaire:
                    this.configureAsSolitaire();
                    break;

            default:
                    this.setConfiguration( SolitaireCheckersConfigurations.Solitaire );
                    break;
        }

        //XXX bug-fix of phantom yellow cells when configuration is changed
        this.clearPossibleJumps();

        this.setStatus( this.toString() );
        this.updateObservers();
    }

    /**
     * Set the number of starting pegs.
     * @param int the number of starting pegs
     */
    private void setNumberOfStartingPegs( int count ) {
        this.numberOfStartingPegs = count;
    }

    /**
     * Set this game's status.
     * @param String new status
     */
    private void setStatus( String status ) {
        this.status = new String( status );
//        this.status.insert(0, status);
//        this.status.append("\n");
    }

    /**
     * Start this game.
     */
    @Override
    public void start() {
        this.reset();
    }

    /**
     * Return this game's configuration as a string.
     * @return String
     */
    @Override
    public String toString() {
        StringBuffer s;

        s = new StringBuffer( "Solitaire Checkers in " );
        s.append( this.getConfiguration().toString() );
        s.append( " configuration" );

        return s.toString();
    }

    /**
     * Transpose the column for position row1, column1 compared to row2.
     * @param int row1
     * @param int column1
     * @param int row2
     * @return int the transposed column for row2
     */
    private int transposeColumn( int row1, int column1, int row2 ) {
        int column2;

        if ( board[row1].length == board[row2].length ) {
            column2 = column1;
        } else {
            if ( board[row1].length == MAX_COLUMNS ) {
                column2 = column1 - DELTA;
            } else {
                column2 = column1 + DELTA;
            }
        }

        return column2;
    }

    /**
     * The game has changed state!
     * Inform all registered observers.
     */
    private void updateObservers() {
        this.setChanged();
        this.notifyObservers();
    }
}