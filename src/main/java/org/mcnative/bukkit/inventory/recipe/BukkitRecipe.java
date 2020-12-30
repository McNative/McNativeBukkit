package org.mcnative.bukkit.inventory.recipe;

import org.mcnative.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.recipe.Recipe;

public class BukkitRecipe implements Recipe {

    private final org.bukkit.inventory.Recipe original;

    public BukkitRecipe(org.bukkit.inventory.Recipe original) {
        this.original = original;
    }

    @Override
    public ItemStack getResult() {
        return new BukkitItemStack(original.getResult());
    }
}
