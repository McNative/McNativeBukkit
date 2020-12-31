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

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.inventory.MinecraftPlayerInventoryClickEvent;
import org.mcnative.runtime.api.service.inventory.ClickType;
import org.mcnative.runtime.api.service.inventory.InventoryAction;
import org.mcnative.runtime.api.service.inventory.SlotType;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;

public class BukkitPlayerInventoryClickEvent extends BukkitPlayerInventoryInteractEvent<InventoryClickEvent> implements MinecraftPlayerInventoryClickEvent {

    private final InventoryAction action;
    private final ClickType clickType;
    private final ItemStack clickedItem;
    private final ItemStack cursor;
    private final SlotType slotType;

    public BukkitPlayerInventoryClickEvent(InventoryClickEvent original, Player player) {
        super(original, player);
        this.action = mapInventoryAction(original.getAction());
        this.clickType = mapClickType(original.getClick());
        this.clickedItem = original.getCurrentItem() == null ? null : new BukkitItemStack(original.getCurrentItem());
        this.cursor = original.getCursor() == null ? null : new BukkitItemStack(original.getCursor());
        this.slotType = mapSlotType(original.getSlotType());
    }

    @Override
    public InventoryAction getAction() {
        return this.action;
    }

    @Override
    public ClickType getClickType() {
        return this.clickType;
    }

    @Override
    public ItemStack getClickedItem() {
        return this.clickedItem;
    }

    @Override
    public ItemStack getCursor() {
        return this.cursor;
    }

    @Override
    public int getHotbarButton() {
        return getOriginal().getHotbarButton();
    }

    @Override
    public int getRawSlot() {
        return getOriginal().getRawSlot();
    }

    @Override
    public int getSlot() {
        return getOriginal().getSlot();
    }

    @Override
    public SlotType getSlotType() {
        return this.slotType;
    }

    @Override
    public boolean isLeftClick() {
        return getOriginal().isLeftClick();
    }

    @Override
    public boolean isRightClick() {
        return getOriginal().isRightClick();
    }

    @Override
    public boolean isShiftClick() {
        return getOriginal().isShiftClick();
    }

    private InventoryAction mapInventoryAction(org.bukkit.event.inventory.InventoryAction action) {
        switch (action) {
            case NOTHING: return InventoryAction.NOTHING;
            case PLACE_ALL: return InventoryAction.PLACE_ALL;
            case PLACE_ONE: return InventoryAction.PLACE_ONE;
            case PLACE_SOME: return InventoryAction.PLACE_SOME;
            case PICKUP_ALL: return InventoryAction.PICKUP_ALL;
            case PICKUP_ONE: return InventoryAction.PICKUP_ONE;
            case CLONE_STACK: return InventoryAction.CLONE_STACK;
            case HOTBAR_SWAP: return InventoryAction.HOTBAR_SWAP;
            case PICKUP_HALF: return InventoryAction.PICKUP_HALF;
            case COLLECT_TO_CURSOR: return InventoryAction.COLLECT_TO_CURSOR;
            case DROP_ALL_CURSOR: return InventoryAction.DROP_ALL_CURSOR;
            case PICKUP_SOME: return InventoryAction.PICKUP_SOME;
            case DROP_ALL_SLOT: return InventoryAction.DROP_ALL_SLOT;
            case DROP_ONE_SLOT: return InventoryAction.DROP_ONE_SLOT;
            case DROP_ONE_CURSOR: return InventoryAction.DROP_ONE_CURSOR;
            case SWAP_WITH_CURSOR: return InventoryAction.SWAP_WITH_CURSOR;
            case HOTBAR_MOVE_AND_READD: return InventoryAction.HOTBAR_MOVE_AND_READD;
            case MOVE_TO_OTHER_INVENTORY: return InventoryAction.MOVE_TO_OTHER_INVENTORY;
            default: return InventoryAction.UNKNOWN;
        }
    }

    private ClickType mapClickType(org.bukkit.event.inventory.ClickType clickType) {
        switch (clickType) {
            case LEFT: return ClickType.LEFT;
            case DROP: return ClickType.DROP;
            case RIGHT: return ClickType.RIGHT;
            case MIDDLE: return ClickType.MIDDLE;
            case CREATIVE: return ClickType.CREATIVE;
            case NUMBER_KEY: return ClickType.NUMBER_KEY;
            case SHIFT_LEFT: return ClickType.SHIFT_LEFT;
            case SHIFT_RIGHT: return ClickType.SHIFT_RIGHT;
            case CONTROL_DROP: return ClickType.CONTROL_DROP;
            case DOUBLE_CLICK: return ClickType.DOUBLE_CLICK;
            case WINDOW_BORDER_LEFT: return ClickType.WINDOW_BORDER_LEFT;
            case WINDOW_BORDER_RIGHT: return ClickType.WINDOW_BORDER_RIGHT;
            default: return ClickType.UNKNOWN;
        }
    }

    private SlotType mapSlotType(InventoryType.SlotType slotType) {
        switch (slotType) {
            case FUEL: return SlotType.FUEL;
            case ARMOR: return SlotType.ARMOR;
            case RESULT: return SlotType.RESULT;
            case OUTSIDE: return SlotType.OUTSIDE;
            case CRAFTING: return SlotType.CRAFTING;
            case QUICKBAR: return SlotType.QUICKBAR;
            case CONTAINER: return SlotType.CONTAINER;
        }
        throw new UnsupportedOperationException("Can't map slot type " + slotType);
    }
}
