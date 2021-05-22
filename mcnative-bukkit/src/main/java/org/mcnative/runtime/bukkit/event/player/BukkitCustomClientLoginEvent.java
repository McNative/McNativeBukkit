package org.mcnative.runtime.bukkit.event.player;

import org.mcnative.runtime.api.event.player.login.MinecraftPlayerCustomClientLoginEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.client.CustomClient;

public class BukkitCustomClientLoginEvent implements MinecraftPlayerCustomClientLoginEvent {

    private final ConnectedMinecraftPlayer player;

    public BukkitCustomClientLoginEvent(ConnectedMinecraftPlayer player) {
        this.player = player;
    }

    @Override
    public CustomClient getClient() {
        return player.getCustomClient();
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
