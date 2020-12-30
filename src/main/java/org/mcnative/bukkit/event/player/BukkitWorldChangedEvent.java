/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.02.20, 20:51
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

import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.MinecraftPlayerWorldChangedEvent;
import org.mcnative.runtime.api.service.world.World;

public class BukkitWorldChangedEvent implements MinecraftPlayerWorldChangedEvent {

    private final Player player;
    private final World from;

    public BukkitWorldChangedEvent(Player player, World from) {
        this.player = player;
        this.from = from;
    }

    @Override
    public World getFrom() {
        return from;
    }

    @Override
    public World getTo() {
        return player.getWorld();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Player getOnlinePlayer() {
        return player;
    }
}
