package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;

import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.basescenes.TetrisBaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.component.MultiMedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * The single player challenge scene.
 */

public class ChallengeScene extends TetrisBaseScene {
    String mode;
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);


    /**
     * Create a new challenge scene
     * @param gameWindow the Game Window
     */

    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }
    public ChallengeScene(GameWindow gameWindow, String mode) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        this.mode = mode;
    }
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        //Set Listeners
        singleGame.setOnLineCleared(this::lineCleared);
        singleGame.livesProperty().addListener(ChallengeScene::setLives);
        singleGame.levelProperty().addListener(ChallengeScene::levelUp);
        singleGame.scoreProperty().addListener((observable, oldScore, newScore) -> setScore(oldScore, newScore));
        singleGame.setOnGameLoop(this::gameLoop);
        singleGame.setOnGameOver(this::gameOver);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPress);

        singleGame.start();
        gameBoard.requestFocus();

    }



    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        setupGame();

        var challengePane = new StackPane();

        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.getStyleClass().add("challenge-background");

        setupBorderPane(challengePane);
        root.getChildren().add(challengePane);


        setupPieceBoards();
        setupTopPane("Challenge Mode");
        setupSideVbox();
        timer();
        //methods linked together
        gameBoard.setOnBlockClick(this::blockClicked);
        gameBoard.setOnRightClick(this::rotateRight);
        currentPieceBoard.setOnLeftClick(this::rotateRight);
        nextPieceBoard.setOnBlockClick(this::swapBlock);
        gameBoard.requestFocus();
    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */

    @Override
    protected void blockClicked(GameBlock gameBlock) {

        if(singleGame.blockClicked(gameBlock)){
            MultiMedia.playAudio("place.wav");
            singleGame.restartGameLoop();
        }
        else {
            logger.info("Unable to place {}", gameBlock);
            MultiMedia.playAudio("fail.wav");
        }
        highscore.set(getTopScore());
    }

    /**
     * Handle key presses
     * @param keyEvent the key event object
     */

    @Override
    protected void keyPress(KeyEvent keyEvent) {
        if(!(scene.getFocusOwner() instanceof javafx.scene.control.TextInputControl)){
            logger.info("Key Pressed: " + keyEvent.getCode());
            logger.info(scene.getFocusOwner());
            super.keyPress(keyEvent);
        }
        gameBoard.requestFocus();
    }

    /**
     * Check if the E key is pressed to exit the scene and enter start menu
     * @param keyCode keycode
     */

    @Override
    protected void checkGameExit(KeyCode keyCode) {
        if (keyCode == KeyCode.E) {
            logger.info("Exiting game...");
            endGame();
            gameWindow.startMenu();
        }
    }
    /**
     * Rotates the current game piece clockwise (to the right) and updates the current piece board accordingly.
     */
    public void rotateRight() {
            singleGame.rotateCurrentPiece();
            currentPieceBoard.setPiece(singleGame.getCurrentPiece());
    }
    /**
     * Rotates the current game piece counter-clockwise (to the left) and updates the current piece board accordingly.
     */

    public void rotateLeft() {

        singleGame.rotateCurrentPiece(3);
        currentPieceBoard.setPiece(singleGame.getCurrentPiece());
    }

    /**
     * Swapping the current and next piece
     */
    protected void swapBlock(GameBlock block){
        logger.info("Swapping block");
        singleGame.swapCurrentPiece();
        currentPieceBoard.setPiece(singleGame.getCurrentPiece());
        nextPieceBoard.setPiece(singleGame.getFollowingPiece());
    }

    @Override
    protected void swapBlock() {
        logger.info("Swapping block");
        singleGame.swapCurrentPiece();
        currentPieceBoard.setPiece(singleGame.getCurrentPiece());
        nextPieceBoard.setPiece(singleGame.getFollowingPiece());
    }
    /** Set up the game object and model */
    @Override
    public void setupGame() {
        logger.info("Starting a new challenge");
        singleGame = new Game(5, 5, mode);
        singleGame.setNextPieceListener(this::updatePieceBoards);
    }
    /**
     * Updates the current piece board with the specified current game piece and the next piece board
     * with the specified next game piece.
     * @param currentPiece  the current game piece to be displayed on the current piece board
     * @param nextPiece     the next game piece to be displayed on the next piece board
     */
    public void updatePieceBoards(GamePiece currentPiece, GamePiece nextPiece){
        Platform.runLater(() -> {
            if(currentPiece == null){
                logger.info("Current piece is null");
                return;
            }
            currentPieceBoard.setPiece(currentPiece);
            nextPieceBoard.setPiece(nextPiece);

            currentPieceBoard.requestLayout();
            nextPieceBoard.requestLayout();
        });

    }

    /**
     * Notifies the game that a line has been cleared, and triggers visual effects and sound effects accordingly.
     *
     * @param clearedBlocks  the set of coordinates representing the blocks that were cleared
     */
    public void lineCleared(Set<GameBlockCoordinate> clearedBlocks) {
        // Pass the cleared blocks to the fadeOut method of the GameBoard
        gameBoard.fadeOut(clearedBlocks);
        MultiMedia.playAudio("clear.wav");
    }

    /**
     * Set the lives in the game
     * @param observable the observable value representing the number of lives
     * @param oldLives the old value of lives
     * @param newLives the new value of lives
     */
    private static void setLives(
            ObservableValue<? extends Number> observable, Number oldLives, Number newLives) {
        // Play sound when live count changes
        MultiMedia.playAudio(oldLives.intValue() > newLives.intValue() ? "lifelose.wav" : "lifegain.wav");
    }
    /**
     * Play a sound when leveling up.
     *
     * @param observable the observable object
     * @param initialLevel the old lives value
     * @param newLevel the new lives value
     */
    private static void levelUp(
            ObservableValue<? extends Number> observable, Number initialLevel, Number newLevel) {

        if (newLevel.intValue() != initialLevel.intValue()) {
            MultiMedia.playAudio("level.wav");
        }
    }
    /** Stop the game and music */
    protected void endGame() {
        logger.info("Ending game");

        singleGame.stop();
        MultiMedia.stopAll();
    }
    /** End the game on game over and show the score scene. */
    public void gameOver(){
        MultiMedia.stopAll();
        singleGame.stop();
        gameWindow.startScoresScene(singleGame);
    }

    /**
     * Get the top score from the scores file
     * @return the top score
     */
    public int getTopScore(){
        String data = "scores.txt";
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(-1);


        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(data))){
            String line;
            while((line = bufferedReader.readLine()) != null) {
                String[] score = line.split(":");
                if(parseInt(score[1]) != -1){
                    scores.add(parseInt(score[1]));
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            logger.error("An error occurred while loading scores.", e);
        }

        Collections.sort(scores, Collections.reverseOrder());
        if(scores.get(0) == -1){
            scores.add(250);
        }
        Collections.sort(scores, Collections.reverseOrder());
        if(singleGame.getScore() >= scores.get(0)){
            return singleGame.getScore();
        }
        else{
            return scores.get(0);
        }

    }

    /**
     * Parse the string to an integer
     * @param number the number to be parsed
     * @return the parsed number
     */
    public int parseInt(String number){
        int num = -1;
        try{
            num = Integer.parseInt(number);
            return num;
        } catch (NumberFormatException e) {
            logger.info("NULL number not valid");
            return -1;
        }
    }
}
