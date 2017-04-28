package Buycraft.util;

import PluginReference.ChatColor;

public class Chat {
    private static final String header = ChatColor.WHITE + "|----------------------" + ChatColor.LIGHT_PURPLE + " BUYCRAFT " + ChatColor.WHITE + "---------------------";
    private static final String footer = ChatColor.WHITE + "|----------------------------------------------------";
    private static final String seperator = ChatColor.WHITE + "| ";

    private Chat() {}

    public static String header() {
        return header;
    }

    public static String footer() {
        return footer;
    }

    public static String seperator() {
        return seperator;
    }
}
