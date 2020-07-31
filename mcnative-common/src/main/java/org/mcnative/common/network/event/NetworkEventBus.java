/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.05.20, 17:31
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

package org.mcnative.common.network.event;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.event.DefaultEventBus;
import net.pretronic.libraries.event.executor.MethodEventExecutor;
import net.pretronic.libraries.event.network.*;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectException;
import net.pretronic.libraries.utility.reflect.UnsafeInstanceCreator;
import org.mcnative.common.McNative;
import org.mcnative.common.network.messaging.MessageReceiver;
import org.mcnative.common.network.messaging.MessagingChannelListener;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

public class NetworkEventBus extends DefaultEventBus implements MessagingChannelListener {

    @Override
    public void subscribe(ObjectOwner owner, Object listener) {
        Objects.requireNonNull(owner, "Owner can't be null.");
        Objects.requireNonNull(listener, "Listener can't be null.");

        for (Method method : listener.getClass().getDeclaredMethods()) {
            try {
                NetworkListener info = method.getAnnotation(NetworkListener.class);
                if (info != null && method.getParameterTypes().length == 1) {
                    Class<?> eventClass = method.getParameterTypes()[0];
                    Class<?> mappedClass = getMappedClass(eventClass);
                    if (mappedClass == null) mappedClass = eventClass;
                    addExecutor(mappedClass, new MethodEventExecutor(owner, info.priority(), listener, eventClass, method));
                }
            } catch (Exception var11) {
                throw new IllegalArgumentException("Could not register listener " + listener, var11);
            }
        }
    }

    @Override
    public <T> void callEvents(Class<T> executionClass, Object... events) {
        if(events.length > 1) throw new IllegalArgumentException("Network eventbus can not execute multiple events for the same execution class");
        if(checkNetworkEvent(executionClass)){
            callNetworkEvents(executionClass,events);
        }
        super.callEvents(executionClass, events);
    }

    @Override
    public <T> void callEventsAsync(Class<T> executionClass, Runnable callback, Object... events) {
        if(events.length > 1) throw new IllegalArgumentException("Network eventbus can not execute multiple events for the same execution class");
        checkNetworkEvent(executionClass);
        if(checkNetworkEvent(executionClass)){
            callNetworkEvents(executionClass,events);
        }
        super.callEventsAsync(executionClass,callback, events);
    }

    private <T> boolean checkNetworkEvent(Class<T> executionClass){
        NetworkEvent event = executionClass.getAnnotation(NetworkEvent.class);
        if(event == null) throw new IllegalArgumentException(executionClass.getName()+" is not a @NetworkEvent");
        return event.type() != NetworkEventType.SELF_MANAGED;
    }

    private <T> void callNetworkEvents(Class<T> executionClass,Object[] events){
        Object event = events[0];
        if(events.length != 1) throw new IllegalArgumentException("Network event bus does not support multiple event objects");
        McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                .async()
                .execute(() -> {
                    Document eventData;
                    if(event instanceof NetworkEventAdapter) eventData = ((NetworkEventAdapter) event).write();
                    else eventData = Document.newDocument(event);
                    eventData.set("MCNATIVE_EVENT_ACTION","call");
                    eventData.set("eventClass",event.getClass());

                    if(executionClass != event.getClass()){
                        eventData.set("executionClass",executionClass);
                    }

                    McNative.getInstance().getNetwork().sendBroadcastMessage("mcnative_event",eventData);
                });
    }

    @Internal
    public void executeNetworkEvent(EventOrigin origin,Document data){
        try{
            Class<?> executionClass = data.getObject("executionClass",Class.class);
            Class<?> eventClass = data.getObject("eventClass",Class.class);
            if(executionClass == null) executionClass = eventClass;

            Object event;
            if(NetworkEventAdapter.class.isAssignableFrom(eventClass)){
                event = UnsafeInstanceCreator.newInstance(eventClass);
                ((NetworkEventAdapter)event).read(data);
            }else{
                event = data.getAsObject(eventClass);
            }
            super.callEventsAsync(origin,executionClass,event);
        }catch (ReflectException ignored){}
    }

    @Override
    public Document onMessageReceive(MessageReceiver sender, UUID requestId, Document request) {
        String action = request.getString("MCNATIVE_EVENT_ACTION");
        if(action.equalsIgnoreCase("call")){
            executeNetworkEvent(sender,request);
        }
        return null;
    }
}
