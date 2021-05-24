package org.mcnative.runtime.bukkit.inventory.item.data;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.service.inventory.item.data.SkullItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BukkitSkullItemData extends BukkitItemData<SkullMeta> implements SkullItemData {

    private static final Class<?> GAME_PROFILE_CLASS;
    private static final Constructor<?> GAME_PROFILE_CONSTRUCTOR;
    private static final Field GAME_PROFILE_PROPERTIES_FIELD;

    private static final Class<?> MULTI_MAP_CLASS;

    private static final Class<?> PROPERTY_MAP_CLASS;
    private static final Field PROPERTY_MAP_PROPERTIES_FIELD;
    private static final Method MULTIMAP_PUT_METHOD;

    private static final Class<?> PROPERTY_CLASS;
    private static final Constructor<?> PROPERTY_CONSTRUCTOR;

    static {
        try {
            GAME_PROFILE_CLASS = Class.forName("com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't get class com.mojang.authlib.GameProfile", e);
        }


        try {
            GAME_PROFILE_CONSTRUCTOR = GAME_PROFILE_CLASS.getDeclaredConstructor(UUID.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't get constructor for class com.mojang.authlib.GameProfile", e);
        }
        GAME_PROFILE_PROPERTIES_FIELD = ReflectionUtil.getField(GAME_PROFILE_CLASS, "properties");


        try {
            PROPERTY_MAP_CLASS = Class.forName("com.mojang.authlib.properties.PropertyMap");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't get class com.mojang.authlib.properties.PropertyMap", e);
        }
        PROPERTY_MAP_PROPERTIES_FIELD = ReflectionUtil.getField(PROPERTY_MAP_CLASS, "properties");


        try {
            MULTI_MAP_CLASS = Class.forName("com.google.common.collect.Multimap");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't get class com.google.common.collect.Multimap", e);
        }
        MULTIMAP_PUT_METHOD = ReflectionUtil.getMethod(MULTI_MAP_CLASS, "put", new Class[]{Object.class, Object.class});


        try {
            PROPERTY_CLASS = Class.forName("com.mojang.authlib.properties.Property");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't get class com.mojang.authlib.properties.Property", e);
        }
        try {
            PROPERTY_CONSTRUCTOR = PROPERTY_CLASS.getConstructor(String.class, String.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't get constructor for class com.mojang.authlib.properties.Property", e);
        }
    }

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
                propertyMap = GAME_PROFILE_PROPERTIES_FIELD.get(profile);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't get properties field for class com.mojang.authlib.GameProfile", e);
            }
            Object propertyMapProperties;
            try {
                propertyMapProperties = PROPERTY_MAP_PROPERTIES_FIELD.get(propertyMap);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't get properties field for class com.mojang.authlib.properties.PropertyMap", e);
            }
            Object mappedProperty = newProperty(property);
            try {
                MULTIMAP_PUT_METHOD.invoke(propertyMapProperties, property.getName(), mappedProperty);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Can't invoke put method for GameProfile property map");
            }
        }
        return profile;
    }

    private Object newGameProfile(UUID id, String name) {
        try {
            return GAME_PROFILE_CONSTRUCTOR.newInstance(id, name);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Can't create instance for class com.mojang.authlib.GameProfile");
        }
    }

    private Object newProperty(GameProfile.Property property) {
        try {
            return PROPERTY_CONSTRUCTOR.newInstance(property.getName(), property.getValue(), property.getSignature());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("Can't create instance for class com.mojang.authlib.properties.Property");
        }
    }
}
