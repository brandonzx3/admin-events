package net.smokesignals.adminevents.events;

import org.bukkit.entity.Player;

public interface IEvent {
    public void OnPlayerJoin(Player player);
    public void OnPlayerLeave(Player player);
    public void ExecuteDie();
}