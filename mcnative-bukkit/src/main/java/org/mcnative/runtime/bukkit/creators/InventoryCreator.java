package org.mcnative.runtime.bukkit.creators;

import org.bukkit.Bukkit;
import org.bukkit.inventory.InventoryHolder;
import org.mcnative.runtime.api.service.inventory.Inventory;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.bukkit.inventory.BukkitInventoryHolder;
import org.mcnative.runtime.bukkit.inventory.type.BukkitChestInventory;

import java.util.function.Function;

public class InventoryCreator implements Function<Object[], Inventory> {

    @Override
    public Inventory apply(Object[] parameters) {
        InventoryOwner owner = parameters.length > 0 && parameters[0] instanceof InventoryOwner ? (InventoryOwner) parameters[0] : null;
        int size = parameters.length > 1 && parameters[1] instanceof Integer ? (int) parameters[1] : 27;
        String title = parameters.length > 2 && parameters[2] instanceof String ? (String) parameters[2] : "Chest";
        InventoryHolder holder = owner != null ? new BukkitInventoryHolder(owner) : null;
        return new BukkitChestInventory<>(owner, Bukkit.createInventory(holder, size, title));
    }
}
