package uk.ac.soton.comp1206.game;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.component.MultiMedia;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static javafx.application.Platform.runLater;


/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    protected static final Logger logger = LogManager.getLogger(Game.class);
    /**
     * Bindable properties
     */
    protected final IntegerProperty score = new SimpleIntegerProperty(0);
    protected final IntegerProperty level = new SimpleIntegerProperty(0);
    protected final IntegerProperty lives = new SimpleIntegerProperty(3);
    protected final IntegerProperty multiplier = new SimpleIntegerProperty(1);
    protected final StringProperty multiplierString = new SimpleStringProperty("");

    private String mode;

    /**
     * Listeners
     */
    protected NextPieceListener nextPieceListener;
    protected LineClearedListener lineClearedListener;
    protected GameLoopListener gameLoopListener;
    protected GameOverListener gameOverListener;


    protected GamePiece currentPiece;
    protected GamePiece followingPiece;
    /**
     * Game Loop
     */
    protected final ScheduledExecutorService execute;
    protected ScheduledFuture<?> nextLoop;
    protected final int rows;
    protected final int cols;
    protected Grid grid;

    protected final Random random = new Random();


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        this.grid = new Grid(cols, rows);
        execute = Executors.newSingleThreadScheduledExecutor();

    }
    public Game(int cols, int rows, String mode) {
        this.cols = cols;
        this.rows = rows;

        this.grid = new Grid(cols, rows);
        execute = Executors.newSingleThreadScheduledExecutor();
        this.mode = mode;

    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        MultiMedia.stopAll();
        MultiMedia.playBackground("challengeScene.mp3");
        startGameLoop();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        // Initializing bindable values
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);

        followingPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Reassign the following piece to the current one
     * generate a new game piece and assign it to the following piece
     */

    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();

        logger.info("The next piece is {}", currentPiece);

        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
        logger.info("nextPiece method successfully called");


    }
    /**
     * Creates a new game piece and returns it.
     *
     * @return a new random game piece
     */

    public GamePiece spawnPiece() {
        var maxPieces = 14;
        var randomPiece = GamePiece.createPiece(random.nextInt(maxPieces));
        logger.info("Picking random piece: {}", randomPiece);
        return randomPiece;

    }

    /**
     * Handles the click event on a GameBlock.
     * Checks if the current piece can be placed at the position of the clicked block.
     * If the piece can be placed, it updates the grid, plays a placement sound, and performs actions after placing the piece.
     *
     * @param gameBlock The GameBlock that was clicked.
     * @return {@code true} if the piece was successfully placed at the clicked block, {@code false} otherwise.
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("Block clicked: {},{}", x, y);
        if (grid.canPlayPiece(currentPiece,x,y)) {
            if (grid.playPiece(currentPiece, x, y)) {
                MultiMedia.playAudio("place.wav");
                afterPiece();
                return true;
            }
        }
        logger.error("Cant add piece at {}, {}", x, y);
        return false;
    }

    /**
     * Checks for and clears full rows and columns after placing a piece on the game grid.
     * Updates the score based on the lines cleared and resets the multiplier if no lines are cleared.
     */
    public void afterPiece() {
        HashSet<GameBlockCoordinate> clearBlocks = new HashSet<>();
        int linesCleared = 0;

        for (int y = 0; y < rows; y++) {
            boolean rowsFull = true;
            for (int x = 0; x < cols; ++x) {
                //logger.info("grid.get {}", grid.get(x,y));
                if (grid.get(x, y) == 0) {
                    rowsFull = false;
                    break;
                }
            }
            if (rowsFull) {
                linesCleared++;
                for (int x = 0; x < cols; ++x) {
                    clearBlocks.add(new GameBlockCoordinate(x, y));
                    grid.set(x, y, 0);
                }
            }
        }
        for (int x = 0; x < cols; ++x) {
            boolean colsFull = true;
            for (int y = 0; y < rows; y++) {
                if (grid.get(x, y) == 0) {
                    colsFull = false;
                    break;
                }
            }
            if (colsFull) {
                linesCleared++;
                for (int y = 0; y < rows; ++y) {
                    clearBlocks.add(new GameBlockCoordinate(x, y));
                    grid.set(x, y, 0);
                }
            }
        }
        logger.info("Lines cleared: {}", linesCleared);

        if(linesCleared > 0){
            int increaseScore = linesCleared * clearBlocks.size() * 10 * multiplier.get();
            score.set(score.get() + increaseScore);
            multiplier.set(multiplier.get()+1);
            multiplierString.set("X" + multiplier.get());
            logger.info("Clear blocks size: {} MUltiplier: {}", clearBlocks.size(), multiplier.get());
        }
        else{
            multiplier.set(1);
            multiplierString.set("X1");
        }
        //if i already linked the listener in challenge scene
        if(!clearBlocks.isEmpty() && lineClearedListener!=null){
            lineClearedListener.lineCleared(clearBlocks);
        }
        level.set(score.get()/1000);
        nextPiece();
    }


    /**
     * Rotates the current game piece by the specified amount.
     *
     * @param rotate the amount by which to rotate the piece. Positive values rotate clockwise, negative values rotate counterclockwise.
     */
    public void rotateCurrentPiece(int rotate){
        if(currentPiece!= null) {
            currentPiece.rotate(rotate);
            MultiMedia.playAudio("rotate.wav");
        }
    }
    /**
     * Rotates the current piece clockwise if it exists.
     * Plays a rotation sound after the rotation is performed.
     */
    public void rotateCurrentPiece(){
        if(currentPiece!= null) {
            currentPiece.rotate();
            MultiMedia.playAudio("rotate.wav");
        }
    }
    /**
     * Swaps the current piece with the following piece.
     * Plays a transition sound after the swap is performed.
     */
    public void swapCurrentPiece(){
        logger.info("Swapping piece");
        GamePiece temp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = temp;
        MultiMedia.playAudio("transition.wav");
    }



    /**
     * Timer delay for the next game loop cycle.
     *
     * Decides between the calculated time and the minimum time.
     *
     * @return the time in ms
     */
    public int getTimerDelay() {
        int baseTime = switch (mode) {
            case "easy" -> 14000;
            case "normal" -> 12000;
            case "hard" -> 8000;
            default -> 12000;
        };
        // This method calculates the delay for the next game loop cycle based on the current level of the game.
        // The delay decreases as the level increases, with a minimum delay of 2500 milliseconds.
        int time = baseTime - 2500 * level.get();
        return Math.max(time, 2500);
    }

    /**
     * Start the game loop
     */
    public void startGameLoop() {
        nextLoop = execute.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if (gameLoopListener != null) {
            gameLoopListener.gameLoopStarted(getTimerDelay());
        }
    }

    /**
     * Restart game loop
     */
    public void restartGameLoop() {
        if (nextLoop != null) {
            nextLoop.cancel(false);
        }
        startGameLoop();
    }
    /**
     * Finishing a game loop cycle
     */
    public void gameLoop() {
        Platform.runLater(() -> {
            if (lives.get() >0) {
                lives.set(lives.get() - 1);
            } else {
                logger.info("game loop game over");
                gameOver();
            }

            multiplier.set(1);
            multiplierString.set("X1");

            currentPiece = null;

            nextPiece();

            int timerDelay = getTimerDelay();

            if (gameLoopListener != null) {
                gameLoopListener.gameLoopStarted(timerDelay);
            }
        });

        nextLoop.cancel(false);
        //lowTimerCancel();
        nextLoop = execute.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        //setLowTime();
    }

    /**
     * Stopping the game when it is over.
     */
    protected void gameOver() {
        logger.info("Game over!");
        if (gameOverListener != null) {
            runLater(() -> gameOverListener.gameOver());
        }
    }
    /** Stopping the game */
    public void stop() {
        logger.info("Stopping game!");
        lives.set(0);
        nextLoop.cancel(true);
    }


    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }


    public int getScore() {
        return score.get();
    }

    /**
     * Get the level
     * @return score
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Get the level
     * @return level
     */
    public IntegerProperty levelProperty() {
        return level;


    }
    /**
     * Get the lives
     * @return the lives
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     * Set the NextPieceListener
     *
     * @param nextPieceListener the NextPieceListener to set
     */

    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;

    }

    /**
     * Get the following piece
     *
     * @return the following piece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * Set the LineClearedListener
     *
     * @param listener the LineClearedListener to set
     */
    public void setOnLineCleared(LineClearedListener listener) {
        lineClearedListener = listener;
    }

    /**
     * Set the GameLoopListener
     *
     * @param gameLoopListener the GameLoopListener to set
     */

    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }
    /**
     * Set the GameOverListener
     *
     * @param gameOverListener the GameOverListener to set
     */
    public void setOnGameOver(GameOverListener gameOverListener){this.gameOverListener = gameOverListener;}
    /**
     * Get the current piece
     *
     * @return the current piece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }
    /**
     * Get the multiplier string property
     *
     * @return the multiplier string property
     */
    public StringProperty multiplierStringProperty() {
        return multiplierString;
    }
}
