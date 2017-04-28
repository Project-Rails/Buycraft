package Buycraft.tasks;

import Buycraft.MyPlugin;
import Buycraft.api.ApiTask;

import org.json.JSONObject;

import java.util.TimerTask;

public class AuthenticateTask extends ApiTask {
    private MyPlugin plugin;

    public static void call() {
        MyPlugin.getInstance().addTask(new AuthenticateTask());
    }

    private AuthenticateTask() {
        this.plugin = MyPlugin.getInstance();
    }

    public void run() {
        try {
            final JSONObject apiResponse = plugin.getApi().authenticateAction();
            plugin.setAuthenticated(false);
            // call sync
            if (apiResponse != null) {
                        try {
                            plugin.setAuthenticatedCode(apiResponse.getInt("code"));

                            if (apiResponse.getInt("code") == 0) {
                                JSONObject payload = apiResponse.getJSONObject("payload");

                                plugin.setServerID(payload.getInt("serverId"));
                                plugin.setServerCurrency(payload.getString("serverCurrency"));
                                plugin.setServerStore(payload.getString("serverStore"));
                                plugin.setPendingPlayerCheckerInterval(payload.getInt("updateUsernameInterval"));
                                plugin.setAuthenticated(true);

                                plugin.getLogger().info("Authenticated with the specified Secret key.");
                                plugin.getLogger().info("Plugin is now ready to be used.");

                            } else if (apiResponse.getInt("code") == 101) {
                                plugin.getLogger().severe("The specified Secret key could not be found.");
                                plugin.getLogger().severe("Type /buycraft for further advice & help.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ReportTask.setLastException(e);
                        }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }
}
