package org.mcnative.runtime.bukkit.plugin.dependency;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import net.pretronic.libraries.utility.SystemUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.bukkit.plugin.dependency.legacy.BukkitLegacyDependencyLoader;
import org.mcnative.runtime.bukkit.plugin.dependency.legacy.LegacyReflectedDependencyClassLoader;

public class BukkitDependencyLoader {

    public static DependencyClassLoader get(McNative instance){
        if(SystemUtil.getJavaBaseVersion() < 16){
            return new LegacyReflectedDependencyClassLoader();
        }else{
            if(instance.getPlatform().getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_17)){
                instance.getLogger().warn("------------------------");
                instance.getLogger().warn("Versions above Java 15 are not officially supported for "+instance.getPlatform().getProtocolVersion()+", it might cause issues on your server");
                instance.getLogger().warn("------------------------");
            }
            return new BukkitLegacyDependencyLoader();
        }
    }
}
