package uk.ac.soton.comp1206.event;
/**
 * The GameOverListener interface provides a contract for classes that want to listen
 * for events related to the game over state.
 */
public interface GameOverListener {
    /**
     * Called when the game is over.
     */
    void gameOver();
}
