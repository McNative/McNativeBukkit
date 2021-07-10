package org.mcnative.runtime.bukkit.event.player;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.mcnative.runtime.api.service.entity.Item;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.event.player.MinecraftPlayerDropItemEvent;
import org.mcnative.runtime.bukkit.entity.BukkitItem;

public class BukkitMinecraftPlayerDropItemEvent implements MinecraftPlayerDropItemEvent {

    private final PlayerDropItemEvent original;
    private final Player player;

    private Item itemDrop;

    public BukkitMinecraftPlayerDropItemEvent(PlayerDropItemEvent original, Player player) {
        this.original = original;
        this.player = player;
    }

    @Override
    public Item getItemDrop() {
        if(this.itemDrop == null) {
            this.itemDrop = new BukkitItem(original.getItemDrop());//@Todo maybe from world entity pool
        }
        return this.itemDrop;
    }

    @Override
    public boolean isCancelled() {
        return original.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        original.setCancelled(b);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Player getOnlinePlayer() {
        return this.player;
    }
}
