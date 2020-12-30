/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.10.19, 20:21
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

package org.mcnative.runtime.bukkit.world.block;

import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.bukkit.location.BukkitLocation;
import org.mcnative.runtime.bukkit.world.BukkitChunk;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.location.Location;
import org.mcnative.runtime.api.service.location.Vector;
import org.mcnative.runtime.api.service.world.Biome;
import org.mcnative.runtime.api.service.world.Chunk;
import org.mcnative.runtime.api.service.world.World;
import org.mcnative.runtime.api.service.world.block.Block;
import org.mcnative.runtime.api.service.world.block.BlockDirection;
import org.mcnative.runtime.api.service.world.block.data.BlockData;

import java.util.Collection;

public class BukkitBlock implements Block {

    private final org.bukkit.block.Block original;
    private final BukkitWorld world;

    public BukkitBlock(org.bukkit.block.Block original, BukkitWorld world) {
        this.original = original;
        this.world = world;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public Location getLocation() {
        return new BukkitLocation(this.original.getLocation(), this.world);
    }

    @Override
    public Material getMaterial() {
        return Material.getMaterial(original.getType().name());
    }

    @Override
    public void setMaterial(Material material) {
        original.setType(org.bukkit.Material.valueOf(material.getName()));
    }

    @Override
    public boolean isEmpty() {
        return this.original.isEmpty();
    }

    @Override
    public boolean isLiquid() {
        return this.original.isLiquid();
    }

    @Override
    public boolean isPassable() {
        return this.original.isPassable();
    }

    @Override
    public BlockData getData() {
        return null;
    }

    @Override
    public void setData(BlockData data) {

    }

    @Override
    public Chunk getChunk() {
        return new BukkitChunk(this.original.getChunk(), this.world);
    }

    @Override
    public Biome getBiome() {
        return Biome.getBiome(original.getBiome().name());
    }

    @Override
    public void setBiome(Biome biome) {
        original.setBiome(org.bukkit.block.Biome.valueOf(biome.getName()));
    }

    @Override
    public double getTemperature() {
        return this.original.getTemperature();
    }

    @Override
    public double getHumidity() {
        return this.original.getHumidity();
    }

    @Override
    public byte getLightLevel() {
        return this.original.getLightLevel();
    }

    @Override
    public byte getLightFromSky() {
        return this.original.getLightFromSky();
    }

    @Override
    public byte getLightFromBlocks() {
        return this.original.getLightFromBlocks();
    }

    @Override
    public Block getRelativeBlock(Vector Vector) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Block getRelativeBlock(BlockDirection direction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isBlockPowered() {
        return this.original.isBlockPowered();
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        return this.original.isBlockIndirectlyPowered();
    }

    @Override
    public boolean isBlockDirectionPowered(BlockDirection direction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isBlockIndirectlyPowered(BlockDirection direction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getPower() {
        return this.original.getBlockPower();
    }

    @Override
    public int getPower(BlockDirection direction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void breakNaturally() {
        this.original.breakNaturally();
    }

    @Override
    public void breakNaturally(ItemStack tool) {
        original.breakNaturally(((BukkitItemStack)tool).getOriginal());
    }

    @Override
    public Collection<ItemStack> getDrops() {
        return Iterators.map(original.getDrops(), BukkitItemStack::new);
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack tool) {
        return Iterators.map(original.getDrops(((BukkitItemStack)tool).getOriginal()), BukkitItemStack::new);
    }

    @Override
    public boolean isAllowDrops() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setAllowDrop(boolean allowDrop) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(OnlineMinecraftPlayer player) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
