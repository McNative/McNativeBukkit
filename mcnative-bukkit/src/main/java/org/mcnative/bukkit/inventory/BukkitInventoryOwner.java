/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 16.11.19, 15:55
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

package org.mcnative.bukkit.inventory;

import org.bukkit.inventory.InventoryHolder;
import org.mcnative.service.inventory.Inventory;
import org.mcnative.service.inventory.InventoryOwner;

public class BukkitInventoryOwner implements InventoryOwner, InventoryHolder {

    private final InventoryOwner owner;

    public BukkitInventoryOwner(InventoryOwner owner) {
        this.owner = owner;
    }


    @Override
    public Inventory getLinkedInventory() {
        return null;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        return null;
    }
}
