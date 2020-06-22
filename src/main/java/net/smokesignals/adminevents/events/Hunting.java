package net.smokesignals.adminevents.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import me.ishift.rushboard.util.ScoreboardBuilder;
import net.md_5.bungee.api.ChatColor;
import net.smokesignals.adminevents.AdminEvents;

public class Hunting implements Listener, IEvent {
    ArrayList<Player> players = new ArrayList<>();
    HashMap<Player, Integer> playerData = new HashMap<>();

    public Hunting() {
        AdminEvents.INSTANCE.getServer().getPluginManager().registerEvents(this, AdminEvents.INSTANCE);
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "A hunting Event has started!");
    }
    
    @Override
    public void OnPlayerJoin(Player player) {
        players.add(player);
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has joined the event");
        }
        playerData.put(player, 0);
        AdminEvents.renderCommonScorboard(playerData, players);
    }

    @Override
    public void OnPlayerLeave(Player player) {
        players.remove(player);
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has left the event.");
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        AdminEvents.renderCommonScorboard(playerData, players);
    }

    @Override
    public void ExecuteDie() {
        Integer highestScore = 0;
        Player highestPlayer = null;
        for(Player player : playerData.keySet()) {
            Integer score = playerData.get(player);
            if(score > highestScore) {
                highestScore = score;
                highestPlayer = player;
                if(highestPlayer != null) {
                    for(Player players : players) {
                        players.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + highestPlayer.getDisplayName() + " Wins", "", 0, 2 * 20, 1 * 20);
                        players.playSound(players.getLocation(), "minecraft:ui.toast.challenge_complete", 100f, 1f);
                    }
                    Bukkit.broadcastMessage(highestPlayer.getDisplayName() + " won the hunting event with" + " " + highestScore + " points!");
                    highestPlayer.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));
                } else {
                    Bukkit.broadcastMessage("no one won the hunting event");
                }
            }
        }
        HandlerList.unregisterAll(this);
        Hunting self = this;
        AdminEvents.INSTANCE.getServer().getScheduler().runTaskLater(AdminEvents.INSTANCE, new Runnable() {
            @Override
            public void run() {
                while (players.size() > 0) OnPlayerLeave(players.remove(0));
                if (AdminEvents.INSTANCE.current_event == self) {
                    AdminEvents.INSTANCE.current_event = null;
                    AdminEvents.INSTANCE.playersInEvent.clear();
                    AdminEvents.eventIsRunning = false;
                }
            }
        }, 5 * 20);
    }
    
    @EventHandler
    public void OnEntityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        Player player = e.getEntity().getKiller();
        if(players.contains(player)) {
            if(entity.getType() == EntityType.COW) {
                Score(player, 5);
            }
    
            if(entity.getType() == EntityType.PIG) {
                Score(player, 4);
            }
    
            if(entity.getType() == EntityType.SHEEP) {
                Score(player, 2);
            }
    
            if(entity.getType() == EntityType.CHICKEN) {
                Score(player, 4);
            }
        }
    }

    public void Score(Player player, Integer ammount) {
        Integer score = playerData.get(player);
        score += ammount;
        playerData.put(player, score);
        player.sendMessage("You earned " + ammount.toString() + " points!");
        AdminEvents.renderCommonScorboard(playerData, players);
    }
}