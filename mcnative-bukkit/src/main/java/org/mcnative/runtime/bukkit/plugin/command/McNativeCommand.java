/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 12:40
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

package org.mcnative.runtime.bukkit.plugin.command;

import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.NoPermissionAble;
import net.pretronic.libraries.command.NoPermissionHandler;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcnative.runtime.bukkit.player.BukkitPlayerManager;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.CustomCommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

//@Todo create message fallback component
public class McNativeCommand extends Command {

    private final CommandManager commandManager;
    private final net.pretronic.libraries.command.command.Command original;

    public McNativeCommand(CommandManager commandManager, net.pretronic.libraries.command.command.Command original) {
        super(original.getConfiguration().getName()
                , original.getConfiguration().getDescription()
                , ""
                , Arrays.asList(original.getConfiguration().getAliases()));
        Validate.notNull(commandManager, original);
        this.commandManager = commandManager;
        this.original = original;
    }

    public net.pretronic.libraries.command.command.Command getOriginal() {
        return original;
    }

    @Override
    public String getDescription() {
        String description = original.getConfiguration().getDescription();
        return description != null ? description : "";
    }

    @Override
    public String getUsage() {
        return "/"+original.getConfiguration().getName()+" help";
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] arguments) {
        McNative.getInstance().getScheduler().createTask(McNative.getInstance())
                .async()
                .execute(() -> {
                    try {
                        net.pretronic.libraries.command.sender.CommandSender mappedSender = getMappedSender(sender);

                        NoPermissionHandler noPermissionHandler;
                        if(original instanceof NoPermissionAble) {
                            noPermissionHandler = ((NoPermissionAble)original);
                        } else {
                            noPermissionHandler = this.commandManager.getNoPermissionHandler(getOriginal().getOwner());
                        }

                        if(CommandManager.hasPermission(mappedSender, noPermissionHandler, null,
                                original.getConfiguration().getPermission(), label, arguments)) {
                            original.execute(mappedSender,arguments);
                        }
                    }catch (Exception exception){//@Todo optimize error message
                        exception.printStackTrace();
                    }
                });
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if(original instanceof Completable){
            Collection<String> result = ((Completable) original).complete(getMappedSender(sender),args);
            if(result != null){
                if(result instanceof List) return (List<String>) result;
                else return new ArrayList<>(result);
            }
        }
        return super.tabComplete(sender, alias, args);
    }

    //@Todo implement custom permission messages

    @Override
    public boolean testPermission(CommandSender target) {
        return true;
    }

    @Override
    public boolean testPermissionSilent(CommandSender target) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else return original.equals(obj);
    }

    public static net.pretronic.libraries.command.sender.CommandSender getMappedSender(CommandSender sender){
        net.pretronic.libraries.command.sender.CommandSender mapped;
        if(sender.equals(Bukkit.getConsoleSender())){
            mapped = McNative.getInstance().getConsoleSender();
        }else if(sender instanceof Player) {
            mapped = ((BukkitPlayerManager) McNative.getInstance().getPlayerManager()).getMappedPlayer((Player) sender);
        }else{
            mapped = new MappedCommandSender(sender);
        }
        return mapped;
    }

    public final static class MappedCommandSender implements CustomCommandSender {

        private final CommandSender original;

        public MappedCommandSender(CommandSender original) {
            this.original = original;
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
            return original.getClass().isAssignableFrom(originalClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T to(Class<T> originalClass) {
            return (T) original;
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public boolean hasPermission(String permission) {
            return original.hasPermission(permission);
        }

        @Override
        public void sendMessage(String message) {
            original.sendMessage(message);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                    || (obj instanceof CustomCommandSender && original.equals(((CustomCommandSender) obj).getOriginal()))
                    ||  original.equals(obj);
        }

        @Override
        public String toString() {
            return original.toString();
        }
    }

}
