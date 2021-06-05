package org.mcnative.runtime.bukkit.plugin.dependency.modern;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.bukkit.plugin.dependency.URLDependencyClassLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitModernDependencyLoader extends ClassLoader implements DependencyClassLoader {

    private final Map<String, Class<?>> dependencyClasses;
    private final List<URLDependencyClassLoader> dependencyLoaders;

    public BukkitModernDependencyLoader() {
        super(McNativeLauncher.class.getClassLoader());
        this.dependencyClasses = new ConcurrentHashMap<>();
        this.dependencyLoaders = new ArrayList<>();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) {
        Class<?> clazz = this.dependencyClasses.get(name);
        if(clazz == null){
            for (URLDependencyClassLoader loader : dependencyLoaders) {
                try {
                    clazz = loader.findClass(name);
                    this.dependencyClasses.put(name,clazz);
                    return clazz;
                } catch (ClassNotFoundException ignored) {}
            }
        }
        return clazz;
    }

    @Override
    public ClassLoader load(ClassLoader loader0, URL location) {
        URLDependencyClassLoader loader = new URLDependencyClassLoader(new URL[]{location},getClass().getClassLoader());
        dependencyLoaders.add(loader);
        return loader;
    }
}
