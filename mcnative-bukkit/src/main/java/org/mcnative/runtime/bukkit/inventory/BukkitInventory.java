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

package org.mcnative.runtime.bukkit.inventory;

import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.block.Container;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.service.MinecraftService;
import org.mcnative.runtime.api.service.entity.living.HumanEntity;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.inventory.Inventory;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.bukkit.inventory.type.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class BukkitInventory<I extends org.bukkit.inventory.Inventory> implements Inventory {

    private InventoryOwner owner;
    protected final I original;

    public BukkitInventory(InventoryOwner owner, I original) {
        this.owner = owner;
        this.original = original;
    }

    public I getOriginal() {
        return original;
    }

    @Override
    public String getTitle() {
        if(McNative.getInstance().getPlatform().getProtocolVersion().isNewerOrSame(MinecraftProtocolVersion.JE_1_14_1)) {
            return this.original instanceof Container ? ((Container)this.original).getCustomName() : null;
        }
        return (String) ReflectionUtil.invokeMethod(org.bukkit.inventory.Inventory.class, original, "getName", new Object[0]);
    }

    @Override
    public int getSize() {
        return this.original.getSize();
    }

    @Override
    public InventoryOwner getOwner() {
        return this.owner;
    }

    @Override
    public Collection<HumanEntity> getViewers() {
        return null;
    }

    @Override
    public ItemStack getItem(int index) {
        return new BukkitItemStack(this.original.getItem(index));
    }

    @Override
    public ItemStack getItem(int x, int y) {
        return getItem(Inventory.toIndex(x, y));
    }

    @Override
    public Collection<ItemStack> getItems() {
        return Iterators.map(Arrays.asList(this.original.getContents()), BukkitItemStack::new);
    }

    @Override
    public Stream<ItemStack> stream() {
        return getItems().stream();
    }

    @Override
    public int countItemStacks() {
        return this.original.getContents().length;
    }

    @Override
    public int countItemStacks(Material material) {
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
        Validate.notNull(bukkitMaterial, "No bukkit material for %s available.", material.getName());
        int count = 0;
        for (org.bukkit.inventory.ItemStack content : this.original.getContents()) {
            if(content.getType() == bukkitMaterial) count++;
        }
        return count;
    }

    @Override
    public int countItemStacks(ItemStack itemStack) {
        int count = 0;
        for (org.bukkit.inventory.ItemStack content : this.original.getContents()) {
            if(content.equals(((BukkitItemStack)itemStack).getOriginal())) count++;
        }
        return count;
    }

    @Override
    public boolean contains(ItemStack item) {
        return this.original.contains(((BukkitItemStack)item).getOriginal());
    }

    @Override
    public boolean contains(int index, ItemStack item) {
        return ((BukkitItemStack)item).getOriginal().equals(this.original.getItem(index));
    }

    @Override
    public boolean contains(int x, int y, ItemStack item) {
        return contains(Inventory.toIndex(x, y), item);
    }

    @Override
    public boolean contains(Material material) {
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
        Validate.notNull(bukkitMaterial, "Can't find bukkit material for %s", material.getName());
        return this.original.contains(bukkitMaterial);
    }

    @Override
    public boolean contains(int index, Material material) {
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
        Validate.notNull(bukkitMaterial, "No bukkit material for %s available.", material.getName());
        org.bukkit.inventory.ItemStack bukkitItemStack = this.original.getItem(index);
        return bukkitItemStack != null && bukkitItemStack.getType() == bukkitMaterial;
    }

    @Override
    public boolean contains(int x, int y, Material material) {
        return contains(Inventory.toIndex(x, y), material);
    }

    @Override
    public boolean isEmpty() {
        return this.original.getContents().length == 0;
    }

    @Override
    public boolean isEmpty(int startIndex) {
        return isEmpty(startIndex, this.original.getContents().length);
    }

    @Override
    public boolean isEmpty(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            org.bukkit.inventory.ItemStack itemStack = this.original.getItem(i);
            if(itemStack != null && itemStack.getType() != org.bukkit.Material.AIR) return false;
        }
        return true;
    }

    @Override
    public boolean hasPlace() {
        return !isEmpty();
    }

    @Override
    public boolean hasPlace(ItemStack item) {
        if(isEmpty()) return true;
        for (org.bukkit.inventory.ItemStack content : this.original.getContents()) {
            if(((BukkitItemStack)item).getOriginal().equals(content)
                    && item.getAmount()+content.getAmount() <= content.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPlace(Material material) {
        if(isEmpty()) return true;
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
        Validate.notNull(bukkitMaterial, "No bukkit material for %s available.", material.getName());
        for (org.bukkit.inventory.ItemStack content : this.original.getContents()) {
            if(bukkitMaterial == content.getType()
                    && content.getAmount()+1 <= content.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setOwner(InventoryOwner owner) {
        this.owner = owner;
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setItem(int index, ItemStack item) {
        this.original.setItem(index, ((BukkitItemStack)item).getOriginal());
    }

    @Override
    public void setItem(int x, int y, ItemStack item) {
        setItem(Inventory.toIndex(x, y), item);
    }

    @Override
    public void addItems(ItemStack... items) {
        org.bukkit.inventory.ItemStack[] bukkitItems = new org.bukkit.inventory.ItemStack[items.length];
        Iterators.map(items, bukkitItems, itemStack -> ((BukkitItemStack)itemStack).getOriginal());
        this.original.addItem(bukkitItems);
    }

    @Override
    public void addItem(int startIndex, int endIndex, ItemStack item) {
        for (int i = startIndex; i < endIndex; i++) {
            this.original.setItem(i, ((BukkitItemStack)item).getOriginal());
        }
    }

    @Override
    public void remove(ItemStack item) {
        this.original.remove(((BukkitItemStack)item).getOriginal());
    }

    @Override
    public void remove(Material material) {
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
        Validate.notNull(bukkitMaterial, "No bukkit material for %s available.", material.getName());
        this.original.remove(bukkitMaterial);
    }

    @Override
    public void remove(int index) {
        this.original.clear(index);
    }

    @Override
    public void clear() {
        this.original.clear();
    }

    @Override
    public void clear(int startIndex) {
        this.original.clear(startIndex);
    }

    @Override
    public void clear(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            this.original.clear(i);
        }
    }

    @Override
    public void replace(ItemStack source, ItemStack replacement) {
        for (int i = 0; i < this.original.getContents().length; i++) {
            org.bukkit.inventory.ItemStack bukkitItemStack = this.original.getItem(i);
            if(((BukkitItemStack)source).getOriginal().equals(bukkitItemStack)) {
                this.original.setItem(i, ((BukkitItemStack)replacement).getOriginal());
            }
        }
    }

    @Override
    public void replace(Material source, ItemStack replacement) {
        org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(source.getName());
        Validate.notNull(bukkitMaterial, "No bukkit material for %s available.", source.getName());
        for (int i = 0; i < this.original.getContents().length; i++) {
            org.bukkit.inventory.ItemStack bukkitItemStack = this.original.getItem(i);
            if(bukkitItemStack == null) continue;
            if(bukkitMaterial == bukkitItemStack.getType()) {
                this.original.setItem(i, ((BukkitItemStack)replacement).getOriginal());
            }
        }
    }

    @Override
    public void replace(Material source, Material replacement) {
        replace(source, ItemStack.newItemStack(replacement));
    }

    @Override
    public void fill(ItemStack item) {
        fill(0, item);
    }

    @Override
    public void fill(int startIndex, ItemStack item) {
        fill(startIndex, this.original.getContents().length, item);
    }

    @Override
    public void fill(int startIndex, int endIndex, ItemStack item) {
        for (int i = startIndex; i < endIndex; i++) {
            this.original.setItem(i, ((BukkitItemStack)item).getOriginal());
        }
    }

    @Override
    public void fillSpaces(ItemStack item) {
        fillSpaces(0, item);
    }

    @Override
    public void fillSpaces(int startIndex, ItemStack item) {
        fillSpaces(startIndex, this.original.getContents().length, item);
    }

    @Override
    public void fillSpaces(int startIndex, int endIndex, ItemStack item) {
        for (int i = startIndex; i < endIndex; i++) {
            org.bukkit.inventory.ItemStack bukkitItem = this.original.getItem(i);
            if(bukkitItem == null || bukkitItem.getType() == org.bukkit.Material.AIR) {
                this.original.setItem(i, ((BukkitItemStack)item).getOriginal());
            }
        }
    }

    @Override
    public void show(HumanEntity entity) {
        entity.openInventory(this);
    }

    @Override
    public void showAllPlayers() {
        McNative.getInstance().getLocal().getOnlinePlayers().forEach(player -> {
            if(player instanceof Player) ((Player)player).openInventory(this);
        });
    }

    @Override
    public void close(HumanEntity entity) {
        if(entity.getOpenInventory() != null && entity.getOpenInventory().equals(this)) entity.closeInventory();
    }

    @Override
    public void close() {
        getViewers().forEach(HumanEntity::closeInventory);
    }


    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        Iterator<org.bukkit.inventory.ItemStack> bukkitIterator = this.original.iterator();
        return new Iterator<ItemStack>() {
            @Override
            public boolean hasNext() {
                return bukkitIterator.hasNext();
            }

            @Override
            public ItemStack next() {
                return new BukkitItemStack(bukkitIterator.next());
            }
        };
    }

    //@Todo for other inventory types
    public static BukkitInventory<?> mapInventory(org.bukkit.inventory.Inventory inventory) {
        return new BukkitInventory<>(mapInventoryHolder(inventory.getHolder()), inventory);
    }

    public static BukkitInventory<?> mapInventory(org.bukkit.inventory.Inventory inventory, Player holder) {
        return new BukkitInventory<>(holder, inventory);
    }

    public static InventoryOwner mapInventoryHolder(InventoryHolder inventoryHolder) {
        if(inventoryHolder instanceof org.bukkit.entity.Player) {
            ConnectedMinecraftPlayer player = MinecraftService.getInstance().getConnectedPlayer(((org.bukkit.entity.Player)inventoryHolder).getUniqueId());
            if(player instanceof Player) {
                return (Player) player;
            } else {
                throw new IllegalArgumentException("Can't map inventory holder. Online player is not an entity player.");
            }
        }
        return new BukkitInventoryOwner(inventoryHolder);
    }
}
