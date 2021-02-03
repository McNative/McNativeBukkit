/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.02.20, 20:56
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

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.message.MessageProvider;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.plugin.description.DefaultPluginDescription;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.plugin.exception.InvalidPluginDescriptionException;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.libraries.plugin.loader.PluginLoader;
import net.pretronic.libraries.plugin.manager.PluginManager;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.interfaces.OwnerUnregisterAble;
import net.pretronic.libraries.utility.interfaces.ShutdownAble;
import net.pretronic.libraries.utility.io.archive.ZipArchive;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.bukkit.plugin.mapped.BukkitPlugin;
import org.mcnative.runtime.bukkit.plugin.mapped.BukkitPluginLoader;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.service.registry.ServiceRegisterRegistryEvent;
import org.mcnative.runtime.api.event.service.registry.ServiceUnregisterRegistryEvent;
import org.mcnative.runtime.common.event.service.registry.DefaultServiceRegisterRegistryEvent;
import org.mcnative.runtime.common.event.service.registry.DefaultServiceUnregisterRegistryEvent;
import org.mcnative.runtime.common.serviceprovider.message.ResourceMessageExtractor;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BukkitPluginManager implements PluginManager {

    private final static String LOADER_CLASS_NAME = "BukkitMcNativePluginBootstrap";
    private final static String DUMMY_CLASS_NAME = "McNativeDummyPlugin";
    private final static String PLUGIN_CLASS_NAME = "BukkitDummyPlugin";

    private final ServicesManager serviceManager;
    private final Map<String, BiConsumer<Plugin<?>,LifecycleState>> stateListeners;
    private final Collection<PluginLoader> loaders;
    private final Collection<Plugin<?>> plugins;

    private McNativePluginWrapperList pluginWrapperList;

    public BukkitPluginManager() {
        this.serviceManager = Bukkit.getServicesManager();
        this.stateListeners = new HashMap<>();
        this.loaders = new ArrayList<>();
        this.plugins = new ArrayList<>();
    }

    @Override
    public PretronicLogger getLogger() {
        return McNative.getInstance().getLogger();
    }

    @Override
    public Collection<Plugin<?>> getPlugins() {
        return plugins;
    }

    @Override
    public Plugin<?> getPlugin(String name) {
        return Iterators.findOne(this.plugins, plugin -> plugin.getDescription().getName().equalsIgnoreCase(name));
    }

    @Override
    public Plugin<?> getPlugin(UUID id) {
        return Iterators.findOne(this.plugins, plugin -> plugin.getDescription().getId().equals(id));
    }

    @Override
    public boolean isPluginEnabled(String name) {
        Plugin<?> plugin =  getPlugin(name);
        return plugin != null && plugin.getLoader().isEnabled();
    }

    @Override
    public Collection<PluginLoader> getLoaders() {
        return loaders;
    }

    @Override
    public PluginLoader createPluginLoader(String name) {
        PluginLoader loader = Iterators.findOne(this.loaders, loader1 -> loader1.getDescription().getName().equalsIgnoreCase(name));

        if(loader == null){
            File pluginDir = new File("plugins");
            if(pluginDir.isDirectory()){
                File[] files = pluginDir.listFiles();
                if(files != null){
                    for (File file : files) {
                        if(file.getName().endsWith(".jar")){
                            ZipArchive archive = new ZipArchive(file);
                            try{
                                InputStream input = archive.getStream("plugin.yml");
                                if(input == null) throw new InvalidPluginDescriptionException("No plugin description found for "+file.getAbsolutePath()+" found");

                                Document description = DocumentFileType.YAML.getReader().read(input);
                                String pluginName = description.getString("name");
                                if(pluginName.equalsIgnoreCase(name)){
                                    loader = new BukkitPluginLoader(file,new DefaultPluginDescription(name,
                                            null,"","","",null,null
                                            ,new PluginVersion(description.getString("version"), -1,-1
                                            ,-1,-1,"RELEASE"),null,null
                                            ,Collections.emptyList(),Collections.emptyList()));
                                    this.loaders.add(loader);
                                }
                            }catch (Exception ignored){}
                        }
                    }
                }
            }
        }
        return loader;
    }

    @Override
    public PluginLoader createPluginLoader(File file) {
        return createPluginLoader(file,null);
    }

    @Override
    public PluginLoader createPluginLoader(File file, PluginDescription pluginDescription) {
        throw new UnsupportedOperationException("Bukkit bridge is not able to detect plugin descriptions");
    }

    @Override
    public PluginDescription detectPluginDescription(File file) {
        throw new UnsupportedOperationException("Bukkit bridge is not able to detect plugin descriptions");
    }

    @Override
    public Collection<PluginDescription> detectPluginDescriptions(File file) {
        throw new UnsupportedOperationException("Bukkit bridge is not able to detect plugin descriptions");
    }

    @Override
    public void setLifecycleStateListener(String s, BiConsumer<Plugin<?>, LifecycleState> biConsumer) {
        this.stateListeners.put(s,biConsumer);
    }

    @Override
    public void executeLifecycleStateListener(String state, LifecycleState stateEvent, Plugin plugin) {
        if(state.equals(LifecycleState.CONSTRUCTION)){
            this.plugins.add(plugin);
        }else if(state.equals(LifecycleState.INITIALISATION)){
            ResourceMessageExtractor.extractMessages(plugin);
        }else if(state.equals(LifecycleState.UNLOAD)){
            this.plugins.remove(plugin);
            this.loaders.remove(plugin.getLoader());

            MessageProvider messageProvider = McNative.getInstance().getRegistry().getServiceOrDefault(MessageProvider.class);
            String module = plugin.getDescription().getMessageModule();
            if(messageProvider != null && module != null){
                messageProvider.unloadPacks(module);
                messageProvider.calculateMessages();
            }
        }

        BiConsumer<Plugin<?>,LifecycleState> listener = this.stateListeners.get(state);
        if(listener != null) listener.accept(plugin,stateEvent);
    }


    @Override
    public Collection<Plugin<?>> enablePlugins(File file) {//@Todo return mapped plugin
        try {
            Bukkit.getPluginManager().loadPlugin(file);
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    @Override
    public void disablePlugins() {
        Bukkit.getPluginManager().disablePlugins();
    }

    @Override
    public void provideLoader(PluginLoader pluginLoader) {
        this.loaders.add(pluginLoader);
    }

    @Override
    public Collection<Class<?>> getAvailableServices() {
        return serviceManager.getKnownServices();
    }

    @Override
    public <T> Collection<T> getServices(Class<T> serviceClass) {
        Validate.notNull(serviceClass);
        Collection<RegisteredServiceProvider<T>> services = serviceManager.getRegistrations(serviceClass);
        return Iterators.map(services, RegisteredServiceProvider::getProvider);
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        T result = getServiceOrDefault(serviceClass,null);
        if(result == null) throw new UnsupportedOperationException("Service is not available.");
        return result;
    }

    @Override
    public <T> T getServiceOrDefault(Class<T> serviceClass) {
        return getServiceOrDefault(serviceClass,null);
    }

    @Override
    public <T> T getServiceOrDefault(Class<T> serviceClass, Supplier<T> supplier) {
        Validate.notNull(serviceClass);
        RegisteredServiceProvider<T> services = serviceManager.getRegistration(serviceClass);
        if(services != null) return services.getProvider();
        else if(supplier != null) return supplier.get();
        return null;
    }

    @Override
    public <T> void registerService(ObjectOwner owner, Class<T> serviceClass, T service, byte priority) {
        Validate.notNull(owner,serviceClass,service);

        org.bukkit.plugin.Plugin mappedOwner;
        if(owner instanceof Plugin<?>) mappedOwner = getMappedPlugin((Plugin<?>) owner);
        else mappedOwner = McNativeLauncher.getPlugin();

        serviceManager.register(serviceClass,service,mappedOwner,mapServicePriorityToBukkit(priority));
        McNative.getInstance().getLocal().getEventBus().callEvent(ServiceRegisterRegistryEvent.class
                ,new DefaultServiceRegisterRegistryEvent(serviceClass,service,owner,priority));
    }

    @Override
    public <T> boolean isServiceAvailable(Class<T> serviceClass) {
        Validate.notNull(serviceClass);
        return serviceManager.getKnownServices().contains(serviceClass);
    }

    @Override
    public void unregisterService(Object o) {
        Validate.notNull(o);
        serviceManager.unregister(o);
        for (RegisteredServiceProvider<?> registration : serviceManager.getRegistrations(o.getClass())) {
            if(registration.getProvider().equals(o)) {
                serviceManager.unregister(registration.getProvider());
                if(registration.getProvider() instanceof ShutdownAble) ((ShutdownAble) registration.getProvider()).shutdown();
                McNative.getInstance().getLocal().getEventBus().callEvent(ServiceUnregisterRegistryEvent.class
                        ,new DefaultServiceUnregisterRegistryEvent(registration.getService(),registration.getProvider(), new BukkitPluginOwner(registration.getPlugin())));
            }
        }
    }

    @Override
    public void unregisterServices(Class<?> aClass) {
        Validate.notNull(aClass);
        for (RegisteredServiceProvider<?> registration : serviceManager.getRegistrations(aClass)) {
            serviceManager.unregister(registration.getProvider());
            if(registration.getProvider() instanceof ShutdownAble) ((ShutdownAble) registration.getProvider()).shutdown();
            McNative.getInstance().getLocal().getEventBus().callEvent(ServiceUnregisterRegistryEvent.class
                    ,new DefaultServiceUnregisterRegistryEvent(registration.getService(),registration.getProvider(), new BukkitPluginOwner(registration.getPlugin())));
        }
    }

    @Override
    public void unregisterServices(ObjectOwner owner) {
        if(owner instanceof Plugin<?>){
            for (RegisteredServiceProvider<?> registration : serviceManager.getRegistrations(getMappedPlugin((Plugin<?>) owner))) {
                serviceManager.unregister(registration.getProvider());
                if(registration.getProvider() instanceof ShutdownAble) ((ShutdownAble) registration.getProvider()).shutdown();
                McNative.getInstance().getLocal().getEventBus().callEvent(ServiceUnregisterRegistryEvent.class
                        ,new DefaultServiceUnregisterRegistryEvent(registration.getService(),registration.getProvider(), new BukkitPluginOwner(registration.getPlugin())));
            }
            for (Class<?> knownService : serviceManager.getKnownServices()) {
                for (RegisteredServiceProvider<?> registration : serviceManager.getRegistrations(knownService)) {
                    if(registration.getProvider() instanceof OwnerUnregisterAble){
                        ((OwnerUnregisterAble) registration.getProvider()).unregister(owner);
                    }
                }
            }
        }else throw new IllegalArgumentException("It is not possible to unsubscribe services, if the owner is not a plugin");
    }

    @Override
    public void shutdown() {
        for (Class<?> knownService : serviceManager.getKnownServices()) {
            for (RegisteredServiceProvider<?> registration : serviceManager.getRegistrations(knownService)) {
                if(registration.getProvider() instanceof ShutdownAble){
                    ((ShutdownAble) registration.getProvider()).shutdown();
                    serviceManager.unregister(registration.getProvider());
                }
            }
        }
    }

    //    && !plugin.getClass().getSimpleName().equals(DUMMY_CLASS_NAME)
    protected void registerBukkitPlugin(org.bukkit.plugin.Plugin plugin){
        if(!plugin.getClass().getSimpleName().equals(LOADER_CLASS_NAME)
                && !plugin.getClass().getSimpleName().equals(PLUGIN_CLASS_NAME)){

            PluginLoader loader = Iterators.findOne(this.loaders, loader1
                    -> loader1 instanceof BukkitPluginLoader
                    && plugin.equals(((BukkitPluginLoader) loader1).getOriginal()));
            if(loader == null){
                loader = new BukkitPluginLoader(plugin);
                this.loaders.add(loader);
            }
            this.plugins.add(loader.getInstance());

            if(plugin instanceof JavaPlugin
                    && plugin.getPluginLoader() instanceof JavaPluginLoader){
                ReflectionUtil.changeFieldValue(JavaPlugin.class,plugin,"loader"
                        ,new McNativePluginWrapperLoader((JavaPluginLoader) plugin.getPluginLoader()));
            }
        }

    }

    protected void unregisterBukkitPlugin(org.bukkit.plugin.Plugin plugin){
        Plugin<?> result = Iterators.removeOne(this.plugins, current
                -> current instanceof BukkitPlugin
                && ((BukkitPlugin) current).getPlugin().equals(plugin));
        if(result != null){
            if(result.getLoader() instanceof BukkitPluginLoader){
                ((BukkitPluginLoader) result.getLoader()).destroy();
            }
        }
    }

    @Internal
    public org.bukkit.plugin.Plugin getMappedPlugin(Plugin<?> original){//@Todo find better solution
        Validate.notNull(original);
        for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()){
            if(plugin.getClass().getSimpleName().equals(LOADER_CLASS_NAME) && plugin.getName().equals(original.getName())){
                return plugin;
            }
        }
        throw new IllegalArgumentException("McNative Mapping error (plugin / mcnative -> bukkit)");
    }

    @Internal
    public Plugin<?> getMappedPlugin(org.bukkit.plugin.Plugin original){
        Validate.notNull(original);
        for (Plugin<?> plugin : plugins){
            if(plugin.equals(original)) return plugin;
        }
        throw new IllegalArgumentException("McNative Mapping error (plugin / bukkit -> mcnative)");
    }

    @Internal
    @SuppressWarnings("unchecked")
    public void inject(){//Must be synchronized for security reasons
        synchronized (this){
            List<org.bukkit.plugin.Plugin> original = (List<org.bukkit.plugin.Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"plugins");
            McNativePluginWrapperList override = new McNativePluginWrapperList(original,this);
            ReflectionUtil.changeFieldValue(Bukkit.getPluginManager(),"plugins",override);
            for (org.bukkit.plugin.Plugin plugin : original){
                registerBukkitPlugin(plugin);
            }
            pluginWrapperList = override;
        }
    }

    @Internal
    public void reset(){
       if(pluginWrapperList != null){
           synchronized (this){
               ReflectionUtil.changeFieldValue(Bukkit.getPluginManager(),"plugins",pluginWrapperList.getOriginal());
           }
       }
    }

    private ServicePriority mapServicePriorityToBukkit(byte priority) {
        if(priority >= net.pretronic.libraries.plugin.service.ServicePriority.HIGHEST) return ServicePriority.Highest;
        if(priority >= net.pretronic.libraries.plugin.service.ServicePriority.HIGH) return ServicePriority.High;
        if(priority == net.pretronic.libraries.plugin.service.ServicePriority.NORMAL) return ServicePriority.Normal;
        if(priority <= net.pretronic.libraries.plugin.service.ServicePriority.LOW) return ServicePriority.Low;
        return ServicePriority.Lowest;
    }
}
