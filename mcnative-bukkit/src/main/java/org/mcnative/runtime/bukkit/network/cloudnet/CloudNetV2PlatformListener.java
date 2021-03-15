/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.07.20, 11:35
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

package org.mcnative.runtime.bukkit.network.cloudnet;

import de.dytanic.cloudnet.bridge.event.bukkit.BukkitSubChannelMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.network.integrations.cloudnet.v2.CloudNetV2Messenger;

public class CloudNetV2PlatformListener implements Listener {

    private final CloudNetV2Messenger messenger;

    public CloudNetV2PlatformListener(CloudNetV2Messenger messenger) {
        this.messenger = messenger;
        Bukkit.getPluginManager().registerEvents(this, McNativeLauncher.getPlugin());
    }

    @EventHandler
    public void onMessageReceive(BukkitSubChannelMessageEvent event){
        this.messenger.handleMessageEvent(event.getChannel(),event.getMessage(),event.getDocument());
    }
}
