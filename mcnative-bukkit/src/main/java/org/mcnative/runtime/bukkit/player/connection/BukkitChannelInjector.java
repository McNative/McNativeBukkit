/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 14.02.20, 22:49
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

package org.mcnative.runtime.bukkit.player.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import net.pretronic.libraries.logging.Debug;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.profile.GameProfile;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BukkitChannelInjector {

    @SuppressWarnings("unchecked")
    private final static Class<? extends ChannelInitializer<?>> PACKET_HANDLER_CLASS = (Class<? extends ChannelInitializer<?>>) BukkitReflectionUtil.getMNSClass("NetworkManager");
    private final static Class<?> GAME_PROFILE_CLASS = BukkitReflectionUtil.getClass("com.mojang.authlib.GameProfile");
    private final static Class<?> LOGIN_LISTENER_CLASS = BukkitReflectionUtil.getMNSClass("LoginListener");
    private final static String PS_LOGIN_LISTENER_CLASS = "protocolsupport.zplatform.impl.spigot.network.handler.SpigotLoginListener";
    private final static String PS_CONNECTION_CLASS = "protocolsupport.api.Connection";
    private final static String PS_PROFILE_CLASS = "protocolsupport.protocol.utils.authlib.LoginProfile";

    private final static Field PACKET_LISTENER_FIELD = ReflectionUtil.findFieldBySimpleName(PACKET_HANDLER_CLASS,"PacketListener");
    private final static Field GAME_PROFILE_FIELD = ReflectionUtil.findFieldByType(LOGIN_LISTENER_CLASS,GAME_PROFILE_CLASS);
    private final static Field UUID_GAME_PROFILE_FIELD = ReflectionUtil.getField(GAME_PROFILE_CLASS,"id");
    private final static Field NAME_GAME_PROFILE_FIELD = ReflectionUtil.getField(GAME_PROFILE_CLASS,"name");

    private final static Field PS_CONNECTION_FIELD;
    private final static Field PS_PROFILE_FIELD;
    private final static Field PS_UUID_GAME_PROFILE_FIELD;
    private final static Field PS_NAME_GAME_PROFILE_FIELD;

    static{
        PACKET_LISTENER_FIELD.setAccessible(true);
        GAME_PROFILE_FIELD.setAccessible(true);
        UUID_GAME_PROFILE_FIELD.setAccessible(true);
        NAME_GAME_PROFILE_FIELD.setAccessible(true);

        if(McNativeHandshakeDecoder.PROTOCOL_SUPPORT){
            try {
                PS_CONNECTION_FIELD = ReflectionUtil.getField(Class.forName(PS_LOGIN_LISTENER_CLASS).getSuperclass(),"connection");
                PS_PROFILE_FIELD = ReflectionUtil.getField(Class.forName(PS_CONNECTION_CLASS),"profile");
                PS_UUID_GAME_PROFILE_FIELD = ReflectionUtil.getField(Class.forName(PS_PROFILE_CLASS),"uuid");
                PS_NAME_GAME_PROFILE_FIELD = ReflectionUtil.getField(Class.forName(PS_PROFILE_CLASS),"name");

                PS_CONNECTION_FIELD.setAccessible(true);
                PS_PROFILE_FIELD.setAccessible(true);
                PS_UUID_GAME_PROFILE_FIELD.setAccessible(true);
                PS_NAME_GAME_PROFILE_FIELD.setAccessible(true);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
            PS_CONNECTION_FIELD = null;
            PS_PROFILE_FIELD = null;
            PS_UUID_GAME_PROFILE_FIELD = null;
            PS_NAME_GAME_PROFILE_FIELD = null;
        }
    }

    private final Collection<ChannelConnection> handshakingConnections;
    private final Map<ChannelHandler,ChannelInitializer<?>> injectedHandlers;

    private Field channelFutureListField;
    private ChannelFutureWrapperList channelFutureWrapperList;

    public BukkitChannelInjector() {
        this.handshakingConnections = ConcurrentHashMap.newKeySet();
        this.injectedHandlers = new HashMap<>();
    }

    public ChannelConnection findConnection(UUID uniqueId){
        try{
            Iterator<ChannelConnection> iterator = handshakingConnections.iterator();
            while (iterator.hasNext()){
                ChannelConnection connection = iterator.next();
                GameProfile gameProfile = connection.getGameProfile();
                if(gameProfile == null){
                    Object packetListener = PACKET_LISTENER_FIELD.get(connection.getNetworkManager());
                    if(packetListener != null && packetListener.getClass().equals(LOGIN_LISTENER_CLASS)){
                        Object profile = GAME_PROFILE_FIELD.get(packetListener);
                        gameProfile = extractGameProfile(profile);
                        connection.setGameProfile(gameProfile);
                    }else if(packetListener != null && packetListener.getClass().getName().equals(PS_LOGIN_LISTENER_CLASS)){
                        Object psConnection = PS_CONNECTION_FIELD.get(packetListener);
                        Object profile = PS_PROFILE_FIELD.get(psConnection);
                        gameProfile = extractPsGameProfile(profile);
                        connection.setGameProfile(gameProfile);
                    }else continue;
                }
                if(gameProfile.getUniqueId().equals(uniqueId)){
                    connection.unregister();
                    iterator.remove();
                    return connection;
                }
            }
            return null;
        }catch (Exception exception){
            exception.printStackTrace();
            throw new UnsupportedOperationException("McNative is not able to extract profile information of connecting channel",exception);
        }
    }

    protected void registerConnection(ChannelConnection connection){
        this.handshakingConnections.add(connection);
    }

    protected void unregisterConnection(Channel channel){
        Iterators.removeOne(this.handshakingConnections, channelConnection -> channelConnection.getChannel().equals(channel));
    }

    //Code optimized from via version (https://github.com/ViaVersion/ViaVersion)

    public void injectChannelInitializer(Consumer<Boolean> after){
        injectChannelInitializer(after,0);
    }

    @SuppressWarnings("unchecked")
    private void injectChannelInitializer(Consumer<Boolean> after, int count){
        try{
            Object connection = BukkitReflectionUtil.getServerConnection();
            if(connection == null){
                if(count < 3){
                    McNative.getInstance().getLogger().error(McNative.CONSOLE_PREFIX+"Could not get server connection, trying again in a view seconds");
                    McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                            .delay(1500, TimeUnit.MILLISECONDS)
                            .execute(() -> injectChannelInitializer(after,count+1));
                }else{
                    McNative.getInstance().getLogger().error(McNative.CONSOLE_PREFIX+"Could not get server connection, please report this issue to the McNative developer team.");
                    after.accept(false);
                }
                return;
            }
            for (Field field : connection.getClass().getDeclaredFields()) {
                if(field.getType().equals(List.class)){
                    if(((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0].equals(ChannelFuture.class)){
                        field.setAccessible(true);
                        Object list = field.get(connection);
                        McNative.getInstance().getLogger().info(McNative.CONSOLE_PREFIX+"Overwriting channel future list "+list);
                        ChannelFutureWrapperList wrapper = new ChannelFutureWrapperList(this,(List<ChannelFuture>) list);
                        field.set(connection,wrapper);
                        this.channelFutureWrapperList = wrapper;
                        this.channelFutureListField = field;
                        after.accept(true);
                        McNative.getInstance().getLogger().info(McNative.CONSOLE_PREFIX+"McNative has overwritten the channel initializer.");
                        return;
                    }
                }
            }
            McNative.getInstance().getLogger().error(McNative.CONSOLE_PREFIX+"Could not override the channel future list in the server connection, please report this issue to the McNative developer team");
            after.accept(false);
        }catch (Exception e){
            throw new ReflectException(e);
        }
    }

    //Code optimized from via version (https://github.com/ViaVersion/ViaVersion)

    @SuppressWarnings("unchecked")
    protected void injectChannelFuture(ChannelFuture future){
        try {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler oldHandler = null;
            ChannelInitializer<SocketChannel> oldInitializer = null;

            //Search the best handler
            for (String name : names) {
                ChannelHandler handler = future.channel().pipeline().get(name);
                Debug.print("[McNative] (Channel-Injector) Found handler "+name+": "+(handler == null ? "null" : handler.getClass()));
                if(handler != null){
                    try{
                        Field field = ReflectionUtil.getField(handler.getClass(), "childHandler");
                        if(field != null && field.getType().equals(ChannelHandler.class)){
                            field.setAccessible(true);
                            oldHandler = handler;
                            oldInitializer = (ChannelInitializer<SocketChannel>) field.get(handler);
                        }
                    }catch (ReflectException ignored){}
                }
            }

            if(oldInitializer == null){
                try{
                    oldHandler = future.channel().pipeline().first();
                    Field field = ReflectionUtil.getField(oldHandler.getClass(), "childHandler");
                    field.setAccessible(true);
                    oldInitializer = (ChannelInitializer<SocketChannel>) field.get(oldHandler);
                }catch (ReflectException exception){
                    exception.printStackTrace();
                    throw new UnsupportedOperationException("Could not override channel adapter. It seams like Mcnative " +
                            "is not compatible with one of your plugins. Please contact the McNative developer team for more information");
                }
            }

            ChannelInitializer<?> newInit = new McNativeChannelInitializer(this,oldInitializer);
            ReflectionUtil.changeFieldValue(oldHandler,"childHandler", newInit);
            this.injectedHandlers.put(oldHandler,oldInitializer);
            Debug.print("[McNative] (Channel-Injector) Overwritten handler "+oldHandler.getClass());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize channel future",e);
        }
    }

    public void reset(){
        if(channelFutureWrapperList != null && channelFutureListField != null){
            Object connection = BukkitReflectionUtil.getServerConnection();
            try {
                channelFutureListField.set(connection,channelFutureWrapperList.getOriginal());
                for (Map.Entry<ChannelHandler, ChannelInitializer<?>> injectedHandlers : this.injectedHandlers.entrySet()) {
                    ReflectionUtil.changeFieldValue( injectedHandlers.getKey(),"childHandler", injectedHandlers.getValue());
                }
            } catch (IllegalAccessException ignored) {}
        }
    }

    public static GameProfile extractGameProfile(Object profile) throws Exception{//@Todo extract properties
        UUID uniqueId = (UUID) UUID_GAME_PROFILE_FIELD.get(profile);
        String name = (String) NAME_GAME_PROFILE_FIELD.get(profile);
        return new GameProfile(uniqueId,name,new GameProfile.Property[]{});
    }

    public static GameProfile extractPsGameProfile(Object profile) throws Exception{//@Todo extract properties
        UUID uniqueId = (UUID) PS_UUID_GAME_PROFILE_FIELD.get(profile);
        String name = (String) PS_NAME_GAME_PROFILE_FIELD.get(profile);
        return new GameProfile(uniqueId,name,new GameProfile.Property[]{});
    }


}
