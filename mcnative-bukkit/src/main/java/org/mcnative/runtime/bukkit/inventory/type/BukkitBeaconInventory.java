/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 16:46
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

package org.mcnative.runtime.bukkit.inventory.type;

import org.mcnative.runtime.bukkit.inventory.BukkitInventory;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.type.BeaconInventory;

public class BukkitBeaconInventory extends BukkitInventory<org.bukkit.inventory.BeaconInventory> implements BeaconInventory {

    public BukkitBeaconInventory(InventoryOwner owner, org.bukkit.inventory.BeaconInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getItem() {
        return new BukkitItemStack(this.original.getItem());
    }

    @Override
    public void setItem(ItemStack item) {
        this.original.setItem(((BukkitItemStack)item).getOriginal());
    }
}
