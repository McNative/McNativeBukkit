package org.mcnative.runtime.bukkit.labymod;

import net.pretronic.libraries.event.Listener;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerCustomClientLoginEvent;
import org.mcnative.runtime.api.player.client.LabyModClient;
import org.mcnative.runtime.bukkit.McNativeBukkitConfiguration;

public class LabyModListener {

    @Listener
    public void onLabyModPlayerLogin(MinecraftPlayerCustomClientLoginEvent event){
        if(event.getClient().getName().equalsIgnoreCase("LabyMod")){
            LabyModClient client = (LabyModClient) event.getClient();
            if(McNativeBukkitConfiguration.LABYMOD_BANNER_ENABLED) client.sendServerBanner(McNativeBukkitConfiguration.LABYMOD_BANNER_URL);
            if(McNativeBukkitConfiguration.LABYMOD_WATERMARK_ENABLED) client.sendWatermark(true);
            if(!McNativeBukkitConfiguration.LABYMOD_VOICECHAT_ENABLED) client.disableVoiceChat();

            if(McNativeBukkitConfiguration.LABYMOD_ALERT_ENABLED) {
                client.sendCurrentGameModeInfo(McNativeBukkitConfiguration.LABYMOD_ALERT_GAMEMODE);
            }
        }
    }

}
