/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 20.09.19, 20:27
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
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.bukkit.player.connection.BukkitChannelInjector;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.connection.ConnectionState;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.common.player.AbstractPlayerManager;
import org.mcnative.runtime.protocol.java.netty.wrapper.McNativeMessageEncoderIgnoreWrapper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class BukkitPlayerManager extends AbstractPlayerManager {

    private final Collection<ConnectedMinecraftPlayer> onlineMinecraftPlayers;

    public BukkitPlayerManager() {
        this.onlineMinecraftPlayers = new ArrayList<>();
    }

    public Collection<ConnectedMinecraftPlayer> getConnectedPlayers() {
        return onlineMinecraftPlayers;
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(UUID uniqueId) {
        return Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getUniqueId().equals(uniqueId));
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(long xBoxId) {
        return Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getXBoxId() == xBoxId);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(String name) {
        return Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getName().equalsIgnoreCase(name));
    }

    @Internal
    public BukkitPlayer getMappedPlayer(org.bukkit.entity.Player player0){
        ConnectedMinecraftPlayer result = Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getUniqueId().equals(player0.getUniqueId()));
        if(result == null){
            throw new IllegalArgumentException("McNative mapping error (BungeeCord -> McNative) ");
        }
        return (BukkitPlayer) result;
    }

    @Internal
    public void registerPlayer(Player player){
        this.onlineMinecraftPlayers.add(player);
        this.offlineMinecraftPlayers.remove(player0 -> player0.getUniqueId().equals(player.getUniqueId()));
    }

    @Internal
    public OnlineMinecraftPlayer unregisterPlayer(UUID uniqueId){
        return Iterators.removeOne(this.onlineMinecraftPlayers, player -> player.getUniqueId().equals(uniqueId));
    }

    @Internal
    public void loadConnectedPlayers(){
        PlayerDataProvider dataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
        ChatChannel serverChat = McNative.getInstance().getLocal().getServerChat();
        if(serverChat != null) serverChat.getPlayers().clear();
        for (org.bukkit.entity.Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            MinecraftPlayerData data = dataProvider.getPlayerData(onlinePlayer.getUniqueId());
            if(data == null){
                Bukkit.getScheduler().runTask(McNativeLauncher.getPlugin(), () -> onlinePlayer.kickPlayer("Unrecognised login"));
                return;
            }

            Channel channel = BukkitReflectionUtil.getPlayerChannel(onlinePlayer);

            GameProfile profile;
            try {
                profile = BukkitChannelInjector.extractGameProfile(BukkitReflectionUtil.getGameProfile(onlinePlayer));
            } catch (Exception ignored) {
                Bukkit.getScheduler().runTask(McNativeLauncher.getPlugin(), () -> onlinePlayer.kickPlayer("Unrecognised login"));
               return;
            }

            Object protocolEncoder = BukkitPendingConnection.VIA_VERSION ?
                    channel.pipeline().get("encoder") :
                    channel.pipeline().get("mcnative-packet-encoder");
            if(protocolEncoder == null){
                Bukkit.getScheduler().runTask(McNativeLauncher.getPlugin(), () -> onlinePlayer.kickPlayer("Unrecognised login"));
                return;
            }

            int protocolVersion = (int) ReflectionUtil.invokeMethod(protocolEncoder,"getProtocolNumber");

            resetChannelPipeline(channel);

            BukkitPendingConnection connection;
            try {
                connection = new BukkitPendingConnection(channel,profile,onlinePlayer.getAddress()
                        , new InetSocketAddress(InetAddress.getLocalHost(),25565),protocolVersion);
                connection.setState(ConnectionState.GAME);
            } catch (UnknownHostException ignored) {
                Bukkit.getScheduler().runTask(McNativeLauncher.getPlugin(), () -> onlinePlayer.kickPlayer("Unrecognised login"));
                return;
            }

            BukkitPlayer player = new BukkitPlayer(onlinePlayer,connection,data);
            connection.setPlayer(player);
            player.setJoining(false);
            registerPlayer(player);
            if(serverChat != null) serverChat.addPlayer(player);
        }
    }

    @SuppressWarnings("unchecked")
    private void resetChannelPipeline(Channel channel){
        if(BukkitPendingConnection.VIA_VERSION && !BukkitPendingConnection.PROTOCOL_LIB){
            ChannelHandler viaEncoder = channel.pipeline().get("via-encoder");
            if(viaEncoder != null && viaEncoder.getClass().getName().equals(McNativeMessageEncoderIgnoreWrapper.class.getName())){
                MessageToByteEncoder<Object> object = (MessageToByteEncoder<Object>) ReflectionUtil.invokeMethod(viaEncoder,"getOriginal");
                channel.pipeline().replace("encoder","encoder",new McNativeMessageEncoderIgnoreWrapper(object));
                channel.pipeline().remove("via-encoder");
            }
        }
        if(channel.pipeline().get("mcnative-packet-encoder") != null){
            channel.pipeline().remove("mcnative-packet-encoder");
        }
        if(channel.pipeline().get("mcnative-packet-rewrite-encoder") != null){
            channel.pipeline().remove("mcnative-packet-rewrite-encoder");
        }
        if(channel.pipeline().get("mcnative-packet-rewrite-decoder") != null){
            channel.pipeline().remove("mcnative-packet-rewrite-decoder");
        }
    }

}
