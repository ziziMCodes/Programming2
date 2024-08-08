package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.MultiMedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.basescenes.BaseScene;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.*;
/**
 * The LobbyScene class handles the lobby scene of the game.
 * It manages the user interface and communication with the server for the lobby.
 */
public class LobbyScene extends BaseScene {
    public static Communicator communicator;
    private final ObservableList<String> channelList = FXCollections.observableArrayList();
    private ListView<String> listViewChannels;
    private ObservableList<String> observableUser = FXCollections.observableArrayList();
    private ListView<String> userListView;
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);



    public static final SimpleStringProperty currentChannel = new SimpleStringProperty("");
    public static final StringProperty nickname = new SimpleStringProperty();
    public int count = 0;
    private Timer timer;
    private VBox channelVBox;
    private VBox chat;
    TextField chatInput;
    TextField channelInput;
    TextArea chatHistory;
    private ComboBox dropdown;
    Boolean isHost = false;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in.
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
        setCommunicator();
        setUpCommunications();
        logger.info("Creating lobby scene");
    }

    @Override
    public void initialise() {
        MultiMedia.stopAll();
        MultiMedia.playBackground("lobby.mp3");
        setUpChannelList();
        requestChannel();
        setupStartListener();
        scene.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {

                gameWindow.startMenu();
            }
        });

    }

    @Override
    public void build() {
        logger.info("Building lobby scene");
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        StackPane stackPane = new StackPane();
        stackPane.setMaxHeight(gameWindow.getHeight());
        stackPane.setMaxWidth(gameWindow.getWidth());
        root.getChildren().add(stackPane);
        //setup border pane
        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("lobby-background");
        stackPane.getChildren().add(pane);

        //Create a chat box
        chat = new VBox();
        chatHistory = new TextArea();
        chatHistory.setEditable(false);
        chatInput = new TextField();
        chatInput.setPromptText("Send chat");
        chatInput.setOnAction(e -> sendMessage(chatInput.getText()));
        Text msgLabel = new Text("Messages:");
        msgLabel.getStyleClass().add("heading");
        chat.getChildren().addAll(msgLabel, chatHistory, chatInput);
        pane.setBottom(chat);
        //Create area for channels
        Text channelLabel = new Text("Create a channel!:");
        channelLabel.getStyleClass().add("heading");
        channelVBox = new VBox(10);
        channelInput = new TextField();
        channelInput.setPromptText("Channel Name");
        channelInput.setOnAction(e -> createChannel(channelInput.getText()));
        channelVBox.getChildren().addAll(channelLabel, channelInput);

        //Create buttons
        Button nickname = new Button("Set nickname");
        Button leave = new Button ("Leave Channel");
        leave.setOnAction(event -> {
            part();
        });

        Button multiplayer = new Button("Start Multiplayer");
        multiplayer.setOnAction(e -> {
            if(isHost){
                showMulti(e);
            }
            else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR - Not Host");
                alert.setHeaderText("NOT HOST");
                alert.setContentText("Wait for the host to start the game... ");
                alert.showAndWait();

            }
        });


        nickname.setOnAction(event -> checkNewNickname());

        var buttons = new VBox(10, leave, nickname, multiplayer);

        channelVBox.getChildren().add(buttons);

        listViewChannels = new ListView<>(channelList);
        listViewChannels.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2){
                String selectChannel = listViewChannels.getSelectionModel().getSelectedItem();
                sendJoinRequest(selectChannel);
            }
        });
        listViewChannels.setPrefSize(150,150);
        channelVBox.getChildren().add(listViewChannels);

        userListView = new ListView<>(observableUser);
        userListView.setPrefSize(150,150);
        channelVBox.getChildren().add(userListView);

        pane.setRight(channelVBox);

        dropdown = new ComboBox(channelList);
        dropdown.setPromptText("Select a channel to join...");
        dropdown.setOnAction(e -> sendJoinRequest(dropdown.getSelectionModel().getSelectedItem().toString()));
        Text dropdownText = new Text("Join a channel...");
        dropdownText.getStyleClass().add("heading");
        pane.setCenter(new HBox(dropdownText, dropdown));



    }

    /**
     * Sets the communicator for the lobby scene.
     */
    public void setCommunicator(){
        communicator.addListener(this::messageReceived);
    }
    /**
     * Sends a message to the server.
     *
     * @param message the message to send
     */
    public void sendMessage(String message){
        //count variable ensures we have one listener at a time
        if(count<1) {
            count++;
            communicator.addListener(sms -> {
                if (sms.startsWith("MSG")) {
                     Platform.runLater(() ->  {
                     chatHistory.appendText(sms.substring(4) + "\n");
                    MultiMedia.playAudio("message.wav");
                    });
                }
            });
        }

        if(message!=null && !message.isEmpty()) {
            communicator.send("MSG " + message);
        }
        chatInput.clear();
    }
    /**
     * Sends a part message to the server.
     */
    public void part(){
        communicator.addListener(part -> {
            if(part.startsWith("PARTED")){
                logger.info("part method called with message: " + part);
                Platform.runLater(() -> {
                    currentChannel.set("");
                });

            }
        });
        communicator.send("PART");
    }
    /**
     * Requests the list of channels from the server.
     */
    public void requestChannel(){
        communicator.addListener(message -> {
            if(message.startsWith("CHANNELS")){
                Platform.runLater(() ->  {
                    handleChannelDisplay(message.substring(9));
                });
            }
        });
        //Get a list of all channels
        communicator.send("LIST" );
    }

    /**
     * Requests the list of users from the server.
     */
    public void requestUsers(){
        communicator.addListener(message -> {
            if(message.startsWith("USERS")){
                handleUsers(message.substring(6));
            }
        });
        communicator.send("USERS");

    }

    /**
     * Sends a join request to the server for a specific channel.
     *
     * @param channelName the name of the channel to join
     */
    public void sendJoinRequest(String channelName){
        communicator.addListener(message -> {
            if(message.startsWith("JOIN")){
                String channelNameReceived = message.substring("CHANNELS ".length());

                Platform.runLater(() ->  {
                    currentChannel.set(channelNameReceived);
                });
            }
        });
        if(channelName!=null && !channelName.isEmpty()){
            logger.info("Joining channel... {}", channelName);
            communicator.send("JOIN " + channelName);
        }
    }

    /**
     * Handles an error message received from the server.
     *
     * @param message the error message
     */
    public void handleError(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Joins a channel.
     *
     * @param channelName the name of the channel to join
     */
    public void joinChannel(String channelName){
        logger.info("joinChannel method called with channelName: " + channelName);
        channelList.add(channelName);
        currentChannel.set(channelName);
    }
    /**
     * Sets up the communications for the lobby scene.
     */
    public void setUpCommunications(){
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                requestChannel();
                requestUsers();
                logger.info("Requested channel...");

            }
        }, 0, 2000);
    }
    /**
     * Sets up the channel list for the lobby scene.
     */
    public void setUpChannelList(){
        listViewChannels.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal!=null){
                logger.info("New val: {}", newVal);
                joinChannel(newVal);
            }
        });
    }
    /**
     * Creates a channel.
     *
     * @param channelName the name of the channel to create
     */
    public void createChannel(String channelName){
        communicator.addListener(msg -> {
            if(msg.startsWith("HOST")){
                isHost = true;
            }

        });
        if(channelName!=null && !channelName.isEmpty()){
            communicator.send("CREATE " + channelName);
        }

    }
    /**
     * Handles the display of channels in the lobby scene.
     *
     * @param message the message received from the server containing the list of channels
     */
    public void handleChannelDisplay(String message) {
        Platform.runLater(() -> {

            if (message.contains("\n")) {
                channelList.setAll(message.split("\n"));
            } else {
                channelList.setAll(message);
            }
        });
    }
    /**
     * Handles the display of users in the lobby scene.
     *
     * @param message the message received from the server containing the list of users
     */
    public void handleUsers(String message){
        Platform.runLater(() -> {

            if (message.contains("\n")) {
                observableUser.setAll(message.split("\n"));
            } else {
               observableUser.setAll(message);
            }
        });
    }
    /**
     * Handles a message received from the server.
     *
     * @param message the message received from the server
     */
    public void messageReceived(String message){
            logger.info("Message: {}", message);

            if (message.startsWith("QUIT")) {
                handleError(message.substring(6));
            }
    }
    /**
     * Checks and sets a new nickname for the user.
     */
    public void checkNewNickname(){
            TextInputDialog dialog = new TextInputDialog("player");
            dialog.setTitle("New Nickname!");
            dialog.setHeaderText("You have entered the lobby..." );
            dialog.setContentText("Please enter your name: ");
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/dialogStyle.css")).toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                //update nickname;
                nickname.set(name);
                communicator.send("NICK " + name);

                logger.info("New nickname set: {}", nickname);
            });
        }
    /**
     * Shows the multiplayer scene.
     *
     * @param event the action event that triggered this method
     */
    private void showMulti(ActionEvent event){
        communicator.send("START");
        MultiMedia.stopAll();
        timer.cancel();
        Platform.runLater(gameWindow::startMultiplayerScene);
        gameWindow.startMultiplayerScene();

    }
    public void setupStartListener(){
        communicator.addListener(message -> {
            if(message.startsWith("START")){
                MultiMedia.stopAll();
                timer.cancel();
                Platform.runLater(() -> {
                    gameWindow.startMultiplayerScene();
                });
            }
        });
    }

}
