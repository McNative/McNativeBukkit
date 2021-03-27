package org.mcnative.bukkit.nms.shared;

import net.pretronic.libraries.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.service.entity.living.HumanEntity;
import org.mcnative.runtime.api.service.inventory.InventoryOwner;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.inventory.type.AnvilInventory;
import org.mcnative.runtime.common.utils.PlayerRegisterAble;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class BukkitAnvilInventory implements AnvilInventory, PlayerRegisterAble {

    protected final Plugin plugin;
    private final InventoryOwner owner;
    private final Map<Player, AnvilInventoryHolder> playerInventories;
    private org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[3];

    private int repairCost;
    private int maximumRepairCost;

    public BukkitAnvilInventory(Plugin plugin, InventoryOwner owner) {
        this.plugin = plugin;
        this.owner = owner;
        this.playerInventories = new ConcurrentHashMap<>();

        this.repairCost = -1;
        this.maximumRepairCost = -1;
    }

    @Override
    public ItemStack getInputLeft() {
        return null;
    }

    @Override
    public ItemStack getInputRight() {
        return null;
    }

    @Override
    public ItemStack getOutput() {
        return getItem(AnvilInventory.SLOT_OUTPUT);
    }

    @Override
    public String getRenameText() {
        return null;
    }

    @Override
    public int getRepairCost() {
        return this.repairCost;
    }

    @Override
    public int getMaximumRepairCost() {
        return this.maximumRepairCost;
    }

    @Override
    public void setInputLeft(ItemStack itemStack) {
        setItem(AnvilInventory.SLOT_INPUT_LEFT, itemStack);
    }

    @Override
    public void setInputRight(ItemStack itemStack) {
        setItem(AnvilInventory.SLOT_INPUT_LEFT, itemStack);
    }

    @Override
    public void setOutput(ItemStack itemStack) {
        setItem(AnvilInventory.SLOT_OUTPUT, itemStack);
    }

    @Override
    public void setRenameText(String s) {

    }

    @Override
    public void setRepairCost(int repairCost) {
        this.repairCost = repairCost;
    }

    @Override
    public void setMaximumRepairCost(int maximumRepairCost) {
        this.maximumRepairCost = maximumRepairCost;
    }

    @Override
    public void clearItemsOnClose(boolean b) {

    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public InventoryOwner getOwner() {
        return null;
    }

    @Override
    public Collection<HumanEntity> getViewers() {
        return null;
    }

    @Override
    public ItemStack getItem(int i) {
        return McNative.getInstance().getObjectFactory().createObject(ItemStack.class, this.items[i]);
    }

    @Override
    public ItemStack getItem(int i, int i1) {
        return null;
    }

    @Override
    public Collection<ItemStack> getItems() {
        return null;
    }

    @Override
    public Stream<ItemStack> stream() {
        return null;
    }

    @Override
    public int countItemStacks() {
        return 0;
    }

    @Override
    public int countItemStacks(Material material) {
        return 0;
    }

    @Override
    public int countItemStacks(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean contains(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean contains(int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean contains(int i, int i1, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean contains(Material material) {
        return false;
    }

    @Override
    public boolean contains(int i, Material material) {
        return false;
    }

    @Override
    public boolean contains(int i, int i1, Material material) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isEmpty(int i) {
        return false;
    }

    @Override
    public boolean isEmpty(int i, int i1) {
        return false;
    }

    @Override
    public boolean hasPlace() {
        return false;
    }

    @Override
    public boolean hasPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean hasPlace(Material material) {
        return false;
    }

    @Override
    public void setOwner(InventoryOwner inventoryOwner) {

    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        System.out.println("Set item");
        updateItem(slot, McNative.getInstance().getObjectFactory().createObject(org.bukkit.inventory.ItemStack.class, itemStack));
    }

    @Override
    public void setItem(int i, int i1, ItemStack itemStack) {

    }

    @Override
    public void addItems(ItemStack... itemStacks) {

    }

    @Override
    public void addItem(int i, int i1, ItemStack itemStack) {

    }

    @Override
    public void remove(ItemStack itemStack) {

    }

    @Override
    public void remove(Material material) {

    }

    @Override
    public void remove(int i) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void clear(int i) {

    }

    @Override
    public void clear(int i, int i1) {

    }

    @Override
    public void replace(ItemStack itemStack, ItemStack itemStack1) {

    }

    @Override
    public void replace(Material material, ItemStack itemStack) {

    }

    @Override
    public void replace(Material material, Material material1) {

    }

    @Override
    public void fill(ItemStack itemStack) {

    }

    @Override
    public void fill(int i, ItemStack itemStack) {

    }

    @Override
    public void fill(int i, int i1, ItemStack itemStack) {

    }

    @Override
    public void fillSpaces(ItemStack itemStack) {

    }

    @Override
    public void fillSpaces(int i, ItemStack itemStack) {

    }

    @Override
    public void fillSpaces(int i, int i1, ItemStack itemStack) {

    }

    @Override
    public void show(HumanEntity humanEntity) {

    }

    @Override
    public void showAllPlayers() {

    }

    @Override
    public void close(HumanEntity humanEntity) {

    }

    @Override
    public void close() {

    }

    @Override
    public Iterator<ItemStack> iterator() {
        return null;
    }

    @Override
    public void registerPlayer(ConnectedMinecraftPlayer connectedMinecraftPlayer) {
        Player player = connectedMinecraftPlayer.getAs(Player.class);
        Validate.notNull(player, "Error: Player can't be null");
        if(this.playerInventories.containsKey(player)) return;

        System.out.println("register player");
        AnvilInventoryHolder holder = createAnvilInventory(player, "Test");
        this.playerInventories.put(player, holder);
        System.out.println("registered "+playerInventories.size());
        //setBukkitInventoryItems(holder.getInventory());
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), this.plugin);

        openAnvilInventory(holder);
    }

    private void updateItems(org.bukkit.inventory.ItemStack[] items) {
        this.items = items;

        for (AnvilInventoryHolder holder : this.playerInventories.values()) {
            setBukkitInventoryItems(holder.getInventory());
        }
    }

    private void updateItem(int slot, org.bukkit.inventory.ItemStack itemStack) {
        this.items[slot] = itemStack;

        for (AnvilInventoryHolder holder : this.playerInventories.values()) {
            holder.getInventory().setItem(slot, itemStack);
        }
    }

    private void setBukkitInventoryItems(Inventory inventory) {
        for (int i = 0; i < this.items.length; i++) {
            org.bukkit.inventory.ItemStack itemStack = this.items[i];
            inventory.setItem(i, itemStack);
        }
    }

    private class AnvilListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            System.out.println("onClick " + event.getRawSlot() + ":" + playerInventories.size());
            System.out.println(event.getInventory());
            for (Map.Entry<Player, AnvilInventoryHolder> entry : playerInventories.entrySet()) {
                Inventory inventory = entry.getValue().getInventory();

                System.out.println(inventory + ":" + event.getInventory());

                if (event.getInventory().equals(inventory) && (event.getRawSlot() < 3 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))) { ;
                    /*if(event.getRawSlot() == AnvilInventory.SLOT_OUTPUT) {
                        System.out.println(event.getCurrentItem().getType());
                        System.out.println("Output" + (event.getCurrentItem() == null || event.getCurrentItem().getType() == org.bukkit.Material.AIR));
                        if(event.getCurrentItem() == null || event.getCurrentItem().getType() == org.bukkit.Material.AIR) {
                            return;
                        }
                    }*/
                    //One tick later, because otherwise inventory content are not updated by craftbukkit/nms
                    Bukkit.getScheduler().runTask(plugin, ()-> {
                        updateItems(inventory.getContents());
                    });
                    break;
                }
            }

        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            for (Map.Entry<Player, AnvilInventoryHolder> entry : playerInventories.entrySet()) {
                Inventory inventory = entry.getValue().getInventory();
                if (event.getInventory().equals(inventory)) {
                    for (Integer rawSlot : event.getRawSlots()) {
                        if(rawSlot < 3) {
                            //One tick later, because otherwise inventory content are not updated by craftbukkit/nms
                            Bukkit.getScheduler().runTask(plugin, ()-> {
                                updateItems(inventory.getContents());
                            });
                            break;
                        }
                    }
                    break;
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            unregisterInventory((Player) event.getPlayer());
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent event) {
            unregisterInventory(event.getPlayer());
        }

        private void unregisterInventory(Player player) {
            System.out.println("unregister " + player.getName());
            for (Map.Entry<Player, AnvilInventoryHolder> entry : playerInventories.entrySet()) {
                if (entry.getKey().equals(player)) {
                    //playerInventories.remove(entry.getKey());
                }
            }
        }
    }
    protected abstract AnvilInventoryHolder createAnvilInventory(Player player, String title);

    protected abstract void openAnvilInventory(AnvilInventoryHolder holder);

}
