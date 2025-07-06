package xyz.baph.PickupWatcher;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class Messaging {
    private static final String helpCommand = "\"/pickup help\"";
    private static final String helpCommandMessage = String.format("See <click:run_command:"
            + helpCommand.replace("\"", "'").replace("/", "")
            + ">%s</click> for more info.", helpCommand);
    private static final Plugin plugin = PickupWatcher.plugin;

    public static Component HelpCommand(CommandSender sender) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<dark_green><click:suggest_command:'/pickup'>/pickup</click><green> - Show plugin info.");
        builder.append("<newline><dark_green><click:suggest_command:'/pickup help'>/pickup help</click><green> - Show this message.");
        if (sender.hasPermission("pickupwatcher.mute"))
            builder.append("<newline><dark_green><click:suggest_command:'/pickup mute'>/pickup mute</click><green> - Toggle pickup messages.");
        if (sender.hasPermission("pickupwatcher.reload"))
            builder.append("<newline><dark_green><click:suggest_command:'/pickup reload'>/pickup reload</click><green> - Reload config.");
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
        String githubLink = "https://github.com/baph-omet/" + plugin.getName();
        return deserialize("<blue>=====<yellow>" + plugin.getName()
                + "<blue>=====<newline>Created by <dark_red>" + meta.getAuthors().get(0) + "<blue> ( <reset><click:open_url:'https://baph.xyz'>"
                + meta.getWebsite() + "</click><blue> )<newline>GitHub:<reset> <click:open_url:'" + githubLink + "'><bold>Click Here</bold></click>"
                + "<newline><blue>Version: <reset>" + meta.getVersion() + "<blue> " + helpCommandMessage);
    }

    public static Component deserialize(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
