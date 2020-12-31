/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 29.04.20, 19:54
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
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.component.server.ProxyServer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.text.components.MessageComponent;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BungeeCordProxy implements ProxyServer {

    private final InetSocketAddress address;

    public BungeeCordProxy(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public String getName() {
        return "Proxy-1";
    }

    @Override
    public String getGroup() {
        return "Proxy";
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
        return McNative.getInstance().getNetwork().getOnlineCount();
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        return McNative.getInstance().getNetwork().getOnlinePlayers();
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        return McNative.getInstance().getNetwork().getOnlinePlayer(uniqueId);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        return McNative.getInstance().getNetwork().getOnlinePlayer(name);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        return McNative.getInstance().getNetwork().getOnlinePlayer(xBoxId);
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        McNative.getInstance().getNetwork().broadcast(component,variables);
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        McNative.getInstance().getNetwork().broadcast(permission,component,variables);
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
        return McNative.getInstance().getNetwork().isOnline();
    }

    @Override
    public NetworkIdentifier getIdentifier() {
        return BungeeCordProxyNetwork.SINGLE_PROXY_IDENTIFIER;
    }

    @Override
    public void sendMessage(String channel, Document request) {
        McNative.getInstance().getNetwork().getMessenger().sendQueryMessage(this,channel,request);
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String channel, Document request) {
        return  McNative.getInstance().getNetwork().getMessenger().sendQueryMessageAsync(this,channel,request);
    }
}
