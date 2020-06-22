package net.smokesignals.adminevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import me.ishift.rushboard.util.ScoreboardBuilder;
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
    public static void renderCommonScorboard(HashMap<Player, Integer> playerData, ArrayList<Player> players) {
        ScoreboardBuilder builder = new ScoreboardBuilder(null);
        builder.setTitle(ChatColor.BOLD + "Score");
        ArrayList<Entry<Player, Integer>> ordered = new ArrayList<>();
        for (Entry<Player, Integer> entry : playerData.entrySet()) {
            for (int i = 0; i <= ordered.size(); i++) {
                if (i == ordered.size() || entry.getValue() >= ordered.get(i).getValue()) {
                    ordered.add(i, entry);
                    break;
                }
            }
        }
        for (Entry<Player, Integer> entry : ordered) {
            builder.addEntry(ChatColor.RED + entry.getKey().getDisplayName() + ChatColor.WHITE + ": " + ChatColor.GOLD + entry.getValue().toString());
        }
        Scoreboard scoreboard = builder.build();
        for (Player player : players) {
            player.setScoreboard(scoreboard);
        }
    }
}