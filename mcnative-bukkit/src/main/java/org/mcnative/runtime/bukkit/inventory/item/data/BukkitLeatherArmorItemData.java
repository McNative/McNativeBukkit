package org.mcnative.runtime.bukkit.inventory.item.data;

import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.mcnative.runtime.api.service.inventory.item.data.LeatherArmorItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.text.format.TextColor;

public class BukkitLeatherArmorItemData extends BukkitItemData<LeatherArmorMeta> implements LeatherArmorItemData {

    public BukkitLeatherArmorItemData(Material material, LeatherArmorMeta original) {
        super(material, original);
    }

    @Override
    public TextColor getColor() {
        return TextColor.make(new java.awt.Color(getOriginal().getColor().asRGB()));
    }

    @Override
    public LeatherArmorItemData setColor(TextColor textColor) {
        getOriginal().setColor(Color.fromRGB(textColor.getColor().getRGB()));
        return this;
    }
}
