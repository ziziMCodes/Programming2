package uk.ac.soton.comp1206.scene.basescenes;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

/**
 * A Base Scene used in the game. Handles common functionality between all scenes.
 */
public abstract class BaseScene {

    protected final GameWindow gameWindow;

    protected GamePane root;
    protected Scene scene;
    protected Game singleGame;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public BaseScene(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Initialise this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Build the layout of the scene
     */
    public abstract void build();

    /**
     * Create a new JavaFX scene using the root contained within this scene
     * @return JavaFX scene
     */
    public Scene setScene() {
        var previous = gameWindow.getScene();
        Scene scene = new Scene(root, previous.getWidth(), previous.getHeight(), Color.BLACK);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/game.css")).toExternalForm());
        this.scene = scene;
        return scene;
    }

    /**
     * Get the JavaFX scene contained inside
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }

}
