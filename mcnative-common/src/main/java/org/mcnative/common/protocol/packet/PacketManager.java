/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 15.09.19, 17:48
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

package org.mcnative.common.protocol.packet;

import net.prematic.libraries.utility.annonations.Nullable;
import org.mcnative.common.connection.MinecraftConnection;
import org.mcnative.common.connection.ConnectionState;
import org.mcnative.common.protocol.MinecraftProtocolVersion;
import org.mcnative.common.protocol.packet.type.MinecraftChatPacket;
import org.mcnative.common.protocol.packet.type.MinecraftTitlePacket;

public interface PacketManager {

    PacketIdentifier getPacketIdentifier(Class<?> packetClass);

    void registerPacket(PacketIdentifier packet);

    default void registerIncomingPacketListener(Class<? extends MinecraftPacket> packetClass, MinecraftPacketListener listener){
        registerPacketListener(PacketDirection.INCOMING,packetClass,listener);
    }

    default void registerOutgoingPacketListener(Class<? extends MinecraftPacket> packetClass, MinecraftPacketListener listener){
        registerPacketListener(PacketDirection.OUTGOING,packetClass,listener);
    }

    void registerPacketListener(PacketDirection direction, Class<? extends MinecraftPacket> packetClass, MinecraftPacketListener listener);

    void unregisterPacketListener(PacketDirection direction, MinecraftPacketListener listener);

    @Nullable(message="Null when packet not should be handled")
    MinecraftPacket createIncomingPacket(ConnectionState state, MinecraftProtocolVersion version,int packetId);

    MinecraftPacket handlePacket(PacketDirection direction,MinecraftProtocolVersion version, MinecraftConnection connection,MinecraftPacket packet);

    static void registerDefaultPackets(PacketManager packetManager){
        packetManager.registerPacket(MinecraftChatPacket.IDENTIFIER);
        packetManager.registerPacket(MinecraftTitlePacket.IDENTIFIER);
    }
}
