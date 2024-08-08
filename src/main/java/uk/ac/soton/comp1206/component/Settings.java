package uk.ac.soton.comp1206.component;

import java.io.*;
import java.util.Properties;

public class Settings {
    private double volume;
    private int resolutionWidth;
    private int resolutionHeight;
    // Add other settings here

    public Settings() {
        load();
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public void setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public void setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

    // Add getters and setters for other settings here

    public void save() {
        Properties prop = new Properties();
        prop.setProperty("volume", String.valueOf(volume));
        prop.setProperty("resolutionWidth", String.valueOf(resolutionWidth));
        prop.setProperty("resolutionHeight", String.valueOf(resolutionHeight));
        // Add other settings here

        try (OutputStream output = new FileOutputStream("config.properties")) {
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void load() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);

            volume = Double.parseDouble(prop.getProperty("volume", "1.0"));
            resolutionWidth = Integer.parseInt(prop.getProperty("resolutionWidth", "800"));
            resolutionHeight = Integer.parseInt(prop.getProperty("resolutionHeight", "600"));
            // Add other settings here
        } catch (IOException ex) {
            // If the file does not exist, create a new one with default settings
            volume = 0.5;
            resolutionWidth = 800;
            resolutionHeight = 600;
            // Set other default settings here
            save();
        }
    }
}