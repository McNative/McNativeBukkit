/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.01.20, 20:21
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

package org.mcnative.runtime.bukkit;

import net.pretronic.libraries.command.command.MainCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.concurrent.TaskScheduler;
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler;
import net.pretronic.libraries.dependency.DependencyManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.DocumentRegistry;
import net.pretronic.libraries.document.injection.DependencyInjectionObjectInstanceFactory;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.event.EventPriority;
import net.pretronic.libraries.event.injection.DefaultInjectorService;
import net.pretronic.libraries.event.injection.InjectorService;
import net.pretronic.libraries.logging.Debug;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.logging.bridge.slf4j.SLF4JStaticBridge;
import net.pretronic.libraries.logging.level.DebugLevel;
import net.pretronic.libraries.logging.level.LogLevel;
import net.pretronic.libraries.message.MessageProvider;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriber;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriberRegistry;
import net.pretronic.libraries.plugin.description.DefaultPluginDescription;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.plugin.loader.DefaultPluginLoader;
import net.pretronic.libraries.plugin.manager.PluginManager;
import net.pretronic.libraries.plugin.service.ServiceClassRegistry;
import net.pretronic.libraries.plugin.service.ServiceRegistry;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mcnative.runtime.api.*;
import org.mcnative.runtime.api.loader.LoaderConfiguration;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.player.PlayerManager;
import org.mcnative.runtime.api.player.bossbar.BossBar;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.player.profile.GameProfileLoader;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;
import org.mcnative.runtime.api.plugin.configuration.ConfigurationProvider;
import org.mcnative.runtime.api.service.inventory.Inventory;
import org.mcnative.runtime.api.service.inventory.gui.GuiManager;
import org.mcnative.runtime.api.service.inventory.gui.implemen.DefaultGuiManager;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderProvider;
import org.mcnative.runtime.api.text.format.ColoredString;
import org.mcnative.runtime.api.utils.Env;
import org.mcnative.runtime.bukkit.commands.GuiOpenCommand;
import org.mcnative.runtime.bukkit.creators.BukkitItemStackCreator;
import org.mcnative.runtime.bukkit.creators.InventoryCreator;
import org.mcnative.runtime.bukkit.creators.ItemStackCreator;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.bukkit.player.permission.BukkitPermissionProvider;
import org.mcnative.runtime.bukkit.player.permission.BukkitPlayerDesign;
import org.mcnative.runtime.bukkit.player.tablist.BukkitTablist;
import org.mcnative.runtime.bukkit.plugin.command.McNativeCommand;
import org.mcnative.runtime.bukkit.plugin.dependency.BukkitDependencyLoader;
import org.mcnative.runtime.bukkit.plugin.dependency.BukkitMiddlewareClassMap;
import org.mcnative.runtime.bukkit.plugin.dependency.LegacyReflectedDependencyClassLoader;
import org.mcnative.runtime.bukkit.plugin.mapped.BukkitPluginDescription;
import org.mcnative.runtime.bukkit.plugin.mapped.BukkitPluginLoader;
import org.mcnative.runtime.common.DefaultLoaderConfiguration;
import org.mcnative.runtime.common.DefaultObjectFactory;
import org.mcnative.runtime.common.player.*;
import org.mcnative.runtime.common.player.data.DefaultPlayerDataProvider;
import org.mcnative.runtime.common.plugin.configuration.DefaultConfigurationProvider;
import org.mcnative.runtime.common.serviceprovider.McNativePlaceholderProvider;
import org.mcnative.runtime.common.serviceprovider.message.DefaultMessageProvider;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class BukkitMcNative implements McNative {

    private final PluginVersion apiVersion;
    private final PluginVersion implementationVersion;
    private final MinecraftPlatform platform;
    private final PretronicLogger logger;
    private final TaskScheduler scheduler;
    private final CommandSender consoleSender;
    private final ObjectFactory factory;
    private final LoaderConfiguration loaderConfiguration;
    private final Collection<Env> variables;

    private final PluginManager pluginManager;
    private final DependencyManager dependencyManager;
    private final PlayerManager playerManager;
    private final LocalService local;
    private final InjectorService injector;

    private Network network;
    private final Document serverProperties;
    private boolean ready;
    private final McNativeConsoleCredentials consoleCredentials;

    protected BukkitMcNative(PluginVersion apiVersion, PluginVersion implVersion, PluginManager pluginManager, PlayerManager playerManager, LocalService local, Collection<Env> variables, McNativeConsoleCredentials consoleCredentials, BukkitMiddlewareClassMap middlewareClassMap) {
        this.apiVersion = apiVersion;
        this.implementationVersion = implVersion;

        JdkPretronicLogger logger0 = new JdkPretronicLogger(Bukkit.getLogger());
        if(McNativeBukkitConfiguration.DEBUG){
            logger0.getLogLevelTranslation().replace(LogLevel.DEBUG,Level.INFO);
            logger0.setPrefixProcessor(level -> level == LogLevel.DEBUG ? "(Debug) " : null);
        }
        Debug.setLogger(logger0);
        Debug.setDebugLevel(DebugLevel.NORMAL);
        Debug.setLogLevel(LogLevel.DEBUG);
        this.logger = logger0;

        this.scheduler = new SimpleTaskScheduler();
        this.platform = new BukkitPlatform(this.scheduler);
        this.dependencyManager = new DependencyManager(this.logger,new File("plugins/McNative/lib/dependencies/"));
        this.dependencyManager.setLoggerPrefix("[McNative] (Dependency-Manager) ");
        //this.dependencyManager.setDefaultLoader(new BukkitDependencyLoader(middlewareClassMap));
        this.dependencyManager.setDefaultLoader(new LegacyReflectedDependencyClassLoader());
        this.factory = new DefaultObjectFactory();
        this.variables = variables;

        this.consoleSender = new McNativeCommand.MappedCommandSender(Bukkit.getConsoleSender());
        this.pluginManager = pluginManager;
        this.playerManager = playerManager;
        this.local = local;
        this.consoleCredentials = consoleCredentials;

        this.serverProperties = DocumentFileType.PROPERTIES.getReader().read(new File("server.properties"));
        this.loaderConfiguration = DefaultLoaderConfiguration.load(new File("plugins/McNative/loader.yml"));
        SLF4JStaticBridge.trySetLogger(logger);

        this.injector = new DefaultInjectorService(new ServiceClassRegistry(pluginManager));
        DocumentRegistry.setInstanceFactory(new DependencyInjectionObjectInstanceFactory(injector));
    }

    @Override
    public PluginVersion getApiVersion() {
        return apiVersion;
    }

    @Override
    public PluginVersion getImplementationVersion() {
        return implementationVersion;
    }

    @Override
    public LoaderConfiguration getRolloutConfiguration() {
        return loaderConfiguration;
    }

    @Override
    public McNativeConsoleCredentials getConsoleCredentials() {
        return this.consoleCredentials;
    }

    @Override
    public Collection<Env> getVariables() {
        return variables;
    }

    @Override
    public Env getVariable(String name) {
        Validate.notNull(name);
        return Iterators.findOne(this.variables, env -> env.getName().equalsIgnoreCase(name));
    }

    @Override
    public boolean hasVariable(String name) {
        Validate.notNull(name);
        return getVariable(name) != null;
    }

    @Override
    public void setVariable(String name, Object value) {
        Validate.notNull(name);
        Iterators.remove(this.variables, env -> env.getName().equalsIgnoreCase(name));
        if(value != null) this.variables.add(new Env(name,value));
    }

    protected void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public MinecraftPlatform getPlatform() {
        return platform;
    }

    @Override
    public PretronicLogger getLogger() {
        return logger;
    }

    @Override
    public ServiceRegistry getRegistry() {
        return pluginManager;
    }

    @Override
    public TaskScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public CommandSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return factory;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public ExecutorService getExecutorService() {
        return GeneralUtil.getDefaultExecutorService();
    }

    @Override
    public InjectorService getInjector() {
        return injector;
    }

    @Override
    public boolean isNetworkAvailable() {
        return network != null;
    }

    @Override
    public Network getNetwork() {
        if(network == null) throw new IllegalArgumentException("Network is not available");
        return network;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public LocalService getLocal() {
        return local;
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    public Document getServerProperties() {
        return serverProperties;
    }

    protected void registerDefaultProviders(){
        pluginManager.registerService(this, ConfigurationProvider.class,new DefaultConfigurationProvider());
        pluginManager.registerService(this, PlayerDataProvider.class,new DefaultPlayerDataProvider());
        pluginManager.registerService(this, MessageProvider.class,new DefaultMessageProvider());
        pluginManager.registerService(this, PermissionProvider.class,new BukkitPermissionProvider());
        pluginManager.registerService(this, GameProfileLoader.class,new MemoryGameProfileLoader());
        pluginManager.registerService(this, GuiManager.class,new DefaultGuiManager());
        pluginManager.registerService(this, PlaceholderProvider.class,new McNativePlaceholderProvider(), EventPriority.LOW);
    }

    protected void registerDefaultCommands() {
        MainCommand command = new org.mcnative.runtime.common.commands.McNativeCommand(this,"s","server");
        command.registerCommand(new GuiOpenCommand(this));
        getLocal().getCommandManager().registerCommand(command);
    }

    protected void registerDefaultDescribers(){
        VariableDescriberRegistry.registerDescriber(PlayerDesign.class);
        VariableDescriberRegistry.registerDescriber(MinecraftPlugin.class);
        VariableDescriberRegistry.registerDescriber(PluginDescription.class);
        VariableDescriberRegistry.registerDescriber(DefaultPluginDescription.class);
        VariableDescriberRegistry.registerDescriber(BukkitPluginDescription.class);
        VariableDescriberRegistry.registerDescriber(DefaultPluginLoader.class);
        VariableDescriberRegistry.registerDescriber(BukkitPluginLoader.class);
        VariableDescriberRegistry.registerDescriber(PluginVersion.class);
        VariableDescriberRegistry.registerDescriber(DefaultPlayerDesign.class);

        VariableDescriber<?> designDescriber = VariableDescriberRegistry.registerDescriber(BukkitPlayerDesign.class);
        ColoredString.makeDescriberColored(designDescriber);

        VariableDescriber<BukkitPlayer> oPlayerDescriber = VariableDescriberRegistry.registerDescriber(BukkitPlayer.class);
        ColoredString.makeFunctionColored(oPlayerDescriber,"displayName");

        VariableDescriber<OfflineMinecraftPlayer> playerDescriber = VariableDescriberRegistry.registerDescriber(OfflineMinecraftPlayer.class);
        ColoredString.makeFunctionColored(playerDescriber,"displayName");
    }

    protected void registerDefaultCreators(){
        factory.registerCreator(ChatChannel.class, objects -> new DefaultChatChannel());
        factory.registerCreator(Tablist.class, objects -> new BukkitTablist());
        factory.registerCreator(BossBar.class, objects -> new DefaultBossBar());
        factory.registerCreator(ItemStack.class, new ItemStackCreator());
        factory.registerCreator(Inventory.class, new InventoryCreator());
        factory.registerCreator(org.bukkit.inventory.ItemStack.class, new BukkitItemStackCreator());
    }

    protected void registerPlayerAdapter() {
        McNative.getInstance().getPlayerManager().registerPlayerAdapter(Player.class, minecraftPlayer -> {
            if(minecraftPlayer instanceof BukkitPlayer) {
                return ((BukkitPlayer)minecraftPlayer).getOriginal();
            }
            return null;
        });
    }
}

