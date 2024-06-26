package fr.formiko.flagsh;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.command.CommandSender;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;

@CommandAlias("flagsh|fh")
public class FlagsHCommand extends BaseCommand {
    private static final String ADMIN_PERMISSION = "flagsh.admin";
    @Default
    @Description("Lists the version of the plugin")
    public static void onFh(CommandSender commandSender) { commandSender.sendMessage(Component.text(FlagsH.getPlugin().toString())); }

    @Subcommand("reload")
    @Description("Reloads the plugin config and data file")
    @CommandPermission(ADMIN_PERMISSION)
    public static void onReload(CommandSender commandSender) {
        FlagsH.getPlugin().reloadConfig();
        FlagsH.getPlugin().reloadFlagDataFile();
        commandSender.sendMessage(Component.text("FlagsH reloaded"));
    }

    @Subcommand("list")
    @Description("Lists all flags")
    @CommandPermission(ADMIN_PERMISSION)
    public static void onList(CommandSender commandSender) {
        StringBuilder sb = new StringBuilder();
        sb.append("Flags list: ");
        int k = 1;
        for (Flag flag : FlagsH.getPlugin().getFlags()) {
            sb.append(k++);
            sb.append(": ");
            sb.append(flag.toString());
            sb.append("\n");
        }
        commandSender.sendMessage(Component.text(sb.toString()));
    }

    @Subcommand("remove")
    @CommandCompletion("@flagshId")
    @Description("Remove a flag or all flags")
    @CommandPermission(ADMIN_PERMISSION)
    public static void onRemove(CommandSender commandSender, @Nonnull String arg) {
        if (arg.equalsIgnoreCase("all")) {
            List<Flag> l = new ArrayList<>(FlagsH.getPlugin().getFlags());
            for (Flag flag : l) {
                flag.remove();
            }
            commandSender.sendMessage(Component.text("All flags removed"));
        } else {
            try {
                int id = Integer.parseInt(arg);
                Flag flag = FlagsH.getPlugin().getFlags().get(id - 1);
                if (flag != null) {
                    flag.remove();
                    commandSender
                            .sendMessage(Component.text("Flag removed. Check the new list with '/flagsh list' to see the new indexes."));
                } else {
                    commandSender.sendMessage(Component.text("Flag not found"));
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                commandSender.sendMessage(Component.text("Invalid flag id"));
            }
        }
    }
}
