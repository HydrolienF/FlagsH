package fr.formiko.flagsh;

import org.bukkit.plugin.java.JavaPlugin;

public class FlagsHPlugin extends JavaPlugin {
    @Override
    public void onEnable() { getLogger().info("FlagsHPlugin enabled"); }

    @Override
    public void onDisable() { getLogger().info("FlagsHPlugin disabled"); }

    @Override
    public void onLoad() { getLogger().info("FlagsHPlugin loaded"); }

    // public static void sendMessage(String message) { Bukkit.getConsoleSender().sendMessage("[FlagsH] " + message); }
}
