package fr.formiko.flagsh;

import org.bukkit.plugin.java.JavaPlugin;

public class FlagsHPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        FlagsH.plugin = this;

        getConfig().addDefault("maxFlagSize", 5f);
        getConfig().addDefault("increasingSizeStep", 0.5f);
        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new PlaceListener(), this);

        getLogger().info("FlagsHPlugin enabled");
    }

    @Override
    public void onDisable() { getLogger().info("FlagsHPlugin disabled"); }

    @Override
    public void onLoad() { getLogger().info("FlagsHPlugin loaded"); }
}
