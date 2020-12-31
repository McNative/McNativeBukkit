/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 13.04.20, 20:37
 * @web %web%
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.runtime.bukkit.plugin.mapped;

import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.libraries.plugin.loader.PluginLoader;
import net.pretronic.libraries.plugin.loader.classloader.PluginClassLoader;
import net.pretronic.libraries.plugin.manager.PluginManager;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.exception.OperationFailedException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.runtime.api.McNative;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BukkitPluginLoader implements PluginLoader {


    private final File location;
    private PluginDescription description;
    private Plugin<?> plugin;
    private org.bukkit.plugin.Plugin original;

    public BukkitPluginLoader(org.bukkit.plugin.Plugin original) {
        this.original = original;
        this.description = new BukkitPluginDescription(original.getDescription());
        this.plugin = new BukkitPlugin(original,this,description);
        this.location = (File) ReflectionUtil.getFieldValue(original.getClass().getClassLoader(),"file");
    }

    public BukkitPluginLoader(File location,PluginDescription description) {
        this.location = location;
        this.description = description;
    }

    public org.bukkit.plugin.Plugin getOriginal() {
        return original;
    }

    @Override
    public File getLocation() {
        return location;
    }

    @Override
    public PluginManager getPluginManager() {
        return McNative.getInstance().getPluginManager();
    }

    @Override
    public PluginDescription getDescription() {
        if(description == null) throw new OperationFailedException("Description is manged by platform, the plugin must be loaded.");
        return description;
    }

    @Override
    public PluginClassLoader getClassLoader() {
        throw new UnsupportedOperationException("Class loader is not available");
    }

    @Override
    public boolean isInstanceAvailable() {
        return original != null;
    }

    @Override
    public boolean isMainClassAvailable() {
        throw new UnsupportedOperationException("Can not be used, is managed by server platform");
    }

    @Override
    public boolean isEnabled() {
        return original != null && original.isEnabled();
    }

    @Override
    public void executeLifeCycleState(String s) {
        //Not supported without exception
    }

    @Override
    public void executeLifeCycleState(String s, LifecycleState lifecycleState) {
        //Not supported without exception
    }

    @Override
    public Plugin<?> getInstance() {
        return plugin;
    }

    @Override
    public Plugin<?> enable() {
        if(!isInstanceAvailable()) load();
        bootstrap();
        return plugin;
    }

    @Override
    public void disable() {
        if(original.isEnabled()) shutdown();
        unload();
    }

    @Override
    public Plugin<?> construct() {
        if(isInstanceAvailable()){
            throw new OperationFailedException("Instance is already created");
        }
        return null;
    }

    @Override
    public void initialize() {
        //Unused
    }

    @Override
    public void load() {
        try {
            original = Bukkit.getPluginManager().loadPlugin(location);
            if(original == null) throw new IllegalArgumentException("Received invalid plugin");
            original.onLoad();
            description = new BukkitPluginDescription(original.getDescription());
            plugin = new BukkitPlugin(original,this,description);
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public void bootstrap() {
        Bukkit.getPluginManager().enablePlugin(original);
    }

    @Override
    public void shutdown() {
        Bukkit.getPluginManager().disablePlugin(original);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unload() {
        if(isEnabled()) shutdown();

        List<org.bukkit.plugin.Plugin> plugins = (List<org.bukkit.plugin.Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"plugins");
        Map<String, org.bukkit.plugin.Plugin> names = (Map<String, org.bukkit.plugin.Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"lookupNames");

        ClassLoader classLoader = original.getClass().getClassLoader();

        names.remove(original.getName());
        plugins.remove(original);

        if (classLoader instanceof URLClassLoader) {
            ReflectionUtil.changeFieldValue(classLoader,"plugin",null);
            ReflectionUtil.changeFieldValue(classLoader,"pluginInit",null);
            Map<String, Class<?>> classes = (Map<String, Class<?>>) ReflectionUtil.getFieldValue(classLoader,"classes");
            classes.clear();
            clearCachedClasses(classLoader);

            try {
                ((URLClassLoader) classLoader).close();
            } catch (IOException ignored) {}
        }

        original = null;
        plugin = null;

        System.gc();//Execute garbage collector
    }

    @SuppressWarnings("unchecked")
    private void clearCachedClasses(ClassLoader classLoader){
        Map<Pattern, org.bukkit.plugin.PluginLoader> loaders = (Map<Pattern, org.bukkit.plugin.PluginLoader>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"fileAssociations");
        for (Map.Entry<Pattern, org.bukkit.plugin.PluginLoader> loader : loaders.entrySet()) {
            if(loader.getValue() instanceof JavaPluginLoader){
                Map<String, Class<?>> classes = (Map<String, Class<?>>) ReflectionUtil.getFieldValue(loader.getValue(),"classes");
                Iterators.removeSilent(classes.entrySet(), entry -> entry.getValue().getClassLoader().equals(classLoader));
            }
        }
    }

    public void destroy(){
        plugin = null;
        original = null;
    }
}
