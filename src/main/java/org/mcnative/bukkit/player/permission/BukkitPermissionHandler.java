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
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.mcnative.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionResult;

import java.util.Collection;
import java.util.Collections;

public class BukkitPermissionHandler implements PermissionHandler {

    private final BukkitPlayer player;

    private final PlayerDesign design;

    public BukkitPermissionHandler(BukkitPlayer player) {
        this.player = player;
        this.design = new BukkitPlayerDesign(player.getOriginal());
    }

    @Override
    public String getPrimaryGroup() {
        return "default";
    }

    @Override
    public Collection<String> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getPermissions() {
        return Iterators.map(player.getOriginal().getEffectivePermissions(), PermissionAttachmentInfo::getPermission);
    }

    @Override
    public Collection<String> getEffectivePermissions() {
        return getPermissions();
    }

    @Override
    public PlayerDesign getDesign() {
        return getDesign(null);
    }

    @Override
    public PlayerDesign getDesign(MinecraftPlayer forPlayer) {
        return design;
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return player.getOriginal().isPermissionSet(permission);
    }

    @Override
    public boolean isPermissionAssigned(String permission) {
        return player.getOriginal().isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.getOriginal().hasPermission(permission);
    }

    @Override
    public PermissionResult hasPermissionExact(String permission) {
        return player.getOriginal().hasPermission(permission) ? PermissionResult.ALLOWED : PermissionResult.DENIED;
    }

    @Override
    public void setPermission(String permission, boolean allowed) {
        throw new UnsupportedOperationException("Bukkit permissions does not support modifying permission, try to use another permission provider.");
    }

    @Override
    public void unsetPermission(String permission) {
        throw new UnsupportedOperationException("Bukkit permissions does not support modifying permission, try to use another permission provider.");
    }

    @Override
    public void addGroup(String name) {
        throw new UnsupportedOperationException("Bukkit permissions does not support modifying groups, try to use another permission provider.");
    }

    @Override
    public void removeGroup(String name) {
        throw new UnsupportedOperationException("Bukkit permissions does not support modifying groups, try to use another permission provider.");
    }

    @Override
    public boolean isOperator() {
        return player.getOriginal().isOp();
    }

    @Override
    public void setOperator(boolean operator) {
        player.getOriginal().setOp(operator);
    }

    @Override
    public boolean isCached() {
        return true;//Unused without exception
    }

    @Override
    public boolean setCached(boolean b) {
        return false;//Unused without exception
    }

    @Override
    public PermissionHandler reload() {
        return this;
    }

    @Override
    public void onPlayerLogout() {
        player.setDesign(design);
    }
}
