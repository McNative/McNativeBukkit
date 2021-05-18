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

package org.mcnative.runtime.bukkit.event.player.inventory;

import org.bukkit.event.inventory.InventoryEvent;
import org.mcnative.runtime.bukkit.inventory.BukkitInventory;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryEvent;
import org.mcnative.runtime.api.service.inventory.Inventory;

public class BukkitPlayerInventoryEvent<T extends InventoryEvent> implements MinecraftPlayerInventoryEvent {

    private final T original;
    private final Player player;
    private final Inventory inventory;

    public BukkitPlayerInventoryEvent(T original, Player player) {
        this.original = original;
        this.player = player;
        this.inventory = BukkitInventory.mapInventory(original.getInventory(), player);
    }

    protected T getOriginal() {
        return original;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Player getOnlinePlayer() {
        return this.player;
    }
}
