package fr.formiko.flagsh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class FlagsHPlugin extends JavaPlugin {
    private List<Flag> flags;
    private static final String FLAGS_FILE_PATH = "plugins/FlagsH/flags.data";

    public List<Flag> getFlags() { return flags; }

    @Override
    public void onEnable() {
        FlagsH.plugin = this;

        getConfig().addDefault("maxFlagSize", 5f);
        getConfig().addDefault("increasingSizeStep", 0.5f);
        getConfig().addDefault("flagEnable", true);
        getConfig().addDefault("bannerEnable", true);
        // TODO add a boolean to allow or not the extention of the flag with wool instead of banner
        // TODO add a boolean to allow or not reduction of the size with a shear
        getConfig().options().copyDefaults(true);
        saveConfig();

        // create the file if it doesn't exist
        if (new File(FLAGS_FILE_PATH).exists()) {
            loadFlags();
        } else {
            flags = new ArrayList<>();
        }
        getLogger().info("FlagsH loaded " + flags.size() + " flags.");


        getServer().getPluginManager().registerEvents(new FlagsHListener(), this);
    }

    @Override
    public void onDisable() { saveFlags(); }


    public boolean saveFlags() {
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(FLAGS_FILE_PATH)))) {
            out.writeObject(flags);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("unchecked")
    public boolean loadFlags() {
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(FLAGS_FILE_PATH)))) {
            flags = (List) in.readObject();
            return true;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
