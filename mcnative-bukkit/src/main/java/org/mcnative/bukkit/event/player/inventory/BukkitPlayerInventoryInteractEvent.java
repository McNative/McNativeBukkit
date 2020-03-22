/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 13:56
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

package org.mcnative.bukkit.event.player.inventory;

import org.bukkit.event.inventory.InventoryInteractEvent;
import org.mcnative.service.entity.living.Player;
import org.mcnative.service.event.player.inventory.MinecraftPlayerInventoryInteractEvent;

public class BukkitPlayerInventoryInteractEvent<T extends InventoryInteractEvent> extends BukkitPlayerInventoryEvent<T> implements MinecraftPlayerInventoryInteractEvent {

    public BukkitPlayerInventoryInteractEvent(T original, Player player) {
        super(original, player);
    }

    @Override
    public boolean isCancelled() {
        return getOriginal().isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        getOriginal().setCancelled(cancelled);
    }
}
