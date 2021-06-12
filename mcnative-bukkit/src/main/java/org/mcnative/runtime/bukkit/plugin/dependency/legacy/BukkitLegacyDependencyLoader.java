package org.mcnative.runtime.bukkit.plugin.dependency.legacy;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import net.pretronic.libraries.utility.interfaces.Destroyable;
import org.mcnative.runtime.bukkit.plugin.dependency.URLDependencyClassLoader;

import java.net.URL;

public class BukkitLegacyDependencyLoader implements DependencyClassLoader, Destroyable {

    private final BukkitMiddlewareClassMap middlewareClassMap;

    public BukkitLegacyDependencyLoader() {
        middlewareClassMap = BukkitMiddlewareClassMap.inject();
    }

    @Override
    public ClassLoader load(ClassLoader classLoader, URL url) {
        URLDependencyClassLoader loader = new URLDependencyClassLoader(new URL[]{url},getClass().getClassLoader());
        middlewareClassMap.addDependencyLoader(loader);
        return loader;
    }

    @Override
    public void destroy() {
        middlewareClassMap.reset();
    }
}
