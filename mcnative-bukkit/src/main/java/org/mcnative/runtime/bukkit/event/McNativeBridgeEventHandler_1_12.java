/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 13:56
 * @web %web%
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.runtime.bukkit.event;

import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.bukkit.event.player.BukkitLegacyTabCompleteEvent;
import org.mcnative.runtime.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.bukkit.plugin.event.BukkitEventBus;
import org.mcnative.runtime.bukkit.plugin.event.McNativeHandlerList;

public class McNativeBridgeEventHandler_1_12 {

    private final BukkitEventBus eventBus;
    private final BukkitPlayerManager playerManager;

    public McNativeBridgeEventHandler_1_12(BukkitEventBus eventBus, BukkitPlayerManager playerManager) {
        this.eventBus = eventBus;
        this.playerManager = playerManager;
        setup();
    }

    private void setup(){
        /* Lifecycle */
        eventBus.registerMappedClass(MinecraftPlayerTabCompleteEvent.class, PlayerChatTabCompleteEvent.class);
        eventBus.registerManagedEvent(PlayerChatTabCompleteEvent.class, this::handleLegacyTabComplete);
    }

    private void handleLegacyTabComplete(McNativeHandlerList handler, PlayerChatTabCompleteEvent event){
        ConnectedMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        BukkitLegacyTabCompleteEvent mcnativeEvent = new BukkitLegacyTabCompleteEvent(event,player);
        handler.callEvents(event,mcnativeEvent);
        this.eventBus.callEvent(MinecraftPlayerTabCompleteResponseEvent.class, mcnativeEvent);
    }
}
