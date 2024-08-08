package uk.ac.soton.comp1206;

import javafx.application.Application;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * JavaFX Application class
 */
public class App extends Application {

    /**
     * Base resolution width
     */
    private final int width = 800;

    /**
     * Base resolution height
     */
    private final int height = 600;

    private static App instance;
    private static final Logger logger = LogManager.getLogger(App.class);
    private Stage stage;


    /**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting client");
        launch();
    }


    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;

        //Open game window
        openGame();
        
    }

    /**
     * Open the game window
     */

    public void openGame() {
        logger.info("Opening game window");

        var gameWindow = new GameWindow(stage,width,height);

        stage.show();
    }

    /**
     * Shutdown the game
     */
    public void shutdown() {
        logger.info("Shutting down");
        System.exit(0);
    }

    /**
     * Get the singleton App instance
     * @return the app
     */
    public static App getInstance() {
        return instance;
    }

}