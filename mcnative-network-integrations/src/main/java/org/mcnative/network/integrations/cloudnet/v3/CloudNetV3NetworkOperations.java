/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.07.20, 10:35
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

package org.mcnative.network.integrations.cloudnet.v3;

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.player.CloudPlayer;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.mcnative.common.network.NetworkOperations;
import org.mcnative.common.network.component.server.MinecraftServer;
import org.mcnative.common.network.component.server.ProxyServer;
import org.mcnative.common.network.component.server.ServerConnectReason;
import org.mcnative.common.network.component.server.ServerConnectResult;
import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.text.components.MessageComponent;

import java.util.concurrent.CompletableFuture;

public class CloudNetV3NetworkOperations implements NetworkOperations {

    private final CloudNetV3Network network;

    public CloudNetV3NetworkOperations(CloudNetV3Network network) {
        this.network = network;
    }

    @Override
    public ProxyServer getProxy(OnlineMinecraftPlayer player) {
        CloudPlayer cloudPlayer = CloudAPI.getInstance().getOnlinePlayer(player.getUniqueId());
        return cloudPlayer != null ? network.getProxy(cloudPlayer.getProxy()) : null;
    }

    @Override
    public MinecraftServer getServer(OnlineMinecraftPlayer player) {
        CloudPlayer cloudPlayer = CloudAPI.getInstance().getOnlinePlayer(player.getUniqueId());
        return cloudPlayer != null ? network.getServer(cloudPlayer.getServer()) : null;
    }

    @Override
    public void connect(OnlineMinecraftPlayer player, MinecraftServer target, ServerConnectReason reason) {
        network.getOnlinePlayer(player.getUniqueId()).connect(target, reason);
    }

    @Override
    public CompletableFuture<ServerConnectResult> connectAsync(OnlineMinecraftPlayer player, MinecraftServer target, ServerConnectReason reason) {
        return network.getDirectOnlinePlayer(player.getUniqueId()).connectAsync(target,reason);
    }

    @Override
    public void kick(OnlineMinecraftPlayer player, MessageComponent<?> message, VariableSet variables) {
        network.getDirectOnlinePlayer(player.getUniqueId()).kick(message, variables);
    }
}