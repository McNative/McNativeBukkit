/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.10.19, 00:11
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

import net.pretronic.libraries.concurrent.TaskScheduler;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;
import org.mcnative.runtime.bukkit.utils.ViaVersionExtensionUtil;
import org.mcnative.runtime.api.MinecraftPlatform;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class BukkitPlatform implements MinecraftPlatform {

    private final Plugin VIAVERSION = Bukkit.getPluginManager().getPlugin("ViaVersion");

    private final MinecraftProtocolVersion protocolVersion = BukkitReflectionUtil.getProtocolVersionByServerVersion();
    private Collection<MinecraftProtocolVersion> versions;
    private final File latestLogLocation;

    public BukkitPlatform(TaskScheduler scheduler) {
        this.latestLogLocation = new File("logs/latest.log");
        versions = Collections.singletonList(protocolVersion);
        trySetViaVersions(scheduler,0);
    }

    private void trySetViaVersions(TaskScheduler scheduler,int amount){
        if(VIAVERSION == null) return;
        if(VIAVERSION.isEnabled()){
            Collection<MinecraftProtocolVersion> result = ViaVersionExtensionUtil.getVersions();
            if(!result.isEmpty()){
                this.versions = Collections.unmodifiableCollection(result);
                return;
            }
        }
        scheduler.createTask(ObjectOwner.SYSTEM)
                .delay(3,TimeUnit.SECONDS)
                .execute(() -> trySetViaVersions(scheduler, amount));
    }

    @Override
    public String getName() {
        return "Bukkit";
    }

    @Override
    public String getVersion() {
        return Bukkit.getVersion();
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public Collection<MinecraftProtocolVersion> getJoinableProtocolVersions() {
        return versions;
    }

    @Override
    public boolean isProxy() {
        return false;
    }

    @Override
    public boolean isService() {
        return true;
    }

    @Override
    public File getLatestLogLocation() {
        return this.latestLogLocation;
    }

}
