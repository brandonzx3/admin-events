package net.smokesignals.adminevents.events;

import org.bukkit.entity.Player;

import net.smokesignals.adminevents.AdminEvents;

public class Test implements Runnable, IEvent {
    AdminEvents adminEvents;

    public Test(AdminEvents parent) {
        this.adminEvents = parent;
        this.run();
    }

    public void OnPlayerJoin(Player player) {
        player.setInvulnerable(true);
    }

    public void run() {
        if(AdminEvents.eventIsRunning) {
            adminEvents.getServer().getScheduler().runTaskLater(adminEvents, this, 1);
        } else {
            for(Player players : adminEvents.playersInEvent) {
                players.setInvulnerable(false);
            }
            adminEvents.playersInEvent.clear();
        }
    }

    @Override
    public void ExecuteDie() {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnPlayerLeave(Player player) {
        // TODO Auto-generated method stub

    }
}