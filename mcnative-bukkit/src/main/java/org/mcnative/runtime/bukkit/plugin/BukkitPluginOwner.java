/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 04.08.19 10:46
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

import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.plugin.Plugin;

public class BukkitPluginOwner implements ObjectOwner {

    private final Plugin plugin;

    public BukkitPluginOwner(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else if(obj instanceof  BukkitPluginOwner) return ((BukkitPluginOwner) obj).getPlugin().equals(plugin);
        else return this.plugin.equals(obj);
    }
}

