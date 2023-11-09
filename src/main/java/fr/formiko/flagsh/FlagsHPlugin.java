package fr.formiko.flagsh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import co.aikar.commands.PaperCommandManager;

public class FlagsHPlugin extends JavaPlugin {
    private List<Flag> flags;

    public @NotNull List<Flag> getFlags() { return flags; }
    private @NotNull File getDataFile() { return new File(getDataFolder(), "flags.data"); }

    @Override
    public void onEnable() {
        FlagsH.setPlugin(this);

        new Metrics(this, 19981);

        getConfig().addDefault("maxFlagSize", 5f);
        getConfig().addDefault("increasingSizeStep", 0.5f);
        getConfig().addDefault("flagEnable", true);
        getConfig().addDefault("bannerEnable", true);
        getConfig().addDefault("forbidenInteractGamemodes", List.of("ADVENTURE"));
        // TODO add a boolean to allow or not the extention of the flag with wool instead of banner
        // TODO add a boolean to allow or not reduction of the size with a shear
        getConfig().options().copyDefaults(true);
        saveConfig();


        // Load flags from data file.
        if (getDataFile().exists()) {
            loadFlags();
        }
        if (flags == null) { // Init list if data file was not found or fail to be read.
            flags = new ArrayList<>();
        }
        getLogger().info("FlagsH loaded " + flags.size() + " flags.");


        getServer().getPluginManager().registerEvents(new FlagsHListener(), this);
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new FlagsHCommand());
        manager.getCommandCompletions().registerAsyncCompletion("flagshId", c -> {
            List<String> l = new ArrayList<>();
            l.add("all");
            for (int i = 0; i < flags.size(); i++) {
                l.add(String.valueOf(i + 1));
            }
            return l;
        });
    }

    @Override
    public void onDisable() { saveFlags(); }


    private boolean saveFlags() {
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(getDataFile())))) {
            out.writeObject(flags);
            return true;
        } catch (IOException e) {
            getLogger().warning("Error while saving flags.");
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("unchecked")
    private boolean loadFlags() {
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(getDataFile())))) {
            flags = (List) in.readObject();
            return true;
        } catch (ClassNotFoundException | IOException e) {
            getLogger().warning("Error while loading flags.");
            e.printStackTrace();
            return false;
        }
    }

    public void reloadFlagDataFile() {
        if (saveFlags()) {
            loadFlags();
        }
    }
}
