package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;



import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * MultiplayerScene class extends ChallengeScene to provide a multiplayer game mode.
 * It handles the game logic, user interface, and communication with the server for multiplayer mode.
 */
public class MultiplayerScene extends ChallengeScene {
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    protected final Communicator communicator;
    private Leaderboard leaderboard;
    private VBox chat;
    TextField chatInput;
    TextArea chatHistory;
    private int count = 0;
    private Timer timer;
    private Timer scoresTimer;



    /**
     * Create a new challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
        setTimerTask();
    }
    /**
     * Builds the multiplayer scene including the game board, leaderboard, and other UI components.
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        setupGame();

        var challengePane = new StackPane();

        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.getStyleClass().add("multiplayer-background");

        setupBorderPane(challengePane);
        root.getChildren().add(challengePane);

        setupPieceBoards();
        setupTopPane("Multiplayer Mode");
        setupSideVbox();
        timer();
        leaderboardUpdate();
        //methods linked together
        gameBoard.setOnBlockClick(this::blockClicked);
        gameBoard.setOnRightClick(this::rotateRight);
        currentPieceBoard.setOnLeftClick(this::rotateRight);
        nextPieceBoard.setOnBlockClick(this::swapBlock);
        gameBoard.setOnMouseEntered(e -> {
            if(singleGame instanceof MultiplayerGame){
                ((MultiplayerGame) singleGame).pieceboard();
            }
        });
        gameBoard.requestFocus();



    }

    /**
     * Sends a request to the server to get the scores of all players.
     */
    public void requestScores(){
        communicator.addListener(msg -> {
            if(msg.startsWith("SCORES")){
                parseScores(msg.substring(7));
            }
        });
        communicator.send("SCORES");
    }

    /**
     * Parses the scores received from the server and updates the leaderboard.
     *
     * @param msg the message received from the server containing the scores
     */
    public void parseScores(String msg){
        List<Pair<String, Pair<Integer, String>>> user = new ArrayList<>();
        //SCORES <Player>:<Score>:<Lives|DEAD>\n<Player>:<Score>:<Lives|DEAD>\n<Player>:<Score>:<Lives|DEAD>\n
        String[] array = msg.split("\n");
        for(String string : array){
            //<Player>:<Score>:<Lives|DEAD>
            String[] split = string.split(":");
            if(parseInt(split[1]) != -1){
                user.add(new Pair<>(split[0], new Pair<>(parseInt(split[1]), split[2])));
            }
        }
        Platform.runLater(() -> {
            leaderboard.updateLeaderboard(user);
        });
    }

    /** Set up the game object and model */
    @Override
    public void setupGame() {
        logger.info("Starting a new challenge");
        singleGame = new MultiplayerGame(communicator, 5, 5);
        singleGame.setNextPieceListener(this::updatePieceBoards);
    }

    /**
     * Sets up the BorderPane which includes the game board and the leaderboard.
     *
     * @param pane the StackPane to which the BorderPane is added
     */
    protected void setupBorderPane(StackPane pane){
        borderPane = new BorderPane();
        pane.getChildren().add(borderPane);
        gameBoard = new GameBoard(singleGame.getGrid(), gameWindow.getWidth() / 2.5, gameWindow.getWidth() / 2.5);
        gameBoard.setGame(singleGame);
        borderPane.setCenter(gameBoard);
        leaderboard = new Leaderboard();
        borderPane.setRight(leaderboard);
    }
    /**
     * Sets a timer task that sends a request to the server for scores at fixed intervals.
     */
    public void setTimerTask(){
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                    communicator.send("SCORES");
            }
        }, 0, 20);
    }

    /**
     * Sets up the VBox on the side of the screen which includes the leaderboard and other game info.
     */
    @Override
    protected void setupSideVbox(){
        //sideVBox contains the score, level, highscore, lives and pieceboards
        sideVbox = new VBox();
        sideVbox.setAlignment(Pos.TOP_LEFT);
        sideVbox.setSpacing(5);
        sideVbox.setPadding(new Insets(5,5,5,5));
        borderPane.setLeft(sideVbox);
        sideVbox.getChildren().add(leaderboard);

        createLivesInfo();
        createHighScoreInfo();
        createLevelInfo();
        Button leave = new Button("Leave");
        leave.setOnAction(event -> {
            part();
        });
        sideVbox.getChildren().add(leave);

        chat = new VBox();
        chatHistory = new TextArea();
        chatHistory.setPrefSize(100,100);
        chatHistory.setEditable(false);
        chatInput = new TextField();
        chatInput.setFocusTraversable(true);
        chatInput.setOnMouseClicked(e -> {
            chatInput.requestFocus();

        });
        chatInput.setPromptText("Send chat");
        chatInput.setOnAction(e -> sendMessage(chatInput.getText()));
        Text msgLabel = new Text("Messages:");
        msgLabel.getStyleClass().add("heading");
        chat.getChildren().addAll(msgLabel, chatHistory, chatInput);
        sideVbox.getChildren().add(chat);
    }
    /**
     * Sets a timer task that updates the leaderboard at fixed intervals.
     */
    public void leaderboardUpdate(){
        scoresTimer = new Timer(true);
        scoresTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                requestScores();
                logger.info("Requested scores...");

            }
        }, 0, 2000);
    }

    /**
     * Sends a message to the server to be broadcasted to all players.
     * @param message The message to be sent
     */
    public void sendMessage(String message){
        //count variable ensures we have one listener at a time
        if(count<1) {
            count++;
            communicator.addListener(sms -> {
                if (sms.startsWith("MSG")) {
                    Platform.runLater(() ->  {
                        chatHistory.appendText(sms.substring(4) + "\n");
                        MultiMedia.playAudio("message.wav");
                    });
                }
            });
        }

        if(message!=null && !message.isEmpty()) {
            communicator.send("MSG " + message);
        }
        chatInput.clear();
    }

    /**
     * Sends a request to the server to leave a channel.
     */
    public void part(){
        communicator.addListener(part -> {
            if(part.startsWith("PARTED")){
                logger.info("part method called with message: " + part);
            }
        });
        if (timer != null) {
            timer.cancel();
        }
        singleGame.stop();
        scoresTimer.cancel();
        communicator.send("PART");
        gameWindow.startMenu();
    }
    @Override
    protected void keyPress(KeyEvent keyEvent) {
        if(!(scene.getFocusOwner() instanceof javafx.scene.control.TextInputControl)){
            logger.info("Key Pressed: " + keyEvent.getCode());
            logger.info(scene.getFocusOwner());
            if(keyEvent.getCode().equals(KeyCode.ESCAPE)){
                if(communicator!=null){
                    communicator.send("PART");
                    LobbyScene.currentChannel.set("");
                    Platform.runLater(() -> {
                        timer.cancel();
                        scoresTimer.cancel();
                        timer.purge();
                        scoresTimer.purge();
                        logger.info("COCONUT");
                    });

                }
            }
            super.keyPress(keyEvent);
        }
        gameBoard.requestFocus();
    }
    @Override
    public int getTopScore(){
        String data = "multiplayerScores.txt";
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
    @Override
    protected void endGame() {
        logger.info("Ending game");
        if(communicator!=null){
            part();
        }

        singleGame.stop();
        MultiMedia.stopAll();
    }
    /** End the game on game over and show the score scene. */
    @Override
    public void gameOver(){
        MultiMedia.stopAll();
        singleGame.stop();
        timer.cancel();
        scoresTimer.cancel();
        timer.purge();
        scoresTimer.purge();
        gameWindow.startScoresScene(singleGame);
    }



}
