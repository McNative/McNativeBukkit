package org.mcnative.runtime.bukkit.inventory.item.data;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.service.inventory.item.data.SkullItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.bukkit.utils.GameProfileUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class BukkitSkullItemData extends BukkitItemData<SkullMeta> implements SkullItemData {

    public BukkitSkullItemData(Material material, SkullMeta original) {
        super(material, original);
    }

    @Override
    public boolean hasOwner() {
        return getOriginal().hasOwner();
    }

    @Override
    public MinecraftPlayer getOwningPlayer() {
        OfflinePlayer player = getOriginal().getOwningPlayer();
        if(player == null) return null;
        return McNative.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }

    @Override
    public GameProfile getGameProfile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SkullItemData setOwningPlayer(MinecraftPlayer minecraftPlayer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SkullItemData setGameProfile(GameProfile gameProfile) {
        ReflectionUtil.changeFieldValue(getOriginal(),"profile", mapGameProfile(gameProfile));
        return this;
    }

    private Object mapGameProfile(GameProfile gameProfile) {
        if(gameProfile == null) return null;
        Object profile = newGameProfile(gameProfile.getUniqueId(), gameProfile.getName());
        for (GameProfile.Property property : gameProfile.getProperties()) {
            Object propertyMap;
            try {
                propertyMap = GameProfileUtil.getGameProfilePropertiesField().get(profile);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't get properties field for class com.mojang.authlib.GameProfile", e);
            }
            Object propertyMapProperties;
            try {
                propertyMapProperties = GameProfileUtil.getPropertyMapPropertiesField().get(propertyMap);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't get properties field for class com.mojang.authlib.properties.PropertyMap", e);
            }
            Object mappedProperty = newProperty(property);
            try {
                GameProfileUtil.getMultimapPutMethod().invoke(propertyMapProperties, property.getName(), mappedProperty);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Can't invoke put method for GameProfile property map");
            }
        }
        return profile;
    }

    private Object newGameProfile(UUID id, String name) {
        try {
            return GameProfileUtil.getGameProfileConstructor().newInstance(id, name);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Can't create instance for class com.mojang.authlib.GameProfile");
        }
    }

    private Object newProperty(GameProfile.Property property) {
        try {
            return GameProfileUtil.getPropertyConstructor().newInstance(property.getName(), property.getValue(), property.getSignature());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("Can't create instance for class com.mojang.authlib.properties.Property");
        }
    }
}
