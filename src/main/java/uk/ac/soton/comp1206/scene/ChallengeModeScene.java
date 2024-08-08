package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.ModeSelectedListener;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * This class represents the Challenge Mode Scene in the game. It extends the BaseScene class.
 * It is responsible for displaying the challenge mode options to the user and handling the user's selection.
 * The user can choose between "Easy", "Normal", and "Hard" modes.
 * The selected mode affects the game's difficulty by adjusting the game speed.
 *
 * <p>Each mode is represented by a button. When a button is clicked, the corresponding mode is set in the Game instance,
 * and the game starts.</p>
 *
 */
public class ChallengeModeScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(ChallengeModeScene.class);
    BorderPane borderPane;
    private ModeSelectedListener modeSelectedListener;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ChallengeModeScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void initialise() {
        logger.info("Initialising Instructions Scene");
        scene.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) gameWindow.startMenu();
        });

    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        root.getChildren().add(challengePane);

        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.getStyleClass().add("menu-background");
        borderPane = new BorderPane();
        challengePane.getChildren().add(borderPane);
        modeButtons();


    }

    /**
     * The game mode buttons
     */

    public void modeButtons(){
        logger.info("Adding buttons");
        //Create buttons for the challenge mode
        //Create buttons for the challenge mode
        Button easyButton = new Button("Easy");
        Button normalButton = new Button("Normal");
        Button hardButton = new Button("Hard");

        easyButton.setOnMouseClicked(event -> {
            if (modeSelectedListener != null) {
                modeSelectedListener.onModeSelected("easy");
            }
            gameWindow.startChallenge("easy");
        });

        normalButton.setOnMouseClicked(event -> {
            if (modeSelectedListener != null) {
                modeSelectedListener.onModeSelected("normal");
            }
            gameWindow.startChallenge("normal");
        });

        hardButton.setOnMouseClicked(event -> {
            if (modeSelectedListener != null) {
                modeSelectedListener.onModeSelected("hard");
            }
            gameWindow.startChallenge("hard");
        });
        var menuButtons = new VBox(10, easyButton, normalButton, hardButton);
        menuButtons.setAlignment(Pos.CENTER);
        borderPane.setCenter(menuButtons);
    }
    public void setModeSelectedListener(ModeSelectedListener listener) {
        this.modeSelectedListener = listener;
    }

}
