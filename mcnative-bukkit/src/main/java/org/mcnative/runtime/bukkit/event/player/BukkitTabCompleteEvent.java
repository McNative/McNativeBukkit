package org.mcnative.runtime.bukkit.event.player;

import org.bukkit.event.server.TabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

import java.util.List;

public class BukkitTabCompleteEvent implements MinecraftPlayerTabCompleteEvent, MinecraftPlayerTabCompleteResponseEvent {

    private final TabCompleteEvent event;
    private final OnlineMinecraftPlayer player;

    public BukkitTabCompleteEvent(TabCompleteEvent event, OnlineMinecraftPlayer player) {
        this.event = event;
        this.player = player;
    }

    @Override
    public String getCursor() {
        return event.getBuffer();
    }

    @Override
    public List<String> getSuggestions() {
        return event.getCompletions();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }
}
