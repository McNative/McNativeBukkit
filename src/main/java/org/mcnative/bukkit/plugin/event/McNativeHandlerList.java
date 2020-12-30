/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 08.02.20, 22:56
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

package org.mcnative.bukkit.plugin.event;

import net.pretronic.libraries.event.executor.ConsumerEventExecutor;
import net.pretronic.libraries.event.executor.EventExecutor;
import net.pretronic.libraries.event.executor.MethodEventExecutor;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.mcnative.runtime.api.McNative;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public class McNativeHandlerList extends HandlerList implements org.bukkit.plugin.EventExecutor,Listener {

    private final Class<?> eventClass;
    private final RegisteredListener[] dummyListener;
    private final List<EventExecutor> executors;
    private BiConsumer<McNativeHandlerList, Object> manager;

    public McNativeHandlerList(Plugin plugin, Class<?> eventClass) {
        this.eventClass = eventClass;
        this.executors = new ArrayList<>();
        this.dummyListener = new RegisteredListener[]{new RegisteredListener(this,this, EventPriority.NORMAL,plugin,false)};
    }

    @Override
    public void register(RegisteredListener listener) {
         this.executors.add(new BukkitEventExecutor(eventClass,listener));
    }

    @Override
    public void registerAll(Collection<RegisteredListener> listeners) {
        for (RegisteredListener listener : listeners) register(listener);
    }

    @Override
    public void unregister(RegisteredListener listener) {
        Iterators.removeSilent(executors, executor -> {
            if(executor instanceof BukkitEventExecutor){
                return ((BukkitEventExecutor) executor).getRegistration().equals(listener);
            }
            return false;
        });
    }

    @Override
    public void unregister(Plugin plugin) {
        Iterators.removeSilent(executors, executor -> {
            if(executor instanceof BukkitEventExecutor){
                return ((BukkitEventExecutor) executor).getRegistration().getPlugin().equals(plugin);
            }
            return false;
        });
    }

    @Override
    public void unregister(Listener listener) {
        unregister((Object) listener);
    }

    public void unregister(Object listener){
        Iterators.removeSilent(executors, executor -> {
            if(executor instanceof BukkitEventExecutor){
                return ((BukkitEventExecutor) executor).getRegistration().getListener().equals(listener);
            }else if(executor instanceof MethodEventExecutor){
                return ((MethodEventExecutor) executor).getListener().equals(listener);
            }
            return false;
        });
    }

    public void unregister(ObjectOwner owner){
        Validate.notNull(owner);
        Iterators.removeSilent(executors, executor -> executor. getOwner() != null && executor.getOwner().equals(owner));
    }

    public void unregister(Consumer<?> handler){
        Iterators.removeSilent(executors, executor -> {
            if(executor instanceof ConsumerEventExecutor){
                return ((ConsumerEventExecutor) executor).getConsumer().equals(handler);
            }
            return false;
        });
    }

    @Override
    public void bake() {
        executors.sort((o1, o2) -> o1.getPriority() >= o2.getPriority()?0:-1);
    }

    @Override
    public RegisteredListener[] getRegisteredListeners() {
        return dummyListener;
    }

    public void callEvents(Object... objects){
        for (EventExecutor executor : executors){
            try {
                executor.execute(objects);
            } catch (AuthorNagException ex) {
                if(executor instanceof BukkitEventExecutor){
                    Plugin plugin = ((BukkitEventExecutor) executor).getRegistration().getPlugin();

                    if (plugin.isNaggable()) {
                        plugin.setNaggable(false);

                        Bukkit.getLogger().log(Level.SEVERE, String.format(
                                "Nag author(s): '%s' of '%s' about the following: %s",
                                plugin.getDescription().getAuthors(),
                                plugin.getDescription().getFullName(),
                                ex.getMessage()
                        ));
                    }
                }else{
                    Bukkit.getLogger().log(Level.SEVERE, "Could not pass event " +eventClass.getSimpleName() + " to " + executor.getOwner().getName(), ex);
                }
            } catch (Throwable ex) {
                String plugin;
                if(executor instanceof BukkitEventExecutor){
                    plugin = ((BukkitEventExecutor) executor).getRegistration().getPlugin().getDescription().getFullName();
                }else{
                    plugin = executor.getOwner().getName();
                }
                Bukkit.getLogger().log(Level.SEVERE, "Could not pass event " + eventClass.getSimpleName() + " to " + plugin, ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E> void registerManagedEvent(BiConsumer<McNativeHandlerList, E> manager){
        this.manager = (BiConsumer<McNativeHandlerList, Object>) manager;
    }

    @Override
    public void execute(Listener listener, Event event) {
        if(manager != null) manager.accept(this,event);
        else callEvents(event);
    }

    public void registerExecutor(EventExecutor executor){
        this.executors.add(executor);
        bake();
    }

    public void clear(){
        this.executors.clear();
    }

    public static McNativeHandlerList get(Class<?> eventClass){
        HandlerList list = getRaw(eventClass);
        return list instanceof McNativeHandlerList ? (McNativeHandlerList) list : null;
    }

    public static HandlerList getRaw(Class<?> eventClass){
        try {
            Method method = eventClass.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            Object result = method.invoke(null);
           return (McNativeHandlerList)result;
        } catch (Exception ignored) {}
        return null;
    }

    public static void inject(Class<?> eventClass, HandlerList override){
        try {
            for (Field field : eventClass.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(HandlerList.class)){
                    HandlerList original = (HandlerList) setFinalStatic(field,override);
                    for (RegisteredListener registeredListener : original.getRegisteredListeners()) {
                        override.register(registeredListener);
                    }
                    List<HandlerList> handlers = getStaticHandlerRegistry();
                    handlers.remove(original);
                    handlers.add(override);
                    return;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Bukkit.getLogger().log(Level.SEVERE,McNative.CONSOLE_PREFIX+" McNative is not able to bridge the event "+eventClass+" from the bukkit platform to the mcnative environment. Please contact the pretronic support for more information.");
    }

    @SuppressWarnings("unchecked")
    private static List<HandlerList> getStaticHandlerRegistry(){
        return (List<HandlerList>) ReflectionUtil.getFieldValue(HandlerList.class,"allLists");
    }

    private static Object setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        ReflectionUtil.grantFinalPrivileges(field);

        Object oldValue = field.get(null);
        field.set(null, newValue);
        return oldValue;
    }
}
