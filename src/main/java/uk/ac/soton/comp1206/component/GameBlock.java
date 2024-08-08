package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import uk.ac.soton.comp1206.game.Game;
/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {


    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.rgb(255, 204, 204), // Light Pink
            Color.rgb(255, 204, 153), // Peach
            Color.rgb(255, 255, 153), // Light Yellow
            Color.rgb(204, 255, 204), // Light Green
            Color.rgb(153, 204, 255), // Light Blue
            Color.rgb(255, 153, 255), // Light Purple
            Color.rgb(255, 204, 153), // Light Orange
            Color.rgb(255, 153, 204), // Light Rose
            Color.rgb(204, 255, 255), // Light Cyan
            Color.rgb(255, 204, 255), // Light Magenta
            Color.rgb(204, 255, 153), // Light Lime
            Color.rgb(204, 153, 255), // Light Violet
            Color.rgb(255, 255, 204), // Light Beige
            Color.rgb(255, 204, 255), // Light Orchid
            Color.rgb(255, 153, 153), // Pastel Red
            Color.rgb(153, 204, 204), // Pastel Turquoise
            Color.rgb(204, 153, 204), // Pastel Purple
            Color.rgb(153, 255, 255), // Pastel Sky Blue
            Color.rgb(255, 204, 153), // Pastel Orange
            Color.rgb(255, 153, 255), // Pastel Pink
    };

    protected Game game;
    protected GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;
    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    /** Set if the block has a dot. */
    private boolean dot = false;

    /** Fadeout animation timer when the row is cleared. */
    private static AnimationTimer timer = null;
    /** Variables storing hover status and color. */
    private boolean hovered;




    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        if (value.get() == 0) {
            paintEmpty();
        } else {
            paintColor(GameBlock.COLOURS[value.get()]);
        }

        // Check if the block has a dot on top
        if (dot) {
            paintDot();
        }

        // Check if the block has a shadow on top
        if (hovered) {
            paintHovered();
        }

    }
    /** Changing block color when hovered. */
    private void paintHovered() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1,1,1,0.5)); // Fill with a solid color, you can choose any color you prefer
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Set up the empty block gradient
        gc.setFill(Color.color(0.8, 0.8, 0.8, 0.2));
        gc.fillRect(0, 0, width, height);

        // Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paint this canvas with the given colour
     * @param color the colour to paint
     */
    private void paintColor(Paint color) {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Fill
        gc.setFill(color);
        gc.fillRect(0, 0, width, height);

        // Make the lighter side
        gc.setFill(Color.color(1, 1, 1, 0.25));
        gc.fillPolygon(new double[] {0, width, 0}, new double[] {0, 0, height}, 3);

        // Adding dark accent
        gc.setFill(Color.color(1, 1, 1, 0.35));
        gc.fillRect(0, 0, width, 3);
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.fillRect(0, 0, 3, height);

        // Adding light accent
        gc.setFill(Color.color(0, 0, 0, 0.35));
        gc.fillRect(width - 3, 0, width, height);
        gc.setFill(Color.color(0, 0, 0, 0.35));
        gc.fillRect(0, height - 3, width, height);

        // Border
        gc.setStroke(Color.color(0, 0, 0, 0.55));
        gc.strokeRect(0, 0, width, height);
    }

    /** Paint the grey dot on top of the block. */
    protected void paintDot() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1, 0.7));
        gc.fillOval(width / 4, height / 4, width / 2, height / 2);
    }
    protected void setHovered(boolean hovered) {
        this.hovered = hovered;
        // Repaint the block
        paint();
    }

    /** Put a dot on the block and repaint the block. */
    protected void setDot() {
        dot = true;
        paint();
    }

    /** Set a fade out effect on the game block. */
    protected void fadeOut() {
        timer = new AnimationTimer() {
                    double opacity = 1;

                    @Override
                    public void handle(long l) {
                        paintEmpty();
                        opacity -= 0.05;
                        if (opacity <= 0.25) {
                            this.stop();
                            timer = null;
                        } else {
                            var gc = getGraphicsContext2D();
                            gc.setFill(Color.color(0, 1, 0, opacity));
                            gc.fillRect(0, 0, width, height);
                        }
                    }
                };
        timer.start();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {

        //logger.info("Binding block at {} {}", x, y);
        value.bind(input);
    }

    @Override
    public String toString() {
        return "GameBlock{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value.get() +
                '}';
    }
}
