package net.smokesignals.adminevents;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.smokesignals.adminevents.commands.Event;
import net.smokesignals.adminevents.events.IEvent;

public class AdminEvents extends JavaPlugin implements Listener {

    public static boolean eventIsRunning = false;
    public ArrayList<Player> playersInEvent = new ArrayList<Player>();
    public static AdminEvents INSTANCE = null;
    public IEvent current_event = null;

    @Override
    public void onEnable() {
        INSTANCE = this;
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("event").setExecutor(new Event(this));
        getLogger().info("Admin Events Plugin Started");
    }

    @EventHandler
    public void player_leavith(PlayerQuitEvent event) {
        try {
            if (playersInEvent.contains(event.getPlayer())) {
                current_event.OnPlayerLeave(event.getPlayer());
            }
        } catch (Exception e) {
            System.out.println("Biggen badden");
            System.out.println(e.toString());
        }
    }
}