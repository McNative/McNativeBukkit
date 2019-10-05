/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 15.09.19, 18:15
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

package org.mcnative.common.protocol.packet.type;

import io.netty.buffer.ByteBuf;
import org.mcnative.common.player.ChatPosition;
import org.mcnative.common.protocol.MinecraftProtocolUtil;
import org.mcnative.common.protocol.MinecraftProtocolVersion;
import org.mcnative.common.protocol.packet.MinecraftPacket;
import org.mcnative.common.protocol.packet.PacketDirection;
import org.mcnative.common.text.Text;
import org.mcnative.common.text.components.ChatComponent;
import org.mcnative.common.text.variable.VariableSet;

public class MinecraftChatPacket implements MinecraftPacket {

    private ChatComponent message;
    private VariableSet variables;
    private ChatPosition position;

    public ChatComponent getMessage() {
        return message;
    }

    public void setMessage(ChatComponent message) {
        this.message = message;
    }

    public VariableSet getVariables() {
        return variables;
    }

    public void setVariables(VariableSet variables) {
        this.variables = variables;
    }

    public ChatPosition getPosition() {
        return position;
    }

    public void setPosition(ChatPosition position) {
        this.position = position;
    }

    @Override
    public int getId(PacketDirection direction, MinecraftProtocolVersion version) {
        return 0;
    }

    @Override
    public void read(MinecraftProtocolVersion version, ByteBuf buffer) {
         message = Text.decompile(MinecraftProtocolUtil.readString(buffer));
    }

    @Override
    public void write(MinecraftProtocolVersion version, ByteBuf buffer) {
        MinecraftProtocolUtil.writeString(buffer,this.message.compile(variables!=null?variables:VariableSet.newEmptySet()).toString());
        buffer.writeByte(position.getId());
    }
}
