/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.03.20, 19:06
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

package org.mcnative.runtime.bukkit.serviceprovider.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.interfaces.OwnerUnregisterAble;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.bukkit.plugin.BukkitPluginManager;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderHook;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderProvider;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.TextComponent;

import java.util.ArrayList;
import java.util.Collection;

public class PlaceHolderApiProvider implements PlaceholderProvider, OwnerUnregisterAble {

    private final BukkitPlayerManager playerManager;
    private final BukkitPluginManager pluginManager;
    private final Collection<McNativePlaceHolderExpansion> expansions;

    public PlaceHolderApiProvider(BukkitPlayerManager playerManager, BukkitPluginManager pluginManager) {
        this.playerManager = playerManager;
        this.pluginManager = pluginManager;
        this.expansions = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "PlaceholderApi";
    }

    @Override
    public Collection<String> getIdentifiers() {
        return PlaceholderAPI.getRegisteredIdentifiers();
    }

    @Override
    public void registerPlaceHolders(ObjectOwner owner, String identifier, PlaceholderHook hook) {
        this.expansions.add(new McNativePlaceHolderExpansion(getPlugin(owner),identifier,hook,owner));
    }

    @Override
    public void unregisterPlaceHolders(String identifier) {
        PlaceholderExpansion expansion = Iterators.findOne(this.expansions, expansion1 -> expansion1.getIdentifier().equalsIgnoreCase(identifier));
        if(expansion != null){
            expansion.unregister();
            this.expansions.remove(expansion);
        }
    }

    @Override
    public void unregisterPlaceHolders(ObjectOwner owner) {
        Collection<McNativePlaceHolderExpansion> expansions = Iterators.remove(this.expansions, expansion -> expansion.getOwner().equals(owner));
        for (PlaceholderExpansion expansion : expansions) {
            expansion.unregister();
        }
    }

    @Override
    public boolean hasIdentifier(String identifier) {
        return PlaceholderAPI.isRegistered(identifier);
    }

    @Override
    public String translate(MinecraftPlayer player, String placeholder) {
        int splitIndex = placeholder.indexOf('_');
        String identifier = splitIndex != -1 ? placeholder.substring(0,splitIndex) : placeholder;
        String parameter = "";
        if(splitIndex != -1) parameter = placeholder.substring(splitIndex+1);
        return translate(player,identifier,parameter);
    }

    @Override
    public String translate(MinecraftPlayer player, String identifier, String parameter) {
        LocalExpansionManager manager = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
        PlaceholderExpansion hook = manager.getExpansion(identifier);
        if(hook != null){
            OfflinePlayer mapped;
            if(player instanceof BukkitPlayer) mapped = ((BukkitPlayer) player).getOriginal();
            else mapped = Bukkit.getOfflinePlayer(player.getUniqueId());
            hook.onRequest(mapped,parameter);
        }
        return null;
    }

    @Override
    public String replacePlaceholders(MinecraftPlayer player, String rawString) {
        OfflinePlayer mapped;
        if(player instanceof BukkitPlayer) mapped = ((BukkitPlayer) player).getOriginal();
        else mapped = Bukkit.getOfflinePlayer(player.getUniqueId());
        return PlaceholderAPI.setPlaceholders(mapped,rawString);
    }

    @Override
    public void replacePlaceholders(MinecraftPlayer player,  MessageComponent<?> rawComponent) {
        if(rawComponent instanceof TextComponent){
            String newTest = replacePlaceholders(player,((TextComponent) rawComponent).getText());
            ((TextComponent) rawComponent).setText(newTest);
        }
        for (MessageComponent<?> extra : rawComponent.getExtras()) {
            replacePlaceholders(player,extra);
        }
    }

    private Plugin getPlugin(ObjectOwner owner){
        if(owner instanceof net.pretronic.libraries.plugin.Plugin){
            return pluginManager.getMappedPlugin((net.pretronic.libraries.plugin.Plugin<?>) owner);
        }else{
            return McNativeLauncher.getPlugin();
        }
    }

    @Override
    public void unregister(ObjectOwner objectOwner) {
        unregisterPlaceHolders(objectOwner);
    }

    private class McNativePlaceHolderExpansion extends PlaceholderExpansion {

        private final Plugin plugin;
        private final String identifier;
        private final PlaceholderHook hook;
        private final ObjectOwner owner;

        public McNativePlaceHolderExpansion(Plugin plugin, String identifier, PlaceholderHook hook,ObjectOwner owner) {
            this.plugin = plugin;
            this.identifier = identifier;
            this.hook = hook;
            this.owner = owner;
            register();
        }

        public ObjectOwner getOwner() {
            return owner;
        }

        @Override
        public boolean persist(){
            return true;
        }

        @Override
        public boolean canRegister(){
            return true;
        }

        @Override
        public String getAuthor(){
            return plugin.getDescription().getAuthors().toString();
        }

        @Override
        public String getIdentifier(){
            return identifier;
        }

        @Override
        public String getVersion(){
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier){
            return replace(playerManager.getMappedPlayer(player),identifier);
        }

        @Override
        public String onRequest(OfflinePlayer player, String identifier){
            if(player.isOnline()){
                return onPlaceholderRequest(player.getPlayer(),identifier);
            }else{
                return replace(playerManager.getPlayer(player.getUniqueId()),identifier);
            }
        }
        private String replace(MinecraftPlayer player,String identifier){
            Object result = hook.onRequest(player,identifier);
            if(result == null) return "NULL";
            else return result instanceof String ? (String) result : result.toString();
        }
    }
}
