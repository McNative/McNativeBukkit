package org.mcnative.bukkit.v18R3;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.mcnative.bukkit.nms.shared.AnvilInventoryHolder;
import org.mcnative.bukkit.nms.shared.BukkitAnvilInventory;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;

public class BukkitAnvilInventory1_8_R3 extends BukkitAnvilInventory {

    public BukkitAnvilInventory1_8_R3(Plugin plugin, InventoryOwner owner) {
        super(plugin, owner);
    }

    @Override
    protected AnvilInventoryHolder createAnvilInventory(Player player, String title) {
        EntityPlayer entityPlayer = toNMS(player);
        final Container container = new CustomContainerAnvil2(this, entityPlayer);

        Inventory inventory = container.getBukkitView().getTopInventory();

        return new AnvilInventoryHolder(player, inventory, container);
    }

    @Override
    protected void openAnvilInventory(AnvilInventoryHolder holder) {
        EntityPlayer entityPlayer = toNMS(holder.getPlayer());

        CraftEventFactory.handleInventoryCloseEvent(entityPlayer);
        entityPlayer.activeContainer = entityPlayer.defaultContainer;

        int containerId = entityPlayer.nextContainerCounter();
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
        entityPlayer.activeContainer = (Container) holder.getContainer();
        ((Container) holder.getContainer()).windowId = containerId;
        ((Container) holder.getContainer()).addSlotListener(entityPlayer);
        holder.setContainerId(containerId);
    }

    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}
