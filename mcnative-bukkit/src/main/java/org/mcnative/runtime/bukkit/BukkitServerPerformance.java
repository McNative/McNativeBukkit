package org.mcnative.runtime.bukkit;

import net.pretronic.libraries.utility.SystemInfo;
import org.mcnative.runtime.api.ServerPerformance;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;

public class BukkitServerPerformance implements ServerPerformance {

    @Override
    public float[] getRecentTps() {
        double[] rawRecentTps = BukkitReflectionUtil.getRecentTps();
        float[] recentTps = new float[rawRecentTps.length];
        for (int i = 0 ; i < rawRecentTps.length; i++) {
            recentTps[i] = (float) rawRecentTps[i];
        }
        return recentTps;
    }

    @Override
    public int getUsedMemory() {
        return (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L);
    }

    @Override
    public float getCpuUsage() {
        return (float) SystemInfo.getPercentProcessCpuLoad(2);
    }
}
