/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.08.19, 22:12
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

package org.mcnative.common;

import java.io.ByteArrayOutputStream;

public class MinecraftOutputStream extends ByteArrayOutputStream {

    private final String channel;
    private MinecraftConnection connection;

    public MinecraftOutputStream(String channel,MinecraftConnection connection) {
        this.channel = channel;
        this.connection = connection;
    }

    public MinecraftConnection getConnection() {
        return connection;
    }

    @Override
    public synchronized void write(int b) {
        if(connection == null || !connection.isConnected()) throw new IllegalArgumentException("Stream is closed.");
        super.write(b);
    }

    @Override
    public void flush(){
        connection.sendData(channel,toByteArray());
    }

    @Override
    public void close() {
        this.connection = null;
    }
}
