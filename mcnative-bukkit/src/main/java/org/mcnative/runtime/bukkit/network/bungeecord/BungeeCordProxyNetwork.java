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

package org.mcnative.runtime.bukkit.network.bungeecord;

import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.entry.DocumentEntry;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.synchronisation.NetworkSynchronisationCallback;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.OwnedObject;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.player.MinecraftPlayerLogoutEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPostLoginEvent;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.NetworkOperations;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.MinecraftServerType;
import org.mcnative.runtime.api.network.component.server.ProxyServer;
import org.mcnative.runtime.api.network.messaging.Messenger;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.service.MinecraftService;
import org.mcnative.runtime.common.network.event.NetworkEventBus;
import org.mcnative.runtime.common.network.event.defaults.NetworkPlayerLogoutEvent;
import org.mcnative.runtime.common.network.event.defaults.NetworkPlayerPostLoginEvent;
import org.mcnative.runtime.common.network.event.defaults.NetworkPlayerServerSwitchEvent;
import org.mcnative.runtime.network.integrations.McNativeGlobalExecutor;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BungeeCordProxyNetwork implements Network {

    protected static final NetworkIdentifier SINGLE_PROXY_IDENTIFIER = new NetworkIdentifier("Proxy-1",new UUID(10,10));

    private final Messenger messenger;
    private final NetworkOperations operations;
    private final NetworkEventBus eventBus;
    private final Collection<OwnedObject<NetworkSynchronisationCallback>> statusCallbacks;

    private final Collection<MinecraftServer> servers;
    private final Collection<OnlineMinecraftPlayer> players;

    private NetworkIdentifier localIdentifier;
    private UUID networkId;
    private ProxyServer proxy;

    public BungeeCordProxyNetwork(ExecutorService executor) {
        this.messenger = new PluginMessageMessenger(this,executor);
        this.eventBus = new NetworkEventBus();
        this.operations = new BungeeCordNetworkOperations(this);
        this.statusCallbacks = new ArrayList<>();
        this.servers = new ArrayList<>();
        this.players = new ArrayList<>();
        this.messenger.registerChannel("mcnative_event", ObjectOwner.SYSTEM,eventBus);
    }

    @Override
    public String getTechnology() {
        return "BungeeCord Proxy Network";
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public NetworkOperations getOperations() {
        return operations;
    }

    @Override
    public boolean isConnected() {
        return messenger.isAvailable();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public CommandManager getCommandManager() {
        throw new IllegalArgumentException("Network commands are currently not supported");
    }

    @Override
    public NetworkIdentifier getLocalIdentifier() {
        if(localIdentifier == null) throw new IllegalArgumentException("Local identifier not received, waiting for first player connection.");
        return localIdentifier;
    }

    @Override
    public NetworkIdentifier getIdentifier(String name) {
        MinecraftServer server = getServer(name);
        return server != null ? server.getIdentifier() : null;
    }

    @Override
    public NetworkIdentifier getIdentifier(UUID uuid) {
        if(proxy.getUniqueId().equals(uuid)) return proxy.getIdentifier();
        for (MinecraftServer server : getServers()) {
            if(server.getUniqueId().equals(uuid)) return server.getIdentifier();
        }
        return null;
    }

    @Override
    public Collection<ProxyServer> getProxies() {
        return Collections.singleton(proxy);
    }

    @Override
    public Collection<ProxyServer> getProxies(String s) {
        return Collections.singleton(proxy);
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    @Override
    public ProxyServer getProxy(String name) {
        return proxy.getName().equalsIgnoreCase(name) ? proxy : null;
    }

    @Override
    public ProxyServer getProxy(UUID uniqueId) {
        return proxy.getIdentifier().getUniqueId().equals(uniqueId) ? proxy : null;
    }

    @Override
    public ProxyServer getLeaderProxy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaderProxy(ProxyServer proxyServer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<MinecraftServer> getServers() {
        return servers;
    }

    @Override
    public Collection<MinecraftServer> getServers(String group) {
        Collection<MinecraftServer> result = new ArrayList<>();
        for (MinecraftServer server : getServers()) {
            if(server.getGroup().equalsIgnoreCase(group)){
                result.add(server);
            }
        }
        return result;
    }

    @Override
    public MinecraftServer getServer(String name) {
        if(localIdentifier != null && localIdentifier.getName().equalsIgnoreCase(name)) return (MinecraftServer) McNative.getInstance().getLocal();
        else return Iterators.findOne(this.servers, server -> server.getName().equalsIgnoreCase(name));
    }

    @Override
    public MinecraftServer getServer(UUID uniqueId) {
        if(localIdentifier != null && localIdentifier.getUniqueId().equals(uniqueId)) return (MinecraftServer) McNative.getInstance().getLocal();
        else return Iterators.findOne(this.servers, server -> server.getIdentifier().getUniqueId().equals(uniqueId));
    }

    @Override
    public void sendBroadcastMessage(String channel, Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST,channel,request);
    }

    @Override
    public void sendProxyMessage(String channel, Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST_PROXY,channel,request);
    }

    @Override
    public void sendServerMessage(String channel, Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST_SERVER,channel,request);
    }

    @Override
    public Collection<NetworkSynchronisationCallback> getStatusCallbacks() {
        return Iterators.map(statusCallbacks, OwnedObject::getObject);
    }

    @Override
    public void registerStatusCallback(Plugin<?> owner, NetworkSynchronisationCallback synchronisationCallback) {
        Validate.notNull(owner,synchronisationCallback);
        this.statusCallbacks.add(new OwnedObject<>(owner,synchronisationCallback));
    }

    @Override
    public void unregisterStatusCallback(NetworkSynchronisationCallback synchronisationCallback) {
        Validate.notNull(synchronisationCallback);
        Iterators.removeOne(this.statusCallbacks, entry -> entry.getObject().equals(synchronisationCallback));
    }

    @Override
    public void unregisterStatusCallbacks(Plugin<?> owner) {
        Validate.notNull(owner);
        Iterators.removeOne(this.statusCallbacks, entry -> entry.getOwner().equals(owner));
    }

    @Override
    public String getGroup() {
        return getIdentifier().getGroup();
    }

    @Override
    public String getStatus() {
        return isConnected() ? "ONLINE" : "OFFLINE";
    }

    @Override
    public int getMaxPlayerCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOnlineCount() {
        return players.size();
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        return players;
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        OnlineMinecraftPlayer player = McNative.getInstance().getLocal().getConnectedPlayer(uniqueId);
        if(player != null) return player;
        return Iterators.findOne(this.players, player0 -> player0.getUniqueId().equals(uniqueId));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        OnlineMinecraftPlayer player = McNative.getInstance().getLocal().getConnectedPlayer(name);
        if(player != null) return player;
        return Iterators.findOne(this.players, player0 -> player0.getName().equalsIgnoreCase(name));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        OnlineMinecraftPlayer player = McNative.getInstance().getLocal().getConnectedPlayer(xBoxId);
        if(player != null) return player;
        return Iterators.findOne(this.players, player0 -> player0.getXBoxId() == xBoxId);
    }

    public OnlineMinecraftPlayer getBungeeCordPlayer(UUID uniqueId){
        return Iterators.findOne(this.players, player -> player.getUniqueId().equals(uniqueId));
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        McNativeGlobalExecutor.broadcast(component, variables);
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        McNativeGlobalExecutor.broadcast(permission,component, variables);
    }

    protected void handleRequest(Document document){
        String action = document.getString("action");
        if("initial-request".equals(action)) {
            handleInitialRequest(document);
        }else if ("player-login".equals(action)) {
            handlePlayerLogin(document);
        }else if ("player-logout".equals(action)) {
            handlePlayerLogout(document);
        }else if ("player-server-switch".equals(action)) {
            handlePlayerServerSwitch(document);
        }
    }

    private void handleInitialRequest(Document document) {
        this.proxy = new BungeeCordProxy(document.getObject("address", InetSocketAddress.class));
        String local = document.getString("local");
        this.localIdentifier = new NetworkIdentifier(local, UUID.nameUUIDFromBytes(local.getBytes()));
        this.networkId = document.getObject("networkId",UUID.class);
        this.servers.clear();
        this.servers.add(MinecraftService.getInstance());
        for (DocumentEntry entry0 : document.getDocument("servers")) {
            Document entry = entry0.toDocument();
            String name = entry.getString("name");

            MinecraftServer server;
            if(name.equals(local)){
                server = MinecraftService.getInstance();
            }
            else{
                server = new BungeeCordNetworkServer(
                    new NetworkIdentifier(name, UUID.nameUUIDFromBytes(local.getBytes()))
                    , MinecraftServerType.valueOf(entry.getString("type"))
                    , entry.getString("permission")
                    , entry.getObject("address", InetSocketAddress.class));
                this.servers.add(server);
            }

            Document players0 = entry.getDocument("players");
            if (players0 != null) {
                for (DocumentEntry playerEntry0 : players0) {
                    Document playerEntry = playerEntry0.toDocument();
                    UUID uniqueId = playerEntry.getObject("uniqueId", UUID.class);

                    PlayerDataProvider provider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
                    MinecraftPlayerData data = provider.getPlayerData(uniqueId);
                    if (data == null) {
                        McNative.getInstance().getLogger().error("[McNative] (BungeeCord-Network) Received unregistered player, this can cause errors.");
                        return;
                    }

                    BungeeCordOnlinePlayer player = new BungeeCordOnlinePlayer(data
                            ,uniqueId
                            ,playerEntry.getString("name")
                            ,playerEntry.getObject("address", InetSocketAddress.class)
                            ,playerEntry.getBoolean("onlineMode")
                            ,server);
                    if(server instanceof BungeeCordNetworkServer){
                        ((BungeeCordNetworkServer) server).addPlayer(player);
                    }
                    this.players.add(player);
                }
            }
        }
    }

    private void handlePlayerLogin(Document document) {
        UUID uniqueId = document.getObject("uniqueId", UUID.class);
        String name = document.getString("name");
        InetSocketAddress address = document.getObject("address", InetSocketAddress.class);
        String serverName = document.getString("server");
        boolean onlineMode = document.getBoolean("onlineMode");

        PlayerDataProvider provider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
        MinecraftPlayerData data = provider.getPlayerData(uniqueId);
        if (data == null) {
            McNative.getInstance().getLogger().error("[McNative] (BungeeCord-Network) Received unregistered player, this can cause errors.");
            return;
        }
        MinecraftServer server = Iterators.findOne(this.servers, server1 -> server1.getName().equalsIgnoreCase(serverName));

        OnlineMinecraftPlayer player = new BungeeCordOnlinePlayer(data, uniqueId, name, address, onlineMode, server);
        this.players.add(player);
        if(server instanceof BungeeCordNetworkServer) ((BungeeCordNetworkServer) server).addPlayer(player);
        this.eventBus.callEvent(MinecraftPlayerPostLoginEvent.class,new NetworkPlayerPostLoginEvent(player));
    }

    private void handlePlayerLogout(Document document) {
        UUID uniqueId = document.getObject("uniqueId", UUID.class);
        OnlineMinecraftPlayer player = Iterators.removeOne(this.players, player1 -> player1.getUniqueId().equals(uniqueId));
        if (player != null) {
            if (player.getServer() instanceof BungeeCordNetworkServer) {
                ((BungeeCordNetworkServer) player.getServer()).removePlayer(player);
            }
            this.eventBus.callEvent(MinecraftPlayerLogoutEvent.class,new NetworkPlayerLogoutEvent(player));
        }
    }

    private void handlePlayerServerSwitch(Document document) {
        UUID uniqueId = document.getObject("uniqueId", UUID.class);
        String serverName = document.getString("target");

        OnlineMinecraftPlayer player = Iterators.removeOne(this.players, player1 -> player1.getUniqueId().equals(uniqueId));
        MinecraftServer server = Iterators.findOne(this.servers, server1 -> server1.getName().equalsIgnoreCase(serverName));
        if(server == null){
            McNative.getInstance().getLogger().error("[McNative] (BungeeCord-Network) "+serverName+" is not available on sub server");
            return;
        }
        if (player instanceof BungeeCordOnlinePlayer) {
            MinecraftServer from = player.getServer();
            if (player.getServer() instanceof BungeeCordNetworkServer) {
                ((BungeeCordNetworkServer) player.getServer()).removePlayer(player);
            }
            ((BungeeCordOnlinePlayer) player).setServer(server);
            if (server instanceof BungeeCordNetworkServer) {
                ((BungeeCordNetworkServer) player.getServer()).addPlayer(player);
            }
            this.eventBus.callEvent(NetworkPlayerServerSwitchEvent.class,new NetworkPlayerServerSwitchEvent(player,from,server));
        }
    }

    protected void handleDisconnect(){
        this.players.clear();
    }

    @Override
    public NetworkIdentifier getIdentifier() {
        return new NetworkIdentifier(getName(),networkId);
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String s, Document document) {
        throw new UnsupportedOperationException();
    }
}
