/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.02.20, 17:01
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

package org.mcnative.bukkit.event.player;

import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.MinecraftPlayerJoinEvent;
import org.mcnative.runtime.api.service.location.Location;

public class BukkitJoinEvent implements MinecraftPlayerJoinEvent {

    private static final String NULL = "MESSAGE_NULL";

    private final PlayerJoinEvent original;
    private final Player player;
    private MessageComponent<?> joinMessage;
    private VariableSet variables;
    private Location location;

    public boolean locationChanged;

    public BukkitJoinEvent(PlayerJoinEvent original,Player player) {
        this.original = original;
        this.player = player;

        locationChanged = false;
    }

    @Override
    public MessageComponent<?> getJoinMessage() {
        return joinMessage;
    }

    @Override
    public VariableSet getJoinMessageVariables() {
        return variables;
    }

    @Override
    public void setJoinMessage(MessageComponent<?> joinMessage, VariableSet variables) {
        if(joinMessage == null){
            original.setJoinMessage(null);
        }else{
            this.joinMessage = joinMessage;
            this.variables = variables;
            this.original.setJoinMessage(NULL);
        }
    }

    @Override
    public Location getSpawnLocation() {
        if(location == null) location = player.getLocation();
        return location;
    }

    @Override
    public void setSpawnLocation(Location location) {
        this.location = location;
        locationChanged = true;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Player getOnlinePlayer() {
        return player;
    }

    public boolean hasMessageChanged() {
        return NULL.equals(original.getJoinMessage());
    }

    public boolean hasLocationChanged() {
        return locationChanged;
    }
}
