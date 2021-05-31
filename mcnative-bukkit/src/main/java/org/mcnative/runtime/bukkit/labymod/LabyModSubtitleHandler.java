package org.mcnative.runtime.bukkit.labymod;

import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.player.design.MinecraftPlayerDesignUpdateEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerCustomClientLoginEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPostLoginEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.player.client.LabyModClient;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class LabyModSubtitleHandler {

    private final double size;
    private final MessageComponent<?> component;

    public LabyModSubtitleHandler(double size, MessageComponent<?> component) {
        this.size = size;
        this.component = component;
        McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM,this);
    }

    @Listener
    public void onLogin(MinecraftPlayerPostLoginEvent event){
        updateAll(event.getOnlinePlayer().getAsConnectedPlayer());
    }

    @Listener
    public void onClientLogin(MinecraftPlayerCustomClientLoginEvent event){
        ConnectedMinecraftPlayer player = event.getOnlinePlayer().getAsConnectedPlayer();
        if(isLabyMod(player)){
            for (ConnectedMinecraftPlayer target : McNative.getInstance().getLocal().getConnectedPlayers()) {
                sendEntry(player,target);
            }
        }
    }

    @Listener
    public void onDesignUpdate(MinecraftPlayerDesignUpdateEvent event){
        ConnectedMinecraftPlayer target = event.getPlayer().getAsConnectedPlayer();
        if(target != null){
            updateAll(target);
        }
    }

    public void updateAll(ConnectedMinecraftPlayer target){
        if(target != null){
            for (ConnectedMinecraftPlayer receiver : McNative.getInstance().getLocal().getConnectedPlayers()) {
                if(isLabyMod(receiver)){
                    sendEntry(receiver,target);
                }
            }
        }
    }

    private boolean isLabyMod(ConnectedMinecraftPlayer player){
        return player.getCustomClient() != null && player.getCustomClient().getName().equalsIgnoreCase("LabyMod");
    }

    public void sendEntry(ConnectedMinecraftPlayer receiver,ConnectedMinecraftPlayer target){
        PlayerDesign design = target.getDesign(receiver);
        VariableSet variables = VariableSet.create();
        variables.add("player",target);
        variables.addDescribed("design",design);

        receiver.getCustomClient(LabyModClient.class).sendSubtitle(target,size,component,variables);
    }

}
