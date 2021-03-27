/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 23.10.19, 18:27
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

import org.mcnative.runtime.bukkit.world.block.BukkitBlock;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.sound.SoundCategory;
import org.mcnative.runtime.api.service.entity.Entity;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.entity.living.animal.Animal;
import org.mcnative.runtime.api.service.entity.living.monster.Monster;
import org.mcnative.runtime.api.service.entity.projectile.arrow.Arrow;
import org.mcnative.runtime.api.service.inventory.item.DroppedItem;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.world.location.Location;
import org.mcnative.runtime.api.utils.positioning.Vector;
import org.mcnative.runtime.api.service.world.Biome;
import org.mcnative.runtime.api.service.world.Chunk;
import org.mcnative.runtime.api.service.world.Effect;
import org.mcnative.runtime.api.service.world.TreeType;
import org.mcnative.runtime.api.service.world.block.Block;
import org.mcnative.runtime.api.service.world.block.data.BlockData;
import org.mcnative.runtime.api.service.world.particle.Particle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class BukkitChunk implements Chunk {

    private final org.bukkit.Chunk original;
    private final BukkitWorld world;

    public BukkitChunk(org.bukkit.Chunk original, BukkitWorld world) {
        this.original = original;
        this.world = world;
    }

    public org.bukkit.Chunk getOriginal() {
        return original;
    }

    @Override
    public BukkitWorld getWorld() {
        return this.world;
    }


    //@Todo can have multiple biomes
    @Override
    public Biome getBiome() {
        return null;
    }

    @Override
    public void setBiome(Biome biome) {
        for(int x = getOriginal().getX(); x <= getOriginal().getX(); x++) {
            for(int z = getOriginal().getZ(); z <= getOriginal().getZ(); z++) {
                getWorld().setBiome(x, z, biome);
            }
        }
    }

    @Override
    public boolean isLoaded() {
        return getOriginal().isLoaded();
    }

    @Override
    public boolean isInUse() {
        return getOriginal().getEntities().length != 0;
    }

    @Override
    public boolean isGenerated() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void load() {
        this.original.load(true);
    }

    @Override
    public void unload() {
        this.original.unload(true);
    }

    @Override
    public void tryUnload() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void regenerate() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isForceLoaded() {
        return this.original.isForceLoaded();
    }

    @Override
    public void setForceLoaded(boolean forceLoaded) {
        this.original.setForceLoaded(forceLoaded);
    }

    @Override
    public void fill(Material material) {
        for(int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    getOriginal().getBlock(x, y, z).setType(org.bukkit.Material.valueOf(material.getName()));
                }
            }
        }
    }

    @Override
    public void fill(int z, Material material) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void fill(int startZ, int endZ, Material material) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Iterator<Block> iterator() {
        Collection<Block> blocks = new ArrayList<>();
        for(int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    blocks.add(new BukkitBlock(getOriginal().getBlock(x, y, z), getWorld()));
                }
            }
        }
        return blocks.iterator();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return new BukkitBlock(getOriginal().getBlock(x, y, z), getWorld());
    }

    @Override
    public Block getBlock(Vector location) {
        return getBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public Block getHighestBlock(int x, int y) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Block getHighestBlock(Vector location) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Block getLowestBlock(int x, int y) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Block getLowestBlock(Vector location) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Biome getBiome(int x, int y) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Biome getBiome(Vector point) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setBiome(int x, int y, Biome biome) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setBiome(Vector point, Biome biome) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public double getTemperature(int x, int z) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public double getHumidity(int x, int z) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Entity> getEntities() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Entity> getLivingEntities() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Animal> getAnimals() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Monster> getMonsters() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Player> getPlayers() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Entity> getEntitiesNear(Vector point, Vector Vector) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Entity> getEntitiesNear(Vector point, Vector Vector, Predicate<Entity> filter) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <E extends Entity> Collection<E> getEntitiesNear(Class<E> entityType, Vector point, Vector Vector) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <E extends Entity> Collection<E> getEntitiesNear(Class<E> entityType, Vector point, Vector Vector, Predicate<Entity> filter) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <E extends Entity> Collection<E> getEntities(Class<E> entityClass) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <E extends Entity> E spawnEntity(Location location, Class<?> clazz) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <E extends Entity> E spawnNoAIEntity(Location location, Class<?> clazz) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <A extends Arrow> A spawnArrow(Vector point, Vector direction, float speed, float spread, Class<A> arrowClass) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Arrow spawnArrow(Vector point, Vector direction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void spawnParticle(Vector point, Particle particle, int amount, Iterable<OnlineMinecraftPlayer> receivers) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void spawnParticle(Vector point, Particle particle, int amount, Vector Vector, Iterable<? extends OnlineMinecraftPlayer> receivers) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void spawnParticle(Vector point, Particle particle, int amount) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void spawnParticle(Vector point, Particle particle, int amount, Vector Vector) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DroppedItem dropItem(Vector location, ItemStack item) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DroppedItem dropItemNaturally(Vector location, ItemStack item) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean createExplosion(Vector point, float power) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean createExplosion(Vector point, float power, boolean fire) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean createExplosion(Vector point, float power, boolean fire, boolean destroyBlocks) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean createExplosion(Vector point, Vector Vector, float power) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean createExplosion(Vector point, Vector Vector, float power, boolean fire) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean createExplosion(Vector point, Vector Vector, float power, boolean fire, boolean destroyBlocks) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void strikeLightning(Vector location, boolean damage) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void createFallingBlock(Vector point, Material material) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void createFallingBlock(Vector point, BlockData data) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void playEffect(Location location, Effect effect) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void playEffect(Location location, Effect effect, Vector Vector) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void playSound(Vector vector, String s, SoundCategory soundCategory, float v, float v1) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean generateTree(Vector location, TreeType treeType) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
