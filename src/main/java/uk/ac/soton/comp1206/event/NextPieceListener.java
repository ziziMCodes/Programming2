package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;
/** Next Piece Listener listens for when a new piece is generated. */
public interface NextPieceListener {
    /**
     * Handles the event of receiving the next game piece.
     *
     * @param currentPiece the current game piece
     */
    void nextPiece(GamePiece currentPiece, GamePiece followingPiece);
}
