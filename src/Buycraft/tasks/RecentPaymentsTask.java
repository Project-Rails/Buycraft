package Buycraft.tasks;

import PluginReference.ChatColor;
import PluginReference.MC_Player;
import Buycraft.MyPlugin;
import Buycraft.api.ApiTask;
import Buycraft.util.Chat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecentPaymentsTask extends ApiTask {
    
    private MC_Player receiver;
    private String playerLookup;
    
    public static void call(MC_Player receiver, String player) {
        MyPlugin.getInstance().addTask(new RecentPaymentsTask(receiver, player));
    }

    private RecentPaymentsTask(MC_Player receiver, String playerLookup) {
        this.receiver = receiver;
        this.playerLookup = playerLookup;
    }

    public void run() {
        try {
            
            JSONObject apiResponse = getApi().paymentsAction(10, playerLookup.length() > 0, playerLookup);
            
            if (apiResponse != null) {
                
                JSONArray entries = apiResponse.getJSONArray("payload");
                
                if(entries != null && entries.length() > 0) {
                    receiver.sendMessage(Chat.header());
                    receiver.sendMessage(Chat.seperator());
                    
                    if(playerLookup.isEmpty())
                    {
                        receiver.sendMessage(Chat.seperator() + "Displaying recent payments over all users: ");
                    }
                    else
                    {
                        receiver.sendMessage(Chat.seperator() + "Displaying recent payments from the user " + playerLookup + ":");
                    }

                    receiver.sendMessage(Chat.seperator());
                    
                    for(int i=0; i<entries.length(); i++) {
                        
                        JSONObject entry = entries.getJSONObject(i);
                        
                        receiver.sendMessage(Chat.seperator() + "[" + entry.getString("humanTime") + "] " + ChatColor.LIGHT_PURPLE + entry.getString("ign") + ChatColor.GREEN + " (" + entry.getString("price") + " " + entry.getString("currency") + ")");
                    }
                    
                    receiver.sendMessage(Chat.seperator());
                    receiver.sendMessage(Chat.footer());
                }
                else
                {
                    receiver.sendMessage(Chat.header());
                    receiver.sendMessage(Chat.seperator());
                    receiver.sendMessage(Chat.seperator() + ChatColor.RED + "No recent payments to display.");
                    receiver.sendMessage(Chat.seperator());
                    receiver.sendMessage(Chat.footer());
                }
            } 
        } catch (JSONException e) {
            getLogger().severe("JSON parsing error.");
            ReportTask.setLastException(e);
        }
    }
}
