package org.mcnative.runtime.bukkit.inventory.item.material.versioned;

import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;

public interface MaterialProtocolRegistrar {

    void register(MinecraftProtocolVersion version);

    static void init(MinecraftProtocolVersion version) {
        McNative.getInstance().getLogger().info("Start registering protocolIds for materials");
        MaterialProtocolRegistrar registrar = null;
        switch (version) {
            case JE_1_8:
            case JE_1_9:
            case JE_1_9_1:
            case JE_1_9_2:
            case JE_1_9_4:
            case JE_1_10:
            case JE_1_11:
            case JE_1_11_2:
            case JE_1_12:
            case JE_1_12_1:
            case JE_1_12_2: {
                registrar = new LegacyMaterialProtocolRegistrar();
                break;
            }
            case JE_1_13: {
                registrar = new MaterialProtocolRegistrar1_13();
                break;
            }
            case JE_1_13_1: {
                registrar = new MaterialProtocolRegistrar1_13_1();
                break;
            }
            case JE_1_13_2: {
                registrar = new MaterialProtocolRegistrar1_13_2();
                break;
            }
            case JE_1_14: {
                registrar = new MaterialProtocolRegistrar1_14();
                break;
            }
            case JE_1_14_1: {
                registrar = new MaterialProtocolRegistrar1_14_1();
                break;
            }
            case JE_1_14_2: {
                registrar = new MaterialProtocolRegistrar1_14_2();
                break;
            }
            case JE_1_14_3: {
                registrar = new MaterialProtocolRegistrar1_14_3();
                break;
            }
            case JE_1_14_4: {
                registrar = new MaterialProtocolRegistrar1_14_4();
                break;
            }
            case JE_1_15: {
                registrar = new MaterialProtocolRegistrar1_15();
                break;
            }
            case JE_1_15_1: {
                registrar = new MaterialProtocolRegistrar1_15_1();
                break;
            }
            case JE_1_15_2: {
                registrar = new MaterialProtocolRegistrar1_15_2();
                break;
            }
            case JE_1_16: {
                registrar = new MaterialProtocolRegistrar1_16();
                break;
            }
            case JE_1_16_1: {
                registrar = new MaterialProtocolRegistrar1_16_1();
                break;
            }
            case JE_1_16_2: {
                registrar = new MaterialProtocolRegistrar1_16_2();
                break;
            }
            case JE_1_16_3: {
                registrar = new MaterialProtocolRegistrar1_16_3();
                break;
            }
            case JE_1_16_4: {
                registrar = new MaterialProtocolRegistrar1_16_4();
                break;
            }
        }
        if(registrar == null) {
            throw new IllegalArgumentException("Unknown minecraft version " + version + ". Can't register MaterialProtocolIds");
        }
        registrar.register(version);
        McNative.getInstance().getLogger().info("Registered protocolIds for materials");
    }
}
