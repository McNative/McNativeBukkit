/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 04.08.19 10:44
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

package org.mcnative.proxy.server;

import org.mcnative.common.connection.MinecraftConnection;
import org.mcnative.common.ServerPingResponse;
import org.mcnative.proxy.ProxiedPlayer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface MinecraftServer extends MinecraftConnection {

    String getPermission();

    void setPermission(String permission);


    MinecraftServerType getType();

    void setType(MinecraftServerType type);


    Collection<ProxiedPlayer> getConnectedPlayers();

    boolean isOnline();


    ServerPingResponse ping();

    CompletableFuture<ServerPingResponse> pingAsync();

}
