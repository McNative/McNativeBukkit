/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.11.19, 14:12
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

package org.mcnative.bukkit.inventory.item;

import net.pretronic.libraries.utility.Iterators;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcnative.service.NBTTag;
import org.mcnative.service.inventory.item.ItemFlag;
import org.mcnative.service.inventory.item.ItemStack;
import org.mcnative.service.inventory.item.data.ItemData;
import org.mcnative.service.inventory.item.material.Enchantment;
import org.mcnative.service.inventory.item.material.Material;

import java.util.*;

public class BukkitItemStack implements ItemStack {

    private final org.bukkit.inventory.ItemStack original;

    public BukkitItemStack(org.bukkit.inventory.ItemStack original) {
        this.original = original;
    }

    public org.bukkit.inventory.ItemStack getOriginal() {
        return original;
    }

    @Override
    public Material getMaterial() {
        return Iterators.findOne(Material.MATERIALS, material -> material.getName().equals(this.original.getType().name()));
    }

    @Override
    public ItemData getData() {
        return null;
    }

    @Override
    public int getAmount() {
        return this.original.getAmount();
    }

    @Override
    public boolean hasDurability() {
        return this.original.getItemMeta() != null &&
                this.original.getItemMeta() instanceof Damageable &&
                ((Damageable)this.original.getItemMeta()).hasDamage();
    }

    @Override
    public int getDurability() {
        if(this.original.getItemMeta() != null) {
            return ((Damageable)this.original.getItemMeta()).getDamage();
        }
        return 0;
    }

    @Override
    public String getDisplayName() {
        return this.original.getItemMeta() != null ? this.original.getItemMeta().getDisplayName() : null;
    }

    @Override
    public NBTTag getTag() {
        return null;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        this.original.getEnchantments().forEach((enchantment, level) -> {
            enchantments.put(Iterators.findOne(Enchantment.ENCHANTMENTS,
                    searchEnchantment -> searchEnchantment.getName().equals(enchantment.getKey().getKey())), level);
        });
        return enchantments;
    }

    @Override
    public List<String> getLore() {
        return this.original.getItemMeta() != null ? this.original.getItemMeta().getLore() : new ArrayList<>();
    }

    @Override
    public List<ItemFlag> getFlags() {
        if(this.original.getItemMeta() == null || this.original.getItemMeta().getItemFlags().isEmpty()) return new ArrayList<>();
        return Iterators.map(this.original.getItemMeta().getItemFlags(),
                oldFlag -> Iterators.findOne(ItemFlag.ITEM_FLAGS, flag -> flag.getName().equals(oldFlag.name())));
    }

    @Override
    public boolean hasDisplayName() {
        return this.original.getItemMeta() != null && this.original.getItemMeta().hasDisplayName();
    }

    @Override
    public boolean hasTag() {
        return this.original.getItemMeta() != null && !this.original.getItemMeta().getItemFlags().isEmpty();
    }

    @Override
    public boolean hasLore() {
        return this.original.getItemMeta() != null && this.original.getItemMeta().hasLore();
    }

    @Override
    public boolean hasFlag(ItemFlag flag) {
        return this.original.getItemMeta() != null && this.original.getItemMeta().hasItemFlag(org.bukkit.inventory.ItemFlag.valueOf(flag.getName()));
    }

    @Override
    public void setMaterial(Material material) {
        this.original.setType(org.bukkit.Material.getMaterial(material.getName()));
    }

    @Override
    public void setData(ItemData data) {

    }

    @Override
    public void setAmount(int amount) {
        this.original.setAmount(amount);
    }

    @Override
    public void setDurability(int durability) {
        if (this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            ((Damageable) meta).setDamage(durability);
            this.original.setItemMeta(meta);
        }
    }

    @Override
    public void setDisplayName(String name) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.setDisplayName(name);
            this.original.setItemMeta(meta);
        }
    }

    @Override
    public void setTag(NBTTag tag) {

    }

    @Override
    public void addEnchantment(Enchantment enchantment) {
        addEnchantment(enchantment, enchantment.getStartLevel());
    }

    @Override
    public void addEnchantment(Enchantment enchantment, int level) {
        this.original.addEnchantment(org.bukkit.enchantments.Enchantment.getByName(enchantment.getName()), level);
    }

    @Override
    public void setLore(List<String> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.setLore(lore);
            this.original.setItemMeta(meta);
        }
    }

    @Override
    public void setLore(String... lore) {
        setLore(Arrays.asList(lore));
    }

    @Override
    public void setLore(int index, String lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            List<String> newLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            newLore.add(index, lore);
            meta.setLore(newLore);
            this.original.setItemMeta(meta);
        }
    }

    @Override
    public void addLore(List<String> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            List<String> newLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            newLore.addAll(lore);
            meta.setLore(newLore);
            this.original.setItemMeta(meta);
        }
    }

    @Override
    public void addLore(String... lore) {
        addLore(Arrays.asList(lore));
    }

    @Override
    public void addLore(String lore) {
        addLore(Collections.singletonList(lore));
    }

    @Override
    public void setFlags(ItemFlag... flags) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.getItemFlags().iterator().forEachRemaining(meta::removeItemFlags);
            this.original.setItemMeta(meta);
            addFlags(flags);
        }
    }

    @Override
    public void addFlags(ItemFlag... flags) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            for (ItemFlag flag : flags) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.valueOf(flag.getName()));
            }
            this.original.setItemMeta(meta);
        }
    }

    @Override
    public void removeFlag(ItemFlag... flags) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            for (ItemFlag flag : flags) {
                meta.removeItemFlags(org.bukkit.inventory.ItemFlag.valueOf(flag.getName()));
            }
            this.original.setItemMeta(meta);
        }
    }
}
