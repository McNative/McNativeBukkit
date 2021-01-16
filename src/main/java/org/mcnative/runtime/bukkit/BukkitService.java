/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 11:47
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

package org.mcnative.runtime.bukkit;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.describer.VariableObjectToString;
import net.pretronic.libraries.plugin.service.ServicePriority;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mcnative.runtime.api.ServerPerformance;
import org.mcnative.runtime.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.bukkit.serviceprovider.economy.VaultEconomyProvider;
import org.mcnative.runtime.bukkit.serviceprovider.permission.VaultPermissionProvider;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.MinecraftServerType;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.protocol.packet.PacketManager;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.service.MinecraftService;
import org.mcnative.runtime.api.service.world.World;
import org.mcnative.runtime.api.service.world.WorldCreator;
import org.mcnative.runtime.common.protocol.DefaultPacketManager;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BukkitService implements MinecraftService, MinecraftServer, VariableObjectToString {

    private final PacketManager packetManager;
    private final BukkitPlayerManager playerManager;
    private final CommandManager commandManager;
    private final EventBus eventBus;

    private ChatChannel serverChat;
    private Tablist serverTablist;
    private ServerStatusResponse statusResponse;

    private final NetworkIdentifier fallbackIdentifier;
    private final ServerPerformance serverPerformance;

    protected BukkitService(CommandManager commandManager,BukkitPlayerManager playerManager, EventBus eventBus) {
        this.packetManager = new DefaultPacketManager();
        this.commandManager = commandManager;
        this.playerManager = playerManager;
        this.eventBus = eventBus;

        initVaultHook();

        fallbackIdentifier = loadReportingId();
        this.serverPerformance = new BukkitServerPerformance();
    }

    private NetworkIdentifier loadReportingId(){
        File file = new File("plugins/McNative/lib/runtime.dat");
        if(file.exists()){
            Document document = DocumentFileType.JSON.getReader().read(new File("plugins/McNative/lib/runtime.dat"));
            UUID uuid =  document.getObject("reportingId",UUID.class);
            if(uuid != null) return  new NetworkIdentifier(Bukkit.getName(),uuid);
        }
        UUID uuid = UUID.randomUUID();
        Document document = Document.newDocument();
        document.set("reportingId",uuid);
        DocumentFileType.JSON.getWriter().write(new File("plugins/McNative/lib/runtime.dat"),document,false);
        return  new NetworkIdentifier(Bukkit.getName(),uuid);
    }

    @Override
    public World getDefaultWorld() {
        return getWorld(McNative.getInstance(BukkitMcNative.class).getServerProperties().getString("level-name"));
    }

    @Override
    public World getWorld(String name) {
        return new BukkitWorld(Bukkit.getWorld(name)); //@Todo optimize with world pool
    }

    @Internal
    public World getMappedWorld(org.bukkit.World world) {
        return new BukkitWorld(world);
    }

    @Override
    public World loadWorld(String name) {
        File file = new File(name);
        if(file.exists()) {
            return new BukkitWorld(Bukkit.createWorld(new org.bukkit.WorldCreator(name)));
        }
        throw new IllegalArgumentException("World " + name + " doesn't exist. Try to generate it.");
    }

    @Override
    public void unloadWorld(World world, boolean save) {
        Bukkit.unloadWorld(((BukkitWorld)world).getOriginal(), save);
    }

    @Override
    public World createWorld(WorldCreator creator) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<ConnectedMinecraftPlayer> getConnectedPlayers() {
        return playerManager.getConnectedPlayers();
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(int id) {
        return playerManager.getConnectedPlayer(id);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(UUID uniqueId) {
        return playerManager.getConnectedPlayer(uniqueId);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(String name) {
        return playerManager.getConnectedPlayer(name);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(long xBoxId) {
        return playerManager.getConnectedPlayer(xBoxId);
    }

    @Override
    public PacketManager getPacketManager() {
        return packetManager;
    }

    @Override
    public void setStatus(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChatChannel getServerChat() {
        return serverChat;
    }

    @Override
    public void setServerChat(ChatChannel channel) {
        Validate.notNull(channel);
        this.serverChat = channel;
    }

    @Override
    public Tablist getServerTablist() {
        return serverTablist;
    }

    @Override
    public void setServerTablist(Tablist tablist) {
        Validate.notNull(tablist);
        this.serverTablist = tablist;
    }

    @Override
    public ServerStatusResponse getStatusResponse() {
        return statusResponse;
    }

    @Override
    public void setStatusResponse(ServerStatusResponse status) {
        this.statusResponse = status;
    }

    @Override
    public ServerPerformance getServerPerformance() {
        return this.serverPerformance;
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return McNative.getInstance().getPlatform().getProtocolVersion();
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(Bukkit.getIp(),Bukkit.getPort());
    }

    @Override
    public MinecraftServerType getType() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setType(MinecraftServerType type) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getPermission() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setPermission(String permission) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ServerStatusResponse ping() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<ServerStatusResponse> pingAsync() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendData(String channel, Document document) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendData(String channel, byte[] data) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendData(String channel, byte[] data, boolean queued) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getName() {
        if(McNative.getInstance().isNetworkAvailable()){
            try{
                return McNative.getInstance().getNetwork().getLocalIdentifier().getName();
            }catch (Exception ignored){}
        }
        return Bukkit.getName();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public String getGroup() {
       return getIdentifier().getGroup();
    }

    @Override
    public String getStatus() {
        return McNative.getInstance().isReady() ? "ONLINE" : "STARTING";
    }

    @Override
    public int getMaxPlayerCount() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        return Iterators.map(playerManager.getConnectedPlayers(), player -> player);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        return getConnectedPlayer(uniqueId);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        return getConnectedPlayer(name);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        return getConnectedPlayer(xBoxId);
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(component,variables);
        getConnectedPlayers().forEach(player -> player.sendMessage(component,variables));
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(permission,component,variables);
        getConnectedPlayers().forEach(player -> {
            if(player.hasPermission(permission)){
                player.sendMessage(component, variables);
            }
        });
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public NetworkIdentifier getIdentifier() {
        if(McNative.getInstance().isNetworkAvailable()){
            try{
                return McNative.getInstance().getNetwork().getLocalIdentifier();
            }catch (Exception ignored){}
        }
        return fallbackIdentifier;
    }

    @Override
    public void sendMessage(String channel, Document request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Document sendQueryMessage(String channel, Document request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String channel, Document request) {
        return null;
    }

    private void initVaultHook() {
        if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> economyService = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (economyService != null) {
                VaultEconomyProvider vaultEconomyProvider = new VaultEconomyProvider(economyService.getProvider());
                McNative.getInstance().getRegistry().registerService(McNative.getInstance(), EconomyProvider.class,
                        vaultEconomyProvider, ServicePriority.LOWEST);
            }

            RegisteredServiceProvider<Permission> permissionService = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            RegisteredServiceProvider<Chat> chatService = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
            if(permissionService != null || chatService != null) {
                VaultPermissionProvider vaultPermissionProvider = new VaultPermissionProvider(permissionService != null ? permissionService.getProvider() : null,
                        chatService != null ? chatService.getProvider() : null);
                McNative.getInstance().getRegistry().registerService(McNative.getInstance(), PermissionProvider.class,
                        vaultPermissionProvider, ServicePriority.LOWEST);
            }
        }
    }

    @Override
    public String toStringVariable() {
        return getName();
    }


}
