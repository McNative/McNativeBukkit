package org.mcnative.runtime.bukkit.inventory.item.material.protocol;

public class LegacyMaterialProtocolId extends DefaultMaterialProtocolId {

    private final int subId;

    public LegacyMaterialProtocolId(int id, int subId) {
        super(id);
        this.subId = subId;
    }

    public int getSubId() {
        return subId;
    }
}
