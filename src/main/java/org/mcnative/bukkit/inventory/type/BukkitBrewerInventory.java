/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 16:47
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
import org.mcnative.runtime.api.service.inventory.type.BrewerInventory;

public class BukkitBrewerInventory extends BukkitInventory<org.bukkit.inventory.BrewerInventory> implements BrewerInventory {

    public BukkitBrewerInventory(InventoryOwner owner, org.bukkit.inventory.BrewerInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getIngredient() {
        return new BukkitItemStack(this.original.getIngredient());
    }

    @Override
    public void setIngredient(ItemStack ingredient) {
        this.original.setIngredient(((BukkitItemStack)ingredient).getOriginal());
    }

    @Override
    public ItemStack getFuel() {
        return new BukkitItemStack(this.original.getFuel());
    }

    @Override
    public void setFuel(ItemStack fuel) {
        this.original.setFuel(((BukkitItemStack)fuel).getOriginal());
    }

    @Override
    public ItemStack getOutput(int index) {
        return new BukkitItemStack(this.original.getItem(index));
    }

    @Override
    public void setOutput(int index, ItemStack item) {
        this.original.setItem(index, ((BukkitItemStack)item).getOriginal());
    }
}
