/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 28.04.20, 17:04
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

package org.mcnative.bukkit.network.bungeecord;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.synchronisation.NetworkSynchronisationCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.mcnative.bukkit.McNativeLauncher;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.messaging.MessageReceiver;
import org.mcnative.runtime.api.network.messaging.MessagingChannelListener;
import org.mcnative.runtime.common.network.messaging.AbstractMessenger;
import org.mcnative.runtime.protocol.java.MinecraftProtocolUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class PluginMessageMessenger extends AbstractMessenger implements PluginMessageListener, Listener {

    private final String CHANNEL_NAME_REQUEST = "mcnative:request";
    private final String CHANNEL_NAME_RESPONSE = "mcnative:response";
    private final String CHANNEL_NAME_NETWORK = "mcnative:network";

    private final BungeeCordProxyNetwork network;
    private final Executor executor;
    private final Map<UUID,CompletableFuture<Document>> resultListeners;

    private Player transport;

    public PluginMessageMessenger(BungeeCordProxyNetwork network,Executor executor) {
        this.network = network;
        this.executor = executor;
        this.resultListeners = new HashMap<>();
        register();
    }

    private void register(){
        Bukkit.getMessenger().registerIncomingPluginChannel(McNativeLauncher.getPlugin(),CHANNEL_NAME_REQUEST,PluginMessageMessenger.this);
        Bukkit.getMessenger().registerIncomingPluginChannel(McNativeLauncher.getPlugin(),CHANNEL_NAME_RESPONSE,PluginMessageMessenger.this);
        Bukkit.getMessenger().registerIncomingPluginChannel(McNativeLauncher.getPlugin(),CHANNEL_NAME_NETWORK,PluginMessageMessenger.this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(McNativeLauncher.getPlugin(),CHANNEL_NAME_REQUEST);
        Bukkit.getMessenger().registerOutgoingPluginChannel(McNativeLauncher.getPlugin(),CHANNEL_NAME_RESPONSE);
        Bukkit.getMessenger().registerOutgoingPluginChannel(McNativeLauncher.getPlugin(),CHANNEL_NAME_NETWORK);
        Bukkit.getPluginManager().registerEvents(PluginMessageMessenger.this,McNativeLauncher.getPlugin());

        Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
        if(iterator.hasNext()) transport = iterator.next();
    }

    @Override
    public String getTechnology() {
        return "Plugin Messaging";
    }

    @Override
    public boolean isAvailable() {
        return transport != null;
    }

    @Override
    public void sendMessage(NetworkIdentifier receiver, String channel, Document request, UUID requestId) {
        byte[] data = writeData(receiver.getUniqueId(),requestId,channel,request,true);
        sendData(CHANNEL_NAME_REQUEST,data);
    }

    @Override
    public void sendMessage(MessageReceiver receiver, String channel, Document request,UUID requestId) {
        if(receiver instanceof MinecraftServer){
            sendMessage(receiver.getIdentifier(), channel, request, requestId);
        }else throw new IllegalArgumentException("Plugin messaging requires a minecraft server as receiver");
    }

    @Override
    public Document sendQueryMessage(MessageReceiver receiver, String channel, Document request) {
        try {
            return sendQueryMessageAsync(receiver,channel,request).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalArgumentException("");
        }
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(MessageReceiver receiver, String channel, Document request) {
        CompletableFuture<Document> result = new CompletableFuture<>();
        UUID id = UUID.randomUUID();
        this.resultListeners.put(id,result);
        executor.execute(()-> sendMessage(receiver, channel, request,id));
        return result;
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(NetworkIdentifier receiver, String channel, Document request) {
        CompletableFuture<Document> result = new CompletableFuture<>();
        UUID id = UUID.randomUUID();
        this.resultListeners.put(id,result);
        executor.execute(()-> sendMessage(receiver, channel, request,id));
        return result;
    }

    private byte[] writeData(UUID instance,UUID id,String channel, Document data,boolean request) {
        ByteBuf buffer = Unpooled.directBuffer();
        MinecraftProtocolUtil.writeUUID(buffer,instance);//Destination or Source
        MinecraftProtocolUtil.writeUUID(buffer,id);//Id

        if(request) MinecraftProtocolUtil.writeString(buffer,channel);//Channel

        String requestData = DocumentFileType.JSON.getWriter().write(data,false);
        MinecraftProtocolUtil.writeString(buffer,requestData);


        byte[] data0 = new byte[buffer.readableBytes()];
        buffer.readBytes(data0);
        buffer.release();
        return data0;
    }

    @Override
    public void onPluginMessageReceived(String tag, Player unused0, byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);

        if(tag.equals(CHANNEL_NAME_REQUEST)){
            UUID senderId = MinecraftProtocolUtil.readUUID(buffer);
            UUID identifier = MinecraftProtocolUtil.readUUID(buffer);
            String channel = MinecraftProtocolUtil.readString(buffer);

            MessagingChannelListener listener = getChannelListener(channel);
            if(listener != null){
                MinecraftServer sender = McNative.getInstance().getNetwork().getServer(senderId);
                Document document = DocumentFileType.JSON.getReader().read(MinecraftProtocolUtil.readString(buffer));
                Document result = listener.onMessageReceive(sender,identifier,document);
                if(result != null){
                    byte[] response = writeData(senderId,identifier,channel,result,false);
                    unused0.sendPluginMessage(McNativeLauncher.getPlugin(),CHANNEL_NAME_RESPONSE,response);
                }
            }
        }else if(tag.equals(CHANNEL_NAME_RESPONSE)){
            UUID identifier = MinecraftProtocolUtil.readUUID(buffer);
            CompletableFuture<Document> resultListener = resultListeners.remove(identifier);
            if(resultListener != null){
                Document document = DocumentFileType.JSON.getReader().read(MinecraftProtocolUtil.readString(buffer));
                resultListener.complete(document);
            }
        }else if(tag.equals(CHANNEL_NAME_NETWORK)){
            Document document = DocumentFileType.JSON.getReader().read(new ByteBufInputStream(buffer), StandardCharsets.UTF_8);
            network.handleRequest(document);
        }
        buffer.release();
    }

    public void sendData(String channel, byte[] data){
        if(transport != null){
            transport.sendPluginMessage(McNativeLauncher.getPlugin(),channel,data);
        }else{
            throw new UnsupportedOperationException("The McNative plugin messaging gateway needs at least one player to communicate with the proxy server");
        }
    }

    @EventHandler
    public void handlePlayerConnect(PlayerLoginEvent event){
        if(transport == null){
            transport = event.getPlayer();
            McNative.getInstance().getLogger().info("[McNative] (Plugin-Message-Gateway) connected to proxy");
            for (NetworkSynchronisationCallback statusCallback : network.getStatusCallbacks()) {
                statusCallback.onConnect();
            }
        }
    }

    @EventHandler
    public void handlePlayerDisconnect(PlayerQuitEvent event){
        if(Bukkit.getOnlinePlayers().size() == 1){
            transport = null;
            McNative.getInstance().getLogger().info("[McNative] (Plugin-Message-Gateway) disconnected from proxy");
            network.handleDisconnect();
            for (NetworkSynchronisationCallback statusCallback : network.getStatusCallbacks()) {
                statusCallback.onDisconnect();
            }
        }
    }
}
