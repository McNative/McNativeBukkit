/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 14.02.20, 22:50
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.runtime.bukkit.utils;

import io.netty.channel.Channel;
import net.pretronic.libraries.utility.reflect.ReflectException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BukkitReflectionUtil {

    private static final String BUKKIT_BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_BASE = BUKKIT_BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");
    private static final Map<String,MinecraftProtocolVersion> protocolVersionsByServerVersion = new HashMap<>();

    public static final String SERVER_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
    private static boolean NMS_OBFUSCATED = true;

    static {
        protocolVersionsByServerVersion.put("1_7_R4",MinecraftProtocolVersion.JE_1_7_10);
        protocolVersionsByServerVersion.put("1_8_R1",MinecraftProtocolVersion.JE_1_8);
        protocolVersionsByServerVersion.put("1_8_R2",MinecraftProtocolVersion.JE_1_8);
        protocolVersionsByServerVersion.put("1_8_R3",MinecraftProtocolVersion.JE_1_8);
        protocolVersionsByServerVersion.put("1_9_R1",MinecraftProtocolVersion.JE_1_9);
        protocolVersionsByServerVersion.put("1_9_R2",MinecraftProtocolVersion.JE_1_9_4);
        protocolVersionsByServerVersion.put("1_10_R1",MinecraftProtocolVersion.JE_1_10);
        protocolVersionsByServerVersion.put("1_11_R1",MinecraftProtocolVersion.JE_1_11);
        protocolVersionsByServerVersion.put("1_12_R1",MinecraftProtocolVersion.JE_1_12);
        protocolVersionsByServerVersion.put("1_13_R1",MinecraftProtocolVersion.JE_1_13);
        protocolVersionsByServerVersion.put("1_13_R2",MinecraftProtocolVersion.JE_1_13_2);
        protocolVersionsByServerVersion.put("1_14_R1",MinecraftProtocolVersion.JE_1_14);
        protocolVersionsByServerVersion.put("1_15_R1",MinecraftProtocolVersion.JE_1_15);
        protocolVersionsByServerVersion.put("1_16_R1",MinecraftProtocolVersion.JE_1_16_1);
        protocolVersionsByServerVersion.put("1_16_R2",MinecraftProtocolVersion.JE_1_16_3);
        protocolVersionsByServerVersion.put("1_16_R3",MinecraftProtocolVersion.JE_1_16_4);
        protocolVersionsByServerVersion.put("1_17_R1",MinecraftProtocolVersion.JE_1_17_1);
        protocolVersionsByServerVersion.put("1_18_R1",MinecraftProtocolVersion.JE_1_18_1);
        protocolVersionsByServerVersion.put("1_18_R2",MinecraftProtocolVersion.JE_1_18_2);
        try {
            Class.forName("net.minecraft.network.NetworkManager");
            NMS_OBFUSCATED = false;
        }catch (Throwable ignored){}
    }

    public static Class<?> getClass(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }

    public static Class<?> getCraftClass(String className){
        try {
            return Class.forName(BUKKIT_BASE + "." + className);
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }

    public static Class<?> getMNSClass(String className){
        try {
            if(NMS_OBFUSCATED){
                return Class.forName(NMS_BASE + "." + className.substring(className.lastIndexOf(".")+1));
            }else{
                return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }

    public static Object getServerConnection()  {
        try{
            Class<?> serverClazz = BukkitReflectionUtil.getMNSClass("net.minecraft.server.MinecraftServer");
            Object server = serverClazz.getDeclaredMethod("getServer").invoke(null);
            for (Method method : serverClazz.getDeclaredMethods() ) {
                if (method.getReturnType() != null
                        && method.getReturnType().getSimpleName().equals("ServerConnection")
                        && method.getParameterTypes().length == 0) {
                    return method.invoke(server);
                }
            }
            throw new IllegalArgumentException("No ServerConnection found.");
        }catch (Exception exception){
            throw new ReflectException(exception);
        }
    }

    public static Channel getPlayerChannel(Player player){
        Object entityPlayer = ReflectionUtil.invokeMethod(player,"getHandle");
        Object connection = ReflectionUtil.getFieldValue(entityPlayer,"playerConnection");
        Object networkManager = ReflectionUtil.getFieldValue(connection,"networkManager");
        return ReflectionUtil.getFieldValue(networkManager,"channel", Channel.class);
    }

    public static Object getGameProfile(Player player){
        Object entityPlayer = ReflectionUtil.invokeMethod(player,"getHandle");
        return ReflectionUtil.invokeMethod(getMNSClass("net.minecraft.world.entity.player.EntityHuman") ,entityPlayer,"getProfile",new Object[]{});
    }

    public static int getPing(Player player){
        Object entityPlayer = ReflectionUtil.invokeMethod(player,"getHandle");
        return ReflectionUtil.getFieldValue(entityPlayer,"ping",Integer.class);
    }

    public static String getServerVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
    }

    public static MinecraftProtocolVersion getProtocolVersionByServerVersion(){
        MinecraftProtocolVersion result = protocolVersionsByServerVersion.get(getServerVersion());
        return result != null ? result : MinecraftProtocolVersion.UNKNOWN;
    }

    public static int getNextPlayerContainerId(Player player) {
        try {
            Object handle = handleCraftPlayer(player);
            Class<?> handleClass = handle.getClass();
            Method nextContainerCounter = handleClass.getDeclaredMethod("nextContainerCounter");
            Object count = nextContainerCounter.invoke(handle);
            return (Integer) count;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Object handleCraftPlayer(Player player) {
        try {
            Class<?> craftPlayerClass = getCraftClass("entity.CraftPlayer");
            Method getHandle = craftPlayerClass.getDeclaredMethod("getHandle");
            return getHandle.invoke(player);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static double[] getRecentTps() {
        try {
            Class<?> serverClass = getMNSClass("net.minecraft.server.MinecraftServer");
            Object server = serverClass.getDeclaredMethod("getServer").invoke(null);
            return ReflectionUtil.getFieldValue(serverClass, server, "recentTps", double[].class);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
