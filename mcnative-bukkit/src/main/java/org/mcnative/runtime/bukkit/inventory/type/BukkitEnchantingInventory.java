/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 17:13
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
import org.mcnative.runtime.api.service.inventory.type.EnchantingInventory;

public class BukkitEnchantingInventory extends BukkitInventory<org.bukkit.inventory.EnchantingInventory> implements EnchantingInventory {

    public BukkitEnchantingInventory(InventoryOwner owner, org.bukkit.inventory.EnchantingInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getItem() {
        return new BukkitItemStack(this.original.getItem());
    }

    @Override
    public ItemStack getSecondary() {
        return new BukkitItemStack(this.original.getSecondary());
    }

    @Override
    public void setItem(ItemStack input) {
        this.original.setItem(((BukkitItemStack)input).getOriginal());
    }

    @Override
    public void setSecondary(ItemStack input) {
        this.original.setSecondary(((BukkitItemStack)input).getOriginal());
    }

    @Override
    public void clearItemsOnClose(boolean clear) {

    }
}
