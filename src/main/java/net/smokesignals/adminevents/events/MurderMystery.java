package net.smokesignals.adminevents.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.smokesignals.adminevents.AdminEvents;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

enum Status {
    WAITING,
    STARTING,
    INPROGRESS,
    ENDING,
    DIE
}

public class MurderMystery implements Listener, IEvent {
    static final String team_name = "_murder_mystery";
    public MurderMystery() {
        AdminEvents.INSTANCE.getServer().broadcastMessage("A " + ChatColor.RED + ChatColor.BOLD + "MURDER MYSTERY " + ChatColor.RESET + "event has started");
        //mm_world = AdminEvents.INSTANCE.getServer().getWorld("murdermystery");
        mm_world = AdminEvents.INSTANCE.getServer().getWorld("world");
        AdminEvents.INSTANCE.getServer().getPluginManager().registerEvents(this, AdminEvents.INSTANCE);
        scoreboard = AdminEvents.INSTANCE.getServer().getScoreboardManager().getMainScoreboard();
        team = scoreboard.getTeam(team_name);
        if (team == null) team = scoreboard.registerNewTeam(team_name);
        team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OWN_TEAM);
    }

    World mm_world;
    Team team;
    Scoreboard scoreboard;
    HashMap<Player, SpaceTimeFreeze> prev_player_dat = new HashMap<>();
    Status status = Status.WAITING;
    MurderMysteryCountdown countdown = null;
    ArrayList<Player> players = new ArrayList<>();
    Player murderer = null;
    ArrayList<Player> survivors = new ArrayList<>();
    int min_players = 3;
    int max_players = Integer.MAX_VALUE; //MUHAHAHAHAHAHAHA

    public void OnPlayerJoin(Player player) {
        if (status == Status.INPROGRESS || status == Status.DIE) throw new RuntimeException("DIE");
        if (players.size() >= max_players) throw new RuntimeException("DIE");
        players.add(player);
        prev_player_dat.put(player, new SpaceTimeFreeze(player));
        //clear invin, xp, set adventure mode
        player.getInventory().clear();
        player.setTotalExperience(0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(mm_world.getSpawnLocation());
        team.addPlayer(player);
        //Give sword, bow (infinity), arrow
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);
        ItemMeta sword_meta = sword.getItemMeta();
        sword_meta.setUnbreakable(true);
        sword.setItemMeta(sword_meta);
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 5);
        bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 5);
        ItemMeta bow_meta = bow.getItemMeta();
        bow_meta.setUnbreakable(true);
        bow.setItemMeta(bow_meta);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW));
        for(Player players : players) {
            players.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has joined the event.");
        }
        if (status == Status.WAITING && players.size() >= min_players) {
            status = Status.STARTING;
            countdown = new MurderMysteryCountdown(this);
        }
        if (status == Status.STARTING && players.size() >= max_players && countdown.countdown > 11) countdown.countdown = 10;
    }

    @EventHandler
    public void PlayerGetStabbo(EntityDamageEvent event) {
        if (players.contains(event.getEntity())) {
            Player player = (Player)event.getEntity();
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
                if ((!players.contains(event2.getDamager()) && (event2.getDamager() instanceof Arrow ? !players.contains(((Arrow)event2.getDamager()).getShooter()) : true)) || status != Status.INPROGRESS) event.setCancelled(true);
                else if (player.getHealth() - event.getFinalDamage() <= 0) {
                    event.setCancelled(true);
                    player_die(player, (Player)event2.getDamager());
                }
            } else if (event.getCause() != DamageCause.VOID) event.setCancelled(true);
        }
    }

    void start() {
        status = Status.INPROGRESS;
        murderer = players.get((int)Math.floor(Math.random() * players.size()));
        for (Player player : players) {
            player.sendMessage("Murder Mystery is starting!");
            if (player == murderer) player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "YOU ARE THE MURDERER");
            else {
                player.sendMessage(ChatColor.GREEN + "You are a survivor");
                survivors.add(player);
            };
            player.teleport(mm_world.getSpawnLocation());
        }
    }
    void player_die(Player player, Player killed_by) {
        if (status != Status.INPROGRESS) return;
        for (Player player2 : players) player2.sendMessage(ChatColor.GOLD + "Someone has died...");
        if (players.contains(player)) {
            player.setGameMode(GameMode.SPECTATOR);
            if (player == murderer) {
            //Survivors win
            for (Player player2 : players) {
                player2.sendTitle(ChatColor.GREEN + "" + ChatColor.ITALIC + "SURVIVORS WIN", "", 0, 2 * 20, 1 * 20);
                player2.playSound(player2.getLocation(), "minecraft:ui.toast.challenge_complete", 100f, 1f);
            }
            game_end();
        } else if (survivors.contains(player)) {
                survivors.remove(player);
                if (survivors.contains(killed_by)) {
                    player_die(killed_by, null);
                }
                if (survivors.size() < 1) {
                    //Murderer wins
                    for (Player player2 : players) {
                        player2.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "MURDERER WINS", "", 0, 2 * 20, 1 * 20);
                        player2.playSound(player2.getLocation(), "minecraft:event.raid.horn", 100f, 1f);
                    }
                    game_end();
                }
            }
        };
    }
    void game_end() {
        status = Status.ENDING;
        AdminEvents.INSTANCE.getServer().broadcastMessage("Murder Mystery has ended.");
        HandlerList.unregisterAll(this);
        for (Player player : players) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        MurderMystery self = this;
        AdminEvents.INSTANCE.getServer().getScheduler().runTaskLater(AdminEvents.INSTANCE, new Runnable() {
            @Override
            public void run() {
                status = Status.DIE;
                while (players.size() > 0) OnPlayerLeave(players.remove(0));
                if (AdminEvents.INSTANCE.current_event == self) {
                    AdminEvents.INSTANCE.current_event = null;
                    AdminEvents.INSTANCE.playersInEvent.clear();
                    AdminEvents.eventIsRunning = false;
                }
            }
        }, 5 * 20);
    }

    @Override
    public void OnPlayerLeave(Player player) {
        players.remove(player);
        if (status != Status.DIE && status != Status.ENDING) {
            String msg = player.getName() + " has quit Murder Mystery";
            for (Player players : players) players.sendMessage(msg);
        }
        player_die(player, null);
        team.removePlayer(player);
        prev_player_dat.get(player).restore(player);
    }

    @EventHandler
    public void OnSomethingDie(PlayerDeathEvent event) {
        if (players.contains(event.getEntity())) {
            event.setDroppedExp(0);
        }
    }

    @Override
    public void ExecuteDie() {
        // TODO Auto-generated method stub
        game_end();
    }
}

class MurderMysteryCountdown implements Runnable {
    MurderMystery mm;
    int countdown = 31;
    boolean cancelled = false;
    public MurderMysteryCountdown(MurderMystery mm) {
        this.mm = mm;
        this.run();
    }

    @Override
    public void run() {
        countdown--;
        if (cancelled) return;
        if (countdown > 0) {
            String msg = ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in " + String.valueOf(countdown) + " seconds...";
            for (Player player : mm.players) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
            }
            AdminEvents.INSTANCE.getServer().getScheduler().runTaskLater(AdminEvents.INSTANCE, this, 20);
        } else {
            mm.start();
        }
    }
    
}

class SpaceTimeFreeze {
    ArrayList<ItemStack> inventory = new ArrayList<>();
    int exp;
    GameMode gamemode;
    Location position;
    double health;
    int hunger;
    public SpaceTimeFreeze(Player player) {
        for (int i = 0; i <= player.getInventory().getSize(); i++) {
            ItemStack tmp = player.getInventory().getItem(i);
            if (tmp == null) inventory.add(null);
            else inventory.add(tmp.clone());
        };
        exp = player.getTotalExperience();
        gamemode = player.getGameMode();
        position = player.getLocation();
        health = player.getHealth();
        hunger = player.getFoodLevel();
    }

    public void restore(Player player) {
        for (int i = 0; i < inventory.size(); i++) {
            player.getInventory().setItem(i, inventory.get(i));
        };
        player.setTotalExperience(exp);
        player.setGameMode(gamemode);
        player.teleport(position);
        player.setHealth(health);
        player.setFoodLevel(hunger);
    }
}