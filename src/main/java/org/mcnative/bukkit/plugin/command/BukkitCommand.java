/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.12.19, 15:25
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

package org.mcnative.bukkit.plugin.command;

import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.mcnative.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.CustomCommandSender;
import org.mcnative.runtime.api.serviceprovider.permission.Permissable;

import java.util.Set;

public class BukkitCommand implements Command {

    private final org.bukkit.command.Command original;
    private final ObjectOwner owner;
    private final CommandConfiguration configuration;

    public BukkitCommand(org.bukkit.command.Command original, ObjectOwner owner) {
        this.original = original;
        this.owner = owner;
        this.configuration = CommandConfiguration.newBuilder()
                .name(original.getName())
                .permission(original.getPermission())
                .aliases(original.getAliases().toArray(new String[0]))
                .create();
    }

    public org.bukkit.command.Command getOriginal() {
        return original;
    }

    @Override
    public ObjectOwner getOwner() {
        return owner;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void execute(CommandSender sender, String[] arguments) {
        org.bukkit.command.CommandSender mapped;
        if(sender.equals(McNative.getInstance().getConsoleSender())){
            mapped = Bukkit.getConsoleSender();
        }else if(sender instanceof BukkitPlayer){
            mapped = ((BukkitPlayer) sender).getOriginal();
        }else mapped = new MappedCommandSender(sender);
        original.execute(mapped,getConfiguration().getName(),arguments);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else return original.equals(obj);
    }

    public final static class MappedCommandSender implements org.bukkit.command.CommandSender, CustomCommandSender {

        private final CommandSender original;

        public MappedCommandSender(CommandSender original) {
            this.original = original;
        }

        @Override
        public void sendMessage(String text) {
            original.sendMessage(text);
        }

        @Override
        public void sendMessage(String[] strings) {
            for (String string : strings) original.sendMessage(string);
        }

        @Override
        public Server getServer() {
            return Bukkit.getServer();
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public boolean isPermissionSet(String permission) {
            if(original instanceof Permissable) return ((Permissable) original).isPermissionSet(permission);
            return hasPermission(permission);
        }

        @Override
        public boolean isPermissionSet(Permission permission) {
            if(original instanceof Permissable) return ((Permissable) original).isPermissionSet(permission.getName());
            return hasPermission(permission);
        }

        @Override
        public boolean hasPermission(String permission) {
            return original.hasPermission(permission);
        }

        @Override
        public boolean hasPermission(Permission permission) {//@Todo check if right implementation
            return original.hasPermission(permission.getName());
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
            throw new UnsupportedOperationException("McNative mapped command manager does not support permission attcahements");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            throw new UnsupportedOperationException("McNative mapped command manager does not support permission attcahements");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
            throw new UnsupportedOperationException("McNative mapped command manager does not support permission attcahements");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int i) {
            throw new UnsupportedOperationException("McNative mapped command manager does not support permission attcahements");
        }

        @Override
        public void removeAttachment(PermissionAttachment permissionAttachment) {
            throw new UnsupportedOperationException("McNative mapped command manager does not support permission attcahements");
        }

        @Override
        public void recalculatePermissions() {
            //Unused
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            throw new UnsupportedOperationException("McNative mapped command manager does not support permission attcahements");
        }

        @Override
        public boolean isOp() {
            if(original instanceof Permissable) return ((Permissable) original).isOperator();
            return false;
        }

        @Override
        public void setOp(boolean operator) {
            if(original instanceof Permissable) ((Permissable) original).setOperator(operator);
        }

        @Override
        public Object getOriginal() {
            return original;
        }

        @Override
        public Class<?> getOriginalClass() {
            return original.getClass();
        }

        @Override
        public boolean instanceOf(Class<?> originalClass) {
            return originalClass.isAssignableFrom(original.getClass());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T to(Class<T> originalClass) {
            return (T) original;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                    || (obj instanceof CustomCommandSender && original.equals(((CustomCommandSender) obj).getOriginal()))
                    || original.equals(obj);
        }

        @Override
        public String toString() {
            return original.toString();
        }
    }
}
