package Buycraft;

import PluginReference.*;
import Buycraft.api.Api;
import Buycraft.api.ApiTask;
import Buycraft.commands.BuycraftCommand;
import Buycraft.tasks.AuthenticateTask;
import Buycraft.tasks.CommandDeleteTask;
import Buycraft.tasks.CommandExecuteTask;
import Buycraft.tasks.PendingPlayerCheckerTask;
import Buycraft.tasks.ReportTask;
import Buycraft.util.Chat;
import Buycraft.util.Language;
import Buycraft.util.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MyPlugin extends PluginBase {
    private static Logger logger = Logger.getLogger("Buycraft");

    private static MC_Server server;

    private static MyPlugin instance;

    public static String version = "17w17a";

    private Settings settings;
    private Language language;
    
    private Api api;

    private int serverID = 0;
    private String serverCurrency = "";
    private String serverStore = "";

    private CommandExecuteTask commandExecutor;
    private CommandDeleteTask commandDeleteTask;
    private PendingPlayerCheckerTask pendingPlayerCheckerTask;

    public Timer pendingPlayerCheckerTaskExecutor;

    private boolean authenticated = false;
    private int authenticatedCode = 1;

    private String folderPath;

    private ExecutorService executors = null;

    private boolean enabled = false;
    
    public MyPlugin() {
        instance = this;
    }

    public void addTask(ApiTask task) {
        executors.submit(task);
    }

    public void onStartup(MC_Server argServer) {
        logger.info("============ Buycraft ============");
        logger.info("Buycraft is strating up...");
        server = argServer;

        // thread pool model
        executors = Executors.newFixedThreadPool(5);
        folderPath = "plugins_mod" + File.separator + "Buycraft" + File.separator;
        
        checkDirectory();

        moveFileFromJar("README.md", getFolderPath() + "/README.txt", true);

        version = "1.1";

        settings = new Settings();
        language = new Language();

        api = new Api();

        commandExecutor = new CommandExecuteTask();
        commandDeleteTask = new CommandDeleteTask();
        pendingPlayerCheckerTask = new PendingPlayerCheckerTask();

        server.registerCommand(new BuycraftCommand());

        enabled = true;
    }

    public void onServerFullyLoaded() {
        logger.info("Authenticating Buycraft...");
        AuthenticateTask.call();
    }

    public PluginInfo getPluginInfo()
    {
        PluginInfo info = new PluginInfo();
        info.description = "Buycraft for Project Rails (" + version + ")";
        info.eventSortOrder = 1000.0f; // call me much later on than default.
        return info;
    }

    public void onPlayerJoin(MC_Player plr) {
        if (plr.getName().equalsIgnoreCase("Buycraft")) {
            plr.kick("This user has been disabled due to security reasons.");
            return;
        }
        pendingPlayerCheckerTask.onPlayerJoin(plr);
    }

    public void onShutdown() {
        logger.info("Buycraft is shutting down...");
        enabled = false;

        // Make sure any commands which have been run are deleted
        commandDeleteTask.runNow();

        executors.shutdown();
        while (!executors.isTerminated()) {
        }
        getLogger().info("Plugin has been disabled.");
    }

    private void checkDirectory() {
        File directory = new File(getFolderPath());

        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public void moveFileFromJar(String jarFileName, String targetLocation, Boolean overwrite) {
        try {
            File targetFile = new File(targetLocation);

            if (overwrite || targetFile.exists() == false || targetFile.length() == 0) {
                InputStream inFile = MyPlugin.class.getClassLoader().getResourceAsStream(jarFileName);
                FileWriter outFile = new FileWriter(targetFile);

                int c;

                while ((c = inFile.read()) != -1) {
                    outFile.write(c);
                }

                inFile.close();
                outFile.close();
            }
        } catch (NullPointerException e) {
            getLogger().info("Failed to create " + jarFileName + ".");
            ReportTask.setLastException(e);
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }

    public Boolean isAuthenticated(MC_Player player) {
        if (!authenticated) {
            if (player != null) {
                player.sendMessage(Chat.header());
                player.sendMessage(Chat.seperator());
                player.sendMessage(Chat.seperator() + ChatColor.RED + "Buycraft has failed to startup.");
                player.sendMessage(Chat.seperator());
                if(authenticatedCode == 101)  {
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "This is because of an invalid secret key,");
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "please enter the Secret key into the settings.conf");
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "file, and reload your server.");
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "You can find your secret key from the control panel.");
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "If it did not resolve the issue, restart your server");
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "a couple of times.");
                    player.sendMessage(Chat.seperator());
                } else {
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "Please execute the '/buycraft report' command and");
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "then send the generated report.txt file to");
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "support@buycraft.net. We will be happy to help.");
                    player.sendMessage(Chat.seperator());
                }
                player.sendMessage(Chat.footer());
            }

            return false;
        } else {
            return true;
        }
    }

    public void setAuthenticated(Boolean value) {
        authenticated = value;
    }

    public void setAuthenticatedCode(Integer value) {
        authenticatedCode = value;
    }

    public Integer getAuthenticatedCode() {
        return authenticatedCode;
    }

    public static MyPlugin getInstance() {
        return instance;
    }

    public Api getApi() {
        return api;
    }

    public void setServerID(Integer value) {
        serverID = value;
    }

    public void setServerCurrency(String value) {
        serverCurrency = value;
    }

    public void setServerStore(String value) {
        serverStore = value;
    }

    public void setPendingPlayerCheckerInterval(int interval) {
        if (pendingPlayerCheckerTaskExecutor != null) {
            pendingPlayerCheckerTaskExecutor.cancel();
            pendingPlayerCheckerTaskExecutor = null;
        }

        // Convert seconds to ticks
        interval *= 20;

        if (getSettings().getBoolean("commandChecker")) {
            pendingPlayerCheckerTaskExecutor = new Timer();
            pendingPlayerCheckerTaskExecutor.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    pendingPlayerCheckerTask.call(false);
                }
            }, 20, interval);
        }

    }

    public Integer getServerID() {
        return serverID;
    }

    public String getServerStore() {
        return serverStore;
    }
    
    public CommandExecuteTask getCommandExecutor() {
        return commandExecutor;
    }

    public CommandDeleteTask getCommandDeleteTask() {
        return commandDeleteTask;
    }

    public PendingPlayerCheckerTask getPendingPlayerCheckerTask() {
        return pendingPlayerCheckerTask;
    }

    public String getServerCurrency() {
        return serverCurrency;
    }

    public String getVersion() {
        return version;
    }

    public Settings getSettings() {
        return settings;
    }

    public Language getLanguage() {
        return language;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public Logger getLogger() {
        return logger;
    }

    public MC_Server getServer() {
        return server;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
