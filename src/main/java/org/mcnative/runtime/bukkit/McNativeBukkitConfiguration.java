/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.03.20, 17:48
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

package org.mcnative.runtime.bukkit;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.annotations.DocumentKey;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.message.MessageProvider;
import net.pretronic.libraries.message.bml.Message;
import net.pretronic.libraries.message.bml.parser.MessageParser;
import net.pretronic.libraries.utility.map.Pair;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.MessageKeyComponent;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.common.plugin.configuration.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class McNativeBukkitConfiguration {

    @DocumentKey("debug")
    public static boolean DEBUG = false;

    @DocumentKey("console.networkId")
    public static String CONSOLE_NETWORK_ID = "00000-00000-00000";

    @DocumentKey("console.secret")
    public static String CONSOLE_SECRET = "00000-00000-00000";

    @DocumentKey("console.mafEnabled")
    public static boolean CONSOLE_MAF_ENABLED = true;

    @DocumentKey("player.displayName.format")
    public static String PLAYER_DISPLAY_NAME_FORMAT = "{design.color}{name}";

    @DocumentKey("player.displayName.applyOnBukkit")
    public static boolean PLAYER_DISPLAY_APPLY_ON_BUKKIT = false;

    public static Map<String,String> PLAYER_COLORS_COLORS = new LinkedHashMap<>();
    public static String PLAYER_COLORS_DEFAULT = "&7";


    public static boolean PLAYER_TABLIST_ENABLED = false;

    public static String PLAYER_TABLIST_PREFIX = "{design.prefix}";
    public static String PLAYER_TABLIST_SUFFIX = "{design.suffix}";

    public static boolean PLAYER_TABLIST_OVERVIEW_ENABLED = false;
    public static String PLAYER_TABLIST_OVERVIEW_HEADER = "&4Header";
    public static String PLAYER_TABLIST_OVERVIEW_FOOTER = "&8Footer";

    public static boolean PLAYER_CHAT_ENABLED = false;
    public static String PLAYER_CHAT_FORMAT = "&e{design.chat}{player.name}&8:&f {message}";


    public static transient MessageComponent<?> PLAYER_CHAT;
    public static transient MessageComponent<?> PLAYER_TABLIST_PREFIX_LOADED;
    public static transient MessageComponent<?> PLAYER_TABLIST_SUFFIX_LOADED;
    public static transient MessageComponent<?> PLAYER_TABLIST_OVERVIEW_HEADER_LOADED;
    public static transient MessageComponent<?> PLAYER_TABLIST_OVERVIEW_FOOTER_LOADED;

    @DocumentKey("server.groupDelimiter")
    public static String SERVER_GROUP_DELIMITER = "_";

    public static boolean SERVER_STATUS_ENABLED = false;
    @DocumentKey("server.status.versionInfo")
    public static String SERVER_STATUS_VERSION_INFO = "1.8.8";
    @DocumentKey("server.status.playerInfo")
    public static List<String> SERVER_STATUS_PLAYER_INFO = Collections.singletonList("This server is running McNative!");
    public static String SERVER_STATUS_DESCRIPTION_LINE1 = "&6McNative &8- &9Minecraft Application Framework";
    public static String SERVER_STATUS_DESCRIPTION_LINE2 = "&7Are you ready to build with us?";

    public static transient MessageComponent<?> SERVER_STATUS_DESCRIPTION_LINE1_COMPILED;
    public static transient MessageComponent<?> SERVER_STATUS_DESCRIPTION_LINE2_COMPILED;

    static {
        PLAYER_COLORS_COLORS.put("mcnative.player.color.administrator","&4");
        PLAYER_COLORS_COLORS.put("mcnative.player.color.moderator","&c");
        PLAYER_COLORS_COLORS.put("mcnative.player.color.premium","&6");
    }

    public static boolean load(PretronicLogger logger, File location){
        logger.info(McNative.CONSOLE_PREFIX+"Searching configuration file");
        Pair<File, DocumentFileType> configSpec = Document.findExistingType(location,"config");
        File configFile;
        DocumentFileType type;

        if(configSpec == null){
            configFile = new File(location,"config.yml");
            type = DocumentFileType.YAML;
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException exception) {
                logger.error(McNative.CONSOLE_PREFIX+"Could not create configuration file (config.yml)",exception);
                exception.printStackTrace();
                return false;
            }
        }else{
            configFile = configSpec.getKey();
            type = configSpec.getValue();
        }
        logger.info(McNative.CONSOLE_PREFIX+"Loading configuration (config.{})",type.getEnding());

        try{
            FileConfiguration.FILE_TYPE = type;
            Document config = type.getReader().read(configFile);
            Document.loadConfigurationClass(McNativeBukkitConfiguration.class,config);
            type.getWriter().write(configFile,config);
        }catch (Exception exception){
            logger.info(McNative.CONSOLE_PREFIX+"Could not load configuration (config."+type.getEnding()+")",exception);
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean postLoad(){
        PLAYER_CHAT = parseCustomMessage(PLAYER_CHAT_FORMAT);
        PLAYER_TABLIST_PREFIX_LOADED = parseCustomMessage(PLAYER_TABLIST_PREFIX);
        PLAYER_TABLIST_SUFFIX_LOADED = parseCustomMessage(PLAYER_TABLIST_SUFFIX);

        PLAYER_TABLIST_OVERVIEW_HEADER_LOADED = parseCustomMessage(PLAYER_TABLIST_OVERVIEW_HEADER);
        PLAYER_TABLIST_OVERVIEW_FOOTER_LOADED = parseCustomMessage(PLAYER_TABLIST_OVERVIEW_FOOTER);

        SERVER_STATUS_DESCRIPTION_LINE1_COMPILED = parseCustomMessage(SERVER_STATUS_DESCRIPTION_LINE1);
        SERVER_STATUS_DESCRIPTION_LINE2_COMPILED = parseCustomMessage(SERVER_STATUS_DESCRIPTION_LINE2);

        OfflineMinecraftPlayer.DISPLAY_NAME_FORMAT = PLAYER_DISPLAY_NAME_FORMAT;
        return true;
    }
    private static MessageComponent<?> parseCustomMessage(String input){
        Message message = new MessageParser(McNative.getInstance().getRegistry()
                .getService(MessageProvider.class).getProcessor(),input).parse();
        return new MessageKeyComponent(message);
    }
}
