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

package org.mcnative.runtime.bukkit.event.player;

import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.MinecraftPlayerQuitEvent;

public class BukkitQuitEvent implements MinecraftPlayerQuitEvent {

    private static final String NULL = "MESSAGE_NULL";

    private final PlayerQuitEvent original;
    private final Player player;
    private MessageComponent<?> joinMessage;
    private VariableSet variables;

    public boolean locationChanged;

    public BukkitQuitEvent(PlayerQuitEvent original, Player player) {
        this.original = original;
        this.player = player;

        locationChanged = false;
    }

    @Override
    public MessageComponent<?> getQuietMessage() {
        return joinMessage;
    }

    @Override
    public VariableSet getQuietMessageVariables() {
        return variables;
    }

    @Override
    public void setQuietMessage(MessageComponent<?> joinMessage, VariableSet variables) {
        if(joinMessage == null){
            original.setQuitMessage(null);
        }else{
            this.joinMessage = joinMessage;
            this.variables = variables;
            this.original.setQuitMessage(NULL);
        }
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
        return NULL.equals(original.getQuitMessage());
    }

    public boolean hasLocationChanged() {
        return locationChanged;
    }
}
