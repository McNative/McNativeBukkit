/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.02.20, 19:57
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

import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.api.event.service.local.LocalServicePingEvent;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;

import java.net.InetSocketAddress;

public class BukkitServerListPingEvent implements LocalServicePingEvent {

    private final InetSocketAddress clientAddress;
    private final InetSocketAddress virtualAddress;
    private ServerStatusResponse statusResponse;

    public BukkitServerListPingEvent(InetSocketAddress clientAddress, InetSocketAddress virtualAddress, ServerStatusResponse statusResponse) {
        this.clientAddress = clientAddress;
        this.virtualAddress = virtualAddress;
        this.statusResponse = statusResponse;
    }

    @Override
    public InetSocketAddress getClientAddress() {
        return clientAddress;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return virtualAddress;
    }

    @Override
    public ServerStatusResponse getResponse() {
        return statusResponse;
    }

    @Override
    public void setResponse(ServerStatusResponse response) {
        Validate.notNull(response);
        this.statusResponse = response;
    }
}
