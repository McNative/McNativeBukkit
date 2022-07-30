package org.mcnative.bukkit.v18R3;

import net.minecraft.server.v1_8_R3.*;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.mcnative.bukkit.nms.shared.BukkitAnvilInventory;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.service.inventory.type.AnvilInventory;

import java.util.Map;

public class CustomContainerAnvil2 extends ContainerAnvil {

    private final static BlockPosition BLOCK_POSITION_ZERO = new BlockPosition(0, 0, 0);

    private final AnvilInventory anvilInventory;

    public CustomContainerAnvil2(BukkitAnvilInventory anvilInventory, EntityHuman entityhuman) {
        super(entityhuman.inventory, entityhuman.world, BLOCK_POSITION_ZERO, entityhuman);
        this.anvilInventory = anvilInventory;
    }

    @Override
    public boolean a(EntityHuman human) {
        return true;
    }

    @Override
    public void b(EntityHuman entityhuman) {

    }

    @Override
    public void a(String s) {
        super.a(s);
    }

    private IInventory getProcessSlots() {
        return ReflectionUtil.getFieldValue(ContainerAnvil.class, this, "h", IInventory.class);
    }

    private IInventory getResultSlot() {
        return ReflectionUtil.getFieldValue(ContainerAnvil.class, this, "g", IInventory.class);
    }

    private EntityHuman getEntityHuman() {
        return ReflectionUtil.getFieldValue(ContainerAnvil.class, this, "m", EntityHuman.class);
    }

    private String getTextInput() {
        return ReflectionUtil.getFieldValue(ContainerAnvil.class, this, "l", String.class);
    }

    private int getK() {
        return ReflectionUtil.getFieldValue(ContainerAnvil.class, this, "k", Integer.class);
    }

    private void setK(int k) {
        ReflectionUtil.changeFieldValue(ContainerAnvil.class, this, "k", k);
    }

    private int getMaximumRepairCost() {
        return this.anvilInventory.getMaximumRepairCost() <= 0 ? 39 : this.anvilInventory.getMaximumRepairCost();
    }

    private int getRepairCost() {
        return this.a;
    }

    private void setRepairCost(int repairCost) {
        if(repairCost > 0 && anvilInventory.getRepairCost() != -1) {
            this.a = anvilInventory.getRepairCost();
            return;
        }
        this.a = repairCost;
    }

    private void handleCustomItems() {
        ItemStack leftSlot = getProcessSlots().getItem(0);
        if(leftSlot == null) return;
        System.out.println("Output");
        System.out.println(anvilInventory.getOutput());
        ItemStack resultItem = CraftItemStack.asNMSCopy(McNative.getInstance().getObjectFactory().createObject(org.bukkit.inventory.ItemStack.class, anvilInventory.getOutput()));
        resultItem.setRepairCost(this.anvilInventory.getRepairCost());

        getResultSlot().setItem(0, resultItem);
        b();
        for (ICrafting listener : this.listeners) {
            listener.setContainerData(this, 0, this.anvilInventory.getRepairCost());
        }
    }

    @Override
    public void e() {
        handleCustomItems();
        if(true) return;
        System.out.println("updateAnvilDisplay");
        ItemStack leftSlot = getProcessSlots().getItem(0);

        setRepairCost(1);
        int reRepairCostAddition = 0;
        byte costOffsetModifier = 0;

        // If the item in the left-most slot is null...
        if (leftSlot == null) {
            // Make sure we don't have a result item.
            getResultSlot().setItem(0, null);
            setRepairCost(0);
        } else {
            ItemStack resultItem = leftSlot.cloneItemStack();
            ItemStack rightSlot = getProcessSlots().getItem(1);
            Map<Integer, Integer> leftEnchantments = EnchantmentManager.a(resultItem);
            boolean usingEnchantedBook;
            int existingReRepairCost = leftSlot.getRepairCost() + (rightSlot == null ? 0 : rightSlot.getRepairCost());

            setK(0);
            // If we have an item in the right-most slot...
            if (rightSlot != null) {
                //rightSlot.setRepairCost(35);//
                usingEnchantedBook = rightSlot.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(rightSlot).size() > 0;
                if (resultItem.e() && resultItem.getItem().a(leftSlot, rightSlot)) {
                    int k = Math.min(resultItem.h(), resultItem.j() / 4);
                    if (k <= 0) {
                        getResultSlot().setItem(0, null);
                        setRepairCost(0);
                        return;
                    }
                    int someVariable;
                    for (someVariable = 0; k > 0 && someVariable < rightSlot.count; someVariable++) {
                        int new_durability = resultItem.h() - k;
                        resultItem.setData(new_durability);
                        reRepairCostAddition++;
                        k = Math.min(resultItem.h(), resultItem.j() / 4);
                    }
                    setK(someVariable);
                } else {
                    // If we're not apply an enchantment and we're not repairing
                    // an item...
                    if (!usingEnchantedBook && (resultItem.getItem() != rightSlot.getItem() || !resultItem.e())) {
                        // Make sure we don't have a result item.
                        getResultSlot().setItem(0, (ItemStack) null);
                        setRepairCost(0);
                        return;
                    }

                    // If we're not using an enchanted book (therefore, we must
                    // be repairing at this point)...
                    if (resultItem.e() && !usingEnchantedBook) {
                        // Compute the new durability. Max durability-damage
                        int leftDurability = leftSlot.j() - leftSlot.h();
                        int rightDurability = rightSlot.j() - rightSlot.h();
                        int i1 = rightDurability + resultItem.j() * 12 / 100;
                        int k1 = leftDurability + i1;

                        int j1 = resultItem.j() - k1;
                        if (j1 < 0) {
                            j1 = 0;
                        }
                        if (j1 < resultItem.getData()) {
                            resultItem.setData(j1);
                            reRepairCostAddition += 2;
                        }
                    }

                    Map<Integer, Integer> rightEnchantments = EnchantmentManager.a(rightSlot);
                    for (int enchantmentID : rightEnchantments.keySet()) {
                        Enchantment enchantment = Enchantment.getById(enchantmentID);
                        if(enchantment != null) {
                            // Compute a new enchantment level.
                            int leftLevel = leftEnchantments.getOrDefault(
                                    enchantmentID, 0);
                            int rightLevel = rightEnchantments.get(enchantmentID);
                            int newLevel;
                            if(leftLevel == rightLevel) {
                                rightLevel++;
                                newLevel = rightLevel;
                            } else {
                                newLevel = Math.max(rightLevel, leftLevel);
                            }
                            rightLevel = newLevel;
                            boolean enchantable = enchantment.canEnchant(leftSlot);
                            if(getEntityHuman().abilities.canInstantlyBuild || leftSlot.getItem() == Items.ENCHANTED_BOOK) {
                                enchantable = true;
                            }
                            for (Integer enchantID : leftEnchantments.keySet()) {
                                if(enchantID != enchantmentID && !enchantment.a(Enchantment.getById(enchantID))) {
                                    enchantable = false;
                                    reRepairCostAddition++;
                                }
                            }
                            if(enchantable) {
                                // Make sure we don't apply a level too high.
                                if(rightLevel > enchantment.getMaxLevel()) {
                                    rightLevel = enchantment.getMaxLevel();
                                }
                                leftEnchantments.put(enchantmentID, rightLevel);
                                int randomCostModifier = 0;
                                switch (enchantment.getRandomWeight()) {
                                    case 1:
                                        randomCostModifier = 8;
                                        break;
                                    case 2:
                                        randomCostModifier = 4;
                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;
                                    case 5:
                                        randomCostModifier = 2;
                                        break;
                                    case 10:
                                        randomCostModifier = 1;
                                }
                                if(usingEnchantedBook) {
                                    randomCostModifier = Math.max(1, randomCostModifier / 2);
                                }
                                reRepairCostAddition += randomCostModifier * rightLevel;
                            }
                        }
                    }
                }
            }

            // If the textbox is blank...
            if (StringUtils.isBlank(getTextInput())) {

                // Remove the name on our item.
                if (leftSlot.hasName()) {
                    costOffsetModifier = 1;
                    reRepairCostAddition += costOffsetModifier;
                    resultItem.r();//Remove the name of item
                }
            } else if (!getTextInput().equals(leftSlot.getName())) {
                // Name the item.
                costOffsetModifier = 1;
                reRepairCostAddition += costOffsetModifier;
                resultItem.c(getTextInput());//Set name to item
                //this.textbox = leftSlot.getName();
            }

            // Apply the costs for re-repairing the items.
            setRepairCost(existingReRepairCost + reRepairCostAddition);
            if (reRepairCostAddition <= 0) {
                resultItem = null;
            }
            //Max cost check
            if (costOffsetModifier == reRepairCostAddition && costOffsetModifier > 0 && getRepairCost() >= 40) {
                setRepairCost(getMaximumRepairCost());
            }

            // Max out at exp-cost 40 repairs.
            if (getRepairCost() >= 40 && !getEntityHuman().abilities.canInstantlyBuild) {
                resultItem = null;
            }

            // Apply everything to our result item.
            if (resultItem != null) {
                setRepairCost(resultItem.getRepairCost());
                if (rightSlot != null && getRepairCost() < rightSlot.getRepairCost()) {
                    setRepairCost(rightSlot.getRepairCost());
                }
                setRepairCost(getRepairCost() * 2 + 1);
                resultItem.setRepairCost(getRepairCost());//repairCost
                EnchantmentManager.a(leftEnchantments, resultItem);//Apply enchantments to resultItem
            }

            getResultSlot().setItem(0, resultItem);
            b();
            System.out.println("Update" + getRepairCost());
            for (ICrafting listener : this.listeners) {
                listener.setContainerData(this, 0, getRepairCost());
            }
        }
    }
}