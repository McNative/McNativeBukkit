/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 12:37
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

import net.pretronic.libraries.command.NotFoundHandler;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.api.McNative;

import java.util.*;

public class McNativeCommandMap extends SimpleCommandMap implements Listener {

    private final BukkitCommandManager commandManager;
    private final SimpleCommandMap original;
    protected final Map<String,Command> knownCommands;

    @SuppressWarnings("unchecked")
    public McNativeCommandMap(BukkitCommandManager commandManager,SimpleCommandMap original) {
        super(Bukkit.getServer());
        this.commandManager = commandManager;
        this.original = original;
        this.knownCommands = (Map<String, Command>) ReflectionUtil.getFieldValue(SimpleCommandMap.class,original, "knownCommands");

        for (Map.Entry<String, Command> entry : this.knownCommands.entrySet()) {
            if(!(entry.getValue() instanceof McNativeCommand)){
                String[] parts = entry.getKey().split(":");
                if(parts.length == 2){
                    String owner;
                    if(entry.getValue() instanceof PluginCommand){
                        owner = ((PluginCommand) entry.getValue()).getPlugin().getName();
                    }else{
                        owner = parts[0];
                    }
                    commandManager.provideCommand(new BukkitCommand(entry.getValue(),getOwner(owner)));
                }
            }
        }
        Bukkit.getPluginManager().registerEvents(this, McNativeLauncher.getPlugin());
    }

    public SimpleCommandMap getOriginal() {
        return original;
    }

    @Override
    public void setFallbackCommands() {
        original.setFallbackCommands();
    }

    @Override
    public void registerAll(String fallbackPrefix, List<Command> commands) {
        if (commands != null) {
            for (Command c : commands) {
                register(fallbackPrefix, c);
            }
        }

    }

    @Override
    public boolean register(String fallbackPrefix, Command command) {
        if(original == null) return false;
        return register(command.getName(),fallbackPrefix, command);
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command) {
        boolean result =  original.register(label, fallbackPrefix, command);
        if(result && !(command instanceof McNativeCommand)){
            commandManager.provideCommand(new BukkitCommand(command,getOwner(fallbackPrefix)));
        }
        return result;
    }

    private ObjectOwner getOwner(String name){
        Plugin<?> plugin = McNative.getInstance().getPluginManager().getPlugin(name);
        return plugin != null ? plugin : ObjectOwner.SYSTEM;
    }

    @Override
    public boolean dispatch(CommandSender sender, String command) throws CommandException {
        if(!original.dispatch(sender,command)){
            NotFoundHandler handler = commandManager.getNotFoundHandler();
            if(handler != null){
                String[] arguments = command.split(" ");
                handler.handle(McNativeCommand.getMappedSender(sender),arguments[0], Arrays.copyOfRange(arguments,1,arguments.length));
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void clearCommands() {
        commandManager.clearCommands();
        original.clearCommands();
    }

    @Override
    public Command getCommand(String name) {
        return original.getCommand(name);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine) {
        return original.tabComplete(sender, cmdLine);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine, Location location) {
        return original.tabComplete(sender, cmdLine, location);
    }

    @Override
    public Collection<Command> getCommands() {
        return original.getCommands();
    }

    @Override
    public void registerServerAliases() {
        original.registerServerAliases();
    }

    public void unregister(Object command){
        Iterators.removeSilent(this.knownCommands.entrySet(), entry -> entry.getValue().equals(command));
        commandManager.unregisterCommand(command);
    }

    public synchronized void unregister(org.bukkit.plugin.Plugin plugin){
        synchronized (this){
            Iterator<Map.Entry<String, Command>> iterator =  this.knownCommands.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Command> entry = iterator.next();
                if(entry.getValue() instanceof PluginCommand){
                    if(((PluginCommand) entry.getValue()).getPlugin().equals(plugin)){
                        iterator.remove();
                        commandManager.unregisterCommand(entry.getValue());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event){
        unregister(event.getPlugin());
    }
}
