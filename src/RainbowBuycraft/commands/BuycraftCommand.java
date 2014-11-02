package RainbowBuycraft.commands;

import PluginReference.ChatColor;
import PluginReference.MC_Command;
import PluginReference.MC_Player;
import RainbowBuycraft.MyPlugin;
import RainbowBuycraft.tasks.AuthenticateTask;
import RainbowBuycraft.tasks.RecentPaymentsTask;
import RainbowBuycraft.tasks.ReportTask;
import RainbowBuycraft.util.Chat;

import java.util.List;

public class BuycraftCommand implements MC_Command {
    MyPlugin plugin = MyPlugin.getInstance();

    @Override
    public String getCommandName() {
        return "buycraft";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getHelpLine(MC_Player player) {
        return ChatColor.AQUA + "/buycraft" + ChatColor.WHITE + " <reload/forcecheck/secret/payments <ign>>";
    }

    @Override
    public void handleCommand(MC_Player player, String[] args) {
        if (args.length > 0) {

            if(args[0].equalsIgnoreCase("payments")) {
                String playerLookup = "";

                if(args.length == 2) {
                    playerLookup = args[1];
                }

                RecentPaymentsTask.call(player, playerLookup);

                return;
            }

            if (args[0].equalsIgnoreCase("report")) {
                // Call the report task, if it fails we don't send the following messages to the player
                if (ReportTask.call(player)) {
                    player.sendMessage(Chat.header());
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.seperator() + ChatColor.GREEN + "Beginning generation of report");
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.footer());
                }
                return;
            }

            if (args[0].equalsIgnoreCase("secret")) {
                if(plugin.getSettings().getBoolean("disable-secret-command") == false) {
                    if (args.length == 2) {
                        String secretKey = args[1];

                            player.sendMessage(Chat.header());
                            player.sendMessage(Chat.seperator());
                            player.sendMessage(Chat.seperator() + ChatColor.GREEN + "Server authenticated. Type /buycraft for confirmation.");
                            player.sendMessage(Chat.seperator());
                            player.sendMessage(Chat.footer());

                        plugin.getSettings().setString("secret", secretKey);
                        plugin.getApi().setApiKey(secretKey);

                        AuthenticateTask.call();

                        return;
                    } else {
                        player.sendMessage(Chat.header());
                        player.sendMessage(Chat.seperator());
                        player.sendMessage(Chat.seperator() + ChatColor.RED + "Please enter a valid secret key.");
                        player.sendMessage(Chat.seperator());
                        player.sendMessage(Chat.footer());

                        return;
                    }
                } else {
                    player.sendMessage(Chat.header());
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.seperator() + ChatColor.RED + "Please change the key in settings.conf.");
                    player.sendMessage(Chat.seperator());
                    player.sendMessage(Chat.footer());

                    return;
                }
            }

            if (plugin.isAuthenticated(player)) {
                if (args[0].equalsIgnoreCase("forcecheck")) {
                    plugin.getPendingPlayerCheckerTask().call(true);

                        player.sendMessage(Chat.header());
                        player.sendMessage(Chat.seperator());
                        player.sendMessage(Chat.seperator() + ChatColor.GREEN + "Force check successfully executed.");
                        player.sendMessage(Chat.seperator());
                        player.sendMessage(Chat.footer());

                    return;
                }
            }

        } else {

            MyPlugin plugin = MyPlugin.getInstance();

            if (plugin.isAuthenticated(player)) {
                player.sendMessage(Chat.header());
                player.sendMessage(Chat.seperator());

                if (player.hasPermission("buycraft.admin")) {
                    player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "/buycraft forcecheck:" + ChatColor.GREEN + " Check for pending commands");
                    player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "/buycraft secret <key>:" + ChatColor.GREEN + " Set the Secret key");
                    player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "/buycraft payments <ign>:" + ChatColor.GREEN + " Get recent payments of a user");
                    player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "/buycraft report:" + ChatColor.GREEN + " Generate an error report");
                }

                player.sendMessage(Chat.seperator());
                player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "Server ID: " + ChatColor.GREEN + String.valueOf(plugin.getServerID()));
                player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "Server URL: " + ChatColor.GREEN + String.valueOf(plugin.getServerStore()));
                player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "Version: " + ChatColor.GREEN + String.valueOf(plugin.getVersion()) + ChatColor.GOLD + " CoolV1994's Port.");
                player.sendMessage(Chat.seperator() + ChatColor.LIGHT_PURPLE + "Website: " + ChatColor.GREEN + "http://www.project-rainbow.org/site/plugin-releases/rainbowbuycraft/");
                player.sendMessage(Chat.footer());
            }

        }
    }

    @Override
    public boolean hasPermissionToUse(MC_Player player) {
        return player.hasPermission("buycraft.admin") || player.isOp();
    }

    @Override
    public List<String> getTabCompletionList(MC_Player mc_player, String[] strings) {
        return null;
    }
}
