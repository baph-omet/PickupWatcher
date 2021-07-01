package net.iamvishnu.PickupWatcher;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class PickupWatcher extends JavaPlugin implements CommandExecutor, Listener {

	static final Logger serverLog = Logger.getLogger("Minecraft");
	static Plugin plugin;

	@Override
	public void onEnable() {
		plugin = this;
		saveDefaultConfig();
		getConfig();

		/*
		 * boolean isPapermc = false; try { isPapermc =
		 * Class.forName("com.destroystokyo.paper.VersionHistoryManager.VersionData") !=
		 * null; } catch (final ClassNotFoundException e) {
		 * Bukkit.getLogger().info("Not paper"); }
		 * 
		 * if (!isPapermc) { serverLog.severe(
		 * "Paper not detected! This plugin requires Paper in order to work. The plugin will now disable."
		 * ); getServer().getPluginManager().disablePlugin(this); }
		 */

		getCommand("pickup").setExecutor(this);

		getServer().getPluginManager().registerEvents(this, plugin);

		serverLog.info(getName() + " started successfully.");
	}

	@Override
	public void onDisable() {
		plugin = null;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPickup(EntityPickupItemEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		final Player player = (Player) e.getEntity();
		if (IsMuted(player))
			return;

		final ItemStack stack = e.getItem().getItemStack();
		String message = "";
		if (stack.getAmount() == 1)
			message = getConfig().getConfigurationSection("messages").getString("message_single");
		else
			message = getConfig().getConfigurationSection("messages").getString("message_multiple").replace("%amount%",
					Integer.toString(stack.getAmount()));

		message = ChatColor.translateAlternateColorCodes('&', message.replace("%item%", GetItemName(stack)));

		SendMessage(player, message);
	}

	@SuppressWarnings("deprecation")
	private String GetItemName(ItemStack stack) {
		final ItemMeta meta = stack.getItemMeta();
		if (meta.hasDisplayName())
			return meta.getDisplayName();
		if (meta.hasLocalizedName())
			return meta.getLocalizedName();
		return Capitalize(stack.getType().name().replace("_", " "));
	}

	private String Capitalize(String str) {
		final StringBuilder builder = new StringBuilder();
		boolean firstLetterCapitalized = false;
		boolean afterSpaceCapitalize = false;
		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (c == ' ' && firstLetterCapitalized)
				afterSpaceCapitalize = true;
			if (!Character.isAlphabetic(c))
				continue;

			if (!firstLetterCapitalized)
				firstLetterCapitalized = true;
			if (afterSpaceCapitalize)
				afterSpaceCapitalize = false;

			builder.append(Character.toUpperCase(c));
		}

		return builder.toString();
	}

	private void SendMessage(Player player, String message) {
		final String location = getConfig().getConfigurationSection("messages").getString("message_location")
				.toUpperCase();
		switch (location) {
		case "ACTIONBAR":
			player.sendActionBar(Component.text().content(message));
			break;
		case "CHAT":
			player.sendMessage(message);
			break;
		default:
			serverLog.warning("Invalid location supplied. Expected: ACTIONBAR, CHAT. Found: " + location);
			break;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Messaging.pluginInfo());
			return true;
		}

		boolean handled = true;
		switch (args[0]) {
		case "help":
			sender.sendMessage(Messaging.HelpCommand(sender));
			break;
		case "mute":
			CommandMute(sender);
			break;
		case "reload":
			CommandReload(sender);
			break;
		default:
			handled = false;
			break;
		}

		return handled;
	}

	private void CommandMute(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Messaging.playersOnly());
			return;
		}

		final String perm = "pickupwatcher.mute";
		if (!sender.hasPermission(perm)) {
			sender.sendMessage(Messaging.noPerms(perm));
			return;
		}

		ToggleMute((Player) sender);
	}

	private void CommandReload(CommandSender sender) {
		final String perm = "pickupwatcher.reload";
		if (!sender.hasPermission(perm))
			sender.sendMessage(Messaging.noPerms(perm));

		reloadConfig();
		sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
	}

	private void ToggleMute(Player player) {
		final String path = "muted_players";
		final List<String> players = getConfig().getStringList(path);
		final String uuid = player.getUniqueId().toString();

		if (IsMuted(player))
			players.remove(uuid);
		else
			players.add(uuid);

		getConfig().set(path, players);

		saveConfig();
	}

	private boolean IsMuted(Player player) {
		return getConfig().getStringList("muted_players").contains(player.getUniqueId().toString());
	}
}
