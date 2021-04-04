package org.mcnative.runtime.bukkit.creators;

import net.pretronic.libraries.utility.Validate;
import org.bukkit.inventory.ItemStack;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;

import java.util.function.Function;

public class BukkitItemStackCreator implements Function<Object[], ItemStack> {

    @Override
    public ItemStack apply(Object[] parameters) {
        if(parameters.length == 0 || parameters[0] == null) return null;
        Validate.isTrue(parameters.length == 1 && parameters[0] instanceof BukkitItemStack, "Not valid inputs to convert to ItemStack from Bukkit");
        return ((BukkitItemStack)parameters[0]).getOriginal();
    }
}
