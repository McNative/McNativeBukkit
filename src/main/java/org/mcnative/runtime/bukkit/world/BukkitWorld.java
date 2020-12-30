/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.10.19, 00:40
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

import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.bukkit.BukkitMcNative;
import org.mcnative.runtime.bukkit.location.BukkitLocation;
import org.mcnative.runtime.bukkit.world.block.BukkitBlock;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.sound.Sound;
import org.mcnative.runtime.api.player.sound.SoundCategory;
import org.mcnative.runtime.api.service.entity.Entity;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.entity.living.animal.Animal;
import org.mcnative.runtime.api.service.entity.living.monster.Monster;
import org.mcnative.runtime.api.service.entity.projectile.arrow.Arrow;
import org.mcnative.runtime.api.service.inventory.item.DroppedItem;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.location.Location;
import org.mcnative.runtime.api.service.location.Vector;
import org.mcnative.runtime.api.service.world.*;
import org.mcnative.runtime.api.service.world.block.Block;
import org.mcnative.runtime.api.service.world.block.data.BlockData;
import org.mcnative.runtime.api.service.world.particle.Particle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class BukkitWorld implements World {

    private final org.bukkit.World original;

    public BukkitWorld(org.bukkit.World original) {
        this.original = original;
    }

    public org.bukkit.World getOriginal() {
        return original;
    }

    @Override
    public String getName() {
        return this.original.getName();
    }

    @Override
    public UUID getUniqueId() {
        return this.original.getUID();
    }

    @Override
    public long getSeed() {
        return this.original.getSeed();
    }

    @Override
    public WorldSettings getSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorldEnvironment getEnvironment() {
        switch (this.original.getEnvironment()) {
            case NORMAL: return WorldEnvironment.EARTH;
            case NETHER: return WorldEnvironment.NETHER;
            case THE_END: return WorldEnvironment.End;
        }
        throw new UnsupportedOperationException(String.format("WorldEnvironment %s is unsupported.", this.original.getEnvironment().name()));
    }

    @Override
    public WorldBorder getBorder() {
        return new BukkitWorldBorder(this.original.getWorldBorder(), this);
    }

    @Override
    public Location getSpawnLocation() {
        return new BukkitLocation(this.original.getSpawnLocation(), this);
    }

    @Override
    public void setSpawnLocation(Location spawnLocation) {
        this.original.setSpawnLocation(((BukkitLocation)spawnLocation).getOriginal());
    }

    @Override
    public boolean isLoaded() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void unload() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Chunk getChunk(int x, int z) {//@Todo chunks from pool ?
        return new BukkitChunk(this.original.getChunkAt(x, z), this);
    }

    @Override
    public Chunk getChunk(Vector location) {
        return getChunk((int) location.getX(), (int) location.getZ());
    }

    @Override
    public Collection<Chunk> getLoadedChunks() {
        Collection<Chunk> loadedChunks = new ArrayList<>();
        for (org.bukkit.Chunk loadedChunk : this.original.getLoadedChunks()) {
            loadedChunks.add(new BukkitChunk(loadedChunk, this));
        }
        return loadedChunks;
    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        Collection<Chunk> loadedChunks = new ArrayList<>();
        for (org.bukkit.Chunk loadedChunk : this.original.getForceLoadedChunks()) {
            loadedChunks.add(new BukkitChunk(loadedChunk, this));
        }
        return loadedChunks;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return loadChunk(x, z, true);
    }

    @Override
    public Chunk loadChunk(int x, int z, boolean generate) {
        this.original.loadChunk(x, z, generate);
        return new BukkitChunk(this.original.getChunkAt(x, z), this);
    }

    @Override
    public Chunk loadChunk(Vector location) {
        return loadChunk(location, true);
    }

    @Override
    public Chunk loadChunk(Vector location, boolean generate) {
        return loadChunk((int) location.getX(), (int) location.getZ(), generate);
    }

    @Override
    public Chunk loadChunk(Chunk chunk) {
        this.original.loadChunk(((BukkitChunk)chunk).getOriginal());
        return chunk;
    }

    @Override
    public long getTime() {
        return this.original.getTime();
    }

    @Override
    public void setTime(long time) {
        this.original.setTime(time);
    }

    @Override
    public long getFullTime() {
        return this.original.getFullTime();
    }

    @Override
    public void setFullTime(long time) {
        this.original.setFullTime(time);
    }

    @Override
    public boolean hasStorm() {
        return this.original.hasStorm();
    }

    @Override
    public void setStorm(boolean storm) {
        this.original.setStorm(storm);
    }

    @Override
    public boolean isThundering() {
        return this.original.isThundering();
    }

    @Override
    public void setThundering(boolean thundering) {
        this.original.setThundering(thundering);
    }

    @Override
    public int getThunderDuration() {
        return this.original.getThunderDuration();
    }

    @Override
    public void setThunderDuration(int duration) {
        this.original.setThunderDuration(duration);
    }

    @Override
    public int getWeatherDuration() {
        return this.original.getWeatherDuration();
    }

    @Override
    public void setWeatherDuration(int duration) {
        this.original.setWeatherDuration(duration);
    }

    @Override
    public boolean isAutoSave() {
        return this.original.isAutoSave();
    }

    @Override
    public void setAutoSave(boolean autoSave) {
        this.original.setAutoSave(autoSave);
    }

    @Override
    public void save() {
        this.original.save();
    }

    @Override
    public File getFolder() {
        return this.original.getWorldFolder();
    }

    @Override
    public boolean isDefault() {
        return McNative.getInstance(BukkitMcNative.class).getServerProperties().getString("level-name").equals(getName());
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return new BukkitBlock(this.original.getBlockAt(x, y, z), this);
    }

    @Override
    public Block getBlock(Vector location) {
        return getBlock((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    @Override
    public Block getHighestBlock(int x, int z) {
        return new BukkitBlock(this.original.getHighestBlockAt(x, z), this);
    }

    @Override
    public Block getHighestBlock(Vector location) {
        return getHighestBlock((int) location.getX(), (int) location.getZ());
    }

    @Override
    public Block getLowestBlock(int x, int z) {
        for (int y = 0; y < this.original.getMaxHeight(); y++) {
            org.bukkit.block.Block block = this.original.getBlockAt(x, y, z);
            if(!block.isEmpty()) return new BukkitBlock(block, this);
        }
        return null;
    }

    @Override
    public Block getLowestBlock(Vector location) {
        return getLowestBlock((int) location.getX(), (int) location.getZ());
    }

    @Override
    public Biome getBiome(int x, int z) {
        return Biome.getBiome(getOriginal().getBiome(x, z).name());
    }

    @Override
    public Biome getBiome(Vector point) {
        return getBiome(point.getBlockX(), point.getBlockZ());
    }

    @Override
    public void setBiome(int x, int z, Biome biome) {
        getOriginal().setBiome(x, z, org.bukkit.block.Biome.valueOf(biome.getName()));
    }

    @Override
    public void setBiome(Vector point, Biome biome) {
        setBiome(point.getBlockX(), point.getBlockZ(), biome);
    }

    @Override
    public double getTemperature(int x, int z) {
        return this.original.getTemperature(x, z);
    }

    @Override
    public double getHumidity(int x, int z) {
        return this.original.getHumidity(x, z);
    }

    @Override
    public Collection<Entity> getEntities() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<Entity> getLivingEntities() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<Animal> getAnimals() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<Monster> getMonsters() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<Player> getPlayers() {
        return Iterators.map(getOriginal().getPlayers(), player -> (Player) McNative.getInstance().getPlayerManager().getPlayer(player.getUniqueId()));
    }

    @Override
    public Collection<Entity> getEntitiesNear(Vector point, Vector Vector) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<Entity> getEntitiesNear(Vector point, Vector Vector, Predicate<Entity> filter) {
        throw new UnsupportedOperationException("Currently not supported");
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
        return getOriginal().createExplosion(point.getBlockX(), point.getBlockY(), point.getBlockZ(), power);
    }

    @Override
    public boolean createExplosion(Vector point, float power, boolean fire) {
        return getOriginal().createExplosion(point.getBlockX(), point.getBlockY(), point.getBlockZ(), power, fire);
    }

    @Override
    public boolean createExplosion(Vector point, float power, boolean fire, boolean destroyBlocks) {
        return getOriginal().createExplosion(point.getBlockX(), point.getBlockY(), point.getBlockZ(), power, fire, destroyBlocks);
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
    public void playSound(Vector point, Sound sound, float volume, float pitch) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void playSound(Vector point, Sound sound, SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean generateTree(Vector location, TreeType treeType) {
        return getOriginal().generateTree(new org.bukkit.Location(getOriginal(), location.getBlockX(), location.getBlockY(), location.getBlockZ())
                , org.bukkit.TreeType.valueOf(treeType.getName()));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BukkitWorld && ((BukkitWorld)obj).getName().equals(getName());
    }
}
