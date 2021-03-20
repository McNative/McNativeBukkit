/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 09.11.19, 19:50
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

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcnative.runtime.bukkit.inventory.BukkitInventory;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.type.AnvilInventory;

public class BukkitAnvilInventory extends BukkitInventory<org.bukkit.inventory.AnvilInventory> implements AnvilInventory {

    public BukkitAnvilInventory(InventoryOwner owner, org.bukkit.inventory.AnvilInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getInputLeft() {
        return new BukkitItemStack(this.original.getItem(AnvilInventory.SLOT_INPUT_LEFT));
    }

    @Override
    public ItemStack getInputRight() {
        return new BukkitItemStack(this.original.getItem(AnvilInventory.SLOT_INPUT_RIGHT));
    }

    @Override
    public ItemStack getOutput() {
        return new BukkitItemStack(this.original.getItem(AnvilInventory.SLOT_OUTPUT));
    }

    @Override
    public String getRenameText() {
        return this.original.getRenameText();
    }

    @Override
    public int getRepairCost() {
        return this.original.getRepairCost();
    }

    @Override
    public int getMaximumRepairCost() {
        return this.original.getMaximumRepairCost();
    }

    @Override
    public void setInputLeft(ItemStack input) {
        this.original.setItem(AnvilInventory.SLOT_INPUT_LEFT, ((BukkitItemStack)input).getOriginal());
    }

    @Override
    public void setInputRight(ItemStack input) {
        this.original.setItem(AnvilInventory.SLOT_INPUT_RIGHT, ((BukkitItemStack)input).getOriginal());
    }

    @Override
    public void setOutput(ItemStack output) {
        this.original.setItem(AnvilInventory.SLOT_OUTPUT, ((BukkitItemStack)output).getOriginal());
    }

    @Override
    public void setRenameText(String text) {
        org.bukkit.inventory.ItemStack item = this.original.getItem(AnvilInventory.SLOT_INPUT_LEFT);
        if(item == null) item = new org.bukkit.inventory.ItemStack(Material.PAPER);
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return;
        itemMeta.setDisplayName(text);
        item.setItemMeta(itemMeta);
        this.original.setItem(AnvilInventory.SLOT_INPUT_LEFT, item);
    }

    @Override
    public void setRepairCost(int levels) {
        this.original.setRepairCost(levels);
    }

    @Override
    public void setMaximumRepairCost(int levels) {
        this.original.setMaximumRepairCost(levels);
    }

    @Override
    public void clearItemsOnClose(boolean clear) {

    }
}