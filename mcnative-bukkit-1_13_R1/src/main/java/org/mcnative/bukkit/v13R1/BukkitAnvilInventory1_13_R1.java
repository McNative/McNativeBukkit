package org.mcnative.bukkit.v13R1;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.mcnative.bukkit.nms.shared.AnvilInventoryHolder;
import org.mcnative.bukkit.nms.shared.BukkitAnvilInventory;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;

public class BukkitAnvilInventory1_13_R1 extends BukkitAnvilInventory implements Listener {

    public BukkitAnvilInventory1_13_R1(Plugin plugin, InventoryOwner owner) {
        super(plugin, owner);
    }

    @Override
    protected AnvilInventoryHolder createAnvilInventory(Player player, String title) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.ANVIL, title);
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        return new AnvilInventoryHolder(player, inventory, null);
    }

    @Override
    protected void openAnvilInventory(AnvilInventoryHolder holder) {
        holder.getPlayer().openInventory(holder.getInventory());
    }

    @Override
    protected void setRepairCost(AnvilInventoryHolder holder, int cost) {
        Inventory inventory = holder.getInventory();
        if(inventory instanceof AnvilInventory) {
            ((AnvilInventory)inventory).setRepairCost(cost);
        } else {
            throw new IllegalArgumentException("Inventory must be a AnvilInventory");
        }
    }

    @Override
    protected void setMaximumRepairCost(AnvilInventoryHolder holder, int cost) {
        Inventory inventory = holder.getInventory();
        if(inventory instanceof AnvilInventory) {
            ((AnvilInventory)inventory).setMaximumRepairCost(cost);
        } else {
            throw new IllegalArgumentException("Inventory must be a AnvilInventory");
        }
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {

    }
}
