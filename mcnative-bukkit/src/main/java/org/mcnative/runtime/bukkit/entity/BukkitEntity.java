/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.10.19, 20:25
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

package org.mcnative.runtime.bukkit.entity;

import org.mcnative.runtime.bukkit.location.BukkitLocation;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.api.service.entity.Entity;
import org.mcnative.runtime.api.service.world.location.Location;
import org.mcnative.runtime.api.service.world.World;

import java.util.List;
import java.util.UUID;

public interface BukkitEntity<E extends org.bukkit.entity.Entity> extends Entity {

    E getOriginal();

    BukkitWorld getBukkitWorld();

    @Override
    default int getId() {
        return getOriginal().getEntityId();
    }

    @Override
    default UUID getUniqueId() {
        return getOriginal().getUniqueId();
    }

    @Override
    default World getWorld() {
        return getBukkitWorld();
    }

    @Override
    default Location getLocation() {
        return new BukkitLocation(getOriginal().getLocation(), getBukkitWorld());
    }

    @Override
    default boolean isOnGround() {
        return getOriginal().isOnGround();
    }

    @Override
    default List<Entity> getNeighbors(double radius) {
        return null;
    }

    @Override
    default List<Entity> getNeighbors(double x, double y, double z) {
        return null;
    }

    @Override
    default void teleport(Location location) {
        getOriginal().teleport(((BukkitLocation)location).getOriginal());
    }

    @Override
    default void teleport(int x, int y, int z) {
        getOriginal().teleport(new org.bukkit.Location(getBukkitWorld().getOriginal(), x, y, z));
    }

    @Override
    default void teleport(int x, int y, int z, short pitch, short yaw) {
        getOriginal().teleport(new org.bukkit.Location(getBukkitWorld().getOriginal(), x, y, z, pitch, yaw));
    }

    @Override
    default void teleport(World world, int x, int y, int z) {
        getOriginal().teleport(new org.bukkit.Location(getBukkitWorld().getOriginal(), x, y, z));
    }

    @Override
    default void teleport(World world, int x, int y, int z, short pitch, short yaw) {
        getOriginal().teleport(new org.bukkit.Location(((BukkitWorld)world).getOriginal(), x, y, z, pitch, yaw));
    }

    //@Todo
    @Override
    default void despawn() {

    }
}
