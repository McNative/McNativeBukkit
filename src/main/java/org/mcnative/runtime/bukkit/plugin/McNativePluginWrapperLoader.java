/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 27.04.20, 19:18
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

package org.mcnative.runtime.bukkit.plugin;

import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.event.execution.ExecutionType;
import net.pretronic.libraries.event.executor.MethodEventExecutor;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.runtime.api.McNative;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class McNativePluginWrapperLoader implements PluginLoader {

    private final JavaPluginLoader original;

    public McNativePluginWrapperLoader(JavaPluginLoader original) {
        this.original = original;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        return original.loadPlugin(file);
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        return original.getPluginDescription(file);
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return original.getPluginFileFilters();
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        Validate.notNull(plugin, "Plugin can not be null");
        Validate.notNull(listener, "Listener can not be null");

        EventBus eventBus = McNative.getInstance().getLocal().getEventBus();

        boolean useTimings = Bukkit.getPluginManager().useTimings();
        Map<Class<? extends Event>, Set<RegisteredListener>> result = new HashMap<>();

        Class<?> eventClass = listener.getClass();
        while (eventClass != null) {
            registerMethods(plugin, eventBus, eventClass, listener, result, useTimings);
            eventClass = eventClass.getSuperclass();
        }
        return result;
    }

    private void registerMethods(Plugin plugin, EventBus eventBus, Class<?> eventsClass, Listener listener, Map<Class<? extends Event>, Set<RegisteredListener>> result, boolean useTimings) {
        for (final Method method : eventsClass.getDeclaredMethods()) {
            method.setAccessible(true);
            final EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler != null && !method.isBridge() && !method.isSynthetic() && method.getParameterTypes().length == 1) {
                Class<?> eventClass = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(eventClass)) {//McNative event
                    Class<?> mappedClass = eventBus.getMappedClass(eventClass);
                    if (mappedClass == null) mappedClass = eventClass;

                    ObjectOwner mappedOwner;
                    if (McNative.getInstance().getPluginManager() instanceof BukkitPluginManager) {
                        mappedOwner = ((BukkitPluginManager) McNative.getInstance().getPluginManager()).getMappedPlugin(plugin);
                    } else {
                        mappedOwner = new BukkitPluginOwner(plugin);
                    }
                    eventBus.addExecutor(mappedClass, new MethodEventExecutor(mappedOwner, mapPriority(eventHandler.priority()), ExecutionType.BLOCKING, listener, eventClass, method));
                } else { //Bukkit Event
                    Class<? extends Event> directClass = eventClass.asSubclass(Event.class);
                    Set<RegisteredListener> eventSet = result.computeIfAbsent(directClass, k -> new HashSet<>());
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
                        eventSet.add(new TimedRegisteredListener(listener, executor, eventHandler.priority(), plugin, eventHandler.ignoreCancelled()));
                    } else {
                        eventSet.add(new RegisteredListener(listener, executor, eventHandler.priority(), plugin, eventHandler.ignoreCancelled()));
                    }
                }
            }
        }
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        original.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        original.disablePlugin(plugin);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else return original.equals(obj);
    }

    private byte mapPriority(EventPriority priority){
        if(priority == EventPriority.HIGHEST) return net.pretronic.libraries.event.EventPriority.HIGHEST;
        else if(priority == EventPriority.HIGH) return net.pretronic.libraries.event.EventPriority.HIGH;
        else if(priority == EventPriority.LOW) return net.pretronic.libraries.event.EventPriority.LOW;
        else if(priority == EventPriority.LOWEST) return net.pretronic.libraries.event.EventPriority.LOW;
        else return net.pretronic.libraries.event.EventPriority.LOWEST;
    }
}
