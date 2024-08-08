package uk.ac.soton.comp1206.component;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MultiMedia class is responsible for handling all audio and visual media in the game.
 * It provides methods for playing audio, music, video and loading images.
 */
public class MultiMedia {
    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;

    private static final BooleanProperty audioEnabledProperty = new SimpleBooleanProperty(true);
    private static final Logger logger = LogManager.getLogger(MultiMedia.class);
    private static final DoubleProperty volume = new SimpleDoubleProperty(0.5);

    /**
     * Plays the specified audio file
     * @param file the name of the audio file
     */

    public static void playAudio(String file) {
        if (!getAudioEnabled()) return;
        String audio = Objects.requireNonNull(MultiMedia.class.getResource("/sounds/" + file)).toExternalForm();
        logger.info("Playing audio: " + audio);
        try {
            audioPlayer = new MediaPlayer(new Media(audio));
            audioPlayer.play();
        } catch (Exception e) {
            setAudioEnabled(false);
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    /**
     * Plays the specified background music file on loop
     * @param file the name of the music file
     */
    public static void playBackground(String file) {
        if (!getAudioEnabled()) return;
        String background = Objects.requireNonNull(MultiMedia.class.getResource("/music/" + file)).toExternalForm();
        logger.info("Playing audio: " + background);
        try {
            musicPlayer = new MediaPlayer(new Media(background));
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.play();
        } catch (Exception e) {
            setAudioEnabled(false);
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }
    /**
     * Stops all playing audio and music.
     */
    public static void stopAll() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }

        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    /**
     * Returns the audio enabled property.
     * @return
     */
    public static BooleanProperty audioEnabledProperty() {
        return audioEnabledProperty;
    }
    /**
     * Sets the audio enabled property.
     * @param enabled
     */
    public static void setAudioEnabled(boolean enabled) {
        logger.info("Audio enabled set to: " + enabled);
        audioEnabledProperty().set(enabled);
    }
    /**
     * Loads the specified image.
     *
     * @param image the name of the image file
     * @return the loaded Image object
     */
    public static Image getImage(String image) {
        try {
            return new Image(
                    Objects.requireNonNull(MultiMedia.class.getResource("/images/" + image))
                            .toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to load image: {}", image);
            return null;
        }
    }
    /**
     * Returns the audio enabled property.
     *
     * @return the audio enabled property
     */
    public static boolean getAudioEnabled() {
        return audioEnabledProperty().get();
    }


    /**
     * Sets the volume for the audio and music players.
     * @param volume the volume level, a value between 0.0 and 1.0
     */
    public static void setVolume(double volume) {
        if(musicPlayer!=null) {
            if (audioPlayer != null) {
                audioPlayer.setVolume(volume);
            }

            if (musicPlayer != null) {
                musicPlayer.setVolume(volume);
            }
        }
    }
    public static double getVolume() {
        return volume.get();
    }
    public static DoubleProperty volumeProperty() {
        return volume;
    }



}
