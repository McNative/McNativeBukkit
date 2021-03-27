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

package org.mcnative.runtime.bukkit.inventory.item;

import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcnative.runtime.api.service.NBTTag;
import org.mcnative.runtime.api.service.inventory.item.ItemFlag;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.data.ItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Enchantment;
import org.mcnative.runtime.api.service.inventory.item.material.Material;

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
        if(this.original == null || this.original.getType() == org.bukkit.Material.AIR) return Material.AIR;
        return Iterators.findOne(Material.MATERIALS, material -> material.getName().equals(this.original.getType().name()));
    }

    @Override
    public ItemData getData() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public ItemStack setMaterial(Material material) {
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
        Validate.notNull(bukkitMaterial, "Can't map bukkit material " + material.getName());
        this.original.setType(bukkitMaterial);
        return this;
    }

    @Override
    public ItemStack setData(ItemData data) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ItemStack setAmount(int amount) {
        this.original.setAmount(amount);
        return this;
    }

    @Override
    public ItemStack setDurability(int durability) {
        if (this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            ((Damageable) meta).setDamage(durability);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack setDisplayName(String name) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.setDisplayName(name);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack setTag(NBTTag tag) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ItemStack addEnchantment(Enchantment enchantment) {
        return addEnchantment(enchantment, enchantment.getStartLevel());
    }

    @Override
    public ItemStack addEnchantment(Enchantment enchantment, int level) {
        this.original.addEnchantment(org.bukkit.enchantments.Enchantment.getByName(enchantment.getName()), level);
        return this;
    }

    @Override
    public ItemStack setLore(List<String> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.setLore(lore);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

    @Override
    public ItemStack setLore(int index, String lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            List<String> newLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            newLore.add(index, lore);
            meta.setLore(newLore);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack addLore(List<String> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            List<String> newLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            newLore.addAll(lore);
            meta.setLore(newLore);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack addLore(String... lore) {
        return addLore(Arrays.asList(lore));
    }

    @Override
    public ItemStack addLore(String lore) {
        return addLore(Collections.singletonList(lore));
    }

    @Override
    public ItemStack setFlags(ItemFlag... flags) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.getItemFlags().iterator().forEachRemaining(meta::removeItemFlags);
            this.original.setItemMeta(meta);
            addFlags(flags);
        }
        return this;
    }

    @Override
    public ItemStack addFlags(ItemFlag... flags) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            for (ItemFlag flag : flags) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.valueOf(flag.getName()));
            }
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack removeFlag(ItemFlag... flags) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            for (ItemFlag flag : flags) {
                meta.removeItemFlags(org.bukkit.inventory.ItemFlag.valueOf(flag.getName()));
            }
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o instanceof ItemStack) {
            if(o instanceof BukkitItemStack) {
                return ((BukkitItemStack) o).original.equals(this.original);
            }
            ItemStack itemStack = (ItemStack) o;
            return isSimilar(itemStack);
        }
        return false;
    }

    public boolean isSimilar(ItemStack stack) {
        if (stack == null) {
            return false;
        } else if (stack == this) {
            return true;
        } else {//@Todo compare nbt tag
            Material comparisonType = getMaterial();
            return comparisonType.equals(stack.getMaterial())
                    && this.getDurability() == stack.getDurability()
                    && getAmount() == stack.getAmount()
                    && (!hasDurability() || getDurability() == stack.getDurability())
                    && (!hasDisplayName() || getDisplayName().equals(stack.getDisplayName()))
                    && (!hasLore() || getLore().equals(stack.getLore()))
                    && hasSameFlags(stack)
                    && hasSameEnchantments(stack);
        }
    }

    public boolean hasSameEnchantments(ItemStack stack) {
        if(getEnchantments().size() != stack.getEnchantments().size()) return false;
        for (Map.Entry<Enchantment, Integer> entry : getEnchantments().entrySet()) {
            if(stack.getEnchantments().containsKey(entry.getKey())) {
                int level = stack.getEnchantments().get(entry.getKey());
                if(entry.getValue() != level) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean hasSameFlags(ItemStack stack) {
        if(stack.getFlags().size() != getFlags().size()) return false;
        for (int i = 0; i < getFlags().size(); i++) {
            if(!getFlags().get(i).equals(stack.getFlags().get(i))) return false;
        }
        return true;
    }
}
