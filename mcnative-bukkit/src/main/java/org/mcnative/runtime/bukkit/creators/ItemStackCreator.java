package org.mcnative.runtime.bukkit.creators;

import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.inventory.item.material.MaterialProtocolId;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.bukkit.inventory.item.material.protocol.LegacyMaterialProtocolId;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ItemStackCreator implements Function<Object[], ItemStack> {

    private static Method BUKKIT_LEGACY_MATERIAL_BY_ID = null;

    @Override
    public ItemStack apply(Object[] parameters) {
        if(parameters.length == 0 || parameters[0] == null) return null;

        org.bukkit.inventory.ItemStack itemStack;

        if(parameters[0] instanceof org.bukkit.inventory.ItemStack) {
            itemStack = (org.bukkit.inventory.ItemStack) parameters[0];
        } else {
            Material material = (Material) parameters[0];
            org.bukkit.Material bukkitMaterial = null;
            if(McNative.getInstance().getPlatform().getProtocolVersion().isLegacy()) {
                MaterialProtocolId protocolId = material.getProtocolIds().get(McNative.getInstance().getPlatform().getProtocolVersion());
                Validate.notNull(protocolId, "Can't get protocol id for material " + material.getName());
                if(protocolId instanceof LegacyMaterialProtocolId) {
                    LegacyMaterialProtocolId legacyId = (LegacyMaterialProtocolId) protocolId;
                    bukkitMaterial = getLegacyBukkitMaterial(legacyId.getId());
                    itemStack = new org.bukkit.inventory.ItemStack(bukkitMaterial, 1,(short) legacyId.getSubId());
                } else {
                    throw new IllegalArgumentException("Can't cast protocol id to legacy for material " + material.getName());
                }
            } else {
                bukkitMaterial = org.bukkit.Material.getMaterial(material.getName());
                Validate.notNull(bukkitMaterial, "Can't get bukkit material for " + material.getName());
                itemStack = new org.bukkit.inventory.ItemStack(bukkitMaterial);
            }
        }
        return new BukkitItemStack(itemStack);
    }

    private org.bukkit.Material getLegacyBukkitMaterial(int id) {
        if(BUKKIT_LEGACY_MATERIAL_BY_ID == null) {
            BUKKIT_LEGACY_MATERIAL_BY_ID = ReflectionUtil.getMethod(org.bukkit.Material.class, "getMaterial");
            Validate.notNull(BUKKIT_LEGACY_MATERIAL_BY_ID, "Can't get method getMaterial for bukkit legacy material");
        }
        Object value = ReflectionUtil.invokeMethod(BUKKIT_LEGACY_MATERIAL_BY_ID, null, id);
        if(!(value instanceof org.bukkit.Material)) throw new IllegalArgumentException("Can't get bukkit material for id " + id+ "("+value+")");
        return (org.bukkit.Material) value;
    }
}
