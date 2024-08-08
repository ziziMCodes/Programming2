package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import uk.ac.soton.comp1206.event.LeftClickedListener;
import uk.ac.soton.comp1206.game.GamePiece;
/**
 * A PieceBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The PieceBoard can hold an internal grid of its own, for example, for displaying an upcoming block. It also is
 * linked to an external grid, for the main game board.
 *
 * The PieceBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class PieceBoard extends GameBoard{
    protected LeftClickedListener leftClickedListener;
    /**
     * Constructs a PieceBoard with the specified number of columns, rows, width, and height.
     * Additionally, sets an event handler to handle mouse clicks on the board,
     * triggering the leftClick method when the primary mouse button is clicked.
     *
     * @param cols   the number of columns in the PieceBoard
     * @param rows   the number of rows in the PieceBoard
     * @param width  the width of the PieceBoard
     * @param height the height of the PieceBoard
     */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
        this.setOnMouseClicked(event  -> {
            if(event.getButton()==MouseButton.PRIMARY){
                leftClick();
            }
        });
    }
    /**
     * Sets the current game piece on the board, clearing the board first and then placing the new piece.
     * This method updates the grid with the values from the provided GamePiece object.
     *
     * @param piece the GamePiece to set on the board
     */
    public void setPiece(GamePiece piece){
        clearBoard();
        int[][] pieceGrid = piece.getGrid();
        //Columns and rows, update manually
        //3 by 3 grid
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                grid.set(i,j,pieceGrid[i][j]);
            }
        }
    }

    /**
     * Places a dot at the centre of the pieceboard
     */
    public void dot(){
        //calculate centre coordinates
        double x = Math.ceil((double) getRows()/2)-1;
        double y = Math.ceil((double) getCols()/2)-1;
        blocks[(int)x][(int) y].setDot();
    }

    /**
     * Handles the left click event on the PieceBoard.
     * If a leftClickedListener is set, it invokes the onLeftClicked method of the listener.
     */
    private void leftClick() {
        if (this.leftClickedListener != null) {
            this.leftClickedListener.setOnLeftClicked();
        }
    }
    /**
     * Sets the listener to handle an event when the PieceBoard is left-clicked.
     *
     * @param handler the listener to add
     */
    public void setOnLeftClick(LeftClickedListener handler) {
        leftClickedListener = handler;
    }




}
