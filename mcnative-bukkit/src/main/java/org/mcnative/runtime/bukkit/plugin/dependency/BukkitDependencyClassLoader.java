package org.mcnative.runtime.bukkit.plugin.dependency;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitDependencyClassLoader extends URLClassLoader {

    private final JavaPluginLoader loader;
    private final Method getClassByName;
    private final Method addClassToCache;

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public BukkitDependencyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls,parent);

        this.loader = (JavaPluginLoader) ReflectionUtil.getFieldValue(getClass().getClassLoader(),"loader");

        try {
            this.getClassByName = loader.getClass().getDeclaredMethod("getClassByName",String.class);
            this.addClassToCache = loader.getClass().getDeclaredMethod("setClass",String.class,Class.class);

            this.getClassByName.setAccessible(true);
            this.addClassToCache.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("Searching class "+name);
        Class<?> result = classes.get(name);

        if (result == null) {
            result = getClassByName(name);
            if (result == null) {
                result = super.findClass(name);
                if(result == null) throw new ClassNotFoundException();
                addClassToCache(name, result);
                classes.put(name, result);
            }
        }
        return result;
    }

    private Class<?> getClassByName(String name){
        try {
            return (Class<?>) getClassByName.invoke(loader,name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void addClassToCache(String name,Class<?> clazz){
        try {
            addClassToCache.invoke(loader,name,clazz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}

