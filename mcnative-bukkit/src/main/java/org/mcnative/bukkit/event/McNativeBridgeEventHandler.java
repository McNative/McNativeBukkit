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

package org.mcnative.bukkit.event;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.mcnative.bukkit.event.player.*;
import org.mcnative.bukkit.event.player.inventory.BukkitPlayerInventoryClickEvent;
import org.mcnative.bukkit.event.player.inventory.BukkitPlayerInventoryCloseEvent;
import org.mcnative.bukkit.event.player.inventory.BukkitPlayerInventoryDragEvent;
import org.mcnative.bukkit.event.player.inventory.BukkitPlayerInventoryOpenEvent;
import org.mcnative.bukkit.player.BukkitPendingConnection;
import org.mcnative.bukkit.player.BukkitPlayer;
import org.mcnative.bukkit.player.BukkitPlayerManager;
import org.mcnative.bukkit.player.connection.BukkitChannelInjector;
import org.mcnative.bukkit.player.connection.ChannelConnection;
import org.mcnative.bukkit.plugin.command.BukkitCommandManager;
import org.mcnative.bukkit.plugin.event.BukkitEventBus;
import org.mcnative.bukkit.plugin.event.McNativeHandlerList;
import org.mcnative.bukkit.world.BukkitWorld;
import org.mcnative.common.McNative;
import org.mcnative.common.connection.ConnectionState;
import org.mcnative.common.event.player.MinecraftPlayerLogoutEvent;
import org.mcnative.common.event.player.login.MinecraftPlayerLoginEvent;
import org.mcnative.common.event.player.login.MinecraftPlayerPostLoginEvent;
import org.mcnative.common.player.data.MinecraftPlayerData;
import org.mcnative.common.player.data.PlayerDataProvider;
import org.mcnative.service.event.player.MinecraftPlayerJoinEvent;
import org.mcnative.service.event.player.MinecraftPlayerWorldChangedEvent;
import org.mcnative.service.event.player.inventory.MinecraftPlayerInventoryClickEvent;
import org.mcnative.service.event.player.inventory.MinecraftPlayerInventoryCloseEvent;
import org.mcnative.service.event.player.inventory.MinecraftPlayerInventoryDragEvent;
import org.mcnative.service.event.player.inventory.MinecraftPlayerInventoryOpenEvent;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class McNativeBridgeEventHandler {

    private final BukkitEventBus eventBus;
    private final BukkitCommandManager commandManager;
    private final BukkitChannelInjector injector;
    private final BukkitPlayerManager playerManager;
    private final Map<UUID,BukkitPendingConnection> pendingConnections;

    public McNativeBridgeEventHandler(BukkitChannelInjector injector, BukkitEventBus eventBus, BukkitPlayerManager playerManager) {
        this.injector = injector;
        this.eventBus = eventBus;
        this.playerManager = playerManager;
        this.commandManager = null;

        this.pendingConnections = new ConcurrentHashMap<>();

        setup();
    }

    private void setup(){
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
        eventBus.registerMappedClass(MinecraftPlayerLogoutEvent.class, PlayerQuitEvent.class);
        eventBus.registerManagedEvent(PlayerQuitEvent.class, this::handleLogoutEvent);


        //Inventory click
        eventBus.registerMappedClass(MinecraftPlayerInventoryClickEvent.class, InventoryClickEvent.class);
        eventBus.registerManagedEvent(InventoryClickEvent.class, this::handleInventoryClick);

        //Inventory close
        eventBus.registerMappedClass(MinecraftPlayerInventoryCloseEvent.class, InventoryCloseEvent.class);
        eventBus.registerManagedEvent(InventoryCloseEvent.class, this::handleInventoryClose);

        //Inventory drag
        eventBus.registerMappedClass(MinecraftPlayerInventoryDragEvent.class, InventoryDragEvent.class);
        eventBus.registerManagedEvent(InventoryDragEvent.class, this::handleInventoryDrag);

        //Inventory open
        eventBus.registerMappedClass(MinecraftPlayerInventoryOpenEvent.class, InventoryOpenEvent.class);
        eventBus.registerManagedEvent(InventoryOpenEvent.class, this::handleInventoryOpen);
    }

    private void handleServerListPing(McNativeHandlerList handler, ServerListPingEvent event){

    }

    private void handlePreLoginEvent(McNativeHandlerList handler, AsyncPlayerPreLoginEvent event) {
        if(!McNative.getInstance().isReady()){
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("Server is still starting");
            return;
        }
        ChannelConnection connection0 = injector.findConnection(event.getUniqueId());
        if(connection0 == null){
            event.setKickMessage("§cAn error occurred.");//@Todo configurable error message
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
            event.setKickMessage("§cAn error occurred.");//@Todo configurable error message
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
            connection.setState(ConnectionState.GAME);
            connection.setPlayer(player);

            MinecraftPlayerPostLoginEvent postLoginEvent = new BukkitPostLoginEvent(player);
            McNative.getInstance().getLocal().getEventBus().callEvent(MinecraftPlayerPostLoginEvent.class,postLoginEvent);

            playerManager.registerPlayer(player);
        }
    }

    private void handleJoinEvent(McNativeHandlerList handler, PlayerJoinEvent event){
        if(!McNative.getInstance().isReady()){
            event.getPlayer().kickPlayer("Server is still starting");
            return;
        }
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
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
    }

    private void handleLogoutEvent(McNativeHandlerList handler, PlayerQuitEvent event){
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        playerManager.unregisterPlayer(event.getPlayer().getUniqueId());
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
    }

    private void handleWorldChangedEvent(McNativeHandlerList handler,PlayerChangedWorldEvent event){
        BukkitPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        player.setWorld(new BukkitWorld(event.getPlayer().getWorld()));//@Todo get world from pool
        BukkitWorld from = new BukkitWorld(event.getFrom());
        MinecraftPlayerWorldChangedEvent mcnativeEvent = new BukkitWorldChangedEvent(player,from);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleWorldChangedEvent(McNativeHandlerList handler,PlayerCommandPreprocessEvent event){
        handler.callEvents(event);

        String command = event.getMessage();

        //if (commandManager.dispatchBukkitCommand(event.getPlayer(),event.getMessage())) return;

        if(commandManager.getNotFoundHandler() != null){
       //     commandManager.getNotFoundHandler().handle();
        }else{

        }
        //@Todo implement paper spigot command


        event.setCancelled(true);
    }

    private void handleServerReload(McNativeHandlerList handler, ServerLoadEvent event){
        if(event.getType() == ServerLoadEvent.LoadType.RELOAD){

        }else{
            handler.callEvents(event);
        }
    }

    private void handleInventoryClick(McNativeHandlerList handler, InventoryClickEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getWhoClicked());
        MinecraftPlayerInventoryClickEvent mcnativeEvent = new BukkitPlayerInventoryClickEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryClose(McNativeHandlerList handler, InventoryCloseEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getPlayer());
        MinecraftPlayerInventoryCloseEvent mcnativeEvent = new BukkitPlayerInventoryCloseEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryDrag(McNativeHandlerList handler, InventoryDragEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getWhoClicked());
        MinecraftPlayerInventoryDragEvent mcnativeEvent = new BukkitPlayerInventoryDragEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }

    private void handleInventoryOpen(McNativeHandlerList handler, InventoryOpenEvent event) {
        BukkitPlayer player = playerManager.getMappedPlayer((org.bukkit.entity.Player) event.getPlayer());
        MinecraftPlayerInventoryOpenEvent mcnativeEvent = new BukkitPlayerInventoryOpenEvent(event, player);
        handler.callEvents(event,mcnativeEvent);
    }
}
