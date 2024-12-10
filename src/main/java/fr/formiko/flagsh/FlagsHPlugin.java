package fr.formiko.flagsh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nonnull;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import co.aikar.commands.PaperCommandManager;

public class FlagsHPlugin extends JavaPlugin {
    private List<Flag> flags;

    public @Nonnull List<Flag> getFlags() { return flags; }
    private @Nonnull File getDataFile() { return new File(getDataFolder(), "flags.data"); }

    @Override
    public void onEnable() {
        FlagsH.setPlugin(this);

        new Metrics(this, 19981);

        saveDefaultConfig();


        // Load flags from data file.
        if (getDataFile().exists()) {
            loadFlags();
        }
        if (flags == null) { // Init list if data file was not found or fail to be read.
            flags = new ArrayList<>();
        }
        getLogger().info(() -> "FlagsH loaded " + flags.size() + " flags.");
        // since Java 8, we can use Supplier, which will be evaluated lazily (better for performance when logging is disabled)


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
            getLogger().log(java.util.logging.Level.SEVERE, "Error while saving flags.", e);
            return false;
        }
    }
    @SuppressWarnings("unchecked")
    private boolean loadFlags() {
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(getDataFile())))) {
            flags = (List<Flag>) in.readObject();
            return true;
        } catch (ClassNotFoundException | IOException e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Error while loading flags.", e);
            return false;
        }
    }

    public void reloadFlagDataFile() {
        if (saveFlags()) {
            loadFlags();
        }
    }

    public void debug(String msg) {
        if (getConfig().getBoolean("debug")) {
            getLogger().info(msg);
        }
    }
    public void debug(Supplier<String> msgSupplier) {
        if (getConfig().getBoolean("debug")) {
            getLogger().info(msgSupplier);
        }
    }

    public static FlagsHPlugin getInstance() { return getPlugin(FlagsHPlugin.class); }
}
