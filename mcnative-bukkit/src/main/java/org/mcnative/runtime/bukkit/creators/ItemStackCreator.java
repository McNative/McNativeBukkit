package org.mcnative.runtime.bukkit.creators;

import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;

import java.util.function.Function;

public class ItemStackCreator implements Function<Object[], ItemStack> {

    @Override
    public ItemStack apply(Object[] parameters) {
        if(parameters.length == 0 || parameters[0] == null) return null;

        org.bukkit.inventory.ItemStack itemStack;

        if(parameters[0] instanceof org.bukkit.inventory.ItemStack) {
            itemStack = (org.bukkit.inventory.ItemStack) parameters[0];
        } else {
            Material material = (Material) parameters[0];
            org.bukkit.Material bukkitMaterial = null;
            for (org.bukkit.Material value : org.bukkit.Material.values()) {
                if(value.toString().equalsIgnoreCase(material.getName())) {
                    bukkitMaterial = value;
                    break;
                }
            }
            Validate.notNull(bukkitMaterial, "Can't create item stack for " + material + ".");
            itemStack = new org.bukkit.inventory.ItemStack(bukkitMaterial);
        }
        return new BukkitItemStack(itemStack);
    }
}
