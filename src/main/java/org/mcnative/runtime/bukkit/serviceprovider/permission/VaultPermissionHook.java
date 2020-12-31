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
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;

public class VaultPermissionHook {

    private final PermissionProvider permissionProvider;
    private final Permission permission;
    private final Chat chat;

    public VaultPermissionHook(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
        this.permission = new PermissionHook();
        this.chat = new ChatHook(permission);
    }

    public Permission getPermission() {
        return permission;
    }

    public Chat getChat() {
        return chat;
    }

    public class ChatHook extends Chat {

        public ChatHook(Permission perms) {
            super(perms);
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return permissionProvider != null;
        }

        @Override
        public String getPlayerPrefix(String world, String player) {
            return getPlayer(player).getPermissionHandler().getDesign().getPrefix();
        }

        @Override
        public void setPlayerPrefix(String world, String player, String prefix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPlayerSuffix(String world, String player) {
            return getPlayer(player).getPermissionHandler().getDesign().getSuffix();
        }

        @Override
        public void setPlayerSuffix(String world, String player, String suffix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getGroupPrefix(String world, String group) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGroupPrefix(String world, String group, String prefix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getGroupSuffix(String world, String group) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGroupSuffix(String world, String group, String suffix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPlayerInfoInteger(String world, String player, String node, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGroupInfoInteger(String world, String group, String node, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPlayerInfoDouble(String world, String player, String node, double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGroupInfoDouble(String world, String group, String node, double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPlayerInfoString(String world, String player, String node, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getGroupInfoString(String world, String group, String node, String defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGroupInfoString(String world, String group, String node, String value) {
            throw new UnsupportedOperationException();
        }
    }

    public class PermissionHook extends Permission {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return permissionProvider != null;
        }

        @Override
        public boolean hasSuperPermsCompat() {
            return false;
        }

        @Override
        public boolean playerHas(String world, String player, String permission) {
            return getPlayer(player).getPermissionHandler().hasPermission(permission);
        }

        @Override
        public boolean playerAdd(String world, String player, String permission) {
            boolean allowed = permission.startsWith("-");
            getPlayer(player).getPermissionHandler().setPermission(permission, allowed);
            return true;
        }

        @Override
        public boolean playerRemove(String world, String player, String permission) {
            getPlayer(player).getPermissionHandler().unsetPermission(permission);
            return true;
        }

        @Override
        public boolean groupHas(String world, String group, String permission) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean groupAdd(String world, String group, String permission) {
            boolean allowed = permission.startsWith("-");
            permissionProvider.setGroupPermission(group, permission, allowed);
            return true;
        }

        @Override
        public boolean groupRemove(String world, String group, String permission) {
            permissionProvider.unsetGroupPermission(group, permission);
            return true;
        }

        @Override
        public boolean playerInGroup(String world, String player, String group) {
            return getPlayer(player).getPermissionHandler().getGroups().contains(group);
        }

        @Override
        public boolean playerAddGroup(String world, String player, String group) {
            getPlayer(player).getPermissionHandler().addGroup(group);
            return true;
        }

        @Override
        public boolean playerRemoveGroup(String world, String player, String group) {
            getPlayer(player).getPermissionHandler().removeGroup(group);
            return true;
        }

        @Override
        public String[] getPlayerGroups(String world, String player) {
            return getPlayer(player).getPermissionHandler().getGroups().toArray(new String[0]);
        }

        @Override
        public String getPrimaryGroup(String world, String player) {
            return getPlayer(player).getPermissionHandler().getPrimaryGroup();
        }

        @Override
        public String[] getGroups() {
            return permissionProvider.getGroups().toArray(new String[0]);
        }

        @Override
        public boolean hasGroupSupport() {
            return true;
        }
    }

    private MinecraftPlayer getPlayer(String name) {
        return McNative.getInstance().getPlayerManager().getPlayer(name);
    }
}
