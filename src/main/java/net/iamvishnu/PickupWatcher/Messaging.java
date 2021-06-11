package net.iamvishnu.PickupWatcher;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Messaging {
	private static String helpCommand = "\"/pickup help\"";
	private static String helpCommandMessage = String.format("See %s for more info.", helpCommand);
	private static Plugin plugin = PickupWatcher.plugin;

	public static String HelpCommand(CommandSender sender) {
		final StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.DARK_GREEN + "/pickup" + ChatColor.GREEN + " - Show plugin info.");
		builder.append(ChatColor.DARK_GREEN + "/pickup help" + ChatColor.GREEN + " - Show this message.");
		if (sender.hasPermission("pickupwatcher.mute"))
			builder.append(ChatColor.DARK_GREEN + "/pickup mute" + ChatColor.GREEN + " - Toggle pickup messages.");
		if (sender.hasPermission("pickupwatcher.reload"))
			builder.append(ChatColor.DARK_GREEN + "/pickup reload" + ChatColor.GREEN + " - Reload config.");
		return builder.toString();
	}

	public static String noSuchSubcommand(String argument) {
		return ChatColor.RED + "No such subcommand " + argument + ". " + helpCommandMessage;
	}

	public static String noPerms(String node) {
		return ChatColor.RED + "You don't have permission to do that. Required permission: " + node;
	}

	public static String playersOnly() {
		return ChatColor.RED + "Only Players can run this command. " + helpCommandMessage;
	}

	public static String pluginInfo() {
		return ChatColor.BLUE + "=====" + ChatColor.YELLOW + plugin.getName() + ChatColor.BLUE
				+ "=====\nCreated by IAMVISHNU Media ( " + PickupWatcher.plugin.getDescription().getWebsite() + " )\n"
				+ "GitHub: https://github.com/iamvishnu-media/" + plugin.getName() + "\n" + "Version: "
				+ plugin.getDescription().getVersion() + "See " + helpCommand + " for commands.";
	}
}
