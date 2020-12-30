/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 13.11.19, 18:52
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

import net.pretronic.libraries.utility.Iterators;
import org.mcnative.bukkit.inventory.BukkitInventory;
import org.mcnative.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.type.PlayerInventory;

public class BukkitPlayerInventory extends BukkitInventory<org.bukkit.inventory.PlayerInventory> implements PlayerInventory {

    public BukkitPlayerInventory(InventoryOwner owner, org.bukkit.inventory.PlayerInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getHelmet() {
        return new BukkitItemStack(this.original.getHelmet());
    }

    @Override
    public ItemStack getChestplate() {
        return new BukkitItemStack(this.original.getChestplate());
    }

    @Override
    public ItemStack getLeggings() {
        return new BukkitItemStack(this.original.getLeggings());
    }

    @Override
    public ItemStack getBoots() {
        return new BukkitItemStack(this.original.getBoots());
    }

    @Override
    public ItemStack[] getArmorContents() {
        ItemStack[] itemStacks = new ItemStack[this.original.getArmorContents().length];
        Iterators.map(this.original.getArmorContents(), itemStacks, BukkitItemStack::new);
        return itemStacks;
    }

    @Override
    public ItemStack getItemInMainHand() {
        return new BukkitItemStack(this.original.getItemInMainHand());
    }

    @Override
    public ItemStack getItemInOffMainHand() {
        return new BukkitItemStack(this.original.getItemInOffHand());
    }

    @Override
    public int getHeldItemSlot() {
        return this.original.getHeldItemSlot();
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        this.original.setHelmet(((BukkitItemStack)helmet).getOriginal());
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        this.original.setChestplate(((BukkitItemStack)chestplate).getOriginal());
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        this.original.setLeggings(((BukkitItemStack)leggings).getOriginal());
    }

    @Override
    public void setBoots(ItemStack boots) {
        this.original.setBoots(((BukkitItemStack)boots).getOriginal());
    }

    @Override
    public void setArmorContents(ItemStack... items) {
        org.bukkit.inventory.ItemStack[] bukkitItemStacks = new org.bukkit.inventory.ItemStack[items.length];
        Iterators.map(items, bukkitItemStacks, itemStack -> ((BukkitItemStack)itemStack).getOriginal());
        this.original.setArmorContents(bukkitItemStacks);
    }

    @Override
    public void setItemInMainHand(ItemStack stack) {
        this.original.setItemInMainHand(((BukkitItemStack)stack).getOriginal());
    }

    @Override
    public void setItemInOffHand(ItemStack stack) {
        this.original.setItemInOffHand(((BukkitItemStack)stack).getOriginal());
    }

    @Override
    public void setHeldItemSlot(int slot) {
        this.original.setHeldItemSlot(slot);
    }

    @Override
    public void clearArmor() {
        this.original.setArmorContents(null);
    }
}
