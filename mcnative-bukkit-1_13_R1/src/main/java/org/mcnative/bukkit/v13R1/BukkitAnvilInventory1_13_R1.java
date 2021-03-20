package org.mcnative.bukkit.v13R1;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.mcnative.bukkit.nms.shared.AnvilInventoryHolder;
import org.mcnative.bukkit.nms.shared.BukkitAnvilInventory;
import org.mcnative.runtime.api.McNative;
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

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        int slot = 0;
        for (org.mcnative.runtime.api.service.inventory.item.ItemStack item : getItems()) {
            event.getInventory().setItem(slot, McNative.getInstance().getObjectFactory().createObject(ItemStack.class, item));
        }
        if(getOutput() != null) {
            event.setResult(McNative.getInstance().getObjectFactory().createObject(ItemStack.class, getOutput()));
        }
        if(getRepairCost() != -1) {
            event.getInventory().setRepairCost(getRepairCost());
        }
        if(getMaximumRepairCost() != -1) {
            event.getInventory().setMaximumRepairCost(getMaximumRepairCost());
        }
    }
}
