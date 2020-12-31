/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 18:58
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
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPendingLoginEvent;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class BukkitPendingLoginEvent implements MinecraftPlayerPendingLoginEvent {

    public final static String NULL = "MESSAGE_NULL (Result mananged by McNative))";

    private final AsyncPlayerPreLoginEvent original;
    private final PendingConnection connection;
    private VariableSet variables;

    public BukkitPendingLoginEvent(AsyncPlayerPreLoginEvent original, PendingConnection connection) {
        this.original = original;
        this.connection = connection;
    }

    @Override
    public MessageComponent<?> getCancelReason() {
        return Text.of(original.getKickMessage());
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
        this.variables = variables;
        this.original.setKickMessage(NULL);
    }

    @Override
    public boolean isCancelled() {
        return this.original.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.original.setLoginResult(canceled ? AsyncPlayerPreLoginEvent.Result.KICK_OTHER : AsyncPlayerPreLoginEvent.Result.ALLOWED);
    }


    @Internal
    public boolean hasMessageChanged(){
        return NULL.equals(original.getKickMessage());
    }
}
