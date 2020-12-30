/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.02.20, 14:08
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
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import org.bukkit.event.player.PlayerLoginEvent;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerLoginEvent;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class BukkitLoginEvent implements MinecraftPlayerLoginEvent {

    private static final String NULL = "MESSAGE_NULL";

    private final PlayerLoginEvent original;
    private final PendingConnection connection;
    private final OnlineMinecraftPlayer player;

    private MessageComponent<?> cancelReason;
    private VariableSet variables;

    public BukkitLoginEvent(PlayerLoginEvent original, PendingConnection connection, OnlineMinecraftPlayer player) {
        this.original = original;
        this.connection = connection;
        this.player = player;
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }

    @Override
    public MessageComponent<?> getCancelReason() {
        if(!original.getKickMessage().equals(NULL)) cancelReason = Text.of(original.getKickMessage());
        return cancelReason;
    }

    @Override
    public VariableSet getCancelReasonVariables() {
        return variables;
    }

    @Override
    public PendingConnection getConnection() {
        return connection;
    }

    @Override
    public void setCancelReason(MessageComponent<?> cancelReason, VariableSet variables) {
        Validate.notNull(cancelReason,variables);
        this.cancelReason = cancelReason;
        this.variables = variables;
        this.original.setKickMessage(NULL);
    }

    @Override
    public boolean isCancelled() {
        return original.getResult() != PlayerLoginEvent.Result.ALLOWED;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        original.setResult(cancelled ? PlayerLoginEvent.Result.KICK_OTHER : PlayerLoginEvent.Result.ALLOWED);
    }

    @Internal
    public boolean hasMessageChanged(){
        return NULL.equals(original.getKickMessage());
    }
}
