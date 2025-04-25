package sotumney;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
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

import java.nio.charset.StandardCharsets;
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
        registerBrandListener();

        getLogger().info("BlockBedrockBoats successfully initialized. Bedrock be gone!");
    }

    private void registerBrandListener() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.CUSTOM_PAYLOAD)) return;

                String channel = event.getPacket().getStrings().read(0);
                getLogger().info("Received channel: " + channel);

                // Check for the Geyser brand packet
                if (channel.equalsIgnoreCase("minecraft:brand") || channel.equalsIgnoreCase("MC|Brand")) {
                    byte[] brandBytes = event.getPacket().getByteArrays().read(0);
                    String brand = new String(brandBytes, StandardCharsets.UTF_8);

                    getLogger().info("Brand Message: " + brand);

                    if (brand.toLowerCase().contains("geyser")) {
                        geyserPlayers.add(event.getPlayer().getUniqueId());
                        getLogger().info("Detected Geyser player: " + event.getPlayer().getName());
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!config.getBoolean("block-bedrock", true)) return;

        if (geyserPlayers.contains(player.getUniqueId())) {
            String rawMessage = config.getString("kick-message", "&cBedrock players are not allowed.");
            String coloredMessage = rawMessage.replace('&', '§'); // Adventure doesn't use the '&' color codes directly
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, coloredMessage);
        }
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
                    .color(TextColor.color(255, 0, 0)));  // Red color for epic fail
            return true;
        }

        boolean current = config.getBoolean("block-bedrock", true);
        boolean newState = !current;

        config.set("block-bedrock", newState);
        saveConfig();

        Component statusMessage = Component.text("Bedrock player blocking is now ")
                .color(newState ? TextColor.color(0, 255, 0) : TextColor.color(255, 0, 0))
                .append(Component.text(newState ? "active" : "inactive")
                        .color(TextColor.color(255, 255, 255)));
        sender.sendMessage(statusMessage);

        return true;
    }
}