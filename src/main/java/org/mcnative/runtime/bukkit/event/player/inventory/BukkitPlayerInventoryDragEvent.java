/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 13:56
 * @web %web%
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

package org.mcnative.runtime.bukkit.event.player.inventory;

import org.bukkit.event.inventory.InventoryDragEvent;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryDragEvent;
import org.mcnative.runtime.api.service.inventory.DragType;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BukkitPlayerInventoryDragEvent extends BukkitPlayerInventoryInteractEvent<InventoryDragEvent> implements MinecraftPlayerInventoryDragEvent {

    private final Map<Integer, ItemStack> newItems;
    private final ItemStack oldCursor;
    private final DragType dragType;

    public BukkitPlayerInventoryDragEvent(InventoryDragEvent original, Player player) {
        super(original, player);
        this.newItems = new HashMap<>();
        original.getNewItems().forEach((key, value) -> newItems.put(key, new BukkitItemStack(value)));
        this.oldCursor = new BukkitItemStack(original.getOldCursor());
        this.dragType = mapDragType(original.getType());
    }

    @Override
    public Map<Integer, ItemStack> getNewItems​() {
        return this.newItems;
    }

    @Override
    public Set<Integer> getRawSlots​() {
        return getOriginal().getRawSlots();
    }

    @Override
    public Set<Integer> getInventorySlots​() {
        return getOriginal().getInventorySlots();
    }

    @Override
    public ItemStack getCursor​() {
        return new BukkitItemStack(getOriginal().getCursor());
    }

    @Override
    public void setCursor(ItemStack cursor) {
        getOriginal().setCursor(((BukkitItemStack)cursor).getOriginal());
    }

    @Override
    public ItemStack getOldCursor() {
        return this.oldCursor;
    }

    @Override
    public DragType getDragType() {
        return this.dragType;
    }

    private DragType mapDragType(org.bukkit.event.inventory.DragType dragType) {
        switch (dragType) {
            case EVEN: return DragType.EVEN;
            case SINGLE: return DragType.SINGLE;
        }
        throw new UnsupportedOperationException("Can't map drag type " + dragType);
    }
}
