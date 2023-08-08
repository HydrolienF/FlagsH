package fr.formiko.flagsh;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class FlagsHPlugin extends JavaPlugin {
    private List<Flag> flags;

    public List<Flag> getFlags() { return flags; }

    @Override
    public void onEnable() {
        FlagsH.plugin = this;

        getConfig().addDefault("maxFlagSize", 5f);
        getConfig().addDefault("increasingSizeStep", 0.5f);
        // TODO add a boolean to allow or not the creation of bigger banners
        // TODO add a boolean to allow or not the creation of bigger flags
        // TODO add a boolean to allow or not the extention of the flag with wool instead of banner
        // TODO add a boolean to allow or not reduction of the size with a shear
        getConfig().options().copyDefaults(true);
        saveConfig();

        // TODO load flags from file
        flags = new ArrayList<>();

        getServer().getPluginManager().registerEvents(new FlagsHListener(), this);

        getLogger().info("FlagsHPlugin enabled");
    }

    @Override
    public void onDisable() { getLogger().info("FlagsHPlugin disabled"); }

    @Override
    public void onLoad() { getLogger().info("FlagsHPlugin loaded"); }
}
