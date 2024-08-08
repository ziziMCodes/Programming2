package uk.ac.soton.comp1206.scene;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.collections.FXCollections;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.component.ScoresList;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
/**
 * The Score Scene. Requests the remote scores and updates both the remote and local scores with the
 * new high score.
 */
public class ScoresScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final ObservableList<Pair<String, Integer>> localScores = FXCollections.observableArrayList();
    protected Communicator communicator;
    private final SimpleListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());
    private boolean nameInputDialogShown = false;
    private Game game;
    ScoresList scoresList;
    ScoresList remoteScoresList;



    /**
     * Build the score scene after the game is finished.
     *
     * @param gameWindow the game window that will hold the scene
     * @param game       the game with the scores and other data
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        communicator = gameWindow.getCommunicator();

    }

    /** Initialize the Score Scene */
    @Override
    public void initialise() {
        MultiMedia.stopAll();
        MultiMedia.playBackground("scoresScene.mp3");
        loadScores();
        loadOnlineScores();
        checkNewHighScore();
        manuallyUpdate();
        logger.info("Scores scene initialised.");
        scene.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) gameWindow.startMenu();
        });

    }

    /**
     * Build the scene
     */
    @Override
    public void build() {
        logger.info("Building scores scene...");
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        StackPane pane = new StackPane();
        pane.setMaxWidth(gameWindow.getWidth());
        pane.setMaxHeight(gameWindow.getHeight());
        pane.getStyleClass().add("scores-background");
        root.getChildren().add(pane);

        // Create the scores lists
        scoresList = new ScoresList();
        remoteScoresList = new ScoresList();

        // Bind the scores lists to their respective properties
        scoresList.scoreProperty().bind(new SimpleListProperty<>(localScores));
        remoteScoresList.scoreProperty().bind(remoteScores);

        // Create title labels for each score list

        Text localTitle = new Text("Local High Scores");
        if(game instanceof MultiplayerGame){
            localTitle.setText("Multiplayer High Scores");
        }
        Text onlineTitle = new Text("Online High Scores");

        // Apply styling to the title labels
        localTitle.getStyleClass().add("heading");
        onlineTitle.getStyleClass().add("heading");

        // Create a VBox to hold each score list and its title
        VBox localScoreBox = new VBox(localTitle, scoresList);
        VBox onlineScoreBox = new VBox(onlineTitle, remoteScoresList);

        // Apply spacing and alignment to the VBox containers
        localScoreBox.setSpacing(20);
        onlineScoreBox.setSpacing(20);

        // Set the alignment of the VBox containers to the center
        localScoreBox.setAlignment(Pos.CENTER_LEFT);
        onlineScoreBox.setAlignment(Pos.CENTER_RIGHT);

        // Create an HBox to hold the local and online score boxes
        HBox scoreBoxes = new HBox(localScoreBox, onlineScoreBox);

        // Apply spacing and alignment to the HBox container
        scoreBoxes.setSpacing(10);
        scoreBoxes.setAlignment(Pos.CENTER);

        // Create a BorderPane to hold the HBox container
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(scoreBoxes);

        // Add the BorderPane to the root pane
        pane.getChildren().add(borderPane);
        scoresList.animateScores();
        remoteScoresList.animateScores();

    }


    /**
     * Load the scores from scores.txt file
     * Have a default list of scores
     */

    public void loadScores(){
        String data;
        if(game instanceof MultiplayerGame){
            data = "multiplayerScores.txt";
        } else {
            data = "scores.txt";
        }
        Path path = Paths.get(data);
        if(Files.notExists(path)){
            logger.info("Going into if statement as file isnt a directory");
            localScores.add(new Pair<>("Madonna", 50));
            localScores.add(new Pair<>("Son", 100));
            localScores.add(new Pair<>("Zahra", 150));
            localScores.add(new Pair<>("Jungkook", 200));
            localScores.add(new Pair<>("The Weeknd", 250));
            writeScores();
        }
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(data))){
            localScores.clear();
            localScores.addAll(bufferedReader.lines()
                    .map(line -> {
                        String[] part = line.split(":");
                        return new Pair<>(part[0], Integer.parseInt(part[1]));
                    })
                    .sorted(Comparator.comparing(Pair<String, Integer> ::getValue).reversed())
                    .toList());
            logger.info("Scores loaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("An error occurred while loading scores.", e);
        }
    }


    /**
     * Write the scores to the scores.txt file
     */
    private void writeScores() {
        String data;
        if(game instanceof MultiplayerGame){
            data = "multiplayerScores.txt";
        } else {
            data = "scores.txt";
        }
        try(BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(data))){
            for(Pair<String, Integer> score : localScores){
                bufferedWriter.write(score.getKey() + ":" + score.getValue());
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if there are any high scores to add to the list of scores
     */
    public void checkNewHighScore(){
        logger.info("Check new high score");
        if (nameInputDialogShown) {
            logger.info("Dialogue shown true");

            return; // If the dialog has already been shown, exit the method
        }

        int finalScore = game.getScore();
        Optional<Pair<String, Integer>> highScore = localScores.stream().min(Comparator.comparing(Pair<String,Integer>::getValue));
        if(highScore.isEmpty() || finalScore > highScore.get().getValue()){
            TextInputDialog dialog = new TextInputDialog("player");
            dialog.setTitle("New Highscore!");
            dialog.setHeaderText("You have achieved a score of: " + finalScore + "!" );
            dialog.setContentText("Please enter your name: ");
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/dialogStyle.css")).toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                logger.info("Name: {} and score {}", name,finalScore);
                localScores.add(new Pair<>(name, finalScore));
                writeScores();
                writeOnlineScore(name, finalScore);
                nameInputDialogShown = true;

                logger.info("New high score added: {} - {}", name, finalScore);
            });
        }

    }

    /**
     * Load the online scores by sending a requeset to the server and waiting foe the response
     */
    public void loadOnlineScores(){
        communicator.addListener(this::messageReceived);
        communicator.send("HISCORES");
        logger.info("Requesting online scores...");
    }
    /**
     * Processes the incoming message received from the server, extracting high scores data and updating the remote scores list.
     *
     * @param message  the message received from the server
     */
    public void messageReceived(String message) {
        // Parse the incoming message to extract high scores data
        if (message.startsWith("HISCORES")) {
            String[] scoreEntries = message.split("\n");
            List<Pair<String, Integer>> parsedScores = new ArrayList<>();
            for (String entry : scoreEntries) {
                String[] parts = entry.split(":");
                String name = parts[0];
                int score = Integer.parseInt(parts[1]);
                parsedScores.add(new Pair<>(name, score));
            }

            // Update the remoteScores list on the JavaFX Application Thread
            Platform.runLater(() -> remoteScores.setAll(parsedScores));
        }
        else {
            // Log invalid message received
            logger.warn("Received invalid message: {}", message);
        }
    }

    /**
     * Writes the online score to the server if it is higher than the current lowest score,
     * and updates the remote scores list accordingly.
     *
     * @param name   the name associated with the score
     * @param score  the score to be written
     */
    public void writeOnlineScore(String name, int score) {
        Optional<Pair<String, Integer>> minScore = remoteScores.stream().min(Comparator.comparing(Pair::getValue));
        if (minScore.isEmpty() || score > minScore.get().getValue()) {
            // Send the new high score to the server
            communicator.send("HISCORE " + name + ":" + score);

            // Add the new score to the remote scores list
            remoteScores.add(new Pair<>(name, score));
        }
    }

    /**
     * Manually update the scores list

     */
    public void manuallyUpdate(){
        loadScores();
        scoresList.updateScores();
        remoteScoresList.updateScores();
        logger.info("Manually updating score: {}", scoresList.toString());
    }



}
