/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 14.08.19, 19:45
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

package org.mcnative.common;

import net.prematic.libraries.document.Document;
import net.prematic.libraries.document.DocumentRegistry;
import org.mcnative.common.text.outdated.Text;

import java.io.File;

public interface Configuration extends Document {

    File getFile();

    Text getText(String key);

    boolean save();



    static void load(Class<?> configurationClass){
        DocumentRegistry.loadClass(configurationClass);
    }

    static Configuration newConfiguration(File location){
        return null;
    }
}