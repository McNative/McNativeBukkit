package org.mcnative.runtime.bukkit.inventory.item.data;

import net.pretronic.libraries.utility.Iterators;
import org.bukkit.Color;
import org.bukkit.inventory.meta.PotionMeta;
import org.mcnative.runtime.api.service.inventory.item.data.PotionItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Material;
import org.mcnative.runtime.api.service.potion.PotionData;
import org.mcnative.runtime.api.service.potion.PotionEffect;
import org.mcnative.runtime.api.service.potion.PotionEffectType;
import org.mcnative.runtime.api.text.format.TextColor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BukkitPotionItemData extends BukkitItemData<PotionMeta> implements PotionItemData {

    public BukkitPotionItemData(Material material, PotionMeta original) {
        super(material, original);
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        return Collections.unmodifiableList(Iterators.map(getOriginal().getCustomEffects(), this::mapPotionEffect));
    }

    @Override
    public PotionItemData addCustomEffect(PotionEffect potionEffect, boolean overwrite) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PotionItemData removeCustomEffect(PotionEffectType potionEffectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PotionItemData clearCustomEffects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCustomEffects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCustomEffect(PotionEffectType potionEffectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasColor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TextColor getColor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PotionItemData setColor(TextColor textColor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PotionData getBasePotionData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PotionData getBasePotionData(Consumer<PotionData> consumer) {
        throw new UnsupportedOperationException();
    }

    private PotionEffect mapPotionEffect(org.bukkit.potion.PotionEffect effect) {
        if(effect == null) return null;
        return new PotionEffect(mapPotionEffectType(effect.getType()), effect.getAmplifier(), effect.getDuration(),
                effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
    }

    private PotionEffectType mapPotionEffectType(org.bukkit.potion.PotionEffectType effectType) {
        if(effectType == null) return null;
        return new PotionEffectType(effectType.getName(), effectType.isInstant(), mapColor(effectType.getColor()));
    }

    private TextColor mapColor(Color color) {
        return TextColor.make(new java.awt.Color(color.asRGB()));
    }
}
