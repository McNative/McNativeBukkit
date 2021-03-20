/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 25.04.20, 20:54
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

package org.mcnative.runtime.bukkit.player.tablist;

import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.common.player.tablist.AbstractTablist;

public class BukkitTablist extends AbstractTablist {


    @Override
    public String getPlayerTablistNames(ConnectedMinecraftPlayer receiver, TablistEntry entry) {
        return ((BukkitPlayer) receiver).getTablistTeamNames().get(entry);
    }

    @Override
    public int getTablistTeamIndexAndIncrement(ConnectedMinecraftPlayer receiver) {
        return ((BukkitPlayer) receiver).getTablistTeamIndexAndIncrement();
    }

    @Override
    public void putTablistNames(ConnectedMinecraftPlayer receiver, TablistEntry entry, String team) {
        ((BukkitPlayer) receiver).getTablistTeamNames().put(entry,team);
    }

    @Override
    public void removeTablistNames(ConnectedMinecraftPlayer receiver, TablistEntry entry) {
        ((BukkitPlayer) receiver).getTablistTeamNames().remove(entry);
    }
}
