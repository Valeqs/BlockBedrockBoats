package sotumney;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class BlockBedrockBoats extends JavaPlugin implements Listener {

    private FloodgateApi floodgateApi;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        floodgateApi = FloodgateApi.getInstance();

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("BlockBedrockBoats succesfully ininitialized. Sotumney did a thing!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("block-bedrock", true)) return;

        UUID uuid = event.getPlayer().getUniqueId();
        if (floodgateApi.isFloodgatePlayer(uuid)) {
            String rawMessage = config.getString("kick-message");
            String coloredMessage = ChatColor.translateAlternateColorCodes('&', rawMessage);
            event.getPlayer().kick(coloredMessage);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("togglebedrockblock")) return false;

        if (!sender.hasPermission("blockbedrockboats.allowtoggle")) {
            sender.sendMessage(ChatColor.RED + "Failed: You do not have the BlockBedrockBoats permission to use this command.");
            return true;
        }

        boolean current = config.getBoolean("block-bedrock", true);
        boolean newState = !current;

        config.set("block-bedrock", newState);
        saveConfig();

        String status = newState ? ChatColor.GREEN + "active" : ChatColor.RED + "inactive";
        sender.sendMessage(ChatColor.YELLOW + "Bedrock player blocking is now " + status + ChatColor.YELLOW + "!");

        return true;
    }
}