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

import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.serviceprovider.permission.PermissionGroup;
import org.mcnative.common.serviceprovider.permission.PermissionHandler;
import org.mcnative.common.serviceprovider.permission.PermissionProvider;

import java.util.Collection;

public class BukkitPermissionProvider implements PermissionProvider {

    @Override
    public Collection<MinecraftPlayer> getOperators() {
        return null;
    }

    @Override
    public Collection<String> getGroups() {
        return null;
    }

    @Override
    public PermissionHandler getPlayerHandler(MinecraftPlayer player) {
        return null;
    }

    @Override
    public PermissionGroup createGroup(String name) {
        throw new IllegalArgumentException("Bukkit permission provides does not support group creating.");
    }

    @Override
    public boolean deleteGroup(String name) {
        return false;
    }
}
