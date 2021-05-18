package org.mcnative.runtime.bukkit.inventory.item.data;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.service.inventory.item.data.SkullItemData;
import org.mcnative.runtime.api.service.inventory.item.material.Material;

public class BukkitSkullItemData extends BukkitItemData<SkullMeta> implements SkullItemData {

    //private static final Class<?> SKULL_META;
    //private static final Field SKULL_META_PROFILE;

    public BukkitSkullItemData(Material material, SkullMeta original) {
        super(material, original);
    }

    @Override
    public boolean hasOwner() {
        return getOriginal().hasOwner();
    }

    @Override
    public MinecraftPlayer getOwningPlayer() {
        OfflinePlayer player = getOriginal().getOwningPlayer();
        if(player == null) return null;
        return McNative.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }

    @Override
    public GameProfile getGameProfile() {
        return null;
    }

    @Override
    public SkullItemData setOwningPlayer(MinecraftPlayer minecraftPlayer) {

        return null;
    }

    @Override
    public SkullItemData setGameProfile(GameProfile gameProfile) {
        return null;
    }
}
