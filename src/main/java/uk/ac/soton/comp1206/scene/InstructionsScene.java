package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.util.Objects;
/** The Instructions scene. Holds the dynamically generated game pieces. */
public class InstructionsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    /**
     * Initialise the scene with listener that exits on any key pressed
     */

    @Override
    public void initialise() {
        logger.info("Initialising Instructions Scene");
        MultiMedia.stopAll();
        scene.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) gameWindow.startMenu();
        });
        MultiMedia.playBackground("instructions.mp3");
    }
    /** Building the scene. */
    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        logger.info("Building Instructions Scene");
        StackPane stackPane = new StackPane();
        stackPane.setMaxHeight(gameWindow.getHeight());
        stackPane.setMaxWidth(gameWindow.getWidth());
        //stackPane.getStyleClass().add("multiplayer-background");

        root.getChildren().add(stackPane);
        BorderPane mainPane = new BorderPane();
        stackPane.getChildren().add(mainPane);

        mainPane.getStyleClass().add("multiplayer-background");


        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(vBox);
        generateTitle(vBox);
        generateImage(vBox);
        generateGamePieces(vBox);

    }

    /**
     * Displays instructions using an image file located at the specified file path.
     * @param vBox UI vbox to add to
     */

    private void generateImage(VBox vBox) {
        ImageView instructionImage =
                new ImageView(
                        Objects.requireNonNull(getClass().getResource("/images/Instructions.png"))
                                .toExternalForm());
        instructionImage.setFitWidth((double) gameWindow.getWidth() / 2.5);
        instructionImage.setPreserveRatio(true);
        vBox.getChildren().add(instructionImage);

    }
    /**
     * Generate the window title.
     *
     * @param vBox UI vbox to add to
     */
    private void generateTitle(VBox vBox) {
        Text title = new Text("Instructions");
        title.getStyleClass().add("heading");
        vBox.getChildren().add(title);
    }
    /**
     * Generates and displays the game pieces in the provided VBox.
     *
     * @param vBox  the VBox to contain the generated game pieces
     */
    public void generateGamePieces(VBox vBox){
        Text pieces = new Text("Game Pieces");
        pieces.getStyleClass().add("heading");
        vBox.getChildren().add(pieces);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(9);
        gridPane.setHgap(9);
        vBox.getChildren().add(gridPane);
        int pieceSize = gameWindow.getWidth()/12;

        double totalPiecesSize = pieceSize * 8;
        double padding = (gameWindow.getWidth() - totalPiecesSize - 50) / 2;
        gridPane.setPadding(new Insets(0, padding, 0, padding));

        int col = 0;
        int row = 0;
        for (int i = 0; i < 15; i++) {
            GamePiece piece = GamePiece.createPiece(i);
            PieceBoard pieceBoard =
                    new PieceBoard(3, 3, pieceSize, pieceSize);
            pieceBoard.setPiece(piece);
            gridPane.add(pieceBoard, col, row);
            col++;
            if (col == 8) {
                col = 0;
                row++;
            }
        }
    }



}
