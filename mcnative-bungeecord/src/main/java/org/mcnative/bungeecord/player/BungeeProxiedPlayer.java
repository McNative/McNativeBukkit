/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 22.08.19, 18:57
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

package org.mcnative.bungeecord.player;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.config.ServerInfo;
import net.prematic.libraries.document.Document;
import org.mcnative.common.McNative;
import org.mcnative.common.player.*;
import org.mcnative.common.player.bossbar.BossBar;
import org.mcnative.common.player.data.MinecraftPlayerData;
import org.mcnative.common.player.profile.GameProfile;
import org.mcnative.common.player.receiver.ReceiverChannel;
import org.mcnative.common.player.scoreboard.BelowNameInfo;
import org.mcnative.common.player.scoreboard.Tablist;
import org.mcnative.common.player.scoreboard.sidebar.Sidebar;
import org.mcnative.common.player.sound.Instrument;
import org.mcnative.common.player.sound.Note;
import org.mcnative.common.player.sound.Sound;
import org.mcnative.common.player.sound.SoundCategory;
import org.mcnative.common.protocol.MinecraftProtocolVersion;
import org.mcnative.common.protocol.packet.MinecraftPacket;
import org.mcnative.common.protocol.support.DefaultProtocolChecker;
import org.mcnative.common.protocol.support.ProtocolCheck;
import org.mcnative.common.text.TextComponent;
import org.mcnative.common.text.outdated.MessageComponent;
import org.mcnative.common.text.variable.VariableSet;
import org.mcnative.proxy.ProxiedPlayer;
import org.mcnative.proxy.ProxyService;
import org.mcnative.proxy.server.MinecraftServer;
import org.mcnative.proxy.server.ServerConnectResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BungeeProxiedPlayer implements ProxiedPlayer {

    private final net.md_5.bungee.api.connection.ProxiedPlayer original;

    private final MinecraftPlayerData playerData;
    private final GameProfile gameProfile;

    public BungeeProxiedPlayer(net.md_5.bungee.api.connection.ProxiedPlayer original, MinecraftPlayerData playerData, GameProfile gameProfile) {
        this.original = original;
        this.playerData = playerData;
        this.gameProfile = gameProfile;
    }

    @Override
    public MinecraftServer getServer() {
        return null;
    }

    @Override
    public MinecraftServer getDefaultConnectServer() {
        return ProxyService.getInstance().getConnectHandler().getServer(this);
    }

    @Override
    public MinecraftServer getDefaultFallbackServer() {
        return ProxyService.getInstance().getFallbackHandler().getFallbackServer(this,getServer(),null);
    }

    @Override
    public void connect(MinecraftServer target) {
        if(target instanceof ServerInfo) original.connect((ServerConnectRequest) target);
        else{
            ServerInfo info = ProxyServer.getInstance().getServerInfo(target.getName());
            if(info != null) original.connect((ServerConnectRequest) target);
            else throw new IllegalArgumentException("The targeted server is not registered as a server.");
        }
    }

    @Override
    public CompletableFuture<ServerConnectResult> connectAsync(MinecraftServer target) {
        return null;
    }

    @Override
    public DeviceInfo getDevice() {
        return null;//Not supported on BungeeCord
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return MinecraftProtocolVersion.of(original.getPendingConnection().getVersion());
    }

    @Override
    public String getClientName() {
        return null;
    }

    @Override
    public boolean isOnlineMode() {
        return original.getPendingConnection().isOnlineMode();
    }

    @Override
    public PlayerSettings getSettings() {
        return null;
    }

    @Override
    public int getPing() {
        return original.getPing();
    }

    @Override
    public Sidebar getSidebar() {
        return null;
    }

    @Override
    public void setSidebar(Sidebar sidebar) {

    }

    @Override
    public Tablist getTablist() {
        return null;
    }

    @Override
    public void setTablist(Tablist tablist) {

    }

    @Override
    public BelowNameInfo getBelowNameInfo() {
        return null;
    }

    @Override
    public void setBelowNameInfo(BelowNameInfo info) {

    }

    @Override
    public void kick(String reason) {

    }

    @Override
    public void performCommand(String command) {

    }

    @Override
    public void chat(String message) {

    }

    @Override
    public Collection<ReceiverChannel> getChatChannels() {
        return null;
    }

    @Override
    public void addChatChannel(ReceiverChannel channel) {

    }

    @Override
    public void removeChatChannel(ReceiverChannel channel) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(MessageComponent... components) {

    }

    @Override
    public void sendMessage(MessageComponent component, VariableSet variables) {

    }

    @Override
    public void sendTitle(String title, String subTitle, int stayTime) {

    }

    @Override
    public void sendTitle(TextComponent title, MessageComponent subTitle, int stayTime) {

    }

    @Override
    public void sendTitle(Title title) {

    }

    @Override
    public void resetTitle() {

    }

    @Override
    public void sendActionbar(MessageComponent... message) {

    }

    @Override
    public void sendActionbar(long seconds, MessageComponent... message) {

    }

    @Override
    public Collection<BossBar> getActiveBossBars() {
        return null;
    }

    @Override
    public void addBossBar(BossBar bossBar) {

    }

    @Override
    public void removeBossBar(BossBar bossBar) {

    }

    @Override
    public void playNote(Instrument instrument, Note note) {

    }

    @Override
    public void playSound(Sound sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void stopSound(Sound sound) {

    }

    @Override
    public void stopSound(String sound, SoundCategory category) {

    }

    @Override
    public void setResourcePack(String url) {

    }

    @Override
    public void setResourcePack(String url, String hash) {

    }

    @Override
    public InetSocketAddress getAddress() {
        return original.getAddress();
    }

    @Override
    public boolean isConnected() {
        return original.isConnected();
    }

    @Override
    public void disconnect(String message) {

    }

    @Override
    public void disconnect(TextComponent... reason) {

    }

    @Override
    public void sendPacket(MinecraftPacket packet) {

    }

    @Override
    public void sendPacketAsync(MinecraftPacket packet) {

    }

    @Override
    public void sendData(String channel, byte[] output) {

    }

    @Override
    public InputStream sendDataQuery(String channel, byte[] output) {
        return null;
    }

    @Override
    public InputStream sendDataQuery(String channel, Consumer<OutputStream> output) {
        return null;
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public UUID getUniqueId() {
        return original.getUniqueId();
    }

    @Override
    public long getXBoxId() {
        return playerData.getXBoxId();
    }

    @Override
    public long getFirstPlayed() {
        return playerData.getFirstPlayed();
    }

    @Override
    public long getLastPlayed() {
        return playerData.getLastPlayed();
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }

    @Override
    public Document getProperties() {
        return playerData.getProperties();
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getDisplayName(MinecraftPlayer player) {
        return null;
    }

    @Override
    public PlayerDesign getDesign() {
        return null;
    }

    @Override
    public PlayerDesign getDesign(MinecraftPlayer player) {
        return null;
    }

    @Override
    public <T extends MinecraftPlayer> T getAs(Class<T> otherPlayerClass) {
        return (T) McNative.getInstance().getPlayerManager().translate(otherPlayerClass,this);
    }

    @Override
    public OnlineMinecraftPlayer getAsOnlinePlayer() {
        return this;
    }

    @Override
    public boolean isOnline() {
        return isConnected();
    }


    @Override
    public boolean isWhitelisted() {
        return McNative.getInstance().getWhitelistHandler().isWhitelisted(this);
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {
        McNative.getInstance().getWhitelistHandler().set(this,whitelisted);
    }

    @Override
    public boolean isOperator() {
        return McNative.getInstance().getPermissionHandler().isOperator(this);
    }

    @Override
    public void setOperator(boolean operator) {
        McNative.getInstance().getPermissionHandler().setOperator(this,operator);
    }

    @Override
    public Collection<String> getPermissions() {
        return McNative.getInstance().getPermissionHandler().getPermissions(this);
    }

    @Override
    public Collection<String> getAllPermissions() {
        return McNative.getInstance().getPermissionHandler().getAllPermissions(this);
    }

    @Override
    public Collection<String> getGroups() {
        return McNative.getInstance().getPermissionHandler().getGroups(this);
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return McNative.getInstance().getPermissionHandler().isPermissionSet(this,permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return McNative.getInstance().getPermissionHandler().hasPermission(this,permission);
    }

    @Override
    public void addPermission(String permission) {
        McNative.getInstance().getPermissionHandler().addPermission(this,permission);
    }

    @Override
    public void removePermission(String permission) {
        McNative.getInstance().getPermissionHandler().removePermission(this,permission);
    }

    @Override
    public boolean isBanned() {
        return McNative.getInstance().getPunishmentHandler().isBanned(this);
    }

    @Override
    public String getBanReason() {
        return McNative.getInstance().getPunishmentHandler().getBanReason(this);
    }

    @Override
    public void ban(String reason) {
        McNative.getInstance().getPunishmentHandler().ban(this,reason);
    }

    @Override
    public void ban(String reason, long time, TimeUnit unit) {
        McNative.getInstance().getPunishmentHandler().ban(this,reason,time,unit);
    }

    @Override
    public void unban() {
        McNative.getInstance().getPunishmentHandler().unban(this);
    }

    @Override
    public boolean isMuted() {
        return McNative.getInstance().getPunishmentHandler().isMuted(this);
    }

    @Override
    public String getMuteReason() {
        return McNative.getInstance().getPunishmentHandler().getMuteReason(this);
    }

    @Override
    public void mute(String reason) {
        McNative.getInstance().getPunishmentHandler().mute(this,reason);
    }

    @Override
    public void mute(String reason, long time, TimeUnit unit) {
        McNative.getInstance().getPunishmentHandler().mute(this,reason,time,unit);
    }

    @Override
    public void unmute() {
        McNative.getInstance().getPunishmentHandler().unmute(this);
    }

    @Override
    public void check(Consumer<ProtocolCheck> checker) {
        ProtocolCheck check = new DefaultProtocolChecker();
        checker.accept(check);
        check.process(getProtocolVersion());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}