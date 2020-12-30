/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 16.11.19, 14:43
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

package org.mcnative.bukkit.inventory.type;

import org.mcnative.bukkit.inventory.BukkitInventory;
import org.mcnative.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.type.StonecutterInventory;

public class BukkitStonecutterInventory extends BukkitInventory<org.bukkit.inventory.StonecutterInventory> implements StonecutterInventory {

    public BukkitStonecutterInventory(InventoryOwner owner, org.bukkit.inventory.StonecutterInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getInput() {
        return new BukkitItemStack(this.original.getItem(0));
    }

    @Override
    public void setInput(ItemStack item) {
        this.original.setItem(0, ((BukkitItemStack)item).getOriginal());
    }

    @Override
    public ItemStack getOutput() {
        return new BukkitItemStack(this.original.getItem(1));
    }

    @Override
    public void setOutput(ItemStack item) {
        this.original.setItem(1, ((BukkitItemStack)item).getOriginal());
    }

    @Override
    public void clearItemsOnClose(boolean clear) {

    }
}
