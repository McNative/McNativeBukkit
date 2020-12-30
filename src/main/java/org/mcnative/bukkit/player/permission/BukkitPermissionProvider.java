/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 22.02.20, 14:19
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

package org.mcnative.bukkit.player.permission;

import net.pretronic.libraries.utility.Iterators;
import org.bukkit.Bukkit;
import org.mcnative.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;

import java.util.Collection;
import java.util.Collections;

public class BukkitPermissionProvider implements PermissionProvider {

    @Override
    public Collection<MinecraftPlayer> getOperators() {
        return Iterators.map(Bukkit.getOperators(), player -> McNative.getInstance().getPlayerManager().getPlayer(player.getUniqueId()));
    }

    @Override
    public Collection<String> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public PermissionHandler getPlayerHandler(MinecraftPlayer player) {
        if(player instanceof BukkitPlayer){
            return new BukkitPermissionHandler(((BukkitPlayer) player));
        }
        return null;
    }

    @Override
    public boolean createGroup(String name) {
        throw new IllegalArgumentException("Bukkit permission provides does not support permission groups");
    }

    @Override
    public boolean deleteGroup(String name) {
        throw new IllegalArgumentException("Bukkit permission provides does not support permission groups");
    }

    @Override
    public void setGroupPermission(String group, String permission, boolean allowed) {
        throw new IllegalArgumentException("Bukkit permission provides does not support permission groups");
    }

    @Override
    public void unsetGroupPermission(String group, String permission) {
        throw new IllegalArgumentException("Bukkit permission provides does not support permission groups");
    }
}
