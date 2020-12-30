/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 25.04.20, 20:51
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

package org.mcnative.bukkit.event.player;

import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerChatEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class BukkitChatEvent implements MinecraftPlayerChatEvent {

    private final AsyncPlayerChatEvent event;
    private final OnlineMinecraftPlayer player;
    private ChatChannel chatChannel;
    private MessageComponent<?> outputMessage;
    private VariableSet variables;

    public BukkitChatEvent(AsyncPlayerChatEvent event, ConnectedMinecraftPlayer player) {
        this.event = event;
        this.player = player;
        this.chatChannel = player.getPrimaryChatChannel();
        this.variables = VariableSet.create();
    }

    @Override
    public ChatChannel getChannel() {
        return chatChannel;
    }

    @Override
    public void setChannel(ChatChannel channel) {
        Validate.notNull(channel);
        this.chatChannel = channel;
    }

    @Override
    public String getMessage() {
        return event.getMessage();
    }

    @Override
    public void setMessage(String message) {
        Validate.notNull(message);
        event.setMessage(message);
    }

    @Override
    public MessageComponent<?> getOutputMessage() {
        return outputMessage;
    }

    @Override
    public void setOutputMessage(MessageComponent<?> outputMessage) {
        Validate.notNull(outputMessage);
        this.outputMessage = outputMessage;
    }

    @Override
    public VariableSet getOutputVariables() {
        return variables;
    }

    @Override
    public void setOutputVariables(VariableSet variables) {
        Validate.notNull(variables);
        this.variables = variables;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }
}
