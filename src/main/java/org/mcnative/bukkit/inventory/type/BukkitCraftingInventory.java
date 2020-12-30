/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 16:59
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

import net.pretronic.libraries.utility.Iterators;
import org.mcnative.bukkit.inventory.BukkitInventory;
import org.mcnative.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.bukkit.inventory.recipe.BukkitRecipe;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.recipe.Recipe;
import org.mcnative.runtime.api.service.inventory.type.CraftingInventory;

public class BukkitCraftingInventory extends BukkitInventory<org.bukkit.inventory.CraftingInventory> implements CraftingInventory {

    public BukkitCraftingInventory(InventoryOwner owner, org.bukkit.inventory.CraftingInventory original) {
        super(owner, original);
    }

    @Override
    public ItemStack[] getMatrix() {
        return Iterators.map(this.original.getMatrix(), BukkitItemStack::new);
    }

    @Override
    public void setMatrix(ItemStack[] matrix) {
        this.original.setMatrix(Iterators.map(matrix, itemStack -> ((BukkitItemStack)itemStack).getOriginal()));
    }

    @Override
    public ItemStack getResult() {
        return new BukkitItemStack(this.original.getResult());
    }

    @Override
    public void setResult(ItemStack item) {
        this.original.setResult(((BukkitItemStack)item).getOriginal());
    }

    @Override
    public Recipe getRecipe() {
        return new BukkitRecipe(original.getRecipe());
    }

    @Override
    public void clearItemsOnClose(boolean clear) {

    }
}
