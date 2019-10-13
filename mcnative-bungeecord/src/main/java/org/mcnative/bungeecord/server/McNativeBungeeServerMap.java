/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.08.19, 15:27
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

package org.mcnative.bungeecord.server;

import net.md_5.bungee.api.config.ServerInfo;
import net.prematic.libraries.utility.Iterators;
import org.mcnative.common.McNative;
import org.mcnative.proxy.server.MinecraftServer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class McNativeBungeeServerMap implements Map<String, ServerInfo> {

    private final Collection<MinecraftServer> servers;

    public McNativeBungeeServerMap(Collection<MinecraftServer> servers) {
        this.servers = servers;
    }

    @Override
    public int size() {
        return servers.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return servers.contains(value);
    }

    @Override
    public ServerInfo get(Object key) {
        return Iterators.mapOne(servers, server -> server.getName().equals(key), server -> {
            if(server instanceof WrappedBungeeMinecraftServer) return ((WrappedBungeeMinecraftServer) server).getOriginalInfo();
            else if(server instanceof ServerInfo) return (ServerInfo) server;
            else{
                McNative.getInstance().getLogger().error("Invalid server object, can not get as BungeeCord ServerInfo.");
                return null;
            }
        });
    }

    @Override
    public ServerInfo put(String unused, ServerInfo value) {
        if(value instanceof MinecraftServer) servers.add((MinecraftServer) value);
        else servers.add(new WrappedBungeeMinecraftServer(value));
        return value;
    }

    @Override
    public ServerInfo remove(Object key) {
        MinecraftServer server = Iterators.removeOne(this.servers, server1 -> server1.getName().equals(key));
        return server instanceof ServerInfo? (ServerInfo) server :null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends ServerInfo> map) {
        map.values().forEach((Consumer<ServerInfo>) info -> put(null,info));
    }

    @Override
    public void clear() {
        this.servers.clear();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        servers.forEach(server -> keys.add(server.getName()));
        return keys;
    }

    @Override
    public Collection<ServerInfo> values() {
        return Iterators.map(this.servers, server -> server instanceof ServerInfo? (ServerInfo) server :null);
    }

    @Override
    public Set<Entry<String, ServerInfo>> entrySet() {
        Set<Entry<String, ServerInfo>> entries = new HashSet<>();
        servers.forEach(server -> entries.add(new ServerEntry(server)));
        return entries;
    }

    private static class ServerEntry implements Entry<String, ServerInfo>{

        private final MinecraftServer server;

        public ServerEntry(MinecraftServer server) {
            this.server = server;
        }

        @Override
        public String getKey() {
            return server.getName();
        }

        @Override
        public ServerInfo getValue() {
            if(server instanceof ServerInfo) return (ServerInfo) server;
            else if(server instanceof WrappedBungeeMinecraftServer) return ((WrappedBungeeMinecraftServer) server).getOriginalInfo();
            else throw new IllegalArgumentException("It is not possible to convert the server into a BungeeCord ServerInfo.");
        }

        @Override
        public ServerInfo setValue(ServerInfo value) {
            return null;
        }
    }
}
