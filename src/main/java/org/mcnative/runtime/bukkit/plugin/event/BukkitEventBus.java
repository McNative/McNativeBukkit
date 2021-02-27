/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 09:44
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

package org.mcnative.runtime.bukkit.plugin.event;


import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.event.execution.AsyncEventExecution;
import net.pretronic.libraries.event.execution.EventExecution;
import net.pretronic.libraries.event.execution.ExecutionType;
import net.pretronic.libraries.event.execution.SyncEventExecution;
import net.pretronic.libraries.event.executor.BiConsumerEventExecutor;
import net.pretronic.libraries.event.executor.ConsumerEventExecutor;
import net.pretronic.libraries.event.executor.EventExecutor;
import net.pretronic.libraries.event.executor.MethodEventExecutor;
import net.pretronic.libraries.event.network.EventOrigin;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.mcnative.runtime.bukkit.plugin.BukkitPluginManager;
import org.mcnative.runtime.common.network.event.NetworkEventHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BukkitEventBus implements EventBus {

    private final NetworkEventHandler networkEventHandler;
    private final Executor executor;
    private final BukkitPluginManager pluginManager;
    private final Plugin owner;
    private final Map<Class<?>,Class<?>> mappedClasses;

    private final Map<Class<?>, List<EventExecutor>> executors;

    public BukkitEventBus(Executor executor,BukkitPluginManager pluginManager,Plugin owner) {
        this.executor = executor;
        this.pluginManager = pluginManager;
        this.owner = owner;
        this.executors = new LinkedHashMap<>();
        this.mappedClasses = new HashMap<>();

        this.networkEventHandler = new NetworkEventHandler();
    }

    @Override
    public void subscribe(ObjectOwner owner, Object listener) {
        Validate.notNull(owner,listener);
        for(Method method : listener.getClass().getDeclaredMethods()){
            try{
                Listener info = method.getAnnotation(Listener.class);
                if(info != null && method.getParameterTypes().length == 1){
                    Class<?> eventClass = method.getParameterTypes()[0];
                    Class<?> mappedClass = this.mappedClasses.get(eventClass);
                    if(mappedClass == null) mappedClass = eventClass;
                    addExecutor(mappedClass,new MethodEventExecutor(owner,info.priority(),info.execution(),listener,eventClass,method));
                }
            }catch (Exception exception){
                throw new IllegalArgumentException("Could not register listener "+listener,exception);
            }
        }
    }

    @Override
    public <T> void subscribe(ObjectOwner objectOwner, Class<T> original, Consumer<T> listener, ExecutionType executionType, byte priority) {
        Validate.notNull(objectOwner,original,listener);

        Class<?> mappedClass = this.mappedClasses.get(original);
        if(mappedClass == null) mappedClass = original;

        addExecutor(mappedClass,new ConsumerEventExecutor<>(objectOwner,priority,executionType,original,listener));
    }

    @Override
    public <T> void subscribe(ObjectOwner owner, Class<T> original, BiConsumer<T, EventExecution> listener, ExecutionType executionType, byte priority) {
        Validate.notNull(owner,original,listener);

        Class<?> mappedClass = this.mappedClasses.get(original);
        if(mappedClass == null) mappedClass = original;

        addExecutor(mappedClass,new BiConsumerEventExecutor<>(owner,priority,executionType,original,listener));
    }

    @Override
    public void unsubscribe(Object listener) {
        executors.forEach((event, executors) -> Iterators.removeSilent(executors,
                executor -> executor instanceof MethodEventExecutor
                        && ((MethodEventExecutor) executor).getListener().equals(listener)));

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            if(handlerList instanceof McNativeHandlerList){
                ((McNativeHandlerList) handlerList).unregister(listener);
            }else if(listener instanceof org.bukkit.event.Listener){
                handlerList.unregister((org.bukkit.event.Listener) listener);
            }
        }
    }

    @Override
    public void unsubscribe(Consumer<?> handler) {
        executors.forEach((event, executors) -> Iterators.removeSilent(executors,
                executor -> executor instanceof ConsumerEventExecutor
                        && ((ConsumerEventExecutor) executor).getConsumer().equals(handler)));

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            if(handlerList instanceof McNativeHandlerList) ((McNativeHandlerList) handlerList).unregister(handler);
        }
    }

    @Override
    public void unsubscribe(BiConsumer<?, EventOrigin> handler) {
        executors.forEach((event, executors) -> Iterators.removeSilent(executors,
                executor -> executor instanceof ConsumerEventExecutor
                        && ((BiConsumerEventExecutor) executor).getConsumer().equals(handler)));

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            if(handlerList instanceof McNativeHandlerList) ((McNativeHandlerList) handlerList).unregister(handler);
        }
    }

    @Override
    public void unsubscribe(ObjectOwner owner) {
        Validate.notNull(owner);
        executors.forEach((event, executors) -> Iterators.removeSilent(executors, executor -> executor.getOwner().equals(owner)));
        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            if(handlerList instanceof McNativeHandlerList) ((McNativeHandlerList) handlerList).unregister(owner);
            else{
                if(owner instanceof net.pretronic.libraries.plugin.Plugin<?>){
                    HandlerList.unregisterAll(pluginManager.getMappedPlugin((net.pretronic.libraries.plugin.Plugin<?>) owner));
                }
            }
        }
    }

    @Override
    public void unsubscribeAll(Class<?> event) {
        Validate.notNull(event);
        if(Event.class.isAssignableFrom(event)){
            McNativeHandlerList handlerList = McNativeHandlerList.get(event);
            if(handlerList != null) handlerList.clear();
        }else{
            this.executors.remove(event);
        }
    }

    @Override
    public void addExecutor(Class<?> event, EventExecutor executor) {
        Validate.notNull(event,executor);
        if(Event.class.isAssignableFrom(event)){
            McNativeHandlerList handlerList = getHandlerList(event);
            handlerList.registerExecutor(executor);
            handlerList.bake();
        }else{
            List<EventExecutor> executors = this.executors.computeIfAbsent(event, k -> new ArrayList<>());
            executors.add(executor);
            executors.sort((o1, o2) -> o1.getPriority() >= o2.getPriority()?0:-1);
        }
    }

    @Override
    public <T> void callEvents(EventOrigin origin0,Class<T> original, Object... objects) {
        final EventOrigin origin = origin0 != null ? origin0 : networkEventHandler.getLocal();
        Class<?> event = getMappedClass(original);
        if(event == null) event = original;

        Validate.notNull(event,objects);
        if(Event.class.isAssignableFrom(event)){
            McNativeHandlerList handlerList = getHandlerList(event);
            handlerList.callEvents(objects);
        }else{
            List<EventExecutor> executors = this.executors.get(event);
            if(executors != null){
                new SyncEventExecution(origin,executors.iterator(),this.executor,objects);
            }
            if(networkEventHandler.isNetworkEvent(original)){
                networkEventHandler.handleNetworkEventsAsync(origin,original,objects);
            }
        }
    }

    @Override
    public <T> void callEventsAsync(EventOrigin origin0,Class<T> executionClass, Runnable callback, Object... events) {
        final EventOrigin origin = origin0 != null ? origin0 : networkEventHandler.getLocal();

        Class<?> event = getMappedClass(executionClass);
        if(event == null) event = executionClass;

        if(Event.class.isAssignableFrom(event)){
            final McNativeHandlerList handlerList = getHandlerList(event);
            executor.execute(() -> {
                handlerList.callEvents(events);
                if(callback != null) callback.run();
            });
        }else{
            List<EventExecutor> executors = this.executors.get(executionClass);
            if(executors != null){
                new AsyncEventExecution(origin, executors.iterator(), this.executor, events, () -> {
                    if(networkEventHandler.isNetworkEvent(executionClass)){
                        networkEventHandler.handleNetworkEventsAsync(origin,executionClass,events);
                    }
                    if(callback != null) callback.run();
                });
            }else{
                this.executor.execute(() -> {
                    if(networkEventHandler.isNetworkEvent(executionClass)){
                        networkEventHandler.handleNetworkEventsAsync(origin,executionClass,events);
                    }
                    if(callback != null) callback.run();
                });
            }
        }
    }

    @Override
    public Class<?> getMappedClass(Class<?> original) {
        return this.mappedClasses.get(original);
    }

    @Override
    public void registerMappedClass(Class<?> original, Class<?> mapped) {
        Validate.notNull(original,mapped);
        this.mappedClasses.put(original,mapped);
    }

    public <E extends Event> void registerManagedEvent(Class<E> original, BiConsumer<McNativeHandlerList,E> manager){
        McNativeHandlerList handlerList = McNativeHandlerList.get(original);
        if(handlerList == null){
            handlerList = new McNativeHandlerList(owner,original);
            McNativeHandlerList.inject(original,handlerList);
        }
        handlerList.registerManagedEvent(manager);
    }

    private McNativeHandlerList getHandlerList(Class<?> event){
        McNativeHandlerList handlerList = McNativeHandlerList.get(event);
        if(handlerList == null){
            handlerList = new McNativeHandlerList(owner,event);
            McNativeHandlerList.inject(event,handlerList);
        }
        return handlerList;
    }

    @Internal
    public void reset(){
        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            if (handlerList instanceof McNativeHandlerList) {
                ((McNativeHandlerList) handlerList).registerManagedEvent(null);
            }
        }
    }
}
