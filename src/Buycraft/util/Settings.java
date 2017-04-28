package Buycraft.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import Buycraft.tasks.ReportTask;

public class Settings {
    private final String LOCATION = "plugins_mod/Buycraft/settings.conf";
    private File file;

    private HashMap<String, String> defaultProperties;
    private Properties properties;

    public Settings() {
        this.file = new File(LOCATION);

        this.defaultProperties = new HashMap<String, String>();
        this.properties = new Properties();

        load();
        assignDefault();
    }

    private void load() {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            properties.load(new FileInputStream(LOCATION));
        } catch (IOException e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }

    private void assignDefault() {
        Boolean toSave = false;

        defaultProperties.put("secret", "");
        defaultProperties.put("commandChecker", "true");
        defaultProperties.put("https", "false");
        defaultProperties.put("debug", "false");
        defaultProperties.put("commandThrottleCount", "150");
        defaultProperties.put("disable-secret-command", "false");

        for (Entry<String, String> entry : defaultProperties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!properties.containsKey(key)) {
                properties.setProperty(key, value);

                toSave = true;
            }
        }

        if (toSave) {
            saveSettings();
        }
    }

    private void saveSettings() {
        try {
            properties.store(new FileOutputStream(LOCATION), "Buycraft Plugin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        } catch (IOException e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }

    public boolean getBoolean(String key) {
        if (properties.containsKey(key)) {
            return Boolean.valueOf(getString(key));
        } else {
            throw new RuntimeException("Settings key '" + key + "' not found in the settings.conf file.");
        }

    }

    public int getInt(String key) {
        if (properties.containsKey(key)) {
            return Integer.valueOf(getString(key));
        } else {
            throw new RuntimeException("Settings key '" + key + "' not found in the settings.conf file.");
        }

    }
    
    public String getString(String key) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        } else {
            throw new RuntimeException("Settings key '" + key + "' not found in the settings.conf file.");
        }
    }

    public void setString(String key, String value) {
        properties.setProperty(key, value);

        saveSettings();
    }
}
