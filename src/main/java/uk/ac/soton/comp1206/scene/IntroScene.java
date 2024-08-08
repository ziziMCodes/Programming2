package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Start Scene that flashes at the start of the game.
 */
public class IntroScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(IntroScene.class);
    /** Sequential transition for the fade animation. */
    private SequentialTransition sequence;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public IntroScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Intro Scene");
       // MultiMedia.playAudio("intro.mp3");
        //MultiMedia.setVolume(0.5);
    }

    /** Initializing the scene. */
    public void initialise() {
        scene.setOnKeyPressed(
                (e) -> {
                    MultiMedia.stopAll();
                    sequence.stop();
                    gameWindow.startMenu();
                });
    }

    /**
     * Build the scene UI.
     */
    public void build() {
        logger.info("Building " + this.getClass().getName());

        StackPane introPane = generateLayout();


        ImageView logo = new ImageView(MultiMedia.getImage("ECSGames.png"));
        logo.setFitWidth(gameWindow.getWidth() / 2.5);
        logo.setPreserveRatio(true);
        logo.setOpacity(0);
        introPane.getChildren().add(logo);
        root.getChildren().add(introPane);
        FadeTransition fadeTransitionIn = new FadeTransition(new Duration(1500), logo);
        PauseTransition pause = new PauseTransition(new Duration(1500));
        FadeTransition fadeTransitionOut = new FadeTransition(new Duration(500), logo);
        fadeTransitionOut.setToValue(0);
        fadeTransitionIn.setToValue(1);
        sequence = new SequentialTransition(fadeTransitionIn, pause, fadeTransitionOut); // Changed here
        sequence.play();
        sequence.setOnFinished((e) -> gameWindow.startMenu());
    }


    /**
     * Generate the layout of the intro scene.
     *
     * @return the stack pane that will contain the UI elements
     */
    private StackPane generateLayout() {
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        StackPane introPane = new StackPane();
        introPane.setMaxWidth(gameWindow.getWidth());
        introPane.setMaxHeight(gameWindow.getHeight());
        introPane.getStyleClass().add("intro");
        return introPane;
    }
}