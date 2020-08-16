/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 24.11.19, 16:25
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

package org.mcnative.loader;

import net.pretronic.libraries.dependency.DependencyGroup;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.plugin.RuntimeEnvironment;
import net.pretronic.libraries.plugin.description.DefaultPluginDescription;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.loader.PluginLoader;
import net.pretronic.libraries.plugin.loader.classloader.BridgedPluginClassLoader;
import net.pretronic.libraries.resourceloader.ResourceInfo;
import net.pretronic.libraries.resourceloader.ResourceLoader;
import net.pretronic.libraries.resourceloader.VersionInfo;
import org.mcnative.common.McNative;
import org.mcnative.loader.rollout.RolloutProfile;

import java.io.File;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestPluginExecutor {

    private static final String VERSION_URL = "https://{profile.server}/v1/{resource.id}/versions/latest?plain=true&qualifier={profile.qualifier}";
    private static final String DOWNLOAD_URL = "https://{profile.server}/v1/{resource.id}/versions/{version.build}/download";

    private final PlatformExecutor executor;
    private final File location;
    private final Logger logger;
    private final RuntimeEnvironment<McNative> environment;
    private final RolloutProfile profile;
    private GuestPluginLoader loader;

    public GuestPluginExecutor(PlatformExecutor executor,File location, Logger logger, String runtimeName,RolloutProfile profile) {
        this.executor = executor;
        this.location = location;
        this.logger = logger;
        this.environment = new RuntimeEnvironment<>(runtimeName, McNative.getInstance());
        this.profile = profile;
    }

    public boolean install(){
        InputStream stream = getClass().getClassLoader().getResourceAsStream("mcnative.json");
        if(stream != null){
            setupLoader(stream);
        }else{
            InputStream streamLoader = getClass().getClassLoader().getResourceAsStream("mcnative-loader.json");
            if(streamLoader == null){
                logger.log(Level.SEVERE,"Invalid or corrupt mcnative plugin (mcnative.json or mcnative-loader.json is not available)");
                return false;
            }else{
                try{
                    Document loaderInfo = DocumentFileType.JSON.getReader().read(streamLoader);
                    if(downloadResource(loaderInfo)){
                        stream = getClass().getClassLoader().getResourceAsStream("mcnative.json");
                        setupLoader(stream);
                    }else return false;
                }catch (Exception exception){
                    logger.log(Level.SEVERE,String.format("Could not install plugin %s",exception.getMessage()));
                    return false;
                }
            }
        }
        return true;
    }

    public boolean installDependencies(){
        InputStream stream = loader.getClassLoader().getResourceAsStream("dependencies.json");
        if(stream == null) return true;
        Document data = DocumentFileType.JSON.getReader().read(stream);
        try{
            DependencyGroup dependencies = McNative.getInstance().getDependencyManager().load(data);
            dependencies.install();
            dependencies.loadReflected((URLClassLoader) loader.getClassLoader().asJVMLoader());
        }catch (Exception exception){
            logger.log(Level.SEVERE,String.format("Could not install dependencies %s",exception.getMessage()));
            return false;
        }
        return true;
    }

    private void setupLoader(InputStream descriptionStream){
        PluginDescription description = DefaultPluginDescription.create(
                McNative.getInstance().getPluginManager()
                , DocumentFileType.JSON.getReader().read(descriptionStream));
        this.loader = new GuestPluginLoader(executor,McNative.getInstance().getPluginManager(),environment
                ,new JdkPretronicLogger(logger),new BridgedPluginClassLoader(getClass().getClassLoader())
                ,location,description,false);
    }

    private boolean downloadResource(Document loader){
        String name = loader.getString("plugin.name");

        ResourceInfo info = new ResourceInfo(name,new File("plugins/McNative/lib/resources/"+name.toLowerCase()));
        info.setVersionUrl(replaceLoaderVariables(loader,VERSION_URL));
        ResourceLoader resourceLoader = new ResourceLoader(info);

        if(McNative.getInstance().getMcNativeServerId() != null){
            info.setAuthenticator(httpURLConnection -> {
                httpURLConnection.setRequestProperty("serverId",McNative.getInstance().getMcNativeServerId().getId());
                httpURLConnection.setRequestProperty("serverSecret",McNative.getInstance().getMcNativeServerId().getSecret());
            });
        }

        VersionInfo current = resourceLoader.getCurrentVersion();
        VersionInfo latest = VersionInfo.UNKNOWN;

        logger.log(Level.SEVERE,"(Resource-Loader) Server: "+profile.getServer()+", Qualifier: "+profile.getQualifier());
        try{
            latest = resourceLoader.getLatestVersion();
        }catch (Exception exception){
            logger.log(Level.SEVERE,"(Resource-Loader) Could not get latest "+profile.getQualifier()+" version ("+exception.getMessage()+")");
            if(current == null || current.equals(VersionInfo.UNKNOWN)){
                logger.log(Level.SEVERE,"(Resource-Loader) Resource is not available, shutting down");
                return false;
            }
        }

        if(profile.isAutomatically() || current == null){
            if(latest != null){
                if(resourceLoader.isLatestVersion()){
                    logger.info("(Resource-Loader) "+name+" "+latest.getName()+" (Up to date)");
                }else{
                    info.setDownloadUrl(replaceLoaderVariables(loader,DOWNLOAD_URL));

                    logger.info("(Resource-Loader) Downloading "+name+" "+latest.getName());
                    try{
                        resourceLoader.download(latest);
                        logger.info("(McNative-Loader) Successfully downloaded "+name);
                    }catch (Exception exception){
                        exception.printStackTrace();
                        if(current == null || current.equals(VersionInfo.UNKNOWN)){
                            logger.info("(McNative-Loader) Download failed, shutting down");
                            logger.info("(McNative-Loader) Error: "+exception.getMessage());
                            return false;
                        }else{
                            logger.info("(McNative-Loader) download failed, trying to start an older version");
                            logger.info("(McNative-Loader) Error: "+exception.getMessage());
                        }
                    }
                }
            }
        }else{
            logger.info("(Resource-Loader) automatically updating is disabled");
            logger.info("(Resource-Loader) Latest Version: "+latest.getName());
        }

        try{
            ClassLoader classLoader = getClass().getClassLoader();
            resourceLoader.loadReflected((URLClassLoader) classLoader);
        }catch (Exception exception){
            logger.log(Level.SEVERE,"(Resource-Loader) Could not load "+name+" ("+exception.getMessage()+")");
            return false;
        }

        return true;
    }

    private String replaceLoaderVariables(Document loaderConfig,String input){
        return input
                .replace("{resource.name}",loaderConfig.getString("plugin.name"))
                .replace("{resource.id}",loaderConfig.getString("plugin.id"))
                .replace("{profile.server}",profile.getServer())
                .replace("{profile.qualifier}",profile.getQualifier());
    }

    public PluginLoader getLoader() {
        return loader;
    }

    public void loadGuestPlugin(){
        McNative.getInstance().getPluginManager().provideLoader(loader);
        loader.construct();
        loader.initialize();
        loader.load();
    }

    public void enableGuestPlugin(){
        loader.bootstrapInternal();
    }

    public void disableGuestPlugin(){
        Plugin<?> owner = loader.getInstance();
        loader.shutdownInternal();
        McNative instance = McNative.getInstance();
        instance.getRegistry().unregisterService(owner);
        instance.getScheduler().unregister(owner);
        instance.getLocal().getEventBus().unsubscribe(owner);
        instance.getLocal().getCommandManager().unregisterCommand(owner);
        if(instance.isNetworkAvailable()){
            instance.getNetwork().getMessenger().unregisterChannels(owner);
        }
    }

}
