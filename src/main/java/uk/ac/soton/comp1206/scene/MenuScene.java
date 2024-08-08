package uk.ac.soton.comp1206.scene;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    BorderPane mainPane;
    StackPane menuPane;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        generateMenuImage();

        var challenge = new Text("Start Challenge");
        challenge.getStyleClass().add("menuItem");
        challenge.setOnMouseClicked(this::startChallengeMode);


        // Instructions button
        Text instructionsButton = new Text("Instructions");
        instructionsButton.getStyleClass().add("menuItem");
        instructionsButton.setOnMouseClicked(this::showInstructions);

        // Add event handler for instructions button

        var exitButton = new Text("Exit Game");
        exitButton.getStyleClass().add("menuItem");
        exitButton.setOnMouseClicked(this::exitGame);

        // VBox for menu buttons
       // var menuButtons = new VBox(10, button, settingsButton, instructionsButton, lobbyButton, exitButton);

        var multiplayer = new Text("Multiplayer Mode");
        multiplayer.getStyleClass().add("menuItem");
        multiplayer.setOnMouseClicked(this::openLobby);
        var menuButtons = new VBox(10, challenge, instructionsButton, multiplayer, exitButton);
        menuButtons.setAlignment(Pos.CENTER);
        mainPane.setCenter(menuButtons);

    }
    /**
     * Generates and displays the menu image at the top center of the menu pane, and adds a vertical bouncing animation
     * to the image.
     */
    private void generateMenuImage() {
        ImageView menuImage = new ImageView(MultiMedia.getImage("TetreLOgo.png"));
        menuImage.setFitWidth(gameWindow.getWidth() / 2.2);
        menuImage.setPreserveRatio(true);
        StackPane.setAlignment(menuImage, Pos.TOP_CENTER);
        menuPane.getChildren().add(menuImage);

        var translate = new TranslateTransition(new Duration(2200), menuImage);
        translate.setCycleCount(-1);
        translate.setByY(30);
        translate.setAutoReverse(true);
        translate.play();
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        MultiMedia.stopAll();
        MultiMedia.playBackground("menuMusic.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param mouseEvent event
     */
    private void startGame(MouseEvent mouseEvent) {
        gameWindow.startChallenge();
    }
    /**
     * Handle when the Exit button is pressed
     * @param mouseEvent mouseEvent
     */
    private void exitGame(MouseEvent mouseEvent) {
        logger.info("Game has been exited");
        gameWindow.close(mouseEvent);
    }

    /**
     * Handles the action event when the instructions button is clicked.
     * Switches the scene to the instructions screen.
     *
     * @param mouseEvent mouseEvent
     */
    private void showInstructions(MouseEvent mouseEvent) {
        gameWindow.startInstructions();
    }

    private void openLobby(MouseEvent mouseEvent){
        gameWindow.startLobbyScene();
    }

    private void startChallengeMode(MouseEvent mouseEvent){
        gameWindow.startBeforeChallengeScene();
    }




}
