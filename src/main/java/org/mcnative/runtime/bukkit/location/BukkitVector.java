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

import net.pretronic.libraries.utility.Validate;
import org.bukkit.util.NumberConversions;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.api.service.location.Location;
import org.mcnative.runtime.api.service.location.Vector;
import org.mcnative.runtime.api.service.world.World;

public class BukkitVector implements Vector {

    private final org.bukkit.util.Vector original;

    public BukkitVector(org.bukkit.util.Vector original) {
        this.original = original;
    }

    public BukkitVector(double x, double y, double z) {
        this(new org.bukkit.util.Vector(x, y, z));
    }

    @Override
    public double getX() {
        return this.original.getX();
    }

    @Override
    public double getY() {
        return this.original.getY();
    }

    @Override
    public double getZ() {
        return this.original.getZ();
    }

    @Override
    public void setX(double x) {
        this.original.setX(x);
    }

    @Override
    public void setY(double y) {
        this.original.setY(y);
    }

    @Override
    public void setZ(double z) {
        this.original.setZ(z);
    }

    @Override
    public Vector middle(Vector other) {
        double x = (this.original.getX() + other.getX()) / 2;
        double y = (this.original.getY() + other.getY()) / 2;
        double z = (this.original.getZ() + other.getZ()) / 2;
        return new BukkitVector(x, y, z);
    }

    @Override
    public double distance(Vector other) {
        return Math.sqrt(NumberConversions.square(this.original.getX() - other.getX()) +
                NumberConversions.square(this.original.getY() - other.getY()) +
                NumberConversions.square(this.original.getZ() - other.getZ()));
    }

    @Override
    public double distanceSquared(Vector other) {
        return NumberConversions.square(this.original.getX() - other.getX()) +
                NumberConversions.square(this.original.getY() - other.getY()) +
                NumberConversions.square(this.original.getZ() - other.getZ());
    }

    @Override
    public float angle(Vector other) {
        double dot = dot(other) / (length() * other.length());
        return (float) Math.acos(dot);
    }

    @Override
    public void add(Vector other) {
        this.original.add(new org.bukkit.util.Vector(other.getX(), other.getY(), other.getZ()));
    }

    @Override
    public void add(double x, double y, double z) {
        this.original.add(new org.bukkit.util.Vector(x, y, z));
    }

    @Override
    public void subtract(Vector other) {
        this.original.subtract(new org.bukkit.util.Vector(other.getX(), other.getY(), other.getZ()));
    }

    @Override
    public void subtract(double x, double y, double z) {
        this.original.subtract(new org.bukkit.util.Vector(x, y, z));
    }

    @Override
    public void multiply(Vector multiplier) {
        this.original.multiply(new org.bukkit.util.Vector(multiplier.getX(), multiplier.getY(), multiplier.getZ()));
    }

    @Override
    public void divide(Vector divider) {
        this.original.divide(new org.bukkit.util.Vector(divider.getX(), divider.getY(), divider.getZ()));
    }

    @Override
    public double length() {
        return this.original.length();
    }

    @Override
    public double lengthSquared() {
        return this.original.lengthSquared();
    }

    @Override
    public boolean isIn(Vector min, Vector max) {
        return this.original.getX() >= min.getX() &&
                this.original.getX() <= max.getX() &&
                this.original.getY() >= min.getY() &&
                this.original.getY() <= max.getY() &&
                this.original.getZ() >= min.getZ() &&
                this.original.getZ() <= max.getZ();
    }

    @Override
    public boolean isOut(Vector min, Vector max) {
        return !isIn(min, max);
    }

    @Override
    public double dot(Vector other) {
        return this.original.getX() * other.getX() + this.original.getY() * other.getY() + this.original.getZ() * other.getZ();
    }

    @Override
    public Location toLocation(World world) {
        Validate.isTrue(world instanceof BukkitWorld, "World is not a BukkitWorld.");
        return new BukkitLocation(new org.bukkit.Location(((BukkitWorld)world).getOriginal(), this.original.getX(), this.original.getY(), this.original.getZ()), (BukkitWorld) world);
    }

    @Override
    public Vector clone() {
        return new BukkitVector(this.original.clone());
    }
}
