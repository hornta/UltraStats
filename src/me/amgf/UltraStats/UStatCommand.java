package me.amgf.UltraStats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UStatCommand implements CommandExecutor {
    private ManagedData managedData;
    private JavaPlugin plugin;

    UStatCommand(JavaPlugin plugin, ManagedData managedData) {
        this.managedData = managedData;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }

        if(args.length == 0) {
            Integer entityKills = managedData.numberOfKills(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.GOLD + "Total mob kills by " + ((Player) sender).getName() + ": " + ChatColor.WHITE + entityKills);
            return true;
        }

        if(args.length == 1) {
            String firstArgument = args[0].toLowerCase().trim();

            switch (firstArgument) {
                case "top":
                    return handleTop(sender);
                case "all":
                    return handleAll(sender);
                default:
                    EntityType entityType = getEntityType(firstArgument);
                    Player player = Bukkit.getPlayer(firstArgument);

                    if(entityType != null) {
                        Integer entityKills = managedData.numberOfKills(((Player) sender).getUniqueId(), entityType);
                        sender.sendMessage(ChatColor.GOLD + getEntityTypeName(entityType) + " kills by " + ((Player) sender).getName() + ": " + ChatColor.WHITE + entityKills);
                        return true;
                    } else if(player != null) {
                        Integer entityKills = managedData.numberOfKills(player.getUniqueId());
                        sender.sendMessage(ChatColor.GOLD + "Total mob kills by " + player.getName() + ": " + ChatColor.WHITE + entityKills);
                        return true;
                    } else {
                        sender.sendMessage("Invalid entity type.");
                        return true;
                    }
            }
        }

        if(args.length == 2) {
            String firstArgument = args[0].toLowerCase().trim();
            String secondArgument = args[1].toLowerCase().trim();

            EntityType entityType = getEntityType(secondArgument);

            if(entityType == null) {
                sender.sendMessage("Invalid entity type.");
                return true;
            }

            if(firstArgument.equals("top")) {
                return handleTop(sender, entityType);
            } else if(firstArgument.equals("all")) {
                sender.sendMessage(ChatColor.GOLD + "Total " + getEntityTypeName(entityType) + " kills: " + managedData.numberOfKills(entityType));
            } else {
                Player player = Bukkit.getPlayer(firstArgument);
                if(player == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }

                Integer entityKills = managedData.numberOfKills(player.getUniqueId(), entityType);
                sender.sendMessage(ChatColor.GOLD + getEntityTypeName(entityType) + " kills by " + player.getName() + ": " + ChatColor.WHITE + entityKills);
                return true;
            }

            return true;
        }

        displayUsage(sender);
        return true;
    }

    private EntityType getEntityType(String entityName) {
        EntityType type;
        try {
            type = EntityType.valueOf(entityName.toUpperCase());
        } catch(IllegalArgumentException e) {
            return null;
        }

        return type;
    }

    private String getEntityTypeName(EntityType entityType) {
        String[] nameParts = entityType.name().trim().split("_");
        StringBuilder stringBuilder = new StringBuilder();
        for(String part : nameParts) {
            stringBuilder.append(part.substring(0, 1).toUpperCase());
            stringBuilder.append(part.substring(1).toLowerCase());
            stringBuilder.append(" ");
        }

        return stringBuilder.toString().trim();
    }

    private void displayUsage(CommandSender sender) {
        sender.sendMessage("/ustat");
        sender.sendMessage("/ustat <entity>");
        sender.sendMessage("/ustat all");
        sender.sendMessage("/ustat all <entity>");
        sender.sendMessage("/ustat <player>");
        sender.sendMessage("/ustat <player> <entity>");
        sender.sendMessage("/ustat top");
        sender.sendMessage("/ustat top <entity>");
    }

    private Boolean handleAll(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Total mob kills: " + ChatColor.WHITE + managedData.numberOfKills());
        return true;
    }

    private Boolean handleTop(CommandSender sender) {
        return handleTop(sender, null);
    }

    private Boolean handleTop(CommandSender sender, EntityType entityType) {
        sender.sendMessage(ChatColor.RED + "-- UltraStat Leaderboard --");
        if(entityType == null) {
            sender.sendMessage("Total amount of kills.");
        } else {
            sender.sendMessage("Amount of " + getEntityTypeName(entityType) + " kills.");
        }

        ArrayList<LeaderboardItem> leaderboardItems = new ArrayList<>();

        ConcurrentHashMap<UUID, ConcurrentHashMap<EntityType, Integer>> mobKills = managedData.getMobKills();
        for(Map.Entry<UUID, ConcurrentHashMap<EntityType, Integer>> playerKills : mobKills.entrySet()) {
            Player player = Bukkit.getPlayer(playerKills.getKey());
            if(entityType == null) {
                leaderboardItems.add(new LeaderboardItem(player.getName(), managedData.numberOfKills(playerKills.getKey())));
            } else {
                if(playerKills.getValue().containsKey(entityType)) {
                    leaderboardItems.add(new LeaderboardItem(player.getName(), playerKills.getValue().get(entityType)));
                }
            }
        }

        Collections.sort(leaderboardItems);

        int placement = 1;
        for(LeaderboardItem leaderboardItem : leaderboardItems) {
            sender.sendMessage(placement + ". " + ChatColor.GOLD + leaderboardItem.getName() + " " + ChatColor.WHITE + leaderboardItem.getAmount());

            if(placement == 10) {
                break;
            }
            placement++;
        }

        return true;
    }
}
