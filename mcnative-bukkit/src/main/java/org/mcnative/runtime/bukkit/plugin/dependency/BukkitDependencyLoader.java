package org.mcnative.runtime.bukkit.plugin.dependency;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;

import java.net.URL;

public class BukkitDependencyLoader implements DependencyClassLoader {

    private final BukkitMiddlewareClassMap middlewareClassMap;

    public BukkitDependencyLoader(BukkitMiddlewareClassMap middlewareClassMap) {
        this.middlewareClassMap = middlewareClassMap;
    }

    @Override
    public ClassLoader load(ClassLoader classLoader, URL url) {
        BukkitDependencyClassLoader loader = new BukkitDependencyClassLoader(new URL[]{url},getClass().getClassLoader());
        middlewareClassMap.addDependencyLoader(loader);
        return loader;
    }
}
