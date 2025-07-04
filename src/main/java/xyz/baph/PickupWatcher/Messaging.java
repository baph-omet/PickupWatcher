package xyz.baph.PickupWatcher;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Messaging {
    private static String helpCommand = "\"/pickup help\"";
    private static String helpCommandMessage = String.format("See <click:run_command:/pickup help>%s</click> for more info.", helpCommand);
    private static Plugin plugin = PickupWatcher.plugin;

    public static Component HelpCommand(CommandSender sender) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<dark_green>/pickup<green> - Show plugin info.");
        builder.append("<newline><dark_green>/pickup help<green> - Show this message.");
        if (sender.hasPermission("pickupwatcher.mute"))
            builder.append("<newline><dark_green>/pickup mute<green> - Toggle pickup messages.");
        if (sender.hasPermission("pickupwatcher.reload"))
            builder.append("<newline><dark_green>/pickup reload<green> - Reload config.");
        return deserialize(builder.toString());
    }

    public static Component noSuchSubcommand(String argument) {
        return deserialize("<red>No such subcommand " + argument + ". " + helpCommandMessage);
    }

    public static Component noPerms(String node) {
        return deserialize("<red>You don't have permission to do that. Required permission: " + node);
    }

    public static Component playersOnly() {
        return deserialize("<red>Only Players can run this command. " + helpCommandMessage);
    }

    public static Component pluginInfo() {
        PluginMeta meta = plugin.getPluginMeta();
        return deserialize("<blue>=====<yellow>" + plugin.getName()
                + "<blue>=====<newline>Created by <dark_red>" + meta.getAuthors().get(0) + "<blue> ( <reset>"
                + meta.getWebsite() + "<blue> )<newline>GitHub:<reset> https://github.com/baph-omet/" + plugin.getName()
                + "<newline><blue>Version: <reset>" + meta.getVersion() + "<blue> See <red>" + helpCommand + "<blue> for commands.");
    }

    public static Component deserialize(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
