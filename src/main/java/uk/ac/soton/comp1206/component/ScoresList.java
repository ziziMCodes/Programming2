package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ScoresList class extends VBox to display the scores in the game.
 * It handles the display of player names and their scores.
 */
public class ScoresList extends VBox {
    private static final Logger logger = LogManager.getLogger(ScoresList.class);
    private SimpleListProperty<Pair<String, Integer>> score = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final StringProperty name = new SimpleStringProperty();

    /**
     * ScoreList constructor
     */
    public ScoresList(){
        this.getStyleClass().add("scoreslist");
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);

        score.addListener(((observable, oldScore, newScore) -> updateScores()));
        name.addListener((e) -> this.updateScores());

    }

    /**
     * Update the score list from the array list
     */
    public void updateScores() {
        logger.info("Updating score list");
        //Remove previous children
        getChildren().clear();

        //Loop through the top scores
        int counter = 0;
        for(Pair<String,Integer> currentScore : score.get()) {

            //Only do the top 5 score
            if(counter >= 5) break;
            counter++;

            //Create an HBox for each currentScore
            HBox scoreBox = new HBox();
            scoreBox.getStyleClass().add("scoreitem");
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(10);

            //Add nameText
            Text nameText = new Text(currentScore.getKey());
            if(nameText.getText().equals(name.get())) {
                nameText.getStyleClass().add("myscorer");
            }
            nameText.getStyleClass().add("scorer");
            nameText.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(nameText, Priority.ALWAYS);
            nameText.setFill(Color.WHITE);

            //Add pointsText
            Text pointsText = new Text(currentScore.getValue().toString());
            pointsText.getStyleClass().add("pointsText");
            pointsText.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(pointsText,Priority.ALWAYS);

            //Add nameText and pointsText
            scoreBox.getChildren().addAll(nameText,pointsText);

            //Add currentScore box
            getChildren().add(scoreBox);
        }
    }

    /**
     * Animate the scores
     */
    public void animateScores() {
        for (int i = 0; i < getChildren().size(); i++) {
            Node scoreItem = getChildren().get(i);

            FadeTransition ft = new FadeTransition(Duration.millis(300), scoreItem);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.setDelay(Duration.millis(i * 100)); // delay to create a staggered effect

            ft.play();
        }
    }

    /**
     * Get the internal score property, for linking to the parent
     *
     * @return score property
     */
    public SimpleListProperty<Pair<String, Integer>> scoreProperty() {
        return score;
    }
    @Override
    public String toString() {
        return score.toString();
    }
}