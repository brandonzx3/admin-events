package net.smokesignals.adminevents.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.ishift.rushboard.util.ScoreboardBuilder;
import net.md_5.bungee.api.ChatColor;
import net.smokesignals.adminevents.AdminEvents;

public class Fishing implements Listener, IEvent {

    AdminEvents adminEvents;
    ArrayList<Player> players = new ArrayList<Player>();
    HashMap<Player, Integer> playerData = new HashMap<>();

    public Fishing() {
        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "A fishing Event has started");
        AdminEvents.INSTANCE.getServer().getPluginManager().registerEvents(this, AdminEvents.INSTANCE);
    }

    @Override
    public void OnPlayerJoin(Player player) {
        players.add(player);
        playerData.put(player, 0);
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has joined the event.");
        }
        AdminEvents.renderCommonScorboard(playerData, players);
    }

    @Override
    public void OnPlayerLeave(Player player) {
        players.remove(player);
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has left the event.");
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        playerData.remove(player);
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
            }
        }

        if(highestScore != 0) {
            for(Player players : players) {
                players.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + highestPlayer.getDisplayName() + " Wins", "", 0, 2 * 20, 1 * 20);
                players.playSound(players.getLocation(), "minecraft:ui.toast.challenge_complete", 100f, 1f);
            }
            Bukkit.broadcastMessage(highestPlayer.getDisplayName() + " won the fishing event with" + " " + highestScore + " points!");
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta)book.getItemMeta();
            bookMeta.addStoredEnchant(Enchantment.MENDING, 1, false);
            book.setItemMeta(bookMeta);
            highestPlayer.getInventory().addItem(book);
        } else {
            Bukkit.broadcastMessage("no one won the fishing event");
        }

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

                if(item.getItemStack().getType() == Material.PUFFERFISH) {
                    score(player, 5, Material.PUFFERFISH);
                }

                if(item.getItemStack().getType() == Material.TROPICAL_FISH) {
                    score(player, 10, Material.TROPICAL_FISH);
                }
            }
        }
    }

    public void score(Player player, Integer number, Material mat) {
        Integer score = playerData.get(player);
        score += number;
        playerData.put(player, score);
        if(mat == Material.TROPICAL_FISH) {
            player.sendMessage("You cought a " + "tropical fish" + " +" + number.toString() + " points!");
        } else {
            player.sendMessage("You cought a " + mat.toString().toLowerCase() + " +" + number.toString() + " points!");
        }
        AdminEvents.renderCommonScorboard(playerData, players);
    }
}