/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 22.04.20, 22:35
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

package org.mcnative.runtime.bukkit.serviceprovider.permission;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.pretronic.libraries.utility.Iterators;
import org.bukkit.Bukkit;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;

import java.util.Arrays;
import java.util.Collection;

public class VaultPermissionProvider implements PermissionProvider {

    private Permission permission;
    private Chat chat;

    public VaultPermissionProvider(Permission permission, Chat chat) {
        this.permission = permission;
        this.chat = chat;
    }

    public VaultPermissionProvider setPermission(Permission permission) {
        this.permission = permission;
        return this;
    }

    public VaultPermissionProvider setChat(Chat chat) {
        this.chat = chat;
        return this;
    }

    @Override
    public Collection<MinecraftPlayer> getOperators() {
        return Iterators.map(Bukkit.getOperators(), offlinePlayer ->
                McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId()));
    }

    @Override
    public Collection<String> getGroups() {
        return Arrays.asList(this.chat.getGroups());
    }

    @Override
    public PermissionHandler getPlayerHandler(MinecraftPlayer player) {
        return new VaultPermissionHandler(player.getUniqueId(), permission, chat);
    }

    @Override
    public boolean createGroup(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteGroup(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGroupPermission(String group, String permission, boolean allowed) {
        if(allowed) {
            this.permission.groupAdd((String)null, group,  permission);
        } else {
            this.permission.groupRemove((String)null, group,  permission);
        }
    }

    @Override
    public void unsetGroupPermission(String group, String permission) {
        this.permission.groupRemove((String) null, group, permission);
    }
}
