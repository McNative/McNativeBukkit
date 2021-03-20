/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 13.11.19, 18:45
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

package org.mcnative.runtime.bukkit.entity.living;

import org.mcnative.runtime.bukkit.inventory.BukkitInventory;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.entity.living.HumanEntity;
import org.mcnative.runtime.api.service.inventory.Inventory;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.type.PlayerInventory;

public interface BukkitHumanEntity<E extends org.bukkit.entity.HumanEntity> extends BukkitLivingEntity<E>, HumanEntity {

    @Override
    default PlayerInventory getInventory() {
        return (PlayerInventory) BukkitInventory.mapInventory(getOriginal().getInventory());
    }

    @Override
    default Inventory getOpenInventory() {
        return BukkitInventory.mapInventory(getOriginal().getOpenInventory().getTopInventory());
    }

    @Override
    default ItemStack getItemOnCursor() {
        return new BukkitItemStack(getOriginal().getItemOnCursor());
    }

    @Override
    default boolean isBlocking() {
        return getOriginal().isBlocking();
    }

    @Override
    default boolean isSleeping() {
        return getOriginal().isSleeping();
    }

    @Override
    default void openPlayerInventory() {
        getOriginal().openInventory(getOriginal().getInventory());
    }

    @Override
    default void closeInventory() {
        getOriginal().closeInventory();
    }
}
