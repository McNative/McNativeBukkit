/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 08.04.20, 22:24
 * @web %web%
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

import org.mcnative.runtime.api.protocol.MinecraftEdition;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

public class ViaVersionExtensionUtil {

    public static Collection<MinecraftProtocolVersion> getVersions(){
        Collection<MinecraftProtocolVersion> versions = new ArrayList<>();
        SortedSet<Integer> supported = ProtocolRegistry.getSupportedVersions();
        for (Integer supportedVersion : supported) {
            try{
                versions.add(MinecraftProtocolVersion.of(MinecraftEdition.JAVA,supportedVersion));
            }catch (Exception ignored){}
        }
        return versions;
    }
}
