package org.mcnative.runtime.bukkit.plugin.dependency;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;

import java.net.URL;

public class BukkitDependencyLoader implements DependencyClassLoader {

    @Override
    public ClassLoader load(ClassLoader classLoader, URL url) {
        return new BukkitDependencyUrlClassLoader(new URL[]{url});
    }
}
