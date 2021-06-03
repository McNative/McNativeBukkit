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

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.service.NBTTag;
import org.mcnative.runtime.api.service.inventory.item.ItemFlag;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.data.ItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Enchantment;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.inventory.item.material.MaterialData;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.bukkit.BukkitNBTTag;
import org.mcnative.runtime.bukkit.inventory.item.data.BukkitItemData;
import org.mcnative.runtime.bukkit.inventory.item.data.BukkitSkullItemData;

import java.util.*;
import java.util.function.Consumer;

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
    public BukkitItemData<?> getData() {
        return mapItemData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends MaterialData> ItemStack getData(Class<T> clazz, Consumer<T> consumer) {
        Validate.notNull(clazz, clazz);
        BukkitItemData<?> data = getData();
        /*if(clazz.isAssignableFrom(data.getClass())) {
            throw new IllegalArgumentException("Can't get item data for class " + clazz);
        }*/
        consumer.accept((T) data);
        original.setItemMeta(data.getOriginal());
        return this;
    }

    @Override
    public int getAmount() {
        return this.original.getAmount();
    }

    @Override
    public String getDisplayName() {
        return this.original.getItemMeta() != null ? this.original.getItemMeta().getDisplayName() : null;
    }

    @Override
    public NBTTag getTag() {
        NBTItem nbtItem = new NBTItem(this.original, true);
        return new BukkitNBTTag(nbtItem);
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
    public ItemStack setDisplayName(MessageComponent<?> name) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            meta.setDisplayName(name.compileToString(MinecraftProtocolVersion.JE_1_7));
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack setTag(NBTTag tag) {
        Validate.isTrue(tag instanceof BukkitNBTTag);
        NBTItem nbtItem = new NBTItem(this.original, true);
        nbtItem.mergeCompound(((BukkitNBTTag)tag).getOriginal());
        return this;
    }

    @Override
    public ItemStack addEnchantment(Enchantment enchantment) {
        return addEnchantment(enchantment, enchantment.getStartLevel());
    }

    @Override
    public ItemStack addEnchantment(Enchantment enchantment, int level) {
        Validate.notNull(enchantment);
        Validate.isTrue(level > 0);
        org.bukkit.enchantments.Enchantment bukkitEnchantment = org.bukkit.enchantments.Enchantment.getByName(enchantment.getName());
        Validate.notNull(bukkitEnchantment, "No bukkit enchantment found for " + enchantment.getName());
        this.original.addUnsafeEnchantment(bukkitEnchantment, level);
        return this;
    }

    @Override
    public ItemStack removeEnchantment(Enchantment enchantment) {
        removeEnchantmentSafe(enchantment);
        return this;
    }

    @Override
    public int removeEnchantmentSafe(Enchantment enchantment) {
        Validate.notNull(enchantment);
        org.bukkit.enchantments.Enchantment bukkitEnchantment = org.bukkit.enchantments.Enchantment.getByName(enchantment.getName());
        Validate.notNull(bukkitEnchantment, "No bukkit enchantment found for " + enchantment.getName());
        return this.original.removeEnchantment(bukkitEnchantment);
    }

    @Override
    public ItemStack setLore(List<MessageComponent<?>> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();

            List<String> copiedLore = new ArrayList<>(lore.size());
            for (MessageComponent<?> lore0 : lore) {
                copiedLore.add(lore0.compileToString(MinecraftProtocolVersion.JE_1_7));
            }
            meta.setLore(copiedLore);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack setLore(MessageComponent<?>... lore) {
        return setLore(Arrays.asList(lore));
    }

    @Override
    public ItemStack setLore(int index, MessageComponent<?> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            List<String> newLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            newLore.add(index, lore.compileToString(MinecraftProtocolVersion.JE_1_7));
            meta.setLore(newLore);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack addLore(List<MessageComponent<?>> lore) {
        if(this.original.getItemMeta() != null) {
            ItemMeta meta = this.original.getItemMeta();
            List<String> newLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();

            for (MessageComponent<?> lore0 : lore) {
                newLore.add(lore0.compileToString(MinecraftProtocolVersion.JE_1_7));
            }

            meta.setLore(newLore);
            this.original.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemStack addLore(MessageComponent<?>... lore) {
        return addLore(Arrays.asList(lore));
    }

    @Override
    public ItemStack addLore(MessageComponent<?> lore) {
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
    public ItemStack setGlowing(boolean glowing) {
        if(glowing) {
            original.addEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
            return addFlags(ItemFlag.HIDE_ENCHANTS);
        }
        original.removeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY);
        return removeFlag(ItemFlag.HIDE_ENCHANTS);
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
        } else {
            //@Todo meta check
            Material comparisonType = getMaterial();
            return comparisonType.equals(stack.getMaterial())
                    && getAmount() == stack.getAmount()
                    && (!hasDisplayName() || getDisplayName().equals(stack.getDisplayName()))
                    && (!hasLore() || getLore().equals(stack.getLore()))
                    && hasSameFlags(stack)
                    && hasSameEnchantments(stack)
                    && getTag().equals(stack.getTag());
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

    private BukkitItemData<?> mapItemData() {
        ItemMeta meta = original.getItemMeta();
        if(meta == null) return null;
        if(meta instanceof SkullMeta) {
            return new BukkitSkullItemData(getMaterial(), (SkullMeta) meta);
        }
        throw new IllegalArgumentException("Can't map ItemMeta " + meta.getClass() + " to McNative ItemData");
    }
}
