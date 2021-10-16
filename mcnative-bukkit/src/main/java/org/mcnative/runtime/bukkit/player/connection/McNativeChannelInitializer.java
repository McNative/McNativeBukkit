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
import io.netty.channel.ChannelInitializer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.mcnative.runtime.bukkit.McNativeBukkitConfiguration;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;

import java.lang.reflect.Method;

public class McNativeChannelInitializer extends ChannelInitializer<Channel> {

    @SuppressWarnings("unchecked")
    private final static Class<? extends ChannelInitializer<?>> PACKET_HANDLER_CLASS =
            (Class<? extends ChannelInitializer<?>>) BukkitReflectionUtil.getMNSClass("net.minecraft.network.NetworkManager");

    private final BukkitChannelInjector injector;
    private final ChannelInitializer<Channel> original;
    private final Method method;

    public McNativeChannelInitializer(BukkitChannelInjector injector, ChannelInitializer<Channel> original) {
        this.injector = injector;
        this.original = original;
        Method method = null;
        try {
            method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.method = method;
    }

    public ChannelInitializer<Channel> getOriginal() {
        return original;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        this.method.invoke(this.original, channel);
        Object networkManager = channel.pipeline().get(PACKET_HANDLER_CLASS);
        GenericFutureListener<Future<? super Void>> connectionUnregisterListener = future -> injector.unregisterConnection(channel);

        ChannelConnection connection = new ChannelConnection(channel,networkManager,connectionUnregisterListener);
        this.injector.registerConnection(connection);

        if(McNativeBukkitConfiguration.NETWORK_PACKET_MANIPULATION_HANDSHAKE_INJECTION){
            channel.pipeline().addBefore("decoder","mcnative-handshake-decoder",new McNativeHandshakeDecoder(connection));
        }
        channel.closeFuture().addListener(connectionUnregisterListener);
    }
}
