/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 13.04.20, 20:37
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

package org.mcnative.runtime.bukkit.plugin.mapped;

import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.plugin.description.PluginDescription;
import org.bukkit.plugin.Plugin;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;

public class BukkitPlugin extends MinecraftPlugin {

    private final Plugin plugin;

    public BukkitPlugin(Plugin plugin,BukkitPluginLoader loader,PluginDescription description) {
        this.plugin = plugin;
        initialize(description,loader,new JdkPretronicLogger(plugin.getLogger()), McNative.getInstance());
    }

    @Override
    public McNative getRuntime() {
        return McNative.getInstance();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public int hashCode() {
        return plugin.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else if(obj instanceof BukkitPlugin) return plugin.equals(((BukkitPlugin) obj).getPlugin());
        else if(obj instanceof Plugin) return plugin.equals(obj);
        else return plugin.equals(obj);
    }

    @Override
    public String toString() {
        return plugin.toString();
    }
}
