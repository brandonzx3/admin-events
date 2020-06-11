package net.smokesignals.adminevents.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.smokesignals.adminevents.AdminEvents;

public class Fishing implements Listener, IEvent {

    AdminEvents adminEvents;
    ArrayList<Player> players = new ArrayList<Player>();
    HashMap<Player, Integer> playerData = new HashMap<>();

    public Fishing() {
        Bukkit.broadcastMessage("a Fishing Event has started");
        AdminEvents.INSTANCE.getServer().getPluginManager().registerEvents(this, AdminEvents.INSTANCE);
    }

    @Override
    public void OnPlayerJoin(Player player) {
        players.add(player);
        playerData.put(player, 0);
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has joined the event.");
        }

    }

    @Override
    public void OnPlayerLeave(Player player) {
        players.remove(player);
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has left the event.");
        }
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
            }
        }
        Bukkit.broadcastMessage(highestPlayer.getDisplayName() + " won the fishing event with" + " " + highestScore + " points!");
        HandlerList.unregisterAll(this);
        Fishing self = this;
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
    public void PlayerGetItem(PlayerFishEvent e) {
        Item item = (Item)e.getCaught();
        Player player = e.getPlayer();
        if (players.contains(player)) {
            if(e.getState() == State.CAUGHT_FISH) {
                if(item.getItemStack().getType() == Material.COD) {
                    score(player, 1, Material.COD);
                }
        
                if(item.getItemStack().getType() == Material.SALMON) {
                    score(player, 2, Material.SALMON);
                }
            }
        }
    }

    public void score(Player player, Integer number, Material mat) {
        Integer score = playerData.get(player);
        score += number;
        playerData.put(player, score);
        player.sendMessage("You cought a " + mat.toString() + " +" + number.toString() + " points!");
    }
}