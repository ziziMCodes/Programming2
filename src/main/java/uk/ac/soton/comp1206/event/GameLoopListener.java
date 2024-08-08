package uk.ac.soton.comp1206.event;

/** Game Loop Listener is used for listening when the game loop is reset. */
public interface GameLoopListener {
    /**
     * Called when the game loop is started.
     *
     * @param timerDelay the delay (in milliseconds) between each iteration of the game loop
     */
    void gameLoopStarted(int timerDelay);
}
