/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 15.02.20, 22:02
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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.mcnative.runtime.api.player.profile.GameProfile;

public class ChannelConnection {

    private final Channel channel;
    private final Object networkManager;
    private final GenericFutureListener<Future<? super Void>> connectionUnregisterListener;

    private GameProfile gameProfile;
    private int protocolVersion;
    private String hostname;
    private int port;

    public ChannelConnection(Channel channel, Object networkManager,GenericFutureListener<Future<? super Void>> connectionUnregisterListener) {
        this.channel = channel;
        this.networkManager = networkManager;
        this.connectionUnregisterListener = connectionUnregisterListener;
    }

    public Channel getChannel() {
        return channel;
    }

    public Object getNetworkManager() {
        return networkManager;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    protected void unregister(){
        channel.closeFuture().removeListener(connectionUnregisterListener);
        channel.pipeline().remove("mcnative-handshake-decoder");
    }

    protected void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    protected void initHandshake(int protocolVersion,String hostname, int port){
        this.protocolVersion = protocolVersion;
        this.hostname = hostname;
        this.port = port;
    }
}
