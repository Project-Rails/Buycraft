package RainbowBuycraft.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import PluginReference.MC_Player;
import RainbowBuycraft.MyPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import RainbowBuycraft.api.ApiTask;

/**
 * Fetches an array of players which are waiting for commands to be run.
 * 
 * If any players in the list are online the command fetch task is called
 * If a player which is in the pending players set joins the server the command fetch task is called
 *
 */
public class PendingPlayerCheckerTask extends ApiTask {

    private final MyPlugin plugin;
    private final AtomicBoolean running = new AtomicBoolean(false);
    /** Stores players with pending commands in lower case */
    private HashSet<String> pendingPlayers = new HashSet<String>();
    private boolean manualExecution;
    private long lastPlayerLogin;

    public PendingPlayerCheckerTask() {
        plugin = MyPlugin.getInstance();
        lastPlayerLogin = System.currentTimeMillis();
    }

    public void call(boolean manualExecution) {
        if (running.compareAndSet(false, true)) {
            this.manualExecution = manualExecution;
            addTask(this);
        }
    }

    public synchronized void onPlayerJoin(MC_Player player) {
        // If the player has pending commands we call the package checker
        if (pendingPlayers.remove(player.getName().toLowerCase())) {
            CommandFetchTask.call(false, player);
        }
        lastPlayerLogin = System.currentTimeMillis();
    }

    public void run() {
        try {
            // Don't attempt to run if we are not authenticated
            if (!plugin.isAuthenticated(null)) {
                return;
            }

            // If the command checker is disabled and this was not a manual execution we do nothing
            if (!plugin.getSettings().getBoolean("commandChecker") && !manualExecution) {
                return;
            }

            // Fetch online player list
            MC_Player[] onlinePlayers = plugin.getServer().getPlayers().toArray(new MC_Player[plugin.getServer().getPlayers().size()]);

            // If nobody has logged in for over 3 hours do not execute the package checker (Manual execution is an exception)
            if (!manualExecution && lastPlayerLogin < (System.currentTimeMillis() - 1080000)) {
                return;
            } else if (onlinePlayers.length > 0) {
                lastPlayerLogin = System.currentTimeMillis();
            }

            // Fetch pending players
            JSONObject apiResponse = plugin.getApi().fetchPendingPlayers();

            if (apiResponse == null || apiResponse.getInt("code") != 0) {
                plugin.getLogger().severe("No response/invalid key during pending players check.");
                return;
            }

            JSONObject apiPayload = apiResponse.getJSONObject("payload");

            JSONArray pendingPlayers = apiPayload.getJSONArray("pendingPlayers");
            boolean offlineCommands = apiPayload.getBoolean("offlineCommands");

            // Clear current pending players (Just in case some don't have pending commands anymore)
            resetPendingPlayers();

            ArrayList<MC_Player> onlinePendingPlayers = null;
            // No point in this if there are no pending players
            if (pendingPlayers.length() > 0) {
                onlinePendingPlayers = new ArrayList<MC_Player>();

                // Iterate through each pending player
                for (int i = 0; i < pendingPlayers.length(); ++i) {
                    String playerName = pendingPlayers.getString(i).toLowerCase();
                    MC_Player player = getPlayer(onlinePlayers, playerName);

                    // Check if the player is offline
                    if (player == null) {
                        // Add them to the pending players set
                        addPendingPlayer(playerName);
                    } else {
                        // Add the player to this online pending players list
                        onlinePendingPlayers.add(player);
                    }
                }
            }

            // Check if we need to run the command checker
            if (offlineCommands || (onlinePendingPlayers != null && !onlinePendingPlayers.isEmpty())) {
                // Create the array of players which will need commands to be fetched now
                MC_Player[] players = onlinePendingPlayers != null ? onlinePendingPlayers.toArray(new MC_Player[onlinePendingPlayers.size()]) : new MC_Player[] {};

                // Call the command fetch task
                CommandFetchTask.call(offlineCommands, players);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        } finally {
            running.set(false);
        }
    }

    private synchronized void resetPendingPlayers() {
        pendingPlayers.clear();
    }

    private synchronized void addPendingPlayer(String playerName) {
        pendingPlayers.add(playerName.toLowerCase());
    }

    private MC_Player getPlayer(MC_Player[] players, String name) {
        for (MC_Player player : players) {
            if (player.getName().equalsIgnoreCase(name))
                return player;
        }
        return null;
    }
}
