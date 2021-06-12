/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.02.20, 20:16
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

package org.mcnative.runtime.bukkit.player.connection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.bukkit.Bukkit;
import org.bukkit.event.server.ServerListPingEvent;
import org.mcnative.runtime.bukkit.event.BukkitServerListPingEvent;
import org.mcnative.runtime.bukkit.event.BukkitWrapperServerListPingEvent;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.protocol.MinecraftEdition;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.bukkit.utils.ProtocolSupportExtensionUtil;
import org.mcnative.runtime.protocol.java.MinecraftProtocolUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

//@Todo implement legacy < 1.6 status ping
public class McNativeHandshakeDecoder extends MessageToMessageDecoder<ByteBuf> {

    public static boolean VIA_VERSION = Bukkit.getPluginManager().getPlugin("ViaVersion") != null;
    public static boolean PROTOCOL_LIB = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
    public static boolean PROTOCOL_SUPPORT = Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null;

    private final static int HANDSHAKE_PACKET_ID = 0;
    private final static int HANDSHAKE_PACKET_ID_LEGACY =  0xFE;
    private final static int HANDSHAKE_PING = 0x01;
    private final static int HANDSHAKE_PONG = 0x01;

    private final ChannelConnection connection;

    private boolean finished;
    private boolean statusRequest;

    public McNativeHandshakeDecoder(ChannelConnection connection) {
        this.connection = connection;
        this.finished = false;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> list) {
        try{
            ByteBuf out = buffer.copy();
            if(!finished){
                finished = true;
                int packetId = MinecraftProtocolUtil.readVarInt(buffer);
                if(packetId == HANDSHAKE_PACKET_ID){
                    int protocolVersion = MinecraftProtocolUtil.readVarInt(buffer);
                    if(PROTOCOL_SUPPORT) protocolVersion = ProtocolSupportExtensionUtil.getProtocolVersion((InetSocketAddress) context.channel().remoteAddress());
                    String host = MinecraftProtocolUtil.readString(buffer);
                    int port = buffer.readUnsignedShort();
                    int next = MinecraftProtocolUtil.readVarInt(buffer);
                    connection.initHandshake(protocolVersion,host,port);
                    if(next == 1){
                        tryRemoveHandlers();
                        statusRequest = true;
                        handleServerListPing();
                        out.release();
                        return;
                    }
                }
            }else if(statusRequest){
                int packetId = MinecraftProtocolUtil.readVarInt(buffer);
                if(packetId == HANDSHAKE_PING){
                    long payload = buffer.readLong();
                    ByteBuf resultBuffer = Unpooled.directBuffer();
                    MinecraftProtocolUtil.writeVarInt(resultBuffer,HANDSHAKE_PONG);
                    resultBuffer.writeLong(payload);
                    connection.getChannel().writeAndFlush(resultBuffer);
                }
                out.release();
                return;
            }
            list.add(out);
        }catch (IndexOutOfBoundsException exception){
            //Received wrong data, closing the channel
            context.channel().close();
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    //This is currently only a workaround and should be optimized with an own multiple version implementation
    private void tryRemoveHandlers(){
        if(PROTOCOL_LIB){
            connection.getChannel().pipeline().remove("protocol_lib_encoder");
        }
        if(VIA_VERSION){
            connection.getChannel().pipeline().remove("encoder");
            try {
                connection.getChannel().pipeline().remove("viaversion_packet_handler");
            }catch (NoSuchElementException ignored){}
        }
        if(PROTOCOL_SUPPORT){
            connection.getChannel().pipeline().remove("encoder");
            connection.getChannel().pipeline().remove("decoder");
            connection.getChannel().pipeline().remove("ps_raw_capture_send");
            connection.getChannel().pipeline().remove("ps_raw_capture_receive");
            connection.getChannel().pipeline().remove("ps_logic");
            try {
                connection.getChannel().pipeline().remove("ps_decoder_transformer");
                connection.getChannel().pipeline().remove("ps_encoder_transformer");
            }catch (Exception ignored){}
        }
    }

    private void handleServerListPing(){
        ByteBuf resultBuffer = Unpooled.directBuffer();
        ServerStatusResponse response = McNative.getInstance().getLocal().getStatusResponse().clone();

        MinecraftProtocolVersion clientVersion = MinecraftProtocolVersion.UNKNOWN;
        try{
            clientVersion = MinecraftProtocolVersion.of(MinecraftEdition.JAVA,connection.getProtocolVersion());
        }catch (Exception ignored){}

        if(McNative.getInstance().getPlatform().canJoin(clientVersion)){
            response.getVersion().setProtocol(clientVersion);
        }

        BukkitServerListPingEvent mcnativeEvent = new BukkitServerListPingEvent(
                (InetSocketAddress) connection.getChannel().remoteAddress()
                ,new InetSocketAddress(connection.getHostname(),connection.getPort())
                ,response);
        BukkitWrapperServerListPingEvent bukkitEvent = new BukkitWrapperServerListPingEvent(mcnativeEvent);
        McNative.getInstance().getLocal().getEventBus().callEvents(ServerListPingEvent.class,bukkitEvent,mcnativeEvent);

        MinecraftProtocolUtil.writeVarInt(resultBuffer,HANDSHAKE_PACKET_ID);
        MinecraftProtocolUtil.writeString(resultBuffer,response.compileToString(clientVersion));
        connection.getChannel().writeAndFlush(resultBuffer);
    }
}
