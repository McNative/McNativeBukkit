/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 13:56
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

package org.mcnative.runtime.bukkit.event;

import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.connection.ConnectionState;
import org.mcnative.runtime.api.event.player.MinecraftPlayerChatEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerCommandPreprocessEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerLogoutEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerLoginConfirmEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerLoginEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPostLoginEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.service.event.player.*;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryClickEvent;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryCloseEvent;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryDragEvent;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryOpenEvent;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.bukkit.McNativeBukkitConfiguration;
import org.mcnative.runtime.bukkit.event.player.*;
import org.mcnative.runtime.bukkit.event.player.inventory.BukkitPlayerInventoryClickEvent;
import org.mcnative.runtime.bukkit.event.player.inventory.BukkitPlayerInventoryCloseEvent;
import org.mcnative.runtime.bukkit.event.player.inventory.BukkitPlayerInventoryDragEvent;
import org.mcnative.runtime.bukkit.event.player.inventory.BukkitPlayerInventoryOpenEvent;
import org.mcnative.runtime.bukkit.player.BukkitPendingConnection;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.bukkit.player.PlayerTextInput;
import org.mcnative.runtime.bukkit.player.connection.BukkitChannelInjector;
import org.mcnative.runtime.bukkit.player.connection.ChannelConnection;
import org.mcnative.runtime.bukkit.plugin.event.BukkitEventBus;
import org.mcnative.runtime.bukkit.plugin.event.McNativeHandlerList;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.common.event.player.DefaultMinecraftPlayerLoginConfirmEvent;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class McNativeBridgeEventHandler {

    private final BukkitEventBus eventBus;
    private final BukkitChannelInjector injector;
    private final BukkitPlayerManager playerManager;
    private final Map<UUID,BukkitPendingConnection> pendingConnections;
    private final Map<Long,BukkitPlayer> disconnectingPlayers;

    //For preventing McNative mapping error.
    private boolean firstPlayerConnected;

    //For preventing player interact spigot bug, where action RIGHT_CLICK_BLOCK is called twice
    private final Map<UUID, Long> playerInteractRightClick;

    public McNativeBridgeEventHandler(BukkitChannelInjector injector, BukkitEventBus eventBus, BukkitPlayerManager playerManager) {
        this.injector = injector;
        this.eventBus = eventBus;
        this.playerManager = playerManager;
        this.pendingConnections = new ConcurrentHashMap<>();
        this.disconnectingPlayers = new ConcurrentHashMap<>();
        this.playerInteractRightClick = new ConcurrentHashMap<>();
        firstPlayerConnected = false;
        setup();
        McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM).async()
                .delay(5,TimeUnit.SECONDS).interval(200,TimeUnit.MILLISECONDS).execute(() -> {
                    long timeout = System.currentTimeMillis()+500;
                    disconnectingPlayers.keySet().removeIf(time -> time > timeout);
                });
    }

    private void setup(){

        /* Lifecycle */

        //Ping
        eventBus.registerMappedClass(org.mcnative.runtime.api.event.service.local.LocalServicePingEvent.class, ServerListPingEvent.class);

        //Pre Login
        eventBus.registerMappedClass(MinecraftPlayerLoginEvent.class, AsyncPlayerPreLoginEvent.class);
        eventBus.registerManagedEvent(AsyncPlayerPreLoginEvent.class, this::handlePreLoginEvent);

        //Login
        eventBus.registerMappedClass(MinecraftPlayerLoginEvent.class, PlayerLoginEvent.class);
        eventBus.registerManagedEvent(PlayerLoginEvent.class, this::handleLoginEvent);

        //Join
        eventBus.registerMappedClass(MinecraftPlayerJoinEvent.class, PlayerJoinEvent.class);
        eventBus.registerManagedEvent(PlayerJoinEvent.class, this::handleJoinEvent);

        //World Changed
        eventBus.registerMappedClass(MinecraftPlayerWorldChangedEvent.class, PlayerChangedWorldEvent.class);
        eventBus.registerManagedEvent(PlayerChangedWorldEvent.class, this::handleWorldChangedEvent);

        //Logout
        eventBus.registerMappedClass(MinecraftPlayerQuitEvent.class, PlayerQuitEvent.class);
        eventBus.registerManagedEvent(PlayerQuitEvent.class, this::handleLogoutEvent);


        /* Game */

        eventBus.registerMappedClass(MinecraftPlayerChatEvent.class, AsyncPlayerChatEvent.class);
        eventBus.registerManagedEvent(AsyncPlayerChatEvent.class, this::handleChatEvent);

        eventBus.registerMappedClass(MinecraftPlayerCommandPreprocessEvent.class, PlayerCommandPreprocessEvent.class);
        eventBus.registerManagedEvent(PlayerCommandPreprocessEvent.class, this::handleCommandEvent);

        /* Inventory */

        //Inventory click
        eventBus.registerMappedClass(MinecraftPlayerInventoryClickEvent.class, InventoryClickEvent.class);
        eventBus.registerManagedEvent(InventoryClickEvent.class, this::handleInventoryClickEvent);

        //Inventory close
        eventBus.registerMappedClass(MinecraftPlayerInventoryCloseEvent.class, InventoryCloseEvent.class);
        eventBus.registerManagedEvent(InventoryCloseEvent.class, this::handleInventoryCloseEvent);

        //Inventory drag
        eventBus.registerMappedClass(MinecraftPlayerInventoryDragEvent.class, InventoryDragEvent.class);
        eventBus.registerManagedEvent(InventoryDragEvent.class, this::handleInventoryDragEvent);

        //Inventory open
        eventBus.registerMappedClass(MinecraftPlayerInventoryOpenEvent.class, InventoryOpenEvent.class);
        eventBus.registerManagedEvent(InventoryOpenEvent.class, this::handleInventoryOpenEvent);

        //Interact
        eventBus.registerMappedClass(MinecraftPlayerInteractEvent.class, PlayerInteractEvent.class);
        eventBus.registerManagedEvent(PlayerInteractEvent.class, this::handlePlayerInteractEvent);

        //Drop item
        eventBus.registerMappedClass(MinecraftPlayerDropItemEvent.class, PlayerDropItemEvent.class);
        eventBus.registerManagedEvent(PlayerDropItemEvent.class, this::handlePlayerDropItemEvent);
    }

    private void handlePreLoginEvent(McNativeHandlerList handler, AsyncPlayerPreLoginEvent event) {
        if(!McNative.getInstance().isReady()){
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("Server is still starting");
            return;
        }
        ChannelConnection connection0 = injector.findConnection(event.getUniqueId());
        if(connection0 == null){
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("§cAn error occurred (Pre Login).");//@Todo configurable error message
            return;
        }

        BukkitPendingConnection connection = new BukkitPendingConnection(connection0.getChannel()
                ,connection0.getGameProfile(),(InetSocketAddress) connection0.getChannel().remoteAddress()
                ,new InetSocketAddress(connection0.getHostname(),connection0.getPort())
                ,connection0.getProtocolVersion());

        BukkitPendingLoginEvent mcnativeEvent = new BukkitPendingLoginEvent(event,connection);
        handler.callEvents(mcnativeEvent,event);

        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED){
            if(mcnativeEvent.hasMessageChanged()){
                connection.disconnect(mcnativeEvent.getCancelReason(),mcnativeEvent.getCancelReasonVariables());
            }
        }else{
            this.pendingConnections.put(connection.getUniqueId(),connection);
        }
    }

    private void handleLoginEvent(McNativeHandlerList handler, PlayerLoginEvent event) {
        if(!McNative.getInstance().isReady()){
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("Server is still starting");
            return;
        }
        BukkitPendingConnection connection = this.pendingConnections.remove(event.getPlayer().getUniqueId());
        if(connection == null){
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("§cAn error occurred (Login).");//@Todo configurable error message
            return;
        }

        PlayerDataProvider dataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
        MinecraftPlayerData data = dataProvider.getPlayerData(event.getPlayer().getUniqueId());
        if(data == null){
            long now = System.currentTimeMillis();
            data = dataProvider.createPlayerData(
                    event.getPlayer().getName()
                    ,event.getPlayer().getUniqueId()
                    ,-1,now,now
                    ,connection.getGameProfile());
        }else data.updateLoginInformation(connection.getName(),connection.getGameProfile(),System.currentTimeMillis());
        BukkitPlayer player = new BukkitPlayer(event.getPlayer(),connection,data);

        BukkitLoginEvent mcnativeEvent = new BukkitLoginEvent(event,connection,player);
        handler.callEvents(mcnativeEvent,event);

        if(event.getResult() != PlayerLoginEvent.Result.ALLOWED){
            if(mcnativeEvent.hasMessageChanged()){
                connection.disconnect(mcnativeEvent.getCancelReason(),mcnativeEvent.getCancelReasonVariables());
            }
        }else{
            ChatChannel serverChat = McNative.getInstance().getLocal().getServerChat();
            if(serverChat != null){
                serverChat.addPlayer(player);
                player.setPrimaryChatChannel(serverChat);
            }

            connection.setState(ConnectionState.GAME);
            connection.setPlayer(player);

            playerManager.registerPlayer(player);
            firstPlayerConnected = true;

            if(McNativeBukkitConfiguration.PLAYER_DISPLAY_APPLY_ON_BUKKIT){
                event.getPlayer().setDisplayName(player.getDisplayName());
            }
        }
    }

    private void handleJoinEvent(McNativeHandlerList handler, PlayerJoinEvent event){
        if(!McNative.getInstance().isReady() || !firstPlayerConnected){
            event.getPlayer().kickPlayer("Server is still starting");
            return;
        }
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());

        MinecraftPlayerPostLoginEvent postLoginEvent = new BukkitPostLoginEvent(player);
        McNative.getInstance().getLocal().getEventBus().callEvent(MinecraftPlayerPostLoginEvent.class,postLoginEvent);

        Tablist serverTablist = McNative.getInstance().getLocal().getServerTablist();
        if(serverTablist != null){
            serverTablist.addEntry(player);
            player.setTablist(serverTablist);
            if(McNativeBukkitConfiguration.PLAYER_TABLIST_DELAY_ENABLED){//workaround to fix some incompatibilities with other plugins
                McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                        .delay(McNativeBukkitConfiguration.PLAYER_TABLIST_DELAY_MILLISECONDS,TimeUnit.MILLISECONDS)
                        .execute(() -> serverTablist.updateEntries(player));
            }
        }

        BukkitJoinEvent mcnativeEvent = new BukkitJoinEvent(event,player);

        player.setJoining(true);
        handler.callEvents(mcnativeEvent,event);
        player.setJoining(false);

        if(mcnativeEvent.hasMessageChanged()){
            event.setJoinMessage(null);
            McNative.getInstance().getLocal().broadcast(mcnativeEvent.getJoinMessage(),mcnativeEvent.getJoinMessageVariables());
            //@Todo implement chat channel
        }

        if(mcnativeEvent.hasLocationChanged()){
            player.teleport(mcnativeEvent.getSpawnLocation());
        }

        McNative.getInstance().getScheduler().createTask(McNative.getInstance())
                .delay(500, TimeUnit.MILLISECONDS).execute(()-> {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if(onlinePlayer.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                            this.eventBus.callEvent(MinecraftPlayerLoginConfirmEvent.class,
                                    new DefaultMinecraftPlayerLoginConfirmEvent(player));
                        }
                    }
                });
    }

    private void handleLogoutEvent(McNativeHandlerList handler, PlayerQuitEvent event){
        if(!McNative.getInstance().isReady() || !firstPlayerConnected) return;
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        player.handleLogout();

        BukkitQuitEvent mcnativeEvent = new BukkitQuitEvent(event,player);
        handler.callEvents(mcnativeEvent,event);

        if(mcnativeEvent.hasMessageChanged()){
            event.setQuitMessage(null);
            McNative.getInstance().getLocal().broadcast(mcnativeEvent.getQuietMessage(),mcnativeEvent.getQuietMessageVariables());
            //@Todo implement chat channel
        }
        MinecraftPlayerLogoutEvent logoutEvent = new BukkitPlayerLogoutEvent(player);
        McNative.getInstance().getLocal().getEventBus().callEvent(MinecraftPlayerLogoutEvent.class,logoutEvent);

        ChatChannel serverChat = McNative.getInstance().getLocal().getServerChat();
        if(serverChat != null) serverChat.removePlayer(player);

        player.setTablist(null);

        Tablist serverTablist = McNative.getInstance().getLocal().getServerTablist();
        if(serverTablist != null) serverTablist.removeEntry(player);

        this.disconnectingPlayers.put(System.currentTimeMillis(),player);
        playerManager.unregisterPlayer(event.getPlayer().getUniqueId());
    }

    private void handleWorldChangedEvent(McNativeHandlerList handler,PlayerChangedWorldEvent event){
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        player.setWorld(new BukkitWorld(event.getPlayer().getWorld()));//@Todo get world from pool
        BukkitWorld from = new BukkitWorld(event.getFrom());
        MinecraftPlayerWorldChangedEvent mcnativeEvent = new BukkitWorldChangedEvent(player,from);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleChatEvent(McNativeHandlerList handler, AsyncPlayerChatEvent event){
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());

        PlayerTextInput<?> input = player.getCurrentInput();
        if(input != null) {
            event.setCancelled(true);
            MessageComponent<?> error = input.validate(event.getMessage());
            if(error != null) {
                player.sendMessage(error, VariableSet.create()
                        .addDescribed("player", player)
                        .add("value", event.getMessage()));
                return;
            }
            input.callCallback(event.getMessage());
            player.finishInput();
            return;
        }

        BukkitChatEvent mcnativeEvent = new BukkitChatEvent(event,player);
        handler.callEvents(mcnativeEvent,event);
        if(event.isCancelled()) return;
        if(mcnativeEvent.getChannel() != null){
            event.setCancelled(true);
            if(mcnativeEvent.getOutputMessage() == null){
                mcnativeEvent.getChannel().chat(player,mcnativeEvent.getMessage(),mcnativeEvent.getOutputVariables());
            }else{
                mcnativeEvent.getChannel().sendMessage(mcnativeEvent.getOutputMessage(),mcnativeEvent.getOutputVariables());
            }
            McNative.getInstance().getLogger().info("["+mcnativeEvent.getChannel().getName()+"] "+player.getName()+": "+event.getMessage());
        }
    }

    private void handleCommandEvent(McNativeHandlerList handler, PlayerCommandPreprocessEvent event){
        ConnectedMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftPlayerCommandPreprocessEvent mcnativeEvent = new BukkitMinecraftPlayerCommandPreprocessEvent(event,player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryClickEvent(McNativeHandlerList handler, InventoryClickEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getWhoClicked());
        MinecraftPlayerInventoryClickEvent mcnativeEvent = new BukkitPlayerInventoryClickEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryCloseEvent(McNativeHandlerList handler, InventoryCloseEvent event) {
        if(!McNative.getInstance().isReady() || !firstPlayerConnected) return;
        BukkitPlayer player = Iterators.findOne(this.disconnectingPlayers.values(), player1 -> player1.getUniqueId().equals(event.getPlayer().getUniqueId()));
        if(player == null) player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getPlayer());
        MinecraftPlayerInventoryCloseEvent mcnativeEvent = new BukkitPlayerInventoryCloseEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryDragEvent(McNativeHandlerList handler, InventoryDragEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getWhoClicked());
        MinecraftPlayerInventoryDragEvent mcnativeEvent = new BukkitPlayerInventoryDragEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryOpenEvent(McNativeHandlerList handler, InventoryOpenEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getPlayer());
        MinecraftPlayerInventoryOpenEvent mcnativeEvent = new BukkitPlayerInventoryOpenEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handlePlayerInteractEvent(McNativeHandlerList handler, PlayerInteractEvent event) {
        /*
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            Long lastInteract = this.playerInteractRightClick.get(event.getPlayer().getUniqueId());
            if(lastInteract == null) {
                this.playerInteractRightClick.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
            } else {
                this.playerInteractRightClick.remove(event.getPlayer().getUniqueId());
                if((lastInteract+500) >System.currentTimeMillis()) return;
            }
        }
         */
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftPlayerInteractEvent mcnativeEvent = new BukkitMinecraftPlayerInteractEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handlePlayerDropItemEvent(McNativeHandlerList handler, PlayerDropItemEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftPlayerDropItemEvent mcnativeEvent = new BukkitMinecraftPlayerDropItemEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }
}
