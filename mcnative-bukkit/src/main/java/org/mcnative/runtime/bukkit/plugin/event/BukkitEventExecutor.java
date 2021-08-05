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

package org.mcnative.runtime.bukkit.plugin.event;

import net.pretronic.libraries.event.execution.EventExecution;
import net.pretronic.libraries.event.execution.ExecutionType;
import net.pretronic.libraries.event.executor.EventExecutor;
import net.pretronic.libraries.event.network.EventOrigin;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.logging.Level;

public class BukkitEventExecutor implements EventExecutor {

    private final Class<?> allowedClass;
    private final RegisteredListener registration;
    private final byte priority;

    public BukkitEventExecutor(Class<?> allowedClass, RegisteredListener registration) {
        this.allowedClass = allowedClass;
        this.registration = registration;
        priority = mapPriority(registration.getPriority());
    }

    public RegisteredListener getRegistration() {
        return registration;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.BLOCKING;
    }

    @Override
    public ObjectOwner getOwner() {
        return null;//@Todo implement
    }

    @Override
    public void execute(EventExecution execution, Object... events) {
        for (Object event : events){
            if(allowedClass.isAssignableFrom(event.getClass())){
                if (registration.getPlugin().isEnabled()) {
                    try {
                        registration.callEvent((Event) event);
                    } catch (AuthorNagException var10) {
                        Plugin plugin = registration.getPlugin();
                        if (plugin.isNaggable()) {
                            plugin.setNaggable(false);
                            Bukkit.getServer().getLogger().log(Level.SEVERE, String.format("Nag author(s): '%s' of '%s' about the following: %s"
                                    ,plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), var10.getMessage()));
                        }
                    } catch (Throwable exception) {
                        Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not pass event " + ((Event) event).getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), exception);
                    }
                }
            }

        }
    }

    private byte mapPriority(EventPriority priority){
        if(priority == EventPriority.MONITOR) return net.pretronic.libraries.event.EventPriority.HIGHEST;
        else if(priority == EventPriority.HIGHEST) return net.pretronic.libraries.event.EventPriority.HIGHEST-1;//@Todo change when monitor is implemented
        else if(priority == EventPriority.HIGH) return net.pretronic.libraries.event.EventPriority.HIGH;
        else if(priority == EventPriority.LOW) return net.pretronic.libraries.event.EventPriority.LOW;
        else if(priority == EventPriority.LOWEST) return net.pretronic.libraries.event.EventPriority.LOWEST;
        return net.pretronic.libraries.event.EventPriority.NORMAL;
    }


}
