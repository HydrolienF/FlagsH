package fr.formiko.flagsh;

import java.util.List;
import org.bukkit.GameMode;

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

}
