package org.mcnative.bukkit.nms.shared;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AnvilInventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final Object container;
    private int containerId;

    public AnvilInventoryHolder(Player player, Inventory inventory, Object container) {
        this.player = player;
        this.inventory = inventory;
        this.container = container;
        this.containerId = -1;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Object getContainer() {
        return container;
    }

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }
}
