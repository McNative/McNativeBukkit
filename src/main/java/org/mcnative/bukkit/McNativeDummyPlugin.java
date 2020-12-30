/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 03.04.20, 11:45
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

package org.mcnative.bukkit;

import net.pretronic.libraries.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class McNativeDummyPlugin implements Plugin {

    private final PluginLoader pluginLoader;
    private final PluginDescriptionFile description;
    private boolean enabled;

    protected McNativeDummyPlugin(String version) {
        this.pluginLoader = new DummyClassLoader();
        this.description = new PluginDescriptionFile("McNative",version,getClass().getName());
        this.enabled = true;
    }

    @Override
    public File getDataFolder() {
        return new File("plugins/McNative");
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public FileConfiguration getConfig() {
        throw new UnsupportedOperationException("McNative dummy plugin is not able to provide a configuration");
    }

    @Override
    public InputStream getResource(String s) {
        return null;//Unused
    }

    @Override
    public void saveConfig() {
        //unused
    }

    @Override
    public void saveDefaultConfig() {
        //Unused
    }

    @Override
    public void saveResource(String s, boolean b) {
        //Unused
    }

    @Override
    public void reloadConfig() {
        //Unused
    }

    @Override
    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable() {
        McNativeLauncher.shutdown();
    }

    @Override
    public void onLoad() {
        McNativeLauncher.launchMcNativeInternal(this);
    }

    @Override
    public void onEnable() {
        //Unused
    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean b) {
        //Unused
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
        throw new UnsupportedOperationException("McNative dummy plugin is not able to provide a world generator");
    }

    @Override
    public Logger getLogger() {
        return Bukkit.getLogger();
    }

    @Override
    public String getName() {
        return "McNative";
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;      //Unused
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;      //Unused
    }

    public class DummyClassLoader implements PluginLoader {

        @Override
        public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        @Override
        public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        @Override
        public Pattern[] getPluginFileFilters() {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        /*
         * Method copied and optimized from Bukkit JavaPluginLoader
         * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/plugin/java/JavaPluginLoader.java
         */
        @Override
        public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
            Validate.notNull(plugin, "Plugin can not be null");
            Validate.notNull(listener, "Listener can not be null");

            boolean useTimings = Bukkit.getServer().getPluginManager().useTimings();
            Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
            Set<Method> methods;
            try {
                Method[] publicMethods = listener.getClass().getMethods();
                Method[] privateMethods = listener.getClass().getDeclaredMethods();
                methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
                Collections.addAll(methods, publicMethods);
                Collections.addAll(methods, privateMethods);
            } catch (NoClassDefFoundError e) {
                plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
                return ret;
            }

            for (final Method method : methods) {
                final EventHandler eh = method.getAnnotation(EventHandler.class);
                if (eh == null) continue;
                if (method.isBridge() || method.isSynthetic())  continue;
                final Class<?> checkClass;
                if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                    plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                    continue;
                }
                final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
                method.setAccessible(true);
                Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new HashSet<>());

                EventExecutor executor = (listener1, event) -> {
                    try {
                        if (!eventClass.isAssignableFrom(event.getClass())) return;
                        method.invoke(listener1, event);
                    } catch (InvocationTargetException ex) {
                        throw new EventException(ex.getCause());
                    } catch (Throwable t) {
                        throw new EventException(t);
                    }
                };
                if (useTimings) {
                    eventSet.add(new TimedRegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
                } else {
                    eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
                }
            }
            return ret;
        }

        @Override
        public void enablePlugin(Plugin plugin) {
            if(plugin.equals(McNativeDummyPlugin.this)){
                McNativeLauncher.launchMcNativeInternal(plugin);
                McNativeDummyPlugin.this.enabled = true;
            }else throw new IllegalArgumentException("This is not a McNative dummy plugin");
        }

        @Override
        public void disablePlugin(Plugin plugin) {
            if(plugin.equals(McNativeDummyPlugin.this)){
                Bukkit.getServer().getPluginManager().callEvent(new PluginDisableEvent(plugin));
                McNativeLauncher.shutdown();
                McNativeDummyPlugin.this.enabled = false;
            }else throw new IllegalArgumentException("This is not a McNative dummy plugin");
        }
    }
}
