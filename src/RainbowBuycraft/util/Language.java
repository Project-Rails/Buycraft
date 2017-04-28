package Buycraft.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import Buycraft.tasks.ReportTask;

public class Language {
    private final String LOCATION = "plugins_mod/Buycraft/language.conf";
    private File file;

    private HashMap<String, String> defaultProperties;
    private Properties properties;

    public Language() {
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

        defaultProperties.put("commandsExecuted", "Your purchased packages have been credited.");
        defaultProperties.put("commandExecuteNotEnoughFreeInventory", "%d free inventory slot(s) are required.");
        defaultProperties.put("commandExecuteNotEnoughFreeInventory2", "Please empty your inventory to receive these items.");

        for (Entry<String, String> entry : defaultProperties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!properties.containsKey(key)) {
                properties.setProperty(key, value);

                toSave = true;
            }
        }

        if (toSave) {
            try {
                properties.store(new FileOutputStream(LOCATION), "Buycraft Plugin (English language file)");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ReportTask.setLastException(e);
            } catch (IOException e) {
                e.printStackTrace();
                ReportTask.setLastException(e);
            }
        }
    }

    public String getString(String key) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        } else {
            throw new RuntimeException("Language key '" + key + "' not found in the language.conf file.");
        }
    }
}
