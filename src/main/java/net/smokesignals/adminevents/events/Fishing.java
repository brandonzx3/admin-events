package net.smokesignals.adminevents.events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.md_5.bungee.api.ChatColor;
import net.smokesignals.adminevents.AdminEvents;

public class Fishing implements Listener, Runnable, IEvent {

    AdminEvents adminEvents;
    ArrayList<Player> players = new ArrayList<Player>();

    public Fishing() {
        AdminEvents.INSTANCE.getServer().getPluginManager().registerEvents(this, AdminEvents.INSTANCE);
    }

    @Override
    public void OnPlayerJoin(Player player) {
        players.add(player);
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
        //event die

    }

    @Override
    public void run() {

    }
}