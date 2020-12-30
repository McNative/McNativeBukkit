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

package org.mcnative.bukkit.serviceprovider.permission;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionResult;
import org.mcnative.runtime.common.player.DefaultPlayerDesign;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class VaultPermissionHandler implements PermissionHandler {

    private final UUID playerId;
    private final Permission permission;
    private final Chat chat;

    public VaultPermissionHandler(UUID playerId, Permission permission, Chat chat) {
        this.playerId = playerId;
        this.permission = permission;
        this.chat = chat;
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public boolean setCached(boolean b) {
        return false;
    }

    @Override
    public PermissionHandler reload() {
        return null;
    }

    @Override
    public String getPrimaryGroup() {
        return this.permission.getPrimaryGroup(Bukkit.getPlayer(this.playerId));
    }

    @Override
    public Collection<String> getGroups() {
        return Arrays.asList(permission.getPlayerGroups(Bukkit.getPlayer(this.playerId)));
    }

    @Override
    public Collection<String> getPermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getEffectivePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerDesign getDesign() {
        return new DefaultPlayerDesign("",
                chat.getPlayerPrefix(null, getBukkitPlayer()),
                chat.getPlayerSuffix(null, getBukkitPlayer()),
                chat.getPlayerPrefix(null, getBukkitPlayer()),
                0);
    }

    @Override
    public PlayerDesign getDesign(MinecraftPlayer forPlayer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return this.permission.playerHas(null, getBukkitPlayer(), permission);
    }

    @Override
    public boolean isPermissionAssigned(String permission) {
        return this.permission.playerHas(null, getBukkitPlayer(), permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.permission.playerHas(null, getBukkitPlayer(), permission);
    }

    @Override
    public PermissionResult hasPermissionExact(String permission) {
        //return this.permission.playerHas(null, getBukkitPlayer(), permission);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPermission(String permission, boolean allowed) {
        if(allowed) {
            this.permission.playerAdd(null, getBukkitPlayer(), permission);
        } else {
            this.permission.playerRemove(null, getBukkitPlayer(), permission);
        }
    }

    @Override
    public void unsetPermission(String permission) {
        this.permission.playerRemove(null, getBukkitPlayer(), permission);
    }

    @Override
    public void addGroup(String name) {
        this.permission.playerAddGroup(null, getBukkitPlayer(), name);
    }

    @Override
    public void removeGroup(String name) {
        this.permission.playerRemoveGroup(null, getBukkitPlayer(), name);
    }

    @Override
    public boolean isOperator() {
        OfflinePlayer offlinePlayer = getBukkitPlayer();
        return offlinePlayer.isOp();
    }

    @Override
    public void setOperator(boolean operator) {
        getBukkitPlayer().setOp(operator);
    }

    private OfflinePlayer getBukkitPlayer() {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        Validate.notNull(player, "Bukkit player({}) in VaultPermissionHandler is null", this.playerId);
        return player;
    }
}
