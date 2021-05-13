package org.mcnative.runtime.bukkit.plugin.dependency;

import net.pretronic.libraries.utility.reflect.ReflectException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcnative.runtime.bukkit.McNativeLauncher;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitMiddlewareClassMap implements Map<String, Class<?>> {

    private final Map<String, Class<?>> original;
    private final Map<String, Class<?>> dependencyClasses;
    private final List<BukkitDependencyClassLoader> dependencyLoaders;

    public BukkitMiddlewareClassMap(Map<String, Class<?>> original) {
        this.original = original;
        this.dependencyClasses = new ConcurrentHashMap<>();
        this.dependencyLoaders = new ArrayList<>();
    }

    public Map<String, Class<?>> getOriginal() {
        return original;
    }

    public void addDependencyLoader(BukkitDependencyClassLoader loader){
        this.dependencyLoaders.add(loader);
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return original.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return original.containsValue(value);
    }

    @Override
    public Class<?> get(Object key) {
        System.out.println("CLASS LOOKUP "+key);
        Class<?> clazz = original.get(key);
        if(clazz == null) clazz = this.dependencyClasses.get(key);
        if(clazz == null){
            for (BukkitDependencyClassLoader loader : dependencyLoaders) {
                try {
                    clazz = loader.findClass((String) key);
                    this.dependencyClasses.put((String) key,clazz);
                    return clazz;
                } catch (ClassNotFoundException ignored) {}
            }
        }
        return clazz;
    }

    @Nullable
    @Override
    public Class<?> put(String key, Class<?> value) {
        return original.put(key, value);
    }

    @Override
    public Class<?> remove(Object key) {
        return original.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends Class<?>> m) {
        original.putAll(m);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return original.keySet();
    }

    @NotNull
    @Override
    public Collection<Class<?>> values() {
        return original.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Class<?>>> entrySet() {
        return original.entrySet();
    }

    public void reset(){
        JavaPluginLoader loader = (JavaPluginLoader) ReflectionUtil.getFieldValue(McNativeLauncher.class.getClassLoader(),"loader");
        Field field = ReflectionUtil.getField(loader.getClass(),"classes");
        field.setAccessible(true);
        ReflectionUtil.setUnsafeObjectFieldValue(loader,field,original);
    }

    public static BukkitMiddlewareClassMap inject(){
        JavaPluginLoader loader = (JavaPluginLoader) ReflectionUtil.getFieldValue(McNativeLauncher.class.getClassLoader(),"loader");
        Field field = ReflectionUtil.getField(loader.getClass(),"classes");
        field.setAccessible(true);
        Object original;
        try {
            original = field.get(loader);
            BukkitMiddlewareClassMap middleware = new BukkitMiddlewareClassMap((Map<String, Class<?>>) original);
            ReflectionUtil.setUnsafeObjectFieldValue(loader,field,middleware);
            return middleware;
        } catch (IllegalAccessException e) {
            throw new ReflectException(e);
        }
    }
}
