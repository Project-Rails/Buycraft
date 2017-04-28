package Buycraft.api;

import Buycraft.MyPlugin;
import Buycraft.tasks.ReportTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Api {
    private MyPlugin plugin;

    private String apiUrl;
    private String apiKey;

    public Api() {
        this.plugin = MyPlugin.getInstance();
        this.apiKey = plugin.getSettings().getString("secret");

        if (plugin.getSettings().getBoolean("https")) {
            this.apiUrl = "https://api.buycraft.net/v4";
        } else {
            this.apiUrl = "http://api.buycraft.net/v4";
        }
    }

    public JSONObject authenticateAction() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "info");
        
        apiCallParams.put("serverPort", String.valueOf(plugin.getServer().getServerPort()));
        apiCallParams.put("onlineMode", String.valueOf(plugin.getServer().getOnlineMode()));
        apiCallParams.put("playersMax", String.valueOf(9000)); // No current API support
        apiCallParams.put("version", plugin.getVersion());

        return call(apiCallParams);
    }

    public JSONObject categoriesAction() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "categories");

        return call(apiCallParams);
    }
    
    public JSONObject urlAction(String url) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "url");
        apiCallParams.put("url", url);

        return call(apiCallParams);
    }

    public JSONObject packagesAction() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "packages");

        return call(apiCallParams);
    }

    public JSONObject paymentsAction(int limit, boolean usernameSpecific, String username) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "payments");
        apiCallParams.put("limit", String.valueOf(limit));
        
        if(usernameSpecific) {
            apiCallParams.put("ign", username);
        }

        return call(apiCallParams);
    }

    public JSONObject fetchPendingPlayers() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "pendingUsers");

        return call(apiCallParams);
    }

    public JSONObject fetchPlayerCommands(JSONArray players, boolean offlineCommands) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "commands");
        apiCallParams.put("do", "lookup");

        apiCallParams.put("users", players.toString());
        apiCallParams.put("offlineCommands", String.valueOf(offlineCommands));
        apiCallParams.put("offlineCommandLimit", String.valueOf(plugin.getSettings().getInt("commandThrottleCount")));

        return call(apiCallParams);
    }

    public void commandsDeleteAction(String commandsToDelete) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "commands");
        apiCallParams.put("do", "removeId");

        apiCallParams.put("commands", commandsToDelete);

        call(apiCallParams);
    }

    private JSONObject call(HashMap<String, String> apiCallParams) {
        if (apiKey.length() == 0) {
            apiKey = "unspecified";
        }

        apiCallParams.put("secret", apiKey);
        apiCallParams.put("playersOnline", String.valueOf(plugin.getServer().getPlayers().size()));
  
        String url = apiUrl + generateUrlQueryString(apiCallParams);

        if (url != null) {
            String HTTPResponse = HttpRequest(url);

            try {
                if (HTTPResponse != null) {
                    return new JSONObject(HTTPResponse);
                } else {
                    return null;
                }
            } catch (JSONException e) {
                plugin.getLogger().severe("JSON parsing error.");
                ReportTask.setLastException(e);
            }
        }

        return null;
    }

    public static String HttpRequest(String url) {
        try {
        	
        	if(MyPlugin.getInstance().getSettings().getBoolean("debug")) {
        		MyPlugin.getInstance().getLogger().info("---------------------------------------------------");
        		MyPlugin.getInstance().getLogger().info("Request URL: " + url);
        	}
        	
            String content = "";

            URL conn = new URL(url);
            
            HttpURLConnection yc = (HttpURLConnection) conn.openConnection();
            
            yc.setRequestMethod("GET");
            yc.setConnectTimeout(15000);
            yc.setReadTimeout(15000);
            yc.setInstanceFollowRedirects(false);
            yc.setAllowUserInteraction(false);
            
            BufferedReader in;
            
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content = content + inputLine;
            }

            in.close();
            
            if(MyPlugin.getInstance().getSettings().getBoolean("debug")) {
            	MyPlugin.getInstance().getLogger().info("JSON Response: " + content);
            	MyPlugin.getInstance().getLogger().info("---------------------------------------------------");
            }
            
            return content;
        } catch (ConnectException e) {
            MyPlugin.getInstance().getLogger().severe("HTTP request failed due to connection error.");
            ReportTask.setLastException(e);
        } catch (SocketTimeoutException e) {
            MyPlugin.getInstance().getLogger().severe("HTTP request failed due to timeout error.");
            ReportTask.setLastException(e);
        } catch (FileNotFoundException e) {
            MyPlugin.getInstance().getLogger().severe("HTTP request failed due to file not found.");
            ReportTask.setLastException(e);
        } catch (UnknownHostException e) {
            MyPlugin.getInstance().getLogger().severe("HTTP request failed due to unknown host.");
            ReportTask.setLastException(e);
        } catch (IOException e) {
        	 MyPlugin.getInstance().getLogger().severe(e.getMessage());
             ReportTask.setLastException(e);
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
        
        return null;
    }

    private static String generateUrlQueryString(HashMap<String, String> map) {
        StringBuilder sb = new StringBuilder();

        sb.append("?");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 1) {
                sb.append("&");
            }

            sb.append(String.format("%s=%s",
                    entry.getKey().toString(),
                    entry.getValue().toString()
            ));
        }

        return sb.toString();
    }

    public void setApiKey(String value) {
        apiKey = value;
    }
}
