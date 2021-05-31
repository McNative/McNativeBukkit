package org.mcnative.runtime.bukkit;

import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.client.CustomPluginMessageListener;

public class BukkitCustomPluginMessage implements PluginMessageListener {

    private final ObjectOwner owner;
    private final String channel;
    private final CustomPluginMessageListener listener;

    public BukkitCustomPluginMessage(ObjectOwner owner, String channel, CustomPluginMessageListener listener) {
        this.owner = owner;
        this.channel = channel;
        this.listener = listener;
    }

    public ObjectOwner getOwner() {
        return owner;
    }

    public String getChannel() {
        return channel;
    }

    public CustomPluginMessageListener getListener() {
        return listener;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player0, @NotNull byte[] bytes) {
        if(channel.equalsIgnoreCase(this.channel)){
            ConnectedMinecraftPlayer player = McNative.getInstance().getLocal().getConnectedPlayer(player0.getUniqueId());
            listener.onReceive(player,channel,bytes);
        }
    }
}
