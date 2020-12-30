/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 18:42
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

package org.mcnative.runtime.bukkit.player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.connection.ConnectionState;
import org.mcnative.runtime.api.connection.MinecraftOutputStream;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.protocol.Endpoint;
import org.mcnative.runtime.api.protocol.MinecraftEdition;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacket;
import org.mcnative.runtime.api.protocol.packet.PacketDirection;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftDisconnectPacket;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.protocol.java.netty.MinecraftProtocolEncoder;
import org.mcnative.runtime.protocol.java.netty.rewrite.MinecraftProtocolRewriteDecoder;
import org.mcnative.runtime.protocol.java.netty.rewrite.MinecraftProtocolRewriteEncoder;
import org.mcnative.runtime.protocol.java.netty.wrapper.McNativeMessageDecoderIgnoreWrapper;
import org.mcnative.runtime.protocol.java.netty.wrapper.McNativeMessageEncoderIgnoreWrapper;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;

public class BukkitPendingConnection implements PendingConnection {

    public static boolean VIA_VERSION = Bukkit.getPluginManager().getPlugin("ViaVersion") != null;
    public static boolean PROTOCOL_LIB = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;

    private final Channel channel;
    private final MinecraftProtocolVersion version;
    private final GameProfile gameProfile;
    private final InetSocketAddress address;
    private final InetSocketAddress virtualHost;

    private ConnectionState state;
    private OnlineMinecraftPlayer player;

    public BukkitPendingConnection(Channel channel,GameProfile gameProfile,InetSocketAddress address
            ,InetSocketAddress virtualHost, int protocolVersion) {
        this.channel = channel;
        this.gameProfile = gameProfile;
        this.address = address;
        this.virtualHost = virtualHost;
        this.version = MinecraftProtocolVersion.of(MinecraftEdition.JAVA,protocolVersion);
        this.state = ConnectionState.LOGIN;
        injectPacketCoders();
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public UUID getUniqueId() {
        return gameProfile.getUniqueId();
    }

    @Override
    public long getXBoxId() {
        return -1;
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }

    @Override
    public void setGameProfile(GameProfile profile) {
        throw new UnsupportedOperationException("Currently not supported on bukkit servers");
    }

    @Override
    public void setUniqueId(UUID uniqueId) {
        throw new UnsupportedOperationException("Currently not supported on bukkit servers");
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode();
    }

    @Override
    public void setOnlineMode(boolean online) {
        throw new UnsupportedOperationException("It is not possible to change the online mode of a player on a bukkit server.");
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return virtualHost;
    }

    @Override
    public boolean isPlayerAvailable() {
        return player != null;
    }

    @Override
    public OnlineMinecraftPlayer getPlayer() {
        return player;
    }

    @Override
    public String getName() {
        return gameProfile.getName();
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return version;
    }

    @Override
    public ConnectionState getState() {
        return state;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    @Override
    public void disconnect(MessageComponent<?> reason, VariableSet variables) {
        MinecraftDisconnectPacket packet = new MinecraftDisconnectPacket();
        packet.setReason(reason);
        packet.setVariables(variables);
        sendPacket(packet);
    }

    @Override
    public void sendPacket(MinecraftPacket packet) {
       if(isConnected()){//@Todo implement packet queue
           channel.writeAndFlush(packet);
       }
    }

    @Override
    public void sendLocalLoopPacket(MinecraftPacket packet) {
        throw new UnsupportedOperationException("Currently not supported on bukkit servers");
    }

    @Override
    public OutputStream sendData(String channel) {
        return new MinecraftOutputStream(channel,this);
    }

    @Override
    public void sendData(String channel, byte[] output) {
        throw new UnsupportedOperationException("Currently not supported on bukkit servers");
    }

    @Internal
    public void setState(ConnectionState state){
        this.state = state;
    }

    @Internal
    public void setPlayer(OnlineMinecraftPlayer player){
        this.player = player;
    }

    @SuppressWarnings("unchecked")
    private void injectPacketCoders(){
        if(PROTOCOL_LIB) {
            //Encoder
            MessageToByteEncoder<Object> original = (MessageToByteEncoder<Object>) channel.pipeline().get("protocol_lib_encoder");

            channel.pipeline().replace("protocol_lib_encoder","protocol_lib_encoder",new McNativeMessageEncoderIgnoreWrapper(original));

            channel.pipeline().addBefore("protocol_lib_encoder","mcnative-packet-encoder"
                    ,new MinecraftProtocolEncoder(McNative.getInstance().getLocal().getPacketManager()
                            , Endpoint.UPSTREAM, PacketDirection.OUTGOING,this));

            this.channel.pipeline().addAfter("protocol_lib_encoder","mcnative-packet-rewrite-encoder"
                    ,new MinecraftProtocolRewriteEncoder(McNative.getInstance().getLocal().getPacketManager()
                            ,Endpoint.UPSTREAM, PacketDirection.OUTGOING,this));

            //Decoder
            ByteToMessageDecoder decoderOriginal = extractDecoder(channel.pipeline().get("protocol_lib_decoder"));
            ReflectionUtil.changeFieldValue(ChannelHandlerAdapter.class,decoderOriginal,"added",false);
            this.channel.pipeline().replace("protocol_lib_decoder","protocol_lib_decoder"
                    ,new MinecraftProtocolRewriteDecoder(McNative.getInstance().getLocal().getPacketManager()
                            ,Endpoint.UPSTREAM, PacketDirection.INCOMING,this));

            channel.pipeline().addAfter("protocol_lib_decoder","protocol_lib_decoder-original",decoderOriginal);
        }else if(VIA_VERSION){
            //Encoder
            Object encoder = channel.pipeline().get("encoder");
            MessageToByteEncoder<Object> original;
            if(encoder instanceof McNativeMessageEncoderIgnoreWrapper){
                original = ((McNativeMessageEncoderIgnoreWrapper) encoder).getOriginal();
            }else if(encoder instanceof MessageToByteEncoder){
                original = (MessageToByteEncoder<Object>) encoder;
            }else throw new IllegalArgumentException("Invalid handler, contact the McNative developer team ("+encoder.getClass()+")");

            channel.pipeline().replace("encoder","encoder"
                    ,new MinecraftProtocolEncoder(McNative.getInstance().getLocal().getPacketManager()
                            , Endpoint.UPSTREAM, PacketDirection.OUTGOING,this));

            channel.pipeline().addAfter("encoder","via-encoder",new McNativeMessageEncoderIgnoreWrapper(original));


            this.channel.pipeline().addAfter("via-encoder","mcnative-packet-rewrite-encoder"
                    ,new MinecraftProtocolRewriteEncoder(McNative.getInstance().getLocal().getPacketManager()
                            ,Endpoint.UPSTREAM, PacketDirection.OUTGOING,this));

            //Decoder
            ByteToMessageDecoder originalDecoder = extractDecoder(channel.pipeline().get("decoder"));
            this.channel.pipeline().replace("decoder","decoder"
                    ,new MinecraftProtocolRewriteDecoder(McNative.getInstance().getLocal().getPacketManager()
                            ,Endpoint.UPSTREAM, PacketDirection.INCOMING,this));

            ReflectionUtil.changeFieldValue(ChannelHandlerAdapter.class,originalDecoder,"added",false);
            this.channel.pipeline().addAfter("decoder","via-decoder",originalDecoder);

        }else{
            //Encoder
            this.channel.pipeline().addAfter("encoder","mcnative-packet-encoder"
                    ,new MinecraftProtocolEncoder(McNative.getInstance().getLocal().getPacketManager()
                            , Endpoint.UPSTREAM, PacketDirection.OUTGOING,this));

            this.channel.pipeline().addAfter("mcnative-packet-encoder","mcnative-packet-rewrite-encoder"
                    ,new MinecraftProtocolRewriteEncoder(McNative.getInstance().getLocal().getPacketManager()
                            ,Endpoint.UPSTREAM, PacketDirection.OUTGOING,this));

            //Decoder
            ByteToMessageDecoder original = extractDecoder(channel.pipeline().get("decoder"));
            this.channel.pipeline().replace("decoder","decoder"
                    ,new MinecraftProtocolRewriteDecoder(McNative.getInstance().getLocal().getPacketManager()
                            ,Endpoint.UPSTREAM, PacketDirection.INCOMING,this));

            if(original != null){
                channel.pipeline().addAfter("decoder","minecraft-decoder",new McNativeMessageDecoderIgnoreWrapper(original));
            }
        }
    }

    private ByteToMessageDecoder extractDecoder(Object decoder){
        ByteToMessageDecoder original;
        if(decoder instanceof McNativeMessageDecoderIgnoreWrapper){
            original = ((McNativeMessageDecoderIgnoreWrapper) decoder).getOriginal();
        }else if(decoder instanceof ByteToMessageDecoder){
            original = (ByteToMessageDecoder) decoder;
        }else if(decoder.getClass().getName().equals(MinecraftProtocolRewriteDecoder.class.getName())){
            original = null;
        }else throw new IllegalArgumentException("Invalid handler, contact the McNative developer team ("+decoder.getClass()+")");
        return original;
    }
}
