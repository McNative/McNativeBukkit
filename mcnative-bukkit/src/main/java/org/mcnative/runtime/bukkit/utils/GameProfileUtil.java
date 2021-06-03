package org.mcnative.runtime.bukkit.utils;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.runtime.api.player.profile.GameProfile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class GameProfileUtil {

    private static final Class<?> GAME_PROFILE_CLASS;
    private static final Constructor<?> GAME_PROFILE_CONSTRUCTOR;
    private static final Field GAME_PROFILE_PROPERTIES_FIELD;

    private static final Class<?> MULTI_MAP_CLASS;

    private static final Class<?> PROPERTY_MAP_CLASS;
    private static final Field PROPERTY_MAP_PROPERTIES_FIELD;
    private static final Method MULTIMAP_PUT_METHOD;
    private static final Method MULTIMAP_AS_MAP_METHOD;

    private static final Class<?> PROPERTY_CLASS;
    private static final Field PROPERTY_NAME_FIELD;
    private static final Field PROPERTY_VALUE_FIELD;
    private static final Field PROPERTY_SIGNATURE_FIELD;
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
        GAME_PROFILE_PROPERTIES_FIELD.setAccessible(true);


        try {
            PROPERTY_MAP_CLASS = Class.forName("com.mojang.authlib.properties.PropertyMap");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't get class com.mojang.authlib.properties.PropertyMap", e);
        }
        PROPERTY_MAP_PROPERTIES_FIELD = ReflectionUtil.getField(PROPERTY_MAP_CLASS, "properties");
        PROPERTY_MAP_PROPERTIES_FIELD.setAccessible(true);


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

        MULTIMAP_AS_MAP_METHOD = ReflectionUtil.getMethod(MULTI_MAP_CLASS, "asMap");


        PROPERTY_NAME_FIELD = ReflectionUtil.getField(PROPERTY_CLASS, "name");
        PROPERTY_NAME_FIELD.setAccessible(true);

        PROPERTY_VALUE_FIELD = ReflectionUtil.getField(PROPERTY_CLASS, "value");
        PROPERTY_VALUE_FIELD.setAccessible(true);

        PROPERTY_SIGNATURE_FIELD = ReflectionUtil.getField(PROPERTY_CLASS, "signature");
        PROPERTY_SIGNATURE_FIELD.setAccessible(true);
    }

    public static Class<?> getGameProfileClass() {
        return GAME_PROFILE_CLASS;
    }

    public static Constructor<?> getGameProfileConstructor() {
        return GAME_PROFILE_CONSTRUCTOR;
    }

    public static Field getGameProfilePropertiesField() {
        return GAME_PROFILE_PROPERTIES_FIELD;
    }

    public static Class<?> getMultiMapClass() {
        return MULTI_MAP_CLASS;
    }

    public static Class<?> getPropertyMapClass() {
        return PROPERTY_MAP_CLASS;
    }

    public static Field getPropertyMapPropertiesField() {
        return PROPERTY_MAP_PROPERTIES_FIELD;
    }

    public static Method getMultimapPutMethod() {
        return MULTIMAP_PUT_METHOD;
    }

    public static Class<?> getPropertyClass() {
        return PROPERTY_CLASS;
    }

    public static Constructor<?> getPropertyConstructor() {
        return PROPERTY_CONSTRUCTOR;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Collection<Object>> getPropertyMapEntries(Object propertyMap) {
        try {
            return (Map<String, Collection<Object>>) MULTIMAP_AS_MAP_METHOD.invoke(propertyMap);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't invoke asMap method for GameProfile property map", e);
        }
    }

    public static GameProfile.Property mapProperty(Object property) {
        try {
            return new GameProfile.Property((String) PROPERTY_NAME_FIELD.get(property), (String) PROPERTY_VALUE_FIELD.get(property),
                    (String) PROPERTY_SIGNATURE_FIELD.get(property));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't map property", e);
        }
    }
}
