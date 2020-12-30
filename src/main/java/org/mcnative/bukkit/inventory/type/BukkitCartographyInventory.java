/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 16:55
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
import org.mcnative.runtime.api.service.inventory.type.CartographyInventory;

public class BukkitCartographyInventory extends BukkitInventory<org.bukkit.inventory.CartographyInventory> implements CartographyInventory {

    public BukkitCartographyInventory(InventoryOwner owner, org.bukkit.inventory.CartographyInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getInputLeft() {
        return new BukkitItemStack(this.original.getItem(0));
    }

    @Override
    public ItemStack getInputRight() {
        return new BukkitItemStack(this.original.getItem(1));
    }

    @Override
    public ItemStack getOutput() {
        return new BukkitItemStack(this.original.getItem(2));
    }

    @Override
    public void setInputLeft(ItemStack input) {
        this.original.setItem(0, ((BukkitItemStack)input).getOriginal());
    }

    @Override
    public void setInputRight(ItemStack input) {
        this.original.setItem(1, ((BukkitItemStack)input).getOriginal());
    }

    @Override
    public void setOutput(ItemStack output) {
        this.original.setItem(2, ((BukkitItemStack)output).getOriginal());
    }

    @Override
    public void clearItemsOnClose(boolean clear) {

    }
}
