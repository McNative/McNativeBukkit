/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.10.19, 20:32
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

package org.mcnative.bukkit.entity.living;

import net.pretronic.libraries.utility.Iterators;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.mcnative.bukkit.entity.BukkitEntity;
import org.mcnative.bukkit.location.BukkitLocation;
import org.mcnative.bukkit.world.BukkitWorld;
import org.mcnative.bukkit.world.block.BukkitBlock;
import org.mcnative.runtime.api.service.entity.Entity;
import org.mcnative.runtime.api.service.entity.living.LivingEntity;
import org.mcnative.runtime.api.service.entity.projectile.Projectile;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.inventory.item.potion.PotionEffect;
import org.mcnative.runtime.api.service.inventory.item.potion.PotionEffectType;
import org.mcnative.runtime.api.service.location.Location;
import org.mcnative.runtime.api.service.location.Vector;
import org.mcnative.runtime.api.service.world.block.Block;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface BukkitLivingEntity<E extends org.bukkit.entity.LivingEntity> extends BukkitEntity<E>, LivingEntity {

    @Override
    default double getEyeHeight() {
        return getOriginal().getEyeHeight();
    }

    @Override
    default double getEyeHeight(boolean ignorePose) {
        return getOriginal().getEyeHeight(ignorePose);
    }

    @Override
    default Location getEyeLocation() {
        return new BukkitLocation(getOriginal().getEyeLocation(), getBukkitWorld());
    }

    @Override
    default List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        Set<org.bukkit.Material> materials = new HashSet<>();
        Iterators.map(transparent, materials, material -> null);
        return Iterators.map(
                getOriginal().getLineOfSight(materials, maxDistance),
                block -> new BukkitBlock(block, (BukkitWorld) getWorld()));
    }

    @Override
    default boolean hasLineOfSight(Entity other) {
        return false;
    }

    @Override
    default Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    default Block getTargetBlock(int maxDistance) {
        return null;
    }

    @Override
    default Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }

    @Override
    default PotionEffect getPotionEffect(PotionEffectType type) {
        return null;
    }

    @Override
    default boolean hasPotionEffect(PotionEffectType type) {
        return false;
    }

    @Override
    default void addPotionEffect(PotionEffect effect) {

    }

    @Override
    default void addPotionEffect(PotionEffect effect, boolean force) {

    }

    @Override
    default void addPotionEffects(PotionEffect... potionEffects) {

    }

    @Override
    default void removePotionEffect(PotionEffectType type) {

    }

    @Override
    default int getHandRaisedTime() {
        return 0;
    }

    @Override
    default boolean isHandRaised() {
        return false;
    }

    @Override
    default double getHealth() {
        return getOriginal().getHealth();
    }

    @Override
    default void setHealth(double health) {
        getOriginal().setHealth(health);
    }

    @Override
    default void heal() {
        getOriginal().setHealth(getMaximumHealth());
    }

    @Override
    default double getMaximumHealth() {
        AttributeInstance attribute = getOriginal().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return attribute != null ? attribute.getValue() : 20.0;
    }

    @Override
    default void setMaximumHealth(double health) {
        getOriginal().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
    }

    @Override
    default void damage(double amount) {
        getOriginal().damage(amount);
    }

    @Override
    default  <T extends Projectile> T launchProjectile(Class<T> projectile) {
        return null;
    }

    @Override
    default <T extends Projectile> T launchProjectile(Class<T> projectile, Vector vector) {
        return null;
    }
}
