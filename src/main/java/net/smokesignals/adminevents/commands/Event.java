package net.smokesignals.adminevents.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.smokesignals.adminevents.AdminEvents;
import net.smokesignals.adminevents.events.Fishing;
import net.smokesignals.adminevents.events.Hunting;
import net.smokesignals.adminevents.events.MurderMystery;
import net.smokesignals.adminevents.events.Test;

public class Event implements CommandExecutor {
    AdminEvents adminEvents;

    public Event(AdminEvents actual_thing) {
        adminEvents = actual_thing;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length != 0) {
                switch (args[0]) {
                    default:
                        sender.sendMessage("You did a bad.");
                        break;

                    case "help":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "---Event help---");
                        sender.sendMessage(ChatColor.GOLD + "/event join");
                        sender.sendMessage(ChatColor.GOLD + "/event leave");
                        break;

                    case "join":
                        if (AdminEvents.eventIsRunning) {
                            adminEvents.playersInEvent.add((Player) sender);
                            adminEvents.current_event.OnPlayerJoin((Player)sender);
                        } else {
                            sender.sendMessage("No event is active.");
                        }
                        break;

                    case "start":
                        if (sender.isOp()) {
                            if (args.length == 2) {
                                if (!AdminEvents.eventIsRunning) {
                                    switch (args[1]) {
                                        default:
                                            sender.sendMessage(ChatColor.RED + "This is not an event, please DIE.");
                                            return true;
                                        case "test":
                                            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                                                    + "An admin has started the test event.");
                                            AdminEvents.eventIsRunning = true;
                                            AdminEvents.INSTANCE.current_event = new Test(AdminEvents.INSTANCE);
                                            break;
                                        case "murder":
                                            AdminEvents.eventIsRunning = true;
                                            AdminEvents.INSTANCE.current_event = new MurderMystery();
                                            break;
                                        case "fishing":
                                            AdminEvents.eventIsRunning = true;
                                            AdminEvents.INSTANCE.current_event = new Fishing();
                                            break;
                                        case "hunting":
                                            AdminEvents.eventIsRunning = true;
                                            AdminEvents.INSTANCE.current_event = new Hunting();
                                            break;
                                    }
                                } else {
                                    sender.sendMessage("An event is already running!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Type an event you idiot.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You can't use this command!");
                        }
                        break;

                    case "stop":
                        if (sender.isOp()) {
                            if (AdminEvents.eventIsRunning) {
                                AdminEvents.eventIsRunning = false;
                                adminEvents.current_event.ExecuteDie();
                                adminEvents.current_event = null;
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You can't use this command!");
                        }
                        break;
                    
                    case "leave": {
                        if(AdminEvents.eventIsRunning) {
                            if(adminEvents.playersInEvent.contains(sender)) {
                                adminEvents.current_event.OnPlayerLeave((Player)sender);
                                adminEvents.playersInEvent.remove(sender);
                            } else {
                                sender.sendMessage(ChatColor.RED + "You are not in an event!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You are not in an event!");
                        }
                        break;
                    }
                }
            } else {
                sender.sendMessage("You did a bad.");
            }
        }
        return false;
    }

}