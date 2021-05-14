package org.mcnative.runtime.bukkit.plugin.dependency;

import java.net.URL;
import java.net.URLClassLoader;

public class BukkitDependencyClassLoader extends URLClassLoader {

    public BukkitDependencyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls,parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}

