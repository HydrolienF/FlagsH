package fr.formiko.flagsh;

import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;

public class FlagsHConfig {
    enum OffHandMod {
        DEFAULT, VANILLA, INVERTED
    }
    private static OffHandMod offHandMod;
    private static List<GameMode> gameModes;
    private static double maxFlagSize;
    private static double increasingSizeStep;

    public static void reload() {
        if (FlagsHPlugin.getInstance().getConfig().getDouble("maxFlagSize", -1.0) <= 0) {
            FlagsHPlugin.getInstance().getLogger()
                    .warning(() -> "Invalid maxFlagSize value: " + FlagsHPlugin.getInstance().getConfig().getDouble("maxFlagSize"));
            maxFlagSize = 10.0;
        } else {
            maxFlagSize = FlagsHPlugin.getInstance().getConfig().getDouble("maxFlagSize", 10.0);
        }
        if (FlagsHPlugin.getInstance().getConfig().getDouble("increasingSizeStep", -1.0) <= 0) {
            FlagsHPlugin.getInstance().getLogger().warning(
                    () -> "Invalid increasingSizeStep value: " + FlagsHPlugin.getInstance().getConfig().getDouble("increasingSizeStep"));
            increasingSizeStep = 0.5;
        } else {
            increasingSizeStep = FlagsHPlugin.getInstance().getConfig().getDouble("increasingSizeStep", 0.5);
        }
        try {
            offHandMod = OffHandMod.valueOf(FlagsHPlugin.getInstance().getConfig().getString("offHandMod").toUpperCase());
        } catch (Exception e) {
            FlagsHPlugin.getInstance().getLogger().warning("Invalid offHandMod value");
            offHandMod = OffHandMod.DEFAULT;
        }
        if (gameModes == null) {
            try {
                gameModes = FlagsHPlugin.getInstance().getConfig().getStringList("forbidenInteractGamemodes").stream()
                        .map(String::toUpperCase).map(GameMode::valueOf).toList();
            } catch (Exception e) {
                FlagsHPlugin.getInstance().getLogger().warning("Invalid game mode in forbidenInteractGamemodes");
                gameModes = List.of();
            }
        }
    }


    public static boolean debug() { return FlagsHPlugin.getInstance().getConfig().getBoolean("debug", false); }

    public static boolean flagEnable() { return FlagsHPlugin.getInstance().getConfig().getBoolean("flagEnable", true); }
    public static boolean bannerEnable() { return FlagsHPlugin.getInstance().getConfig().getBoolean("bannerEnable", true); }

    public static double maxFlagSize() { return maxFlagSize; }
    public static double increasingSizeStep() { return increasingSizeStep; }

    public static OffHandMod offHandMod() { return offHandMod; }

    public static boolean isForbidenInteractGamemodes(GameMode gameMode) { return gameModes.contains(gameMode); }

    public static Sound sound(String soundKey) {
        FlagsHPlugin.getInstance().debug(() -> "Flag.playSound() of "+"sounds." + soundKey + ".sound"+": soundName: " + FlagsHPlugin.getInstance().getConfig().getString("sounds." + soundKey + ".sound"));
        String soundName = FlagsHPlugin.getInstance().getConfig().getString("sounds." + soundKey + ".sound");
        if(soundName == null) {
            soundName = switch(soundKey) {
                case "extend" -> "minecraft:block.wool.place";
                case "break" -> "minecraft:block.wood.break";
                case "forbiden_action" -> "minecraft:entity.villager.no";
                default -> "minecraft:block.wool.place";
            };
        }
        String [] t = soundName.split(":");
        NamespacedKey soundNa = new NamespacedKey(t[0], t[1]);
        // return io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry(io.papermc.paper.registry.RegistryKey.SOUND_EVENT).get(soundNa);

        // If minecraft version is >= 1.20.6, use io.papermc.paper.registry.RegistryAccess
        // TODO drop support for version below 1.20.6 when 1.22 will be out and Sound.valueOf no longer exists.
        try {
            Class<?> klassRegistryAccess = Class.forName("io.papermc.paper.registry.RegistryAccess");
            Class<?> klassRegistryKey = Class.forName("io.papermc.paper.registry.RegistryKey");
            Object registryKey = klassRegistryKey.getField("SOUND_EVENT").get(null);
            Object registryAccess = klassRegistryAccess.getMethod("registryAccess").invoke(null);
            Object registry = registryAccess.getClass().getMethod("getRegistry", klassRegistryKey).invoke(registryAccess, registryKey);
            return (Sound) registry.getClass().getMethod("get", NamespacedKey.class).invoke(registry, soundNa);
        } catch (Exception e) {
            return Sound.valueOf(soundName);
        }
    }

    public static boolean shouldPlaySound(String soundKey) {
        return FlagsHPlugin.getInstance().getConfig().getBoolean("sounds." + soundKey + ".enable", true);
    }

    public static float soundVolume(String soundKey) {
        return (float) FlagsHPlugin.getInstance().getConfig().getDouble("sounds." + soundKey + ".volume", 1.0f);
    }

}
