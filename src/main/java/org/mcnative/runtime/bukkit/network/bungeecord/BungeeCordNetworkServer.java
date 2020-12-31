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
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.bukkit.McNativeBukkitConfiguration;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.MinecraftServerType;
import org.mcnative.runtime.api.network.component.server.ServerStatusRequester;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.text.components.MessageComponent;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BungeeCordNetworkServer implements MinecraftServer {

    private final NetworkIdentifier identifier;
    private final MinecraftServerType type;
    private final String permission;
    private final InetSocketAddress address;
    private final Collection<OnlineMinecraftPlayer> players;

    protected BungeeCordNetworkServer(NetworkIdentifier identifier, MinecraftServerType type, String permission
            , InetSocketAddress address) {
        this.identifier = identifier;
        this.type = type;
        this.permission = permission;
        this.address = address;
        this.players = new ArrayList<>();
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return ping().getVersion().getProtocol();
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public MinecraftServerType getType() {
        return type;
    }

    @Override
    public void setType(MinecraftServerType type) {
        throw new UnsupportedOperationException("It is not possible to update the server on a BungeeCord network");
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public void setPermission(String permission) {
        throw new UnsupportedOperationException("It is not possible to update the permission on a BungeeCord network");
    }

    @Override
    public ServerStatusResponse ping() {
        return ServerStatusRequester.getDefault().requestStatus(getAddress());
    }

    @Override
    public CompletableFuture<ServerStatusResponse> pingAsync() {
        return ServerStatusRequester.getDefault().requestStatusAsync(getAddress());
    }

    @Override
    public void sendData(String s, Document document) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void sendData(String s, byte[] bytes) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void sendData(String channel, byte[] data, boolean queued) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public String getName() {
        return identifier.getName();
    }

    @Override
    public String getGroup() {
        return getName().split(McNativeBukkitConfiguration.SERVER_GROUP_DELIMITER)[0];
    }

    @Override
    public String getStatus() {
        return isOnline() ? "ONLINE" : "OFFLINE";
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
        Validate.notNull(uniqueId);
        return Iterators.findOne(this.players, player -> player.getUniqueId().equals(uniqueId));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        Validate.notNull(name);
        return Iterators.findOne(this.players, player -> player.getName().equalsIgnoreCase(name));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        return Iterators.findOne(this.players, player -> player.getXBoxId() == xBoxId);
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public EventBus getEventBus() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public CommandManager getCommandManager() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public boolean isOnline() {
        try {
            ping();
            return true;
        }catch (Exception ignored){}
        return false;
    }

    @Override
    public NetworkIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void sendMessage(String channel, Document request) {
        McNative.getInstance().getNetwork().getMessenger().sendMessage(this,channel,request);
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String channel, Document request) {
        return McNative.getInstance().getNetwork().getMessenger().sendQueryMessageAsync(this,channel,request);
    }

    protected void addPlayer(OnlineMinecraftPlayer player){
        this.players.add(player);
    }

    protected void removePlayer(OnlineMinecraftPlayer player){
        this.players.remove(player);
    }
}
