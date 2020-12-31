/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 13.04.20, 20:37
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

package org.mcnative.runtime.bukkit.plugin.mapped;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.EmptyDocument;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.plugin.description.dependency.Dependency;
import net.pretronic.libraries.plugin.description.mainclass.MainClass;
import net.pretronic.libraries.plugin.description.mainclass.SingleMainClass;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class BukkitPluginDescription implements PluginDescription {

    private final PluginDescriptionFile original;

    public BukkitPluginDescription(PluginDescriptionFile original) {
        this.original = original;
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getDescription() {
        return original.getDescription();
    }

    @Override
    public String getAuthor() {//Can be null, invalid warning
        if(original == null || original.getAuthors() == null) return "Unknown";
        StringBuilder output = new StringBuilder();
        for (String author : original.getAuthors()) {
            output.append(author).append(", ");
        }
        if(output.length() >= 2) {
            output.setLength(output.length()-2);
        } else if(output.length() == 0) {
            return "Unknown";
        }
        return output.toString();
    }

    @Override
    public String getWebsite() {
        return original.getWebsite();
    }

    @Override
    public UUID getId() {
        return null;
    }

    @Override
    public PluginVersion getVersion() {
        return PluginVersion.parse(original.getVersion());
    }

    @Override
    public PluginVersion getLatestVersion() {
        return null;//Unused
    }

    @Override
    public void setLatestVersion(PluginVersion pluginVersion) {
        //Unused
    }

    @Override
    public String getMessageModule() {
        if(original.getName().equalsIgnoreCase("McNative")) return "McNative";
        return null;//Unused
    }

    @Override
    public MainClass getMain() {
        return new SingleMainClass(original.getMain());
    }

    @Override
    public Collection<Dependency> getDependencies() {
        return Collections.emptyList();//@Todo implement and map
    }

    @Override
    public Collection<String> getProviders() {
        return Collections.emptyList();
    }

    @Override
    public Document getProperties() {
        return EmptyDocument.newDocument();
    }
}
