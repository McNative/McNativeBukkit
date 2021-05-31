package org.mcnative.runtime.bukkit.plugin.dependency;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import net.pretronic.libraries.utility.reflect.ReflectException;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class LegacyReflectedDependencyClassLoader implements DependencyClassLoader {
    private static final Method METHOD_ADD_URL;

    public LegacyReflectedDependencyClassLoader() {
    }

    public ClassLoader load(ClassLoader parent, URL location) {
        if(parent == null) parent = Bukkit.class.getClassLoader();
        try {
            METHOD_ADD_URL.invoke(getClass().getClassLoader(), location);
            return parent;
        } catch (InvocationTargetException | IllegalAccessException var4) {
            throw new ReflectException(var4);
        }
    }

    static {
        try {
            METHOD_ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            METHOD_ADD_URL.setAccessible(true);
        } catch (NoSuchMethodException var1) {
            throw new ExceptionInInitializerError(var1);
        }
    }
}
