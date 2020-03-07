/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 31.08.19, 16:04
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

package org.mcnative.buildtool.maven;

import net.prematic.libraries.document.Document;
import net.prematic.libraries.document.type.DocumentFileType;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class McNativePluginManifest {

    @Parameter(required =true)
    private String name;
    private String category;

    private String id;

    @Parameter(required = true)
    private PluginVersion version;

    @Parameter(required = true)
    private String main;

    private String messageModule;
    private String description;
    private String website;
    private String author;
    private Set<String> depends;
    private Set<String> softDepends;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public PluginVersion getVersion() {
        return version;
    }

    public String getMain() {
        return main;
    }

    public String getMessageModule() {
        return messageModule;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public String getAuthor() {
        return author;
    }

    public Set<String> getDepends() {
        if(depends == null) depends = new HashSet<>();
        return depends;
    }

    public Set<String> getSoftdepends() {
        if(softDepends == null) softDepends = new HashSet<>();
        return softDepends;
    }

    public void createManifestFile(File location){
        if(depends == null) this.depends = new HashSet<>();
        getSoftdepends().add("McNative");
        if(messageModule == null) messageModule = getName().replace(" ","_").toLowerCase();
        location.getParentFile().mkdirs();
        DocumentFileType.JSON.getWriter().write(location, Document.newDocument(this));
    }
}
