package uk.ac.soton.comp1206.scene.basescenes;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.scene.LobbyScene;
import uk.ac.soton.comp1206.scene.MultiplayerScene;
import uk.ac.soton.comp1206.ui.GameWindow;

import static uk.ac.soton.comp1206.scene.LobbyScene.communicator;

/**
 * A base tetris scene used for game scenes.
 */
public abstract class TetrisBaseScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(TetrisBaseScene.class);
    /**
     * Bindable properties
     */
    protected final IntegerProperty score = new SimpleIntegerProperty(0);
    protected final IntegerProperty highscore = new SimpleIntegerProperty(0);
    protected final StringProperty multiplier = new SimpleStringProperty("X1");

    protected PieceBoard currentPieceBoard;
    protected PieceBoard nextPieceBoard;

    protected Text multiplierText;

    /**
     * UI
     */
    protected VBox sideVbox;
    protected GridPane topPane;
    protected BorderPane borderPane;
    protected GameBoard gameBoard;
    protected Rectangle timer;
    protected StackPane timerPane;

    /**
     *
     * Create a new scene, passing in the GameWindow which the scene will be present in
     * @param gameWindow the game window
     */
    public TetrisBaseScene(GameWindow gameWindow) {
        super(gameWindow);
    }
    /**
     * Handle key press events
     *
     * @param keyEvent the key event object
     */
    protected void keyPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Keycode: {}", keyCode);
        int xKey = 0;
        int yKey = 0;
        if(!(scene.getFocusOwner() instanceof javafx.scene.control.TextField)) {
            if (gameBoard.getHoverX() == -1 && gameBoard.getHoverY() == -1) {
                gameBoard.updateHover(0, 0);
            }

            // Keyboard mode positions
            xKey = gameBoard.getHoverX();
            yKey = gameBoard.getHoverY();

            // Exit game scene with keyboard
            checkGameExit(keyCode);

            // Place a game piece
            if (keyCode.equals(KeyCode.ENTER) || keyCode.equals(KeyCode.X)) {
                blockClicked(gameBoard.getBlock(xKey, yKey));
            }

            // Move piece to the left

            switch (keyCode) {
                case LEFT, A -> gameBoard.updateHover(Math.max((xKey - 1), 0), yKey);
                case UP, W -> gameBoard.updateHover(xKey, Math.max(yKey - 1, 0));
                case RIGHT, D -> gameBoard.updateHover(Math.min((xKey + 1), gameBoard.getCols() - 1), yKey);
                case DOWN, S -> gameBoard.updateHover(xKey, Math.min(yKey + 1, gameBoard.getRows() - 1));
            }
            // Swap pieces
            if (keyCode.equals(KeyCode.SPACE) || keyCode.equals(KeyCode.R)) {
                logger.info("Swap");
                swapBlock();
                logger.info("Swapblock done");
            }

            // Rotate left (triple rotation to right to achieve it)
            if (keyCode.equals(KeyCode.Q)
                    || keyCode.equals(KeyCode.Z)
                    || keyCode.equals(KeyCode.OPEN_BRACKET)) {
                rotateLeft();
            }

            // Rotate right
            if (keyCode.equals(KeyCode.E)
                    || keyCode.equals(KeyCode.C)
                    || keyCode.equals(KeyCode.CLOSE_BRACKET)) {
                rotateRight();
            }

            // Discard block and reset game loop
            if (keyCode.equals(KeyCode.V)) {
                singleGame.gameLoop();
            }
            if (keyCode.equals(KeyCode.ESCAPE)) {
                if(communicator!=null){

                    communicator.send("PART");
                    LobbyScene.currentChannel.set("");

                }
                if(singleGame!=null) {
                    singleGame.stop();
                    singleGame = null;

                }
                gameWindow.startMenu();
            }
            logger.info("Key press done: ");
        }

        // Refresh hovered block
        gameBoard.hoverRefresh(gameBoard.getBlock(xKey, yKey));
        gameBoard.requestFocus();
    }

    /**
     * Manage the game loop bar animation.
     *
     * @param nextLoop the length of the game loop
     */
    protected void gameLoop(int nextLoop) {
        var greenKeyFrame =
                new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN));
        var startingLengthKeyFrame =
                new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), timerPane.getWidth()));

        var yellowKeyFrame =
                new KeyFrame(
                        new Duration((double) nextLoop * 0.5),
                        new KeyValue(timer.fillProperty(), Color.YELLOW));

        var redKeyFrame =
                new KeyFrame(
                        new Duration((double) nextLoop * 0.75), new KeyValue(timer.fillProperty(), Color.RED));

        var endingLengthKeyFrame =
                new KeyFrame(new Duration(nextLoop), new KeyValue(timer.widthProperty(), 0));

        // Setting up the timeline for the timer bar
        Timeline timeline =
                new Timeline(
                        greenKeyFrame,
                        startingLengthKeyFrame,
                        yellowKeyFrame,
                        redKeyFrame,
                        endingLengthKeyFrame);

        // Start the animation
        timeline.play();

    }

    /**
     * Change the value of the score in the game
     * @param oldScore the old score
     * @param newScore the new score
     */
    protected void setScore(Number oldScore, Number newScore) {
        if (newScore.intValue() > highscore.get()) {
            highscore.set(newScore.intValue());
        }

        // Set up the animation for score increase
        var startValueKeyFrame = new KeyFrame(Duration.ZERO, new KeyValue(score, oldScore));
        var endValueKeyFrame = new KeyFrame(new Duration(150), new KeyValue(score, newScore));
        var timeline = new Timeline(startValueKeyFrame, endValueKeyFrame);

        timeline.play();
    }
    protected void setupBorderPane(StackPane pane){
        borderPane = new BorderPane();
        pane.getChildren().add(borderPane);
        gameBoard = new GameBoard(singleGame.getGrid(), gameWindow.getWidth() / 2.0, gameWindow.getWidth() / 2.0);
        gameBoard.setGame(singleGame);
        borderPane.setCenter(gameBoard);

    }
    protected void setupTopPane(String mode) {
        topPane = new GridPane();
        topPane.setPadding(new Insets(10));
        topPane.setHgap(20);
        topPane.setAlignment(Pos.CENTER);


        borderPane.setTop(topPane);
        Text title = new Text(mode);
        title.getStyleClass().add("title");
        topPane.add(title,1,0);
        GridPane.setHalignment(title, HPos.CENTER);
        createScoreInfo();
        createMultiplierInfo();
    }

    protected void setupSideVbox(){
        //sideVBox contains the score, level, highscore, lives and pieceboards
        sideVbox = new VBox();
        sideVbox.setAlignment(Pos.CENTER);
        sideVbox.setSpacing(5);
        sideVbox.setPadding(new Insets(5,5,5,5));
        borderPane.setLeft(sideVbox);

        createLivesInfo();
        createHighScoreInfo();
        createLevelInfo();
    }
    private void createScoreInfo() {
        VBox scoreBox = new VBox();
        scoreBox.setPrefWidth(140);
        scoreBox.setAlignment(Pos.CENTER);
        Text scoreLabel = new Text("Score");
        scoreLabel.getStyleClass().add("heading");
        scoreBox.getChildren().add(scoreLabel);

        Text scoreField = new Text("0");
        scoreField.getStyleClass().add("score");
        scoreField.textProperty().bind(singleGame.scoreProperty().asString());
        scoreBox.getChildren().add(scoreField);
        topPane.add(scoreBox, 0, 0);
    }

    protected void createLevelInfo() {
        Text levelLabel = new Text("Level");
        levelLabel.getStyleClass().add("heading");
        sideVbox.getChildren().add(levelLabel);

        Text levelField = new Text("0");
        levelField.getStyleClass().add("level");
        levelField.textProperty().bind(singleGame.levelProperty().asString());
        sideVbox.getChildren().add(levelField);
    }

    protected void createLivesInfo() {
        VBox livesBox = new VBox();
        livesBox.setAlignment(Pos.CENTER);
        Text livesLabel = new Text("Lives");
        livesLabel.getStyleClass().add("heading");
        livesBox.getChildren().add(livesLabel);

        Text livesField = new Text("0");
        livesField.getStyleClass().add("lives");
        livesField.textProperty().bind(singleGame.livesProperty().asString());
        livesBox.getChildren().add(livesField);
        topPane.add(livesBox, 2, 0);
    }

    private void createMultiplierInfo() {
        VBox box = new VBox();
        box.setPrefWidth(130);
        box.setAlignment(Pos.CENTER);

        multiplierText = new Text("X1");
        multiplierText.getStyleClass().add("score");
        multiplierText.getStyleClass().add(multiplier.get());
        multiplierText.textProperty().bind(singleGame.multiplierStringProperty());
        box.getChildren().add(multiplierText);
        topPane.add(box, 0, 1);
    }
    /** Generate the highscore related UI elements */
    public void createHighScoreInfo(){
        Text highscoreLabel = new Text("High Score");
        highscoreLabel.getStyleClass().add("heading");
        sideVbox.getChildren().add(highscoreLabel);

        Text highscoreField = new Text("0");
        highscoreField.getStyleClass().add("hiscore");
        sideVbox.getChildren().add(highscoreField);
        highscoreField.textProperty().bind(highscore.asString());


    }

    /** Generate the pieceBoard related UI elements. */
    protected void setupPieceBoards() {
        Text nextPieceLabel = new Text("Current Piece");
        nextPieceLabel.getStyleClass().add("heading");
        borderPane.getChildren().add(nextPieceLabel);

        currentPieceBoard = new PieceBoard(3, 3, 150, 150);
        nextPieceBoard = new PieceBoard(3, 3, 100, 100);
        currentPieceBoard.dot();

        VBox pieceBox = new VBox(10, nextPieceLabel, currentPieceBoard, nextPieceBoard);
        pieceBox.setAlignment(Pos.CENTER);
        borderPane.setRight(pieceBox);
    }

    /**
     * Generate the timer for the scene
     */

    protected void timer() {
        timerPane = new StackPane();
        timer = new Rectangle(400, 30);
        timer.setFill(javafx.scene.paint.Color.RED);
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(timer.widthProperty(), 400)
                ),
                new KeyFrame(
                        Duration.seconds(3),
                        new KeyValue(timer.widthProperty(), 0)
                )
        );
        timeline.play();
        timerPane.getChildren().add(timer);
        StackPane.setAlignment(timer, Pos.CENTER_LEFT);

        BorderPane.setMargin(timerPane, new Insets(5, 5, 5, 5));

        borderPane.setBottom(timerPane);
    }


    protected abstract void checkGameExit(KeyCode keyCode);
    protected abstract void blockClicked(GameBlock gameBlock);
    protected abstract void swapBlock();

    /**
     * Set up the game object and model
     */
    public abstract void setupGame();

    /**
     * Rotate the current piece to the left
     */
    public abstract void rotateLeft();

    /**
     * Rotate the current piece to the right
     */
    public abstract void rotateRight();
}
