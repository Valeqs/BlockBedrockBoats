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
import org.bukkit.event.player.PlayerJoinEvent;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Folia: Task auf dem Spieler-Thread planen
        player.getScheduler().runDelayed(this, task -> {
            String brand = player.getClientBrandName();

            if (brand != null && brand.toLowerCase().contains("geyser")) {
                geyserPlayers.add(player.getUniqueId());
                getLogger().info("Detected Geyser player: " + player.getName());

                if (config.getBoolean("block-bedrock", true)) {
                    String rawMessage = config.getString("kick-message", "Bedrock players are not allowed.");

                    // Kick den Spieler
                    player.kick(Component.text(rawMessage).color(TextColor.color(255, 0, 0)));
                }
            }
        }, null, 1L); // 1 Tick Verzögerung
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
                    .color(TextColor.color(255, 0, 0)));
            return true;
        }

        boolean current = config.getBoolean("block-bedrock", true);
        boolean newState = !current;

        config.set("block-bedrock", newState);
        saveConfig();

        String status = newState ? "active" : "inactive";

        sender.sendMessage(Component.text("Bedrock player blocking is now ")
                .append(Component.text(status).color(newState ? TextColor.color(0, 255, 0) : TextColor.color(255, 0, 0)))
                .append(Component.text("!")));

        return true;
    }
}