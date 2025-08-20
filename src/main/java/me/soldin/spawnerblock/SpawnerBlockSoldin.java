package me.soldin.spawnerblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SpawnerBlockSoldin extends JavaPlugin implements Listener {

    private List<String> blockedMobs;
    private String denyMessage;
    private boolean logActions;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("spawnerblocksoldin").setExecutor((sender, command, label, args) -> {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("spawnerblocksoldin.reload")) {
                    sender.sendMessage("§cУ вас нет прав!");
                    return true;
                }
                reloadConfig();
                loadConfigValues();
                sender.sendMessage("§aКонфиг перезагружен!");
                return true;
            }
            sender.sendMessage("§eИспользование: /spawnerblocksoldin reload");
            return true;
        });

        getLogger().info("SpawnerBlockSoldin запущен на версии " + Bukkit.getBukkitVersion());
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        blockedMobs = config.getStringList("blocked-mobs");
        denyMessage = config.getString("deny-message", "§cНельзя сувать моба в спавнер!");
        logActions = config.getBoolean("log-actions", true);
    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        if (block.getType() != Material.SPAWNER) return;

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;

        if (event.getItem().getType().name().endsWith("_SPAWN_EGG")) {
            String typeName = event.getItem().getType().name().replace("_SPAWN_EGG", "");
            try {
                EntityType type = EntityType.valueOf(typeName);
                if (blockedMobs.contains(type.name())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(denyMessage);
                    if (logActions) {
                        getLogger().info(event.getPlayer().getName() + " пытался вставить " + type.name() + " в спавнер.");
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
