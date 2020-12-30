/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.05.20, 19:10
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

package org.mcnative.bukkit;

import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.io.IORuntimeException;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.network.component.server.ServerVersion;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.TextComponent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class BukkitServerStatusResponse implements ServerStatusResponse {

    private ServerVersion version;
    private MessageComponent<?> description;
    private VariableSet descriptionVariables;
    private String favicon;

    private int maximumPlayers;
    private int onlinePlayers;
    private List<PlayerInfo> players;

    private int ping;

    public BukkitServerStatusResponse() {}

    @Override
    public ServerVersion getVersion() {
        return version;
    }

    @Override
    public ServerStatusResponse setVersion(ServerVersion version) {
        Validate.notNull(version);
        this.version = version;
        return this;
    }

    @Override
    public ServerStatusResponse setVersion(String name, MinecraftProtocolVersion protocol) {
        Validate.notNull(name,protocol);
        this.version = new ServerVersion(name,protocol);
        return this;
    }

    @Override
    public MessageComponent<?> getDescription() {
        return description;
    }

    @Override
    public VariableSet getDescriptionVariables() {
        return descriptionVariables;
    }

    @Override
    public ServerStatusResponse setDescription(MessageComponent<?> description, VariableSet variables) {
        Validate.notNull(description,variables);
        this.description = description;
        this.descriptionVariables = variables;
        return this;
    }

    @Override
    public ServerStatusResponse setDescription(MessageComponent<?> line1, MessageComponent<?> line2, VariableSet variables) {
        TextComponent root = new TextComponent();
        root.setText("");
        root.addExtra(line1);
        root.addExtra(Text.of("\n"));
        root.addExtra(line2);
        return setDescription(root,variables);
    }

    @Override
    public String getFavicon() {
        return favicon;
    }

    @Override
    public ServerStatusResponse setFavicon(String favicon) {
        this.favicon = favicon;
        return this;
    }
    @Override
    public ServerStatusResponse setFavicon(BufferedImage image) {
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", memoryStream);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        setFavicon("data:image/png;base64,"+Base64.getEncoder().encodeToString(memoryStream.toByteArray()));
        return this;
    }

    @Override
    public ServerStatusResponse setFavicon(URL url) {
        try {
            setFavicon(ImageIO.read(url));
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return this;
    }

    @Override
    public ServerStatusResponse setFavicon(File location) {
        try {
            setFavicon(ImageIO.read(location));
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return this;
    }

    @Override
    public int getMaxPlayers() {
        return maximumPlayers;
    }

    @Override
    public ServerStatusResponse setMaxPlayers(int maximumPlayers) {
        this.maximumPlayers = maximumPlayers;
        return this;
    }

    @Override
    public int getOnlinePlayers() {
        if(onlinePlayers == DYNAMIC_CALCULATED) return McNative.getInstance().getLocal().getOnlineCount();
        return onlinePlayers;
    }

    @Override
    public ServerStatusResponse setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
        return this;
    }

    @Override
    public List<PlayerInfo> getPlayerInfo() {
        if(players == null){
            return new ArrayList<>(McNative.getInstance().getLocal().getConnectedPlayers());
        }
        return players;
    }

    @Override
    public ServerStatusResponse setPlayerInfo(List<PlayerInfo> players) {
        Validate.notNull(players);
        this.players = players;
        return this;
    }

    @Override
    public ServerStatusResponse setPlayerInfo(PlayerInfo[] players) {
        Validate.notNull((Object) players);
        this.players = Arrays.asList(players);
        return this;
    }

    @Override
    public ServerStatusResponse addPlayerInfo(PlayerInfo info) {
        Validate.notNull(info);
        if(players == null) players = new ArrayList<>();
        this.players.add(info);
        return this;
    }

    @Override
    public ServerStatusResponse addPlayerInfo(String text) {
        addPlayerInfo(ServerStatusResponse.newPlayerInfo(text));
        return this;
    }

    @Override
    public ServerStatusResponse removePlayerInfo(PlayerInfo info) {
        if(players == null) players = new ArrayList<>();
        this.players.remove(info);
        return this;
    }

    @Override
    public ServerStatusResponse clearPlayerInfo() {
        if(players == null) return this;
        this.players.clear();
        return this;
    }

    @Override
    public int getPing() {
        return ping;
    }

    @Override
    public void setPing(int ping) {
        this.ping = ping;
    }

    @Override
    public ServerStatusResponse clone() {
        ServerStatusResponse result = new BukkitServerStatusResponse();
        result.setVersion(new ServerVersion(version.getName(),version.getProtocol()));
        result.setDescription(description,descriptionVariables);
        result.setMaxPlayers(maximumPlayers);
        result.setOnlinePlayers(onlinePlayers);
        result.setFavicon(favicon);
        if(players != null) result.setPlayerInfo(new ArrayList<>(players));
        return result;
    }
}
