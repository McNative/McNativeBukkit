package org.mcnative.runtime.bukkit.inventory.item.data;

import org.bukkit.inventory.meta.ItemMeta;
import org.mcnative.runtime.api.service.inventory.item.data.ItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Material;

public class BukkitItemData<T extends ItemMeta> implements ItemData {

    private final Material material;
    private final T original;

    public BukkitItemData(Material material, T original) {
        this.material = material;
        this.original = original;
    }

    @Override
    public Material getMaterial() {
        return this.material;
    }

    public T getOriginal() {
        return original;
    }
}
