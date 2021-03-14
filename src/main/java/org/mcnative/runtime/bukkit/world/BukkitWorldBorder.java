/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.10.19, 16:42
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

package org.mcnative.runtime.bukkit.world;

import org.mcnative.runtime.bukkit.location.BukkitLocation;
import org.mcnative.runtime.api.service.world.location.Location;
import org.mcnative.runtime.api.service.world.location.LocationAble;
import org.mcnative.runtime.api.utils.positioning.Vector;
import org.mcnative.runtime.api.service.world.WorldBorder;

public class BukkitWorldBorder implements WorldBorder {

    private final org.bukkit.WorldBorder original;
    private final BukkitWorld world;

    public BukkitWorldBorder(org.bukkit.WorldBorder original, BukkitWorld world) {
        this.original = original;
        this.world = world;
    }

    @Override
    public double getSize() {
        return this.original.getSize();
    }

    @Override
    public void setSize(double size) {
        this.original.setSize(size);
    }

    @Override
    public Location getCenter() {
        return new BukkitLocation(this.original.getCenter(), world);
    }

    @Override
    public void setCenter(Vector location) {
        this.original.setCenter(location instanceof BukkitLocation ? ((BukkitLocation)location).getOriginal() :
                new org.bukkit.Location(this.world.getOriginal(), location.getX(), location.getY(), location.getZ()));
    }

    @Override
    public Location[] getCorners() {
        double sideLength = this.original.getSize()/2;
        Location[] corners = new Location[8];
        Location center = getCenter();
        for (int i = 0; i < 8; i++) {
            corners[i] = center.clone();
        }
        corners[0].add(sideLength, sideLength, sideLength);
        corners[1].add(-sideLength, sideLength, sideLength);
        corners[2].add(sideLength, sideLength, -sideLength);
        corners[3].add(-sideLength, sideLength, -sideLength);
        corners[4].add(sideLength, -sideLength, sideLength);
        corners[5].add(-sideLength, -sideLength, sideLength);
        corners[6].add(sideLength, -sideLength, -sideLength);
        corners[7].add(-sideLength, -sideLength, -sideLength);
        return corners;
    }

    @Override
    public double getDamageBuffer() {
        return this.original.getDamageBuffer();
    }

    @Override
    public void setDamageBuffer(double blocks) {
        this.original.setDamageBuffer(blocks);
    }

    @Override
    public double getDamageAmount() {
        return this.original.getDamageAmount();
    }

    @Override
    public void setDamageAmount(double damage) {
        this.original.setDamageAmount(damage);
    }

    @Override
    public int getWarningTime() {
        return this.original.getWarningTime();
    }

    @Override
    public void setWarningTime(int seconds) {
        this.original.setWarningTime(seconds);
    }

    @Override
    public int getWarningDistance() {
        return this.original.getWarningDistance();
    }

    @Override
    public void setWarningDistance(int distance) {
        this.original.setWarningDistance(distance);
    }

    @Override
    public boolean isInside(Location location) {
        return this.original.isInside(((BukkitLocation)location).getOriginal());
    }

    @Override
    public boolean isInside(LocationAble entity) {
        return isInside(entity.getLocation());
    }

    @Override
    public boolean isOutside(Location location) {
        return !isInside(location);
    }

    @Override
    public boolean isOutside(LocationAble entity) {
        return !isInside(entity);
    }
}
