package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.List;

/**
 * Leaderboard class extends ScoresList to display the leaderboard in the game.
 * It handles the display of player names, scores, and statuses (alive or dead).
 */
public class Leaderboard extends ScoresList{

    /**
     * Updates the leaderboard with the given list of users.
     *
     * @param user a list of pairs, where each pair contains a player's name and a pair of their score and status
     */
    public void updateLeaderboard(List<Pair<String, Pair<Integer, String>>> user){
        Platform.runLater(() -> {
            getChildren().clear();
            user.forEach(score ->{
                String playerName = score.getKey();
                Integer playerScore = score.getValue().getKey();
                String playerStatus = score.getValue().getValue();

                HBox scoreItem = new HBox();
                scoreItem.getStyleClass().add("scoreslist");
                scoreItem.setSpacing(15);

                Text name = new Text(playerName);
                name.getStyleClass().add("leaderboardItem");
                name.setFill(Color.WHITE);

                Text scoreText = new Text(playerScore.toString());
                scoreText.getStyleClass().add("leaderboardItem");
                scoreText.setFill(Color.WHITE);

                Text status;
                if(parseInt(playerStatus) >=0) {
                    status = new Text(playerStatus);
                    scoreText.getStyleClass().add("leaderboardItem");
                    status.setFill(Color.WHITE);
                }
                else{
                    status = new Text("DEAD");
                    scoreText.getStyleClass().add("leaderboardItem");
                    status.setFill(Color.RED);
                }


                scoreItem.getChildren().addAll(name,scoreText,status);
                getChildren().add(scoreItem);

            });
        });
    }
    /**
     * Parses a string into an integer.
     *
     * @param number the string to parse
     * @return the parsed integer, or -1 if the string cannot be parsed into an integer
     */
    public int parseInt(String number){
        int num = -1;
        try{
            num = Integer.parseInt(number);
            return num;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
