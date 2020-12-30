/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 16:41
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

import org.bukkit.inventory.HorseInventory;
import org.mcnative.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.type.ArmorableHorseInventory;

public class BukkitArmorableHorseInventory extends BukkitHorseInventory<HorseInventory> implements ArmorableHorseInventory {

    public BukkitArmorableHorseInventory(InventoryOwner owner, HorseInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack getArmor() {
        return new BukkitItemStack(this.original.getArmor());
    }

    @Override
    public void setArmor(ItemStack stack) {
        this.original.setArmor(((BukkitItemStack)stack).getOriginal());
    }
}
