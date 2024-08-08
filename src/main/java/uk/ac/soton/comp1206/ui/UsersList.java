package uk.ac.soton.comp1206.ui;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of players in the channel.
 */
public class UsersList extends VBox {

    /** The list of players in the channel */
    private final ArrayList<String> users = new ArrayList<>();

    /**
     * Constructs a new PlayerList object and sets its CSS class.
     */
    public UsersList() {
        this.getStyleClass().add("playerBox");
    }

    /**
     * Sets the list of players to be displayed in the player list.
     *
     * @param newPlayers the list of new players to be added
     */
    public void set(List<String> newPlayers) {
        users.clear();
        users.addAll(newPlayers);
        update();
    }

    /**
     * Updates the player list with the current list of players.
     */
    public void update() {
        getChildren().clear();

        Text userName;
        for (String user : users) {
            userName = new Text(user + " ");
            getChildren().add(userName);
        }
    }
}
