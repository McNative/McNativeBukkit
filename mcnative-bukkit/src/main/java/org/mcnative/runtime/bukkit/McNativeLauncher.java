/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 13:56
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

package org.mcnative.runtime.bukkit;

import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.configuration.DefaultCommandConfiguration;
import net.pretronic.libraries.dependency.DependencyManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.DocumentRegistry;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.Destroyable;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.McNativeConsoleCredentials;
import org.mcnative.runtime.api.MinecraftPlatform;
import org.mcnative.runtime.api.connection.MinecraftConnection;
import org.mcnative.runtime.api.event.player.design.MinecraftPlayerDesignUpdateEvent;
import org.mcnative.runtime.api.event.service.local.LocalServiceShutdownEvent;
import org.mcnative.runtime.api.event.service.local.LocalServiceStartupEvent;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.network.component.server.ServerVersion;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.chat.GroupChatFormatter;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.api.player.tablist.TablistFormatter;
import org.mcnative.runtime.api.player.tablist.TablistOverviewFormatter;
import org.mcnative.runtime.api.service.inventory.gui.implemen.GuiListener;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderProvider;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.MessageKeyComponent;
import org.mcnative.runtime.api.text.components.TargetMessageKeyComponent;
import org.mcnative.runtime.api.text.format.TextColor;
import org.mcnative.runtime.api.utils.Env;
import org.mcnative.runtime.bukkit.event.McNativeBridgeEventHandler;
import org.mcnative.runtime.bukkit.inventory.item.material.versioned.MaterialProtocolRegistrar;
import org.mcnative.runtime.bukkit.labymod.LabyModListener;
import org.mcnative.runtime.bukkit.labymod.LabyModSubtitleHandler;
import org.mcnative.runtime.bukkit.network.bungeecord.BungeeCordProxyNetwork;
import org.mcnative.runtime.bukkit.network.cloudnet.CloudNetV2PlatformListener;
import org.mcnative.runtime.bukkit.network.cloudnet.CloudNetV3PlatformListener;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.bukkit.player.connection.BukkitChannelInjector;
import org.mcnative.runtime.bukkit.player.tablist.BukkitTablist;
import org.mcnative.runtime.bukkit.plugin.BukkitPluginManager;
import org.mcnative.runtime.bukkit.plugin.command.BukkitCommandManager;
import org.mcnative.runtime.bukkit.plugin.dependency.legacy.BukkitMiddlewareClassMap;
import org.mcnative.runtime.bukkit.plugin.event.BukkitEventBus;
import org.mcnative.runtime.bukkit.serviceprovider.VaultServiceListener;
import org.mcnative.runtime.bukkit.serviceprovider.placeholder.PlaceHolderApiProvider;
import org.mcnative.runtime.client.integrations.ClientIntegration;
import org.mcnative.runtime.common.McNativeTabCompleteEventHandler;
import org.mcnative.runtime.common.event.service.local.DefaultLocalServiceShutdownEvent;
import org.mcnative.runtime.common.event.service.local.DefaultLocalServiceStartupEvent;
import org.mcnative.runtime.common.maf.MAFService;
import org.mcnative.runtime.common.serviceprovider.message.ResourceMessageExtractor;
import org.mcnative.runtime.network.integrations.cloudnet.v2.CloudNetV2Network;
import org.mcnative.runtime.network.integrations.cloudnet.v3.CloudNetV3Network;
import org.mcnative.runtime.protocol.java.MinecraftJavaProtocol;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class McNativeLauncher implements Listener {

    private static Plugin PLUGIN;

    private static BukkitPluginManager PLUGIN_MANAGER;
    private static BukkitCommandManager COMMAND_MANAGER;
    private static BukkitChannelInjector CHANNEL_INJECTOR;
    private static BukkitEventBus EVENT_BUS;

    public static Plugin getPlugin() {
        return PLUGIN;
    }

    public static void launchMcNative(){
        launchMcNative(new HashMap<>());
    }

    public static void launchMcNative(Map<String,String> variables0){
        Collection<Env> variables = Iterators.map(variables0.entrySet(), entry -> new Env(entry.getKey().toLowerCase(),entry.getValue()));
        launchMcNativeInternal(variables,createDummyPlugin());
    }

    public static void launchMcNativeInternal(Collection<Env> variables,Plugin plugin){
        if(McNative.isAvailable()) return;
        try {
            bootstrapMCNative(variables,plugin);
        }catch (Exception exception){
            exception.printStackTrace();
            Bukkit.getLogger().info(McNative.CONSOLE_PREFIX+"McNative failed to started, shutting down");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private static void bootstrapMCNative(Collection<Env> variables,Plugin plugin){
        PluginVersion apiVersion = PluginVersion.parse(McNativeLauncher.class.getPackage().getSpecificationVersion());
        PluginVersion implementationVersion = PluginVersion.ofImplementation(McNativeLauncher.class);

        PLUGIN = plugin;
        Logger logger = Bukkit.getLogger();
        logger.info(McNative.CONSOLE_PREFIX+"McNative is starting, please wait...");
        logger.info(McNative.CONSOLE_PREFIX+"Api Version: "+apiVersion.getName());
        logger.info(McNative.CONSOLE_PREFIX+"Impl Version: "+implementationVersion.getName());

        if(!plugin.isEnabled()) enablePlugin(plugin);

        DocumentRegistry.getDefaultContext().registerMappingAdapter(CommandConfiguration.class, DefaultCommandConfiguration.class);

        if(!McNativeBukkitConfiguration.load(new JdkPretronicLogger(logger),new File("plugins/McNative/"))) return;

        BukkitPluginManager pluginManager = new BukkitPluginManager();
        pluginManager.inject();
        logger.info(McNative.CONSOLE_PREFIX+"McNative initialised and injected plugin manager.");

        BukkitEventBus eventBus = new BukkitEventBus(GeneralUtil.getDefaultExecutorService(),pluginManager,getPlugin());
        BukkitCommandManager commandManager = new BukkitCommandManager();
        BukkitPlayerManager playerManager = new BukkitPlayerManager();

        PLUGIN_MANAGER = pluginManager;
        COMMAND_MANAGER = commandManager;
        EVENT_BUS = eventBus;

        McNativeConsoleCredentials credentials = setupCredentials(variables);
        BukkitService localService = new BukkitService(commandManager,playerManager,eventBus);
        BukkitMcNative instance = new BukkitMcNative(apiVersion,implementationVersion,pluginManager,playerManager,localService,variables,credentials);

        McNative.setInstance(instance);
        instance.setNetwork(setupNetwork(logger,instance.getExecutorService()));

        BukkitChannelInjector injector = new BukkitChannelInjector();
        CHANNEL_INJECTOR = injector;
        commandManager.inject();

        MinecraftJavaProtocol.register(localService.getPacketManager());
        ClientIntegration.register();
        MaterialProtocolRegistrar.init(instance.getPlatform().getProtocolVersion());

        instance.registerDefaultProviders();
        instance.registerDefaultCommands();
        instance.registerDefaultDescribers();
        instance.registerDefaultCreators();
        instance.registerPlayerAdapter();
        MessageComponent.registerDocumentMessageComponentConverter();

        registerDefaultListener(eventBus, pluginManager);
        new McNativeBridgeEventHandler(injector,eventBus,playerManager);
        new McNativeTabCompleteEventHandler(eventBus, localService.getPacketManager());

        logger.info(McNative.CONSOLE_PREFIX+"McNative has overwritten default bukkit events.");

        registerDependencyHooks(logger,pluginManager,playerManager);

        McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                .delay(1, TimeUnit.SECONDS)
                .execute(() -> {
                    injector.injectChannelInitializer((result) -> {
                        if(result){
                            playerManager.loadConnectedPlayers();
                            instance.setReady(true);
                            logger.info(McNative.CONSOLE_PREFIX+"McNative successfully started.");
                        }else{
                            instance.setReady(true);
                            logger.log(Level.SEVERE,McNative.CONSOLE_PREFIX+"McNative failed injecting the channel initializer, shutting down");
                            Bukkit.getPluginManager().disablePlugin(plugin);
                        }
                    });
                }).addListener(future -> {
                    if(future.isFailed()){
                        future.getThrowable().printStackTrace();
                        instance.setReady(true);
                        logger.log(Level.SEVERE,McNative.CONSOLE_PREFIX+"McNative failed injecting the channel initializer, shutting down");
                        Bukkit.getPluginManager().disablePlugin(plugin);
                    }
                });

        McNativeBukkitConfiguration.postLoad();
        setupConfiguredServices();

        ResourceMessageExtractor.extractMessages(McNativeLauncher.class.getClassLoader(),"system-messages/","McNative");
        if(McNativeBukkitConfiguration.CONSOLE_MAF_ENABLED && McNative.getInstance().getConsoleCredentials() != null){
            MAFService.start();
        }

        McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM).delay(2,TimeUnit.SECONDS)
                .execute(() -> eventBus.callEvent(LocalServiceStartupEvent.class,new DefaultLocalServiceStartupEvent()));

        eventBus.subscribe(McNative.getInstance(), new GuiListener());
    }

    public static void shutdown(){
        if(!McNative.isAvailable()) return;
        Logger logger = Bukkit.getLogger();
        logger.info(McNative.CONSOLE_PREFIX+"McNative is stopping, please wait...");
        McNative instance = McNative.getInstance();
        DependencyManager dependencyManager = null;
        if(instance != null){
            EVENT_BUS.callEvent(LocalServiceShutdownEvent.class,new DefaultLocalServiceShutdownEvent());

            instance.getLogger().shutdown();
            instance.getScheduler().shutdown();
            instance.getExecutorService().shutdown();
            instance.getPluginManager().shutdown();
            ((BukkitMcNative)instance).setReady(false);
            dependencyManager = instance.getDependencyManager();
        }

        if(PLUGIN_MANAGER != null) PLUGIN_MANAGER.reset();
        if(COMMAND_MANAGER != null) COMMAND_MANAGER.reset();
        if(CHANNEL_INJECTOR != null) CHANNEL_INJECTOR.reset();
        if(EVENT_BUS != null) EVENT_BUS.reset();

        McNative.setInstance(null);

        if(dependencyManager instanceof Destroyable) ((Destroyable) dependencyManager).destroy();

        logger.info(McNative.CONSOLE_PREFIX+"McNative successfully stopped.");
    }

    private static void registerDefaultListener(EventBus eventBus, BukkitPluginManager pluginManager) {
        if(Bukkit.getPluginManager().getPlugin("Vault") != null){
            eventBus.subscribe(McNative.getInstance(), new VaultServiceListener(pluginManager));
        }
    }

    private static McNativeConsoleCredentials setupCredentials(Collection<Env> variables){
        Env networkId = Iterators.findOne(variables, env -> env.getName().equalsIgnoreCase("mcnative.networkId"));
        Env secret = Iterators.findOne(variables, env -> env.getName().equalsIgnoreCase("mcnative.secret"));
        if(networkId != null && secret != null){
            return new McNativeConsoleCredentials(networkId.getValue().toString(),secret.getValue().toString());
        }else if(!McNativeBukkitConfiguration.CONSOLE_NETWORK_ID.equals("00000-00000-00000")){
            return new McNativeConsoleCredentials(McNativeBukkitConfiguration.CONSOLE_NETWORK_ID,McNativeBukkitConfiguration.CONSOLE_SECRET);
        }
        return null;
    }

    private static void setupConfiguredServices(){
        McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM,new LabyModListener());
        if(McNativeBukkitConfiguration.LABYMOD_SUBTITLE_ENABLED){
            new LabyModSubtitleHandler(McNativeBukkitConfiguration.LABYMOD_SUBTITLE_SIZE,McNativeBukkitConfiguration.LABYMOD_SUBTITLE_TEXT_COMPONENT);
        }

        if(McNativeBukkitConfiguration.PLAYER_CHAT_ENABLED){
            ChatChannel serverChat = ChatChannel.newChatChannel();
            serverChat.setName("ServerChat");
            serverChat.setMessageFormatter((GroupChatFormatter) (sender, variables, message) -> McNativeBukkitConfiguration.PLAYER_CHAT);
            McNative.getInstance().getLocal().setServerChat(serverChat);
        }

        if(McNativeBukkitConfiguration.PLAYER_TABLIST_ENABLED){
            Tablist tablist = new BukkitTablist();
            tablist.setFormatter(new TablistFormatter() {
                @Override
                public MessageComponent<?> formatPrefix(ConnectedMinecraftPlayer player, TablistEntry entry, VariableSet variables) {
                    if(entry instanceof MinecraftConnection){
                        return new TargetMessageKeyComponent((MinecraftConnection) entry,McNativeBukkitConfiguration.PLAYER_TABLIST_PREFIX_LOADED);
                    }else{
                        return new MessageKeyComponent(McNativeBukkitConfiguration.PLAYER_TABLIST_PREFIX_LOADED);
                    }
                }

                @Override
                public MessageComponent<?> formatSuffix(ConnectedMinecraftPlayer player,TablistEntry entry, VariableSet variables) {
                    if(entry instanceof MinecraftConnection){
                        return new TargetMessageKeyComponent((MinecraftConnection) entry,McNativeBukkitConfiguration.PLAYER_TABLIST_SUFFIX_LOADED);
                    }else{
                        return new MessageKeyComponent(McNativeBukkitConfiguration.PLAYER_TABLIST_SUFFIX_LOADED);
                    }
                }
            });

            if(McNativeBukkitConfiguration.PLAYER_TABLIST_OVERVIEW_ENABLED) {
                tablist.setOverviewFormatter(new TablistOverviewFormatter() {

                    @Override
                    public MessageComponent<?> formatHeader(ConnectedMinecraftPlayer receiver, VariableSet headerVariables, VariableSet footerVariables) {
                        return McNativeBukkitConfiguration.PLAYER_TABLIST_OVERVIEW_HEADER_LOADED;
                    }

                    @Override
                    public MessageComponent<?> formatFooter(ConnectedMinecraftPlayer receiver, VariableSet headerVariables, VariableSet footerVariables) {
                        return McNativeBukkitConfiguration.PLAYER_TABLIST_OVERVIEW_FOOTER_LOADED;
                    }
                });
            }

            McNative.getInstance().getLocal().setServerTablist(tablist);
            McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM,tablist);
        }

        ServerStatusResponse defaultResponse = new BukkitServerStatusResponse();
        defaultResponse.setOnlinePlayers(ServerStatusResponse.DYNAMIC_CALCULATED);
        defaultResponse.setMaxPlayers(Bukkit.getMaxPlayers());
        defaultResponse.setVersion(new ServerVersion(builtVersionInfo(),McNative.getInstance().getPlatform().getProtocolVersion()));
        if(McNativeBukkitConfiguration.SERVER_STATUS_ENABLED){
            defaultResponse.setDescription(McNativeBukkitConfiguration.SERVER_STATUS_DESCRIPTION_LINE1_COMPILED
                    ,McNativeBukkitConfiguration.SERVER_STATUS_DESCRIPTION_LINE2_COMPILED);
            defaultResponse.getVersion().setName(Text.translateAlternateColorCodes('&',McNativeBukkitConfiguration.SERVER_STATUS_VERSION_INFO));
            for (String player : McNativeBukkitConfiguration.SERVER_STATUS_PLAYER_INFO) {
                defaultResponse.addPlayerInfo(Text.translateAlternateColorCodes('&',player));
            }
        }else {
            defaultResponse.setDescription(Text.parse("§7"+Bukkit.getMotd(), TextColor.GRAY));
        }

        File serverIcon = new File("server-icon.png");
        if(serverIcon.exists()){
            defaultResponse.setFavicon(serverIcon);
        }else{
            try{
                defaultResponse.setFavicon(ServerStatusResponse.DEFAULT_FAVICON_URL);
            }catch (Exception ignored){}
        }

        McNative.getInstance().getLocal().setStatusResponse(defaultResponse);

        if(McNativeBukkitConfiguration.PLAYER_DISPLAY_APPLY_ON_BUKKIT){
            McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM, MinecraftPlayerDesignUpdateEvent.class, event -> {
                if(event.getPlayer() instanceof BukkitPlayer){
                    ((BukkitPlayer) event.getPlayer()).getOriginal().setDisplayName(event.getPlayer().getDisplayName());
                }
            });
        }
    }

    private static String builtVersionInfo(){
        MinecraftPlatform platform = McNative.getInstance().getPlatform();
        if(platform.getJoinableProtocolVersions().size() > 1){
            return platform.getMinVersion().getName()+" - "+platform.getMaxVersion().getName();
        }else{
            return platform.getProtocolVersion().getName();
        }
    }

    private static Network setupNetwork(Logger logger,ExecutorService executor){
        if(Bukkit.getPluginManager().getPlugin("CloudNetAPI") != null){
            logger.info(McNative.CONSOLE_PREFIX+"(Network) Initialized CloudNet V2 networking technology");
            CloudNetV2Network network = new CloudNetV2Network(executor);
            new CloudNetV2PlatformListener(network.getMessenger());
            return network;
        }else if(Bukkit.getPluginManager().getPlugin("CloudNet-Bridge") != null){
            logger.info(McNative.CONSOLE_PREFIX+"(Network) Initialized CloudNet V3 networking technology");
            CloudNetV3Network network = new CloudNetV3Network(executor);
            new CloudNetV3PlatformListener(network.getMessenger());
            return network;
        }else if(!Bukkit.getOnlineMode()){
            File spigotConfigFile = new File("spigot.yml");
            if(spigotConfigFile.exists()){
                try {
                    Document spigotConfig = DocumentFileType.YAML.getReader().read(spigotConfigFile);
                    if(!spigotConfig.getBoolean("settings.bungeecord")){
                        logger.warning(McNative.CONSOLE_PREFIX+"(Network) Online mode is enabled, but BungeeCord is disabled in spigot.yml");
                        logger.warning(McNative.CONSOLE_PREFIX+"(Network) If you are using BungeeCord, make sure to set bungeecord to 'true' in your spigt.yml config");
                        return null;
                    }
                }catch (Exception e){
                    logger.log(Level.SEVERE,McNative.CONSOLE_PREFIX+"(Network) Could not read the spigot.yml configuration and detect the network setup");
                    logger.log(Level.SEVERE,McNative.CONSOLE_PREFIX+"(Network) Error: "+e.getMessage());
                }
            }
            logger.info(McNative.CONSOLE_PREFIX+"(Network) Initialized BungeeCord networking technology");
            return new BungeeCordProxyNetwork(executor);
        }
        return null;
    }

    private static void registerDependencyHooks(Logger logger,BukkitPluginManager pluginManager, BukkitPlayerManager playerManager){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            logger.info(McNative.CONSOLE_PREFIX+"(Service) Initialized PlaceHolderApi provider");
            McNative.getInstance().getRegistry().registerService(McNative.getInstance(), PlaceholderProvider.class
                    ,new PlaceHolderApiProvider(playerManager,pluginManager));
        }
    }

    /*
    For registering a listener the plugin must be enabled. This method marks the plugin as enabled, before
    bukkit enables the plugin.
     */
    private static void enablePlugin(Plugin plugin){
        if(plugin instanceof JavaPlugin){
            ReflectionUtil.changeFieldValue(JavaPlugin.class,plugin,"isEnabled",true);
        }else throw new IllegalArgumentException("Could not enable plugin, requires JavaPlugin: "+plugin.getClass());
    }

    @SuppressWarnings("unchecked")
    private static Plugin createDummyPlugin(){
        McNativeDummyPlugin plugin = new McNativeDummyPlugin(McNativeLauncher.class.getPackage().getImplementationVersion());
        List<Plugin> plugins = (List<Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"plugins");
        Map<String, Plugin> lookupNames = (Map<String, Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"lookupNames");
        plugins.add(plugin);
        lookupNames.put(plugin.getName(),plugin);
        return plugin;
    }

}
