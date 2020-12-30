/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.05.20, 19:10
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

package org.mcnative.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.mcnative.bukkit.player.BukkitPlayer;
import org.mcnative.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.PlayerManager;
import org.mcnative.runtime.api.text.Text;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BukkitWrapperServerListPingEvent extends ServerListPingEvent {

    private final BukkitServerListPingEvent mcNativeEvent;

    public BukkitWrapperServerListPingEvent(BukkitServerListPingEvent mcNativeEvent) {
        super(null, null, 0, 0);
        this.mcNativeEvent = mcNativeEvent;
    }

    @Override
    public InetAddress getAddress() {
        return mcNativeEvent.getClientAddress().getAddress();
    }

    @Override
    public String getMotd() {
        return mcNativeEvent.getResponse().getDescription().toPlainText();
    }

    @Override
    public void setMotd(String motd) {
        mcNativeEvent.getResponse().setDescription(Text.of(motd));
    }

    @Override
    public int getNumPlayers() {
        return mcNativeEvent.getResponse().getOnlinePlayers();
    }

    @Override
    public int getMaxPlayers() {
        return mcNativeEvent.getResponse().getMaxPlayers();
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        mcNativeEvent.getResponse().setMaxPlayers(maxPlayers);
    }

    @Override
    public Iterator<Player> iterator() throws UnsupportedOperationException {
        return new ServerPingIterator();
    }

    private class ServerPingIterator implements Iterator<Player> {

        private final Iterator<Player> iterator;
        private Player current;

        public ServerPingIterator() {
            List<Player> players = new ArrayList<>();
            for (ServerStatusResponse.PlayerInfo playerInfo : mcNativeEvent.getResponse().getPlayerInfo()) {
                if(playerInfo instanceof BukkitPlayer){
                    players.add(((BukkitPlayer) playerInfo).getOriginal());
                }
            }
            iterator = players.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Player next() {
            current = iterator.next();
            return current;
        }

        @Override
        public void remove() {
            iterator.remove();
            PlayerManager playerManager = McNative.getInstance().getPlayerManager();
            if(playerManager instanceof BukkitPlayerManager){
                MinecraftPlayer player = ((BukkitPlayerManager) playerManager).getMappedPlayer(current);
                if(player != null){
                    mcNativeEvent.getResponse().removePlayerInfo(player);
                }
            }
        }
    }
}
