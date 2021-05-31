/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.05.20, 20:42
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

import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ProxyServer;
import org.mcnative.runtime.api.network.component.server.ServerConnectReason;
import org.mcnative.runtime.api.network.component.server.ServerConnectResult;
import org.mcnative.runtime.api.player.DeviceInfo;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.Title;
import org.mcnative.runtime.api.player.chat.ChatPosition;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.sound.SoundCategory;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacket;
import org.mcnative.runtime.api.stream.StreamOptional;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.network.integrations.McNativePlayerExecutor;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BungeeCordOnlinePlayer extends OfflineMinecraftPlayer implements OnlineMinecraftPlayer {

    private final UUID uniqueId;
    private final String name;
    private final InetSocketAddress address;
    private final boolean onlineMode;
    private MinecraftServer server;

    public BungeeCordOnlinePlayer(MinecraftPlayerData data,UUID uniqueId,String name,InetSocketAddress address,boolean onlineMode,MinecraftServer server) {
        super(data);
        this.uniqueId = uniqueId;
        this.name = name;
        this.address = address;
        this.server = server;
        this.onlineMode = onlineMode;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public OnlineMinecraftPlayer getAsOnlinePlayer() {
        return this;
    }

    @Override
    public boolean isOnline() {
        return onlineMode;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public DeviceInfo getDevice() {
        return DeviceInfo.JAVA;//Currently only java clients supported
    }

    @Override
    public boolean isOnlineMode() {
        return false;
    }

    @Override
    public int getPing() {
        return McNativePlayerExecutor.getPing(uniqueId);
    }

    @Override
    public CompletableFuture<Integer> getPingAsync() {
        return McNativePlayerExecutor.getPingAsync(uniqueId);
    }

    @Override
    public ProxyServer getProxy() {//Only one
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Override
    public MinecraftServer getServer() {//Need update
        return server;
    }

    @Override
    public void connect(MinecraftServer target, ServerConnectReason reason) {
        McNativePlayerExecutor.connect(uniqueId,target,reason);
    }

    @Override
    public CompletableFuture<ServerConnectResult> connectAsync(MinecraftServer target, ServerConnectReason reason) {
        return McNativePlayerExecutor.connectAsync(uniqueId,target,reason);
    }

    @Override
    public void kick(MessageComponent<?> message, VariableSet variables) {
        McNativePlayerExecutor.kick(uniqueId,message,variables);
    }

    @Override
    public void performCommand(String command) {
        McNativePlayerExecutor.performCommand(uniqueId,command);
    }

    @Override
    public void chat(String message) {
        McNativePlayerExecutor.chat(uniqueId,message);
    }

    @Override
    public void sendMessage(ChatPosition position, MessageComponent<?> component, VariableSet variables) {
        McNativePlayerExecutor.sendMessage(uniqueId,position,component,variables);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables) {
        sendActionbar(message,variables,-1);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables, long staySeconds) {
        McNativePlayerExecutor.sendActionbar(uniqueId,message,variables,staySeconds);
    }

    @Override
    public void sendTitle(Title title) {
        McNativePlayerExecutor.sendTitle(uniqueId,title);
    }

    @Override
    public void resetTitle() {
        McNativePlayerExecutor.resetTitle(uniqueId);
    }

    @Override
    public void sendPacket(MinecraftPacket packet) {
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Override
    public void playSound(String s, SoundCategory soundCategory, float v, float v1) {
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Override
    public void stopSound() {
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Override
    public void stopSound(String s) {
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Override
    public void stopSound(SoundCategory soundCategory) {
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Override
    public void stopSound(String s, SoundCategory soundCategory) {
        throw new UnsupportedOperationException("Currently not supported, implementation in progress");
    }

    @Internal
    public void setServer(MinecraftServer server){
        Validate.notNull(server);
        this.server = server;
    }
}
