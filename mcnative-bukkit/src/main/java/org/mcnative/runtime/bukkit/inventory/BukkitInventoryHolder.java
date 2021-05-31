/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 14:32
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

package org.mcnative.runtime.bukkit.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;

public class BukkitInventoryHolder implements InventoryHolder {

    private final InventoryOwner owner;

    public BukkitInventoryHolder(InventoryOwner owner) {
        this.owner = owner;
    }

    public InventoryOwner getOwner() {
        return owner;
    }

    @Override
    public Inventory getInventory() {
        return ((BukkitInventory<?>)owner.getLinkedInventory()).getOriginal();
    }

    @Override
    public String toString() {
        return "BukkitInventoryHolder{" +
                "owner=" + owner +
                '}';
    }
}
