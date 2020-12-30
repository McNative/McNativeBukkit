/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 22.04.20, 22:34
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

package org.mcnative.bukkit.serviceprovider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.mcnative.bukkit.McNativeLauncher;
import org.mcnative.bukkit.plugin.BukkitPluginManager;
import org.mcnative.bukkit.serviceprovider.economy.VaultEconomyHook;
import org.mcnative.bukkit.serviceprovider.economy.VaultEconomyProvider;
import org.mcnative.bukkit.serviceprovider.permission.VaultPermissionHook;
import org.mcnative.bukkit.serviceprovider.permission.VaultPermissionProvider;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.service.registry.ServiceRegisterRegistryEvent;
import org.mcnative.runtime.api.event.service.registry.ServiceUnregisterRegistryEvent;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;

import java.util.HashMap;
import java.util.Map;

public class VaultServiceListener {

    private final BukkitPluginManager pluginManager;

    private final Map<EconomyProvider, VaultEconomyHook> vaultEconomyProviders;
    private final Map<PermissionProvider, VaultPermissionHook> vaultPermissionProviders;

    public VaultServiceListener(BukkitPluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.vaultEconomyProviders= new HashMap<>();
        this.vaultPermissionProviders = new HashMap<>();
    }

    @Listener
    public void onServiceRegister(ServiceRegisterRegistryEvent event) {
        if(event.isServiceType(EconomyProvider.class)){
            if(!event.isService(VaultEconomyProvider.class)){
                VaultEconomyHook hook = new VaultEconomyHook((EconomyProvider) event.getService());
                Bukkit.getServer().getServicesManager().register(Economy.class
                        ,hook
                        ,findPlugin(event.getOwner())
                        ,mapPriority(event.getPriority()));
                McNative.getInstance().getLogger().info("[McNative] Economy provider [{}] was hooked to vault",
                        event.getService().getClass().getSimpleName());
                this.vaultEconomyProviders.put(event.getService(EconomyProvider.class),hook);
            }
        } else if(event.isServiceType(PermissionProvider.class)) {
            if(!event.isServiceType(VaultPermissionProvider.class)) {
                VaultPermissionHook hook = new VaultPermissionHook((PermissionProvider) event.getService());

                Bukkit.getServer().getServicesManager().register(Permission.class,
                        hook.getPermission(),
                        findPlugin(event.getOwner()),
                        mapPriority(event.getPriority()));
                Bukkit.getServer().getServicesManager().register(Chat.class,
                        hook.getChat(),
                        findPlugin(event.getOwner()),
                        mapPriority(event.getPriority()));
                McNative.getInstance().getLogger().info("[McNative] Permission provider [{}] was hooked to vault",
                        event.getService().getClass().getSimpleName());
                this.vaultPermissionProviders.put(event.getService(PermissionProvider.class), hook);
            }
        }
     }

    @Listener
    public void onServiceUnregister(ServiceUnregisterRegistryEvent event) {
        if(event.isServiceType(EconomyProvider.class)) {
            if(event.getService() instanceof VaultEconomyHook) {
                VaultEconomyHook hook = this.vaultEconomyProviders.get(event.getService(EconomyProvider.class));
                if(hook != null){
                    Bukkit.getServer().getServicesManager().unregister(hook);
                    McNative.getInstance().getLogger().info("Economy provider [{}] was unhooked from vault",
                            event.getService().getClass().getSimpleName());
                }
            }
        } else if(event.isServiceType(PermissionProvider.class)) {
            if(event.getService() instanceof VaultPermissionHook) {
                VaultPermissionHook hook = this.vaultPermissionProviders.get(event.getService(PermissionProvider.class));
                if(hook != null) {
                    Bukkit.getServer().getServicesManager().unregister(hook.getChat());
                    Bukkit.getServer().getServicesManager().unregister(hook.getPermission());

                    McNative.getInstance().getLogger().info("Permission provider [{}] was unhooked from vault",
                            event.getService().getClass().getSimpleName());
                }
            }
        }
    }

    private Plugin findPlugin(ObjectOwner objectOwner) {
        if(objectOwner instanceof net.pretronic.libraries.plugin.Plugin<?>) {
            return this.pluginManager.getMappedPlugin((net.pretronic.libraries.plugin.Plugin<?>) objectOwner);
        } else {
            return McNativeLauncher.getPlugin();
        }
    }

    private ServicePriority mapPriority(byte priority) {
        switch (priority) {
            case net.pretronic.libraries.plugin.service.ServicePriority.HIGHEST : {
                return ServicePriority.Highest;
            }
            case net.pretronic.libraries.plugin.service.ServicePriority.HIGH : {
                return ServicePriority.High;
            }
            case net.pretronic.libraries.plugin.service.ServicePriority.LOW : {
                return ServicePriority.Low;
            }
            case net.pretronic.libraries.plugin.service.ServicePriority.LOWEST : {
                return ServicePriority.Lowest;
            }
            default: {
                return ServicePriority.Normal;
            }
        }
    }
}
