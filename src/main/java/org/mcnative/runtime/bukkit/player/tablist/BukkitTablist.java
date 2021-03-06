/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 25.04.20, 20:54
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

package org.mcnative.runtime.bukkit.player.tablist;

import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import org.mcnative.runtime.bukkit.McNativeBukkitConfiguration;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.api.event.player.design.MinecraftPlayerDesignUpdateEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.player.tablist.*;
import org.mcnative.runtime.api.protocol.packet.type.player.MinecraftPlayerListHeaderAndFooterPacket;
import org.mcnative.runtime.api.protocol.packet.type.scoreboard.MinecraftScoreboardTeamsPacket;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.format.TextColor;
import org.mcnative.runtime.common.player.tablist.SimpleTablistEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitTablist implements Tablist {

    private final Collection<ConnectedMinecraftPlayer> receivers;

    private final List<TablistEntry> entries;

    private TablistFormatter formatter;
    private TablistOverviewFormatter overviewFormatter;

    public BukkitTablist() {
        this.receivers = new ArrayList<>();
        this.entries = new ArrayList<>();
    }

    @Override
    public Collection<ConnectedMinecraftPlayer> getReceivers() {
        return this.receivers;
    }

    @Override
    public List<TablistEntry> getEntries() {
        return this.entries;
    }

    @Override
    public TablistFormatter getFormatter() {
        return formatter;
    }

    @Override
    public void setFormatter(TablistFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void addEntry(ConnectedMinecraftPlayer player) {
        addEntry((TablistEntry)player);
    }

    @Override
    public void addEntry(ConnectedMinecraftPlayer player, PlayerDesign design) {
        Validate.notNull(player,design);
        addEntry(new SimpleTablistEntry(player.getName(),design));
    }

    @Override
    public void addEntry(TablistEntry entry) {
        Validate.notNull(entry);
        this.entries.add(entry);
        for (ConnectedMinecraftPlayer receiver : this.receivers) sendEntry(receiver,entry);
    }

    @Override
    public void removeEntry(ConnectedMinecraftPlayer player) {
        removeEntry((TablistEntry)player);
    }

    @Override
    public void removeEntry(TablistEntry entry) {
        Validate.notNull(entry);
        this.entries.remove(entry);
        for (ConnectedMinecraftPlayer receiver : this.receivers) sendRemoveEntry(receiver,entry);
    }

    @Override
    public void reloadEntry(TablistEntry entry) {
        Validate.notNull(entry);
        if(this.entries.contains(entry)){
            for (ConnectedMinecraftPlayer receiver : this.receivers) sendEntry(receiver,entry);
        }
    }

    @Override
    public TablistOverviewFormatter getOverviewFormatter() {
        return this.overviewFormatter;
    }

    @Override
    public void setOverviewFormatter(TablistOverviewFormatter formatter) {
        this.overviewFormatter = formatter;
    }

    @Override
    public void updateOverview(VariableSet headerVariables, VariableSet footerVariables) {
        for (ConnectedMinecraftPlayer receiver : receivers) {
            updateOverview(receiver, headerVariables, footerVariables);
        }
    }

    @Override
    public void updateOverview(ConnectedMinecraftPlayer player, VariableSet headerVariables, VariableSet footerVariables) {
        if(overviewFormatter != null){
            MinecraftPlayerListHeaderAndFooterPacket packet = new MinecraftPlayerListHeaderAndFooterPacket();
            packet.setHeader(getOverviewFormatter().formatHeader(player, headerVariables, footerVariables));
            packet.setFooter(getOverviewFormatter().formatFooter(player, headerVariables, footerVariables));
            packet.setHeaderVariables(headerVariables);
            packet.setFooterVariables(footerVariables);
            player.sendPacket(packet);
        }
    }

    @Override
    public void updateEntries() {
        for (ConnectedMinecraftPlayer receiver : receivers){
            updateEntries(receiver);
        }
    }

    @Override
    public void updateEntries(ConnectedMinecraftPlayer player) {
        for (TablistEntry entry : entries) sendEntry(player,entry);
    }

    private void sendEntry(ConnectedMinecraftPlayer receiver,TablistEntry entry){
        if(formatter == null || !(receiver instanceof BukkitPlayer)) return;
        PlayerDesign design = entry.getDesign(receiver);
        VariableSet variables = VariableSet.create();
        variables.addDescribed("entry",entry);
        if(entry.isPlayer()) variables.add("player",entry);
        variables.addDescribed("design",design);

        design.appendAdditionalVariables(variables);

        MinecraftScoreboardTeamsPacket.Action action;
        String teamName = ((BukkitPlayer) receiver).getTablistTeamNames().get(entry);
        String priority = buildPriorityString(design.getPriority());

        if(teamName == null){
            teamName = priority+entry.getName().substring(0,4)+((BukkitPlayer) receiver).getTablistTeamIndexAndIncrement();

            action = MinecraftScoreboardTeamsPacket.Action.CREATE;
            ((BukkitPlayer) receiver).getTablistTeamNames().put(entry,teamName);
        }else if(!teamName.startsWith(priority)){
            MinecraftScoreboardTeamsPacket packet = new MinecraftScoreboardTeamsPacket();
            packet.setName("T"+teamName);
            packet.setAction(MinecraftScoreboardTeamsPacket.Action.DELETE);
            receiver.sendPacket(packet);

            teamName = priority+entry.getName().substring(0,4)+((BukkitPlayer) receiver).getTablistTeamIndexAndIncrement();
            action = MinecraftScoreboardTeamsPacket.Action.CREATE;
            ((BukkitPlayer) receiver).getTablistTeamNames().put(entry,teamName);
        }else action = MinecraftScoreboardTeamsPacket.Action.UPDATE;

        MinecraftScoreboardTeamsPacket packet = new MinecraftScoreboardTeamsPacket();
        packet.setName("T"+teamName);
        packet.setAction(action);
        packet.setDisplayName(Text.newBuilder().color(TextColor.RED).text("Tablist").build());
        packet.setPrefix(formatter.formatPrefix(receiver,entry,variables));
        packet.setSuffix(formatter.formatSuffix(receiver,entry,variables));
        packet.setColor(formatter.getColor(receiver,entry));
        packet.setVariables(variables);
        packet.setEntities(new String[]{entry.getName()});
        receiver.sendPacket(packet);
    }

    private void sendRemoveEntry(ConnectedMinecraftPlayer receiver,TablistEntry entry){
        if(!(receiver instanceof BukkitPlayer)) return;
        MinecraftScoreboardTeamsPacket packet = new MinecraftScoreboardTeamsPacket();
        packet.setName("TL-"+entry.getName());
        packet.setAction(MinecraftScoreboardTeamsPacket.Action.DELETE);
        receiver.sendPacket(packet);
        ((BukkitPlayer) receiver).getTablistTeamNames().remove(entry);
    }

    @Internal
    public void attachReceiver(ConnectedMinecraftPlayer player){
        this.receivers.add(player);
        VariableSet variables = VariableSet.create().addDescribed("player", player);
        updateOverview(player, variables, variables);
        for (TablistEntry entry : entries) sendEntry(player,entry);
    }

    @Internal
    public void detachReceiver(ConnectedMinecraftPlayer player){
        this.receivers.remove(player);
        for (TablistEntry entry : entries) sendRemoveEntry(player,entry);
    }

    @Listener
    public void onPlayerDesignUpdate(MinecraftPlayerDesignUpdateEvent event){
        for (TablistEntry entry : this.entries) {
            if(entry instanceof MinecraftPlayer){
                if(((MinecraftPlayer) entry).getUniqueId().equals(event.getPlayer().getUniqueId())){
                    for (ConnectedMinecraftPlayer receiver : receivers) {
                        sendEntry(receiver, event.getPlayer().getAsOnlinePlayer());
                    }
                    return;
                }
            }
        }
    }

    private String buildPriorityString(int priority){
        StringBuilder stringPriority = new StringBuilder(String.valueOf(priority));
        int amount = 6-stringPriority.length();
        for (int i = 0; i < amount; i++) {
            stringPriority.insert(0, "0");
        }
        return stringPriority.toString();
    }
}
