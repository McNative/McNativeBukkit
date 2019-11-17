/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 16:58
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

import org.bukkit.inventory.Inventory;
import org.mcnative.bukkit.inventory.BukkitInventory;
import org.mcnative.service.inventory.InventoryOwner;
import org.mcnative.service.inventory.type.ChestInventory;

public class BukkitChestInventory<I extends Inventory> extends BukkitInventory<I> implements ChestInventory {

    public BukkitChestInventory(InventoryOwner owner, I original) {
        super(owner, original);
    }
}
