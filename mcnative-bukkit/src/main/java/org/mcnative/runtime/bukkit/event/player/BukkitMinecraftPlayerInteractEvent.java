package org.mcnative.runtime.bukkit.event.player;

import net.pretronic.libraries.utility.Validate;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.MinecraftPlayerInteractEvent;
import org.mcnative.runtime.api.service.inventory.EquipmentSlot;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.world.block.Block;
import org.mcnative.runtime.api.service.world.block.BlockAction;
import org.mcnative.runtime.api.service.world.block.BlockDirection;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.bukkit.world.block.BukkitBlock;

public class BukkitMinecraftPlayerInteractEvent implements MinecraftPlayerInteractEvent {

    private final PlayerInteractEvent original;
    private final Player player;

    private BlockAction action;
    private BlockDirection blockDirection;
    private Block block;
    private EquipmentSlot equipmentSlot;
    private ItemStack item;

    public BukkitMinecraftPlayerInteractEvent(PlayerInteractEvent original, Player player) {
        Validate.notNull(original, player);
        this.original = original;
        this.player = player;
    }

    @Override
    public BlockAction getAction() {
        if(action == null) {
            this.action = mapAction(original.getAction());
        }
        return action;
    }

    @Override
    public BlockDirection getBlockDirection() {
        if(this.blockDirection == null) {
            this.blockDirection = mapBlockDirection(original.getBlockFace());
        }
        return blockDirection;
    }

    @Override
    public Block getClickedBlock() {
        if(this.block == null && this.original.getClickedBlock() != null) {
            this.block = new BukkitBlock(original.getClickedBlock(), new BukkitWorld(original.getClickedBlock().getWorld()));//@Todo optimize caching
        }
        return block;
    }

    @Override
    public EquipmentSlot getHand() {
        if(this.equipmentSlot == null && this.original.getHand() != null) {
            this.equipmentSlot = mapEquipmentSlot(this.original.getHand());
        }
        return equipmentSlot;
    }

    @Override
    public ItemStack getItem() {
        if(this.original.getItem() == null) {
            return null;
        }
        if(this.item == null) {
            this.item = new BukkitItemStack(original.getItem());
        }
        return item;
    }

    @Override
    public boolean hasBlock() {
        return this.original.hasBlock();
    }

    @Override
    public boolean hasItem() {
        return this.original.hasItem();
    }

    @Override
    public boolean isCancelled() {
        return this.original.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.original.setCancelled(cancelled);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Player getOnlinePlayer() {
        return this.player;
    }

    private BlockAction mapAction(Action action) {
        switch (action) {
            case PHYSICAL: return BlockAction.PHYSICAL;
            case LEFT_CLICK_AIR: return BlockAction.LEFT_CLICK_AIR;
            case RIGHT_CLICK_AIR: return BlockAction.RIGHT_CLICK_AIR;
            case LEFT_CLICK_BLOCK: return BlockAction.LEFT_CLICK_BLOCK;
            case RIGHT_CLICK_BLOCK: return BlockAction.RIGHT_CLICK_BLOCK;
        }
        throw new IllegalArgumentException("Can't map BlockAction " + action);
    }

    private BlockDirection mapBlockDirection(BlockFace blockFace) {
        switch (blockFace) {
            case UP: return BlockDirection.UP;
            case DOWN: return BlockDirection.DOWN;
            case EAST: return BlockDirection.EAST;
            case SELF: return BlockDirection.SELF;
            case WEST: return BlockDirection.WEST;
            case NORTH: return BlockDirection.NORTH;
            case SOUTH: return BlockDirection.SOUTH;
            case NORTH_EAST: return BlockDirection.NORTH_EAST;
            case NORTH_WEST: return BlockDirection.NORTH_WEST;
            case SOUTH_EAST: return BlockDirection.SOUTH_EAST;
            case SOUTH_WEST: return BlockDirection.SOUTH_WEST;
            case EAST_NORTH_EAST: return BlockDirection.EAST_NORTH_EAST;
            case EAST_SOUTH_EAST: return BlockDirection.EAST_SOUTH_EAST;
            case WEST_NORTH_WEST: return BlockDirection.WEST_NORTH_WEST;
            case WEST_SOUTH_WEST: return BlockDirection.WEST_SOUTH_WEST;
            case NORTH_NORTH_EAST: return BlockDirection.NORTH_NORTH_EAST;
            case NORTH_NORTH_WEST: return BlockDirection.NORTH_NORTH_WEST;
            case SOUTH_SOUTH_EAST: return BlockDirection.SOUTH_SOUTH_EAST;
            case SOUTH_SOUTH_WEST: return BlockDirection.SOUTH_SOUTH_WEST;
        }
        throw new IllegalArgumentException("Can't map BlockFace " + blockFace);
    }

    private EquipmentSlot mapEquipmentSlot(org.bukkit.inventory.EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
            case FEET: return EquipmentSlot.FEET;
            case LEGS: return EquipmentSlot.LEGS;
            case CHEST: return EquipmentSlot.CHEST;
            case HEAD: return EquipmentSlot.HEAD;
            case HAND: return EquipmentSlot.HAND;
            case OFF_HAND: return EquipmentSlot.OFF_HAND;
        }
        throw new IllegalArgumentException("Can't map EquipmentSlot " + equipmentSlot);
    }
}
