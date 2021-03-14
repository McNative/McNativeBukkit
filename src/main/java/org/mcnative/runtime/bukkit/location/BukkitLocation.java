/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.10.19, 20:24
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

package org.mcnative.runtime.bukkit.location;

import org.mcnative.runtime.api.service.world.location.Location;
import org.mcnative.runtime.api.utils.positioning.Position;
import org.mcnative.runtime.api.utils.positioning.Vector;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.api.service.world.Chunk;
import org.mcnative.runtime.api.service.world.World;

public class BukkitLocation extends BukkitVector implements Location {

    private final org.bukkit.Location original;
    private BukkitWorld world;

    public BukkitLocation(org.bukkit.Location original, BukkitWorld world) {
        super(new org.bukkit.util.Vector(original.getX(), original.getY(), original.getZ()));
        this.original = original;
        this.world = world;
    }

    public org.bukkit.Location getOriginal() {
        return original;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public Chunk getChunk() {
        return this.world.getChunk(this.original.getBlockX(), this.original.getBlockZ());
    }

    @Override
    public float getPitch() {
        return this.original.getPitch();
    }

    @Override
    public float getYaw() {
        return this.original.getYaw();
    }

    @Override
    public void setPitch(float pitch) {
        this.original.setPitch(pitch);
    }

    @Override
    public void setYaw(float yaw) {
        this.original.setYaw(yaw);
    }

    @Override
    public Vector getDirection() {
        return new BukkitVector(this.original.getDirection());
    }

    @Override
    public Position setDirection(Vector vector) {
        this.original.setDirection(new org.bukkit.util.Vector(vector.getX(),vector.getY(),vector.getZ()));
        return this;
    }

    @Override
    public void setWorld(World world) {
        this.world = (BukkitWorld) world;
        this.original.setWorld(this.world.getOriginal());
    }

    @Override
    public Position toPosition() {
        return this;
    }

    @Override
    public Location clone() {
        return new BukkitLocation(this.original.clone(), this.world);
    }
}
