package xyz.baph.PickupWatcher;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class PickupWatcher extends JavaPlugin implements CommandExecutor, Listener {

    static final Logger serverLog = Logger.getLogger("Minecraft");
    static Plugin plugin;

    static LinkedHashMap<Player, LinkedHashMap<String, Integer>> queues;
    static LinkedHashMap<Player, Timer> timers;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        getConfig();

        getCommand("pickup").setExecutor(this);

        getServer().getPluginManager().registerEvents(this, plugin);

        queues = new LinkedHashMap<>();
        timers = new LinkedHashMap<>();

        serverLog.info(getName() + " started successfully.");
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.isCancelled()) return;
        if (!(e instanceof Player)) return;
        final Player player = (Player) e.getEntity();
        final ItemStack stack = e.getItem().getItemStack();

        ItemPickedUp(player, stack);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        if (e.isCancelled()) return;
        final Player player = e.getPlayer();
        final ItemStack stack = e.getArrow().getItemStack();

        ItemPickedUp(player, stack);
    }

    private void ItemPickedUp(Player player, ItemStack stack) {
        if (IsMuted(player)) return;
        AddToQueue(player, stack);

        String itemName = GetItemName(stack);

        // serverLog.info("Player " + player.getName() + " picked up " + itemName + " x" + stack.getAmount());
        // serverLog.info("CurrentQueue: " + PrintQueue(player));

        if (itemName.equalsIgnoreCase(FirstItemInQueue(player))) {
            SendQueueMessage(player);
            ResetTimer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        queues.remove(player);
        timers.remove(player);
    }

    private String PrintQueue(Player player) {
        LinkedHashMap<String, Integer> playerQueue = queues.get(player);
        StringBuilder builder = new StringBuilder();
        for (String itemName : playerQueue.keySet()) {
            builder.append(itemName);
            builder.append(": ");
            builder.append(playerQueue.get(itemName));
            builder.append("; ");
        }

        return builder.toString();
    }

    private String FirstItemInQueue(Player player) {
        return queues.get(player).keySet().stream().findFirst().orElse(null);
    }

    private void AddToQueue(Player player, ItemStack stack) {
        if (!queues.containsKey(player)) {
            queues.put(player, new LinkedHashMap<>());
        }

        LinkedHashMap<String, Integer> playerQueue = queues.get(player);
        String itemName = GetItemName(stack);
        int amount = stack.getAmount();
        if (!playerQueue.containsKey(itemName)) {
            playerQueue.put(itemName, amount);
        } else {
            playerQueue.put(itemName, playerQueue.get(itemName) + amount);
        }

        ResetTimer(player);
    }

    private void ResetTimer(Player player) {
        timers.put(player, new Timer(GetMaxSeconds(), () -> {
            MoveToNextStack(player);
            //serverLog.info("Timer ended for player " + player.getName() + ".");
        }));

        timers.get(player).Start();
    }

    private void MoveToNextStack(Player player) {
        LinkedHashMap<String, Integer> playerQueue = queues.get(player);
        String first = FirstItemInQueue(player);
        if (first == null) {
            return;
        }
        playerQueue.remove(first);
        if (!playerQueue.isEmpty()) {
            SendQueueMessage(player);
            ResetTimer(player);
        }
    }

    private void SendQueueMessage(Player player) {
        HashMap<String, Integer> playerQueue = queues.get(player);
        String first = playerQueue.keySet().stream().findFirst().get();
        SendPickupMessage(player, first, playerQueue.get(first));
    }

    private void SendPickupMessage(Player player, String itemName, int quantity) {
        String message = "";
        if (quantity == 1)
            message = getConfig().getConfigurationSection("messages").getString("message_single");
        else
            message = getConfig().getConfigurationSection("messages").getString("message_multiple")
                    .replace("%amount%", Integer.toString(quantity));

        message = ChatColor.translateAlternateColorCodes('&', message
                .replace("%item%", itemName));

        SendMessage(player, message);
    }

    private String GetItemName(ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        String metaName = GetItemName(meta);
        if (metaName != null) return metaName;
        return Capitalize(stack.getType().name().replace('_', ' '));
    }

    private String GetItemName(ItemMeta meta) {
        if (meta.hasDisplayName())
            return meta.displayName().examinableName();
        if (meta.hasCustomName())
            return meta.customName().examinableName();
        if (meta.hasItemName())
            return meta.itemName().examinableName();
        return null;
    }

    private String Capitalize(String str) {
        str = str.trim();
        final StringBuilder builder = new StringBuilder();
        boolean afterSpaceCapitalize = false;
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c == ' ')
                afterSpaceCapitalize = true;
            if (!Character.isAlphabetic(c)) {
                builder.append(c);
                continue;
            }

            if (i == 0 || afterSpaceCapitalize) {
                builder.append(Character.toUpperCase(c));
            } else {
                builder.append(Character.toLowerCase(c));
            }

            if (afterSpaceCapitalize)
                afterSpaceCapitalize = false;
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
                player.sendMessage(Messaging.deserialize(message));
                break;
            default:
                serverLog.warning("Invalid location supplied. Expected: ACTIONBAR, CHAT. Found: " + location);
                break;
        }
        //serverLog.info(player.getName() + " " + message);
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

    private int GetMaxSeconds() {
        return Objects.requireNonNull(plugin.getConfig().getConfigurationSection("messages")).getInt("queue_delay", 3);
    }
}
