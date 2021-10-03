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

package org.mcnative.runtime.bukkit;

import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.protocol.Endpoint;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacketEvent;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacketListener;
import org.mcnative.runtime.api.protocol.packet.PacketDirection;
import org.mcnative.runtime.api.protocol.packet.PacketManager;
import org.mcnative.runtime.api.protocol.packet.type.player.complete.MinecraftPlayerTabCompletePacket;
import org.mcnative.runtime.api.protocol.packet.type.player.complete.MinecraftPlayerTabCompleteResponsePacket;
import org.mcnative.runtime.common.event.player.complete.DefaultMinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.common.event.player.complete.DefaultMinecraftPlayerTabCompleteResponseEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class McNativeTabCompleteEventHandler implements MinecraftPacketListener {

    private final EventBus eventBus;
    private final Map<UUID,String> cursors;

    public McNativeTabCompleteEventHandler(EventBus eventBus, PacketManager packetManager) {
        this.eventBus = eventBus;
        this.cursors = new ConcurrentHashMap<>();

        packetManager.registerPacketListener(Endpoint.UPSTREAM, PacketDirection.INCOMING, MinecraftPlayerTabCompletePacket.class,this);
        packetManager.registerPacketListener(Endpoint.UPSTREAM, PacketDirection.OUTGOING, MinecraftPlayerTabCompleteResponsePacket.class,this);
    }

    @Override
    public void handle(MinecraftPacketEvent event) {
        if(!(event.getConnection() instanceof PendingConnection)) return;
        if(event.getPacket().getClass().equals(MinecraftPlayerTabCompletePacket.class)){
            MinecraftPlayerTabCompletePacket packet = event.getPacket(MinecraftPlayerTabCompletePacket.class);

            System.out.println("REQUEST: "+packet.getCursor());

            cursors.put(((PendingConnection) event.getConnection()).getUniqueId(),packet.getCursor());

            ConnectedMinecraftPlayer player = ((PendingConnection) event.getConnection()).getPlayer();
            MinecraftPlayerTabCompleteEvent mcnativeEvent = new DefaultMinecraftPlayerTabCompleteEvent(packet,player);
            eventBus.callEvent(MinecraftPlayerTabCompleteEvent.class,mcnativeEvent);

            if(mcnativeEvent.isCancelled()) event.setCancelled(true);
            else event.setRewrite(true);
        }else if(event.getPacket().getClass().equals(MinecraftPlayerTabCompleteResponsePacket.class)){
            System.out.println("RESPONSE waiting");
            MinecraftPlayerTabCompleteResponsePacket packet = event.getPacket(MinecraftPlayerTabCompleteResponsePacket.class);

            ConnectedMinecraftPlayer player = ((PendingConnection) event.getConnection()).getPlayer();
            String cursor = cursors.remove(player.getUniqueId());
            System.out.println("RESPONSE: "+cursor);

            MinecraftPlayerTabCompleteResponseEvent mcnativeEvent = new DefaultMinecraftPlayerTabCompleteResponseEvent(packet,cursor,player);
            eventBus.callEvent(MinecraftPlayerTabCompleteResponseEvent.class,mcnativeEvent);

            if(mcnativeEvent.isCancelled()) event.setCancelled(true);
            else {
                if(event.getVersion().isNewerOrSame(MinecraftProtocolVersion.JE_1_13) && cursor != null && cursor.startsWith("/")){
                    packet.setSuggestions(Iterators.map(packet.getSuggestions(), s -> s.length() > 0 && s.charAt(0) == '/' ? s.substring(1) : s));
                }
                event.setRewrite(true);
            }
        }
    }
}
