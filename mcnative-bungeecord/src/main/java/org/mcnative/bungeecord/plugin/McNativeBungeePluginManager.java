/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 26.08.19, 19:20
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

package org.mcnative.bungeecord.plugin;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.event.EventBus;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class McNativeBungeePluginManager extends PluginManager{

    public McNativeBungeePluginManager(ProxyServer proxy) {
        super(proxy);
    }

    public McNativeBungeePluginManager(ProxyServer proxy, Yaml yaml, EventBus eventBus) {
        super(proxy, yaml, eventBus);
    }

    @Override
    public void registerCommand(Plugin plugin, Command command) {
        super.registerCommand(plugin, command);
    }

    @Override
    public void unregisterCommand(Command command) {
        super.unregisterCommand(command);
    }

    @Override
    public void unregisterCommands(Plugin plugin) {
        super.unregisterCommands(plugin);
    }

    @Override
    public boolean isExecutableCommand(String commandName, CommandSender sender) {
        return super.isExecutableCommand(commandName, sender);
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        return super.dispatchCommand(sender, commandLine);
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine, List<String> tabResults) {
        return super.dispatchCommand(sender, commandLine, tabResults);
    }

    @Override
    public Collection<Plugin> getPlugins() {
        return super.getPlugins();
    }

    @Override
    public Plugin getPlugin(String name) {
        return super.getPlugin(name);
    }

    @Override
    public void loadPlugins() {
        super.loadPlugins();
    }

    @Override
    public void enablePlugins() {
        super.enablePlugins();
    }

    @Override
    public void detectPlugins(File folder) {
        super.detectPlugins(folder);
    }

    @Override
    public <T extends Event> T callEvent(T event) {
        return super.callEvent(event);
    }

    @Override
    public void registerListener(Plugin plugin, Listener listener) {
        super.registerListener(plugin, listener);
    }

    @Override
    public void unregisterListener(Listener listener) {
        super.unregisterListener(listener);
    }

    @Override
    public void unregisterListeners(Plugin plugin) {
        super.unregisterListeners(plugin);
    }

    @Override
    public Collection<Map.Entry<String, Command>> getCommands() {
        return super.getCommands();
    }
}
