/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 19:18
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
import org.mcnative.runtime.api.service.inventory.type.FurnaceInventory;

public class BukkitFurnaceInventory extends BukkitInventory<org.bukkit.inventory.FurnaceInventory> implements FurnaceInventory {

    public BukkitFurnaceInventory(InventoryOwner owner, org.bukkit.inventory.FurnaceInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getSmelting() {
        return new BukkitItemStack(this.original.getSmelting());
    }

    @Override
    public ItemStack getFuel() {
        return new BukkitItemStack(this.original.getFuel());
    }

    @Override
    public ItemStack getResult() {
        return new BukkitItemStack(this.original.getResult());
    }

    @Override
    public void setSmelting(ItemStack smelting) {
        this.original.setSmelting(((BukkitItemStack)smelting).getOriginal());
    }

    @Override
    public void setFuel(ItemStack fuel) {
        this.original.setFuel(((BukkitItemStack)fuel).getOriginal());
    }

    @Override
    public void setResult(ItemStack result) {
        this.original.setResult(((BukkitItemStack)result).getOriginal());
    }
}
