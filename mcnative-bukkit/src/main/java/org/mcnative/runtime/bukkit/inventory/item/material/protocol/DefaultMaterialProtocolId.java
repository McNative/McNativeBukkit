package org.mcnative.runtime.bukkit.inventory.item.material.protocol;

import org.mcnative.runtime.api.service.inventory.item.material.MaterialProtocolId;

public class DefaultMaterialProtocolId implements MaterialProtocolId {

    private final int id;

    public DefaultMaterialProtocolId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
