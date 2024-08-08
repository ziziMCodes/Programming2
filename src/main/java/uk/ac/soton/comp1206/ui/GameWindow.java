package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.*;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;

import java.util.Objects;
/**
 * The GameWindow is the single window for the game where everything takes place. To move between screens in the game,
 * we simply change the scene.
 *
 * The GameWindow has methods to launch each of the different parts of the game by switching scenes. You can add more
 * methods here to add more screens to the game.
 */
public class GameWindow {

    private static final Logger logger = LogManager.getLogger(GameWindow.class);

    private final int width;
    private final int height;
    private final Stage stage;

    private BaseScene currentScene;
    private Scene scene;
    final Communicator communicator;
    private final Cursor cursor;

    /**
     * Create a new GameWindow attached to the given stage with the specified width and height
     * @param stage stage
     * @param width width
     * @param height height
     */
    public GameWindow(Stage stage, int width, int height) {
        Image cursorImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cursor.png")));
        this.cursor = new ImageCursor(cursorImage);
        this.width = width;
        this.height = height;

        this.stage = stage;

        //Setup window
        setupStage();

        //Setup resources
        setupResources();

        //Setup default scene
        setupDefaultScene();

        //Setup communicator
        communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");

        //Go to menu
        //startMenu();

        startIntro();

    }

    /**
     * Setup the font and any other resources we need
     */
    private void setupResources() {
        logger.info("Loading resources");

        //We need to load fonts here due to the Font loader bug with spaces in URLs in the CSS files
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Bold.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-ExtraBold.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Loker.ttf"), 32);
    }

    /**
     * Display the main menu
     */
    public void startMenu() {
        loadScene(new MenuScene(this));
    }

    /**
     * Display the single player challenge
     */
    public void startChallenge() { loadScene(new ChallengeScene(this)); }
    public void startChallenge(String mode) { loadScene(new ChallengeScene(this, mode)); }

    /**
     * Setup the default settings for the stage itself (the window), such as the title and minimum width and height.
     */
    public void setupStage() {
        stage.setTitle("TetrECS");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setMaximized(true);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());

    }

    /**
     * Load a given scene which extends BaseScene and switch over.
     * @param newScene new scene to load
     */
    public void loadScene(BaseScene newScene) {
        cleanup();

        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        updateCursor(scene);
        stage.setScene(scene);
        //adjustScale(scene);
        //scale(scene);


        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Setup the default scene (an empty black scene) when no scene is loaded
     */
    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(),width,height, Color.BLACK);
        stage.setScene(this.scene);
    }

    /**
     * When switching scenes, perform any cleanup needed, such as removing previous listeners
     */
    public void cleanup() {
        logger.info("Clearing up previous scene");
        communicator.clearListeners();
    }

    /**
     * Get the current scene being displayed
     * @return scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Get the width of the Game Window
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height of the Game Window
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the communicator
     * @return communicator
     */
    public Communicator getCommunicator() {
        return communicator;
    }

    /**
     * Close the game window
     * @param event mouse event
     */
    public void close(MouseEvent event){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        stage.close();
    }
    /**
     * Starts the instructions scene by loading it into the game window.
     */
    public void startInstructions() {
        this.loadScene(new InstructionsScene(this));
    }
    /**
     * Starts the scores scene with the given game, by loading it into the game window.
     *
     * @param game the game object to be passed to the scores scene
     */
    public void startScoresScene(Game game){
        logger.info("Starting scores scene");
        this.loadScene(new ScoresScene(this, game));
    }

    /**
     * Start the lobby scene
     */
    public void startLobbyScene(){
        logger.info("Starting lobby scene");
        this.loadScene(new LobbyScene(this));
    }

    /**
     * Start the multiplayer scene
     */
    public void startMultiplayerScene(){
        logger.info("Starting multiplayer scene");
        this.loadScene(new MultiplayerScene(this));
    }

    /** Display the intro scene */
    public void startIntro() {
        this.loadScene(new IntroScene(this));
    }
    /**
     * Update the cursor for the scene
     * @param scene scene
     */
    public void updateCursor(Scene scene){
        scene.setCursor(cursor);
    }

    public void startBeforeChallengeScene(){
        this.loadScene(new ChallengeModeScene(this));
    }

}
