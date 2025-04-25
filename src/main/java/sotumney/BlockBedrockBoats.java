package sotumney;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlockBedrockBoats extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private final Set<UUID> geyserPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("BlockBedrockBoats successfully initialized. Bedrock be gone!");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!config.getBoolean("block-bedrock", true)) return;

        // Use a scheduler to check the client's brand name after the player has logged in
        Bukkit.getScheduler().runTask(this, () -> {
            String brandName = player.getClientBrandName(); // Get the client's brand name

            if (brandName != null && brandName.toLowerCase().contains("geyser")) {
                geyserPlayers.add(player.getUniqueId());
                getLogger().info("Detected Geyser player: " + player.getName());

                // Kick the player with a message
                String rawMessage = config.getString("kick-message", "Bedrock players are not allowed.");
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text(rawMessage)
                        .color(TextColor.color(255, 0, 0))); // Red color
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        geyserPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("togglebedrockblock")) return false;

        if (!sender.hasPermission("blockbedrockboats.allowtoggle")) {
            sender.sendMessage(Component.text("Failed: You do not have permission to use this command.")
                    .color(TextColor.color(255, 0, 0))); // Red color for error messages
            return true;
        }

        boolean current = config.getBoolean("block-bedrock", true);
        boolean newState = !current;

        config.set("block-bedrock", newState);
        saveConfig();

        String status = newState ? "active" : "inactive";

        sender.sendMessage(Component.text("Bedrock player blocking is now ")
                .append(Component.text(status).color(newState ? TextColor.color(0, 255, 0) : TextColor.color(255, 0, 0)))
                .append(Component.text("!"))); // Green for active, red for inactive

        return true;
    }
}