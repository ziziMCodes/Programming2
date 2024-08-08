package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.network.Communicator;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** A multiplayer game extending the base Game object adding online functionality. */
public class MultiplayerGame extends Game{
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private final Communicator communicator;
    private final Queue<GamePiece> piecesQueue = new LinkedList<>();
    private int countPieceListener;
    Timer timer;
    int countpiece = 0;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param communicator the communicator to use
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(Communicator communicator, int cols, int rows) {
        super(cols, rows);
        piecesQueue.clear();
        this.communicator = communicator;
        setTimerTask();
        communicator.send("PIECE");
    }

    /**
     * Handles the game piece.
     */
    public void handlePiece() {

        if (countPieceListener < 1) {
            countPieceListener++;
            communicator.addListener(piece -> {
                if (piece.startsWith("PIECE")) {
                    int pieces = parseInt(piece.substring(6));
                    Platform.runLater(() -> {
                      spawnPiece(pieces);
                    });
                }
            });
        }
        communicator.send("PIECE");
    }

    /**
     * Parses a string to an integer.
     * @param string the string to parse
     * @return the parsed integer
     */

    public int parseInt(String string){
        try{
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e){
            System.exit(0);
            return 0;
        }

    }
    /**
     * Spawns a game piece with a given index.
     *
     * @param index the index of the game piece to spawn
     * @return the spawned game piece
     */
    public GamePiece spawnPiece(int index){
        var piece = GamePiece.createPiece(index);
        logger.info("Spawning piece MULTIPLAYERPIECE: {}", piece);
        countpiece++;
        logger.info("countpiece: " + countpiece);
       // int count = piecesQueue.size();
        logger.info("current Queue size: {}", piecesQueue.size());
        piecesQueue.add(piece);
        logger.info("Following piece: {}", followingPiece);
        //logger.info("Current  piece: MONKEY {}", currentPiece);

        logger.info("New queue size: {}", piecesQueue.size());
        logger.info("Picking random piece: {}", piece);
        return piece;
    }
    @Override
    public void afterPiece() {
        HashSet<GameBlockCoordinate> clearBlocks = new HashSet<>();
        int linesCleared = 0;

        for (int y = 0; y < rows; y++) {
            boolean rowsFull = true;
            for (int x = 0; x < cols; ++x) {
                logger.info("grid.get {}", grid.get(x,y));
                if (grid.get(x, y) == 0) {
                    rowsFull = false;
                    logger.info("rowsFull: {}", rowsFull);
                    break;
                }
            }
            logger.info("rowsFull: {}", rowsFull);
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
            communicator.send("SCORE " + score.get());

        }
        else{
            multiplier.set(1);
            multiplierString.set("X1");
        }
        if(!clearBlocks.isEmpty() && lineClearedListener!=null){
            lineClearedListener.lineCleared(clearBlocks);
        }
        level.set(score.get()/1000);
        nextPiece();
    }
    @Override
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        MultiMedia.stopAll();
        MultiMedia.playBackground("multiplayerScene.mp3");
        startGameLoop();
    }
    /**
     * Sends the current game board to the server.
     */
    public void sendBoard(){
        SimpleIntegerProperty[][] gridArray = grid.getGrid();
        String board = "BOARD ";
        for(int x=0; x<gridArray.length; x++){
            for(int y=0; y<gridArray[0].length; y++){

                board = board + gridArray[x][y]+" ";
            }
        }
    }

    /**
     * Finishing a game loop cycle
     */
    @Override
    public void gameLoop() {
        Platform.runLater(() -> {
            if (lives.get() >0) {
                lives.set(lives.get() - 1);
                communicator.send("LIVES " + lives.get());
            } else {
                logger.info("game loop game over");
                communicator.send("DIE");
                gameOver();
                MultiMedia.stopAll();
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
        nextLoop = execute.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        MultiMedia.playBackground("multiplayerScene.mp3");
        // Initializing bindable values
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);

        communicator.send("PIECE");


       // logger.info("POTATO: {}", piecesQueue.size());

        followingPiece = piecesQueue.poll();

        //logger.info("Following piece in initialise WATER: {}", followingPiece);
        nextPiece();

    }

    /**
     * Reassign the following piece to the current one
     * generate a new game piece and assign it to the following piece
     */
    @Override
    public void nextPiece() {
        logger.info("Following piece b4: {}", followingPiece);
        currentPiece = followingPiece;
        followingPiece = piecesQueue.poll();
        communicator.send("PIECE");
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
        logger.info("Following piece: {} and current piece: {}", followingPiece, currentPiece);


    }

    /**
     * Sets a timer task for the game.
     */
    public void setTimerTask(){
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handlePiece();
            }
        }, 0, 50);
    }
    /**
     * Returns the game grid.
     *
     * @return the game grid
     */
    @Override
    public Grid getGrid() {
        return grid;
    }

    @Override
    public void stop() {
        logger.info("Stopping game!");
        lives.set(0);
        if (nextLoop != null) {
            nextLoop.cancel(true);
        }
        timer.cancel();
        communicator.clearListeners();
        piecesQueue.clear();
        execute.shutdown();

    }
    @Override
    public int getTimerDelay(){
        int baseTime = 12000;
        int time = baseTime - 2500 * level.get();
        return Math.max(time, 2500);
    }
    @Override
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("Block clicked: {},{}", x, y);
        logger.info("Current piece: HEHE {}", currentPiece);
        if (grid.canPlayPiece(currentPiece,x,y)) {
            sendBoard();
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
     * This method is used to handle the situation when the following piece is null.
     * It retrieves the next piece from the queue and assigns it to the following piece.
     * If the following piece is not null, this method does nothing.
     */
    public void pieceboard(){
        if(followingPiece == null) {
            logger.info("FOLLOWING PIECE NULL... ADDING PIECE ");
            GamePiece test = piecesQueue.poll();
            logger.info("Test piece: {}", test);
            followingPiece = test;
            nextPiece();

        }
    }

}
