package org.mcnative.runtime.bukkit.entity;

import org.mcnative.runtime.api.service.MinecraftService;
import org.mcnative.runtime.api.service.entity.Item;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.bukkit.BukkitService;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.bukkit.world.BukkitWorld;

import java.util.UUID;

public class BukkitItem implements Item, BukkitEntity<org.bukkit.entity.Item> {

    private final org.bukkit.entity.Item original;
    private final BukkitWorld world;

    public BukkitItem(org.bukkit.entity.Item original) {
        this.original = original;
        this.world = (BukkitWorld) ((BukkitService) MinecraftService.getInstance()).getMappedWorld(original.getWorld());
    }

    @Override
    public ItemStack getItemStack() {
        return new BukkitItemStack(original.getItemStack());
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        original.setItemStack(itemStack == null ? null : ((BukkitItemStack)itemStack).getOriginal());
    }

    @Override
    public int getPickupDelay() {
        return original.getPickupDelay();
    }

    @Override
    public void setPickupDelay(int i) {
        original.setPickupDelay(i);
    }

    @Override
    public boolean canMobPickup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCanMobPickup(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID getThrower() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setThrower(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.bukkit.entity.Item getOriginal() {
        return this.original;
    }

    @Override
    public BukkitWorld getBukkitWorld() {
        return this.world;
    }
}
