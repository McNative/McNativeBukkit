package org.mcnative.runtime.bukkit.plugin.dependency;

import java.net.URL;
import java.net.URLClassLoader;

public class URLDependencyClassLoader extends URLClassLoader {

    public URLDependencyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls,parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}

