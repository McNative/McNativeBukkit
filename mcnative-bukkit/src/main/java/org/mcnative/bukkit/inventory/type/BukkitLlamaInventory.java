/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 16.11.19, 14:37
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

import org.mcnative.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.service.inventory.InventoryOwner;
import org.mcnative.service.inventory.item.ItemStack;
import org.mcnative.service.inventory.type.LlamaInventory;

public class BukkitLlamaInventory extends BukkitHorseInventory<org.bukkit.inventory.LlamaInventory> implements LlamaInventory {

    public BukkitLlamaInventory(InventoryOwner owner, org.bukkit.inventory.LlamaInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getDecor() {
        return new BukkitItemStack(this.original.getDecor());
    }

    @Override
    public void setDecor(ItemStack stack) {
        this.original.setDecor(((BukkitItemStack)stack).getOriginal());
    }
}
