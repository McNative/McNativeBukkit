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

import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.concurrent.TaskScheduler;
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler;
import net.pretronic.libraries.dependency.DependencyManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.event.EventPriority;
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
import net.pretronic.libraries.plugin.service.ServiceRegistry;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.Bukkit;
import org.mcnative.runtime.api.loader.LoaderConfiguration;
import org.mcnative.runtime.api.utils.Env;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.bukkit.player.permission.BukkitPermissionProvider;
import org.mcnative.runtime.bukkit.player.permission.BukkitPlayerDesign;
import org.mcnative.runtime.bukkit.plugin.command.McNativeCommand;
import org.mcnative.runtime.bukkit.plugin.mapped.BukkitPluginDescription;
import org.mcnative.runtime.bukkit.plugin.mapped.BukkitPluginLoader;
import org.mcnative.runtime.api.*;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.player.PlayerManager;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;
import org.mcnative.runtime.api.plugin.configuration.ConfigurationProvider;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderProvider;
import org.mcnative.runtime.api.text.format.ColoredString;
import org.mcnative.runtime.common.DefaultLoaderConfiguration;
import org.mcnative.runtime.common.DefaultObjectFactory;
import org.mcnative.runtime.common.player.DefaultChatChannel;
import org.mcnative.runtime.common.player.DefaultPlayerDesign;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.common.player.data.DefaultPlayerDataProvider;
import org.mcnative.runtime.common.plugin.configuration.DefaultConfigurationProvider;
import org.mcnative.runtime.common.serviceprovider.McNativePlaceholderProvider;
import org.mcnative.runtime.common.serviceprovider.message.DefaultMessageProvider;
import org.mcnative.runtime.protocol.java.MinecraftJavaProtocol;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
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

    private Network network;
    private final Document serverProperties;
    private boolean ready;

    protected BukkitMcNative(PluginVersion apiVersion,PluginVersion implVersion, PluginManager pluginManager, PlayerManager playerManager, LocalService local, Network network) {
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
        this.factory = new DefaultObjectFactory();
        this.variables = new ArrayList<>();

        this.consoleSender = new McNativeCommand.MappedCommandSender(Bukkit.getConsoleSender());
        this.pluginManager = pluginManager;
        this.playerManager = playerManager;
        this.local = local;
        this.network = network;

        this.serverProperties = DocumentFileType.PROPERTIES.getReader().read(new File("server.properties"));
        this.loaderConfiguration = DefaultLoaderConfiguration.load(new File("plugins/McNative/loader.yml"));
        SLF4JStaticBridge.trySetLogger(logger);
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
        return new McNativeConsoleCredentials(McNativeBukkitConfiguration.CONSOLE_NETWORK_ID,McNativeBukkitConfiguration.CONSOLE_SECRET);
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
        pluginManager.registerService(this, PlaceholderProvider.class,new McNativePlaceholderProvider(), EventPriority.LOW);
    }

    protected void registerDefaultCommands() {
        getLocal().getCommandManager().registerCommand(new org.mcnative.runtime.common.commands.McNativeCommand(this,"s","server"));
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
        factory.registerCreator(ItemStack.class, parameters -> {
            Material material = (Material) parameters[0];
            org.bukkit.Material bukkitMaterial = null;
            for (org.bukkit.Material value : org.bukkit.Material.values()) {
                if(value.toString().equalsIgnoreCase(material.getName())) {
                    bukkitMaterial = value;
                    break;
                }
            }

            Validate.notNull(bukkitMaterial, "Can't create item stack for " + material + ".");
            return new BukkitItemStack(new org.bukkit.inventory.ItemStack(bukkitMaterial));
        });
    }

    /*
     @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T newInventory(InventoryOwner owner, Class<T> inventoryClass, int size, String title) {
        InventoryHolder holder = new BukkitInventoryHolder(owner);
        if(inventoryClass == AnvilInventory.class) {
            DefaultAnvilInventory anvilInventory = new DefaultAnvilInventory(owner);
            InventoryRegistry.registerInventory(anvilInventory);
            return (T) anvilInventory;
        } else if(inventoryClass == BeaconInventory.class) {
            return (T) new BukkitBeaconInventory(owner, (org.bukkit.inventory.BeaconInventory)
                    Bukkit.createInventory(holder, InventoryType.BEACON));
        } else if(inventoryClass == BrewerInventory.class) {
            return (T) new BukkitBrewerInventory(owner,(org.bukkit.inventory.BrewerInventory)
                    Bukkit.createInventory(holder, InventoryType.BREWING));
        } else if(inventoryClass == CartographyInventory.class) {
            if(McNative.getInstance().getPlatform().getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_14)) {
                throw new UnsupportedOperationException("Can't create LlamaInventory. Too old version.");
            }
            return (T) new BukkitCartographyInventory(owner, (org.bukkit.inventory.CartographyInventory)
                    Bukkit.createInventory(holder, InventoryType.CARTOGRAPHY));
        } else if(inventoryClass == ChestInventory.class || inventoryClass == Inventory.class || inventoryClass == DoubleChestInventory.class) {
            return (T) new BukkitChestInventory<>(owner, Bukkit.createInventory(holder, size, title));
        } else if(inventoryClass == CraftingInventory.class) {
            return (T) new BukkitCraftingInventory(owner, (org.bukkit.inventory.CraftingInventory)
                    Bukkit.createInventory(holder, InventoryType.CRAFTING));
        } else if(inventoryClass == EnchantingInventory.class) {
            return (T) new BukkitEnchantingInventory(owner, (org.bukkit.inventory.EnchantingInventory)
                    Bukkit.createInventory(holder, InventoryType.ENCHANTING));
        } else if(inventoryClass == FurnaceInventory.class) {
            return (T) new BukkitFurnaceInventory(owner, (org.bukkit.inventory.FurnaceInventory)
                    Bukkit.createInventory(holder, InventoryType.FURNACE));
        } else if(inventoryClass == GrindstoneInventory.class) {
            if(McNative.getInstance().getPlatform().getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_14)) {
                throw new UnsupportedOperationException("Can't create LlamaInventory. Too old version.");
            }
            return (T) new BukkitGrindstoneInventory(owner, (org.bukkit.inventory.GrindstoneInventory)
                    Bukkit.createInventory(holder, InventoryType.GRINDSTONE));
        } else if(inventoryClass == LecternInventory.class) {
            if(McNative.getInstance().getPlatform().getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_14)) {
                throw new UnsupportedOperationException("Can't create LlamaInventory. Too old version.");
            }
            return (T) new BukkitLecternInventory(owner, (org.bukkit.inventory.LecternInventory)
                    Bukkit.createInventory(holder, InventoryType.LECTERN));
        } else if(inventoryClass == LoomInventory.class) {
            if(McNative.getInstance().getPlatform().getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_14)) {
                throw new UnsupportedOperationException("Can't create LlamaInventory. Too old version.");
            }
            return (T) new BukkitLoomInventory(owner, (org.bukkit.inventory.LoomInventory)
                    Bukkit.createInventory(holder, InventoryType.LOOM));
        } else if(inventoryClass == PlayerInventory.class) {
            return (T) new BukkitPlayerInventory(owner, (org.bukkit.inventory.PlayerInventory)
                    Bukkit.createInventory(holder, InventoryType.PLAYER));
        } else if(inventoryClass == StonecutterInventory.class) {
            if(McNative.getInstance().getPlatform().getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_14)) {
                throw new UnsupportedOperationException("Can't create LlamaInventory. Too old version.");
            }
            return (T) new BukkitStonecutterInventory(owner, (org.bukkit.inventory.StonecutterInventory)
                    Bukkit.createInventory(holder, InventoryType.STONECUTTER));
        } else if(inventoryClass == ArmorableHorseInventory.class || inventoryClass == HorseInventory.class
                || inventoryClass == LlamaInventory.class) {
            throw new IllegalArgumentException("Not possible to create " + inventoryClass + " on bukkit platform");
        }
        throw new IllegalArgumentException("Can't create inventory for " + inventoryClass + ".");
    }


     */
}

