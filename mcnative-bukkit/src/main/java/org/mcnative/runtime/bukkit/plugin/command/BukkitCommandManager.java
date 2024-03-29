/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 12:28
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

import net.pretronic.libraries.command.NoPermissionHandler;
import net.pretronic.libraries.command.NotFoundHandler;
import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.mcnative.runtime.bukkit.player.BukkitPlayer;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.common.plugin.DefaultNoPermissionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitCommandManager implements CommandManager {

    private final List<Command> commands;
    private McNativeCommandMap commandMap;

    private NotFoundHandler notFoundHandler;
    private NoPermissionHandler noPermissionHandler;

    private final Map<ObjectOwner, NoPermissionHandler> objectOwnerNoPermissionHandler;

    public BukkitCommandManager() {
        this.commands = new ArrayList<>();
        this.objectOwnerNoPermissionHandler = new HashMap<>();
    }

    public NotFoundHandler getNotFoundHandler() {
        return notFoundHandler;
    }

    @Override
    public Command getCommand(String name) {
        return Iterators.findOne(this.commands, command -> command.getConfiguration().getName().equalsIgnoreCase(name));
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public void setNotFoundHandler(NotFoundHandler notFoundHandler) {
        Validate.notNull(notFoundHandler);
        this.notFoundHandler = notFoundHandler;
    }

    @Override
    public NoPermissionHandler getNoPermissionHandler() {
        if(this.noPermissionHandler == null) return DefaultNoPermissionHandler.DEFAULT;
        return this.noPermissionHandler;
    }

    @Override
    public void setNoPermissionHandler(NoPermissionHandler noPermissionHandler) {
        this.noPermissionHandler = noPermissionHandler;
    }

    @Override
    public NoPermissionHandler getNoPermissionHandler(ObjectOwner objectOwner) {
        Validate.notNull(objectOwner);
        NoPermissionHandler handler = this.objectOwnerNoPermissionHandler.get(objectOwner);
        if(handler == null) handler = getNoPermissionHandler();
        return handler;
    }

    @Override
    public void setNoPermissionHandler(ObjectOwner objectOwner, NoPermissionHandler noPermissionHandler) {
        Validate.notNull(objectOwner, noPermissionHandler);
        this.objectOwnerNoPermissionHandler.put(objectOwner, noPermissionHandler);
    }

    @Override
    public void dispatchCommand(CommandSender sender, String name) {
        org.bukkit.command.CommandSender mapped;
        if(sender.equals(McNative.getInstance().getConsoleSender())){
            mapped = Bukkit.getConsoleSender();
        }else if(sender instanceof BukkitPlayer){
            mapped = ((BukkitPlayer) sender).getOriginal();
        }else mapped = new BukkitCommand.MappedCommandSender(sender);
        commandMap.dispatch(mapped,name);
    }

    @Override
    public void registerCommand(Command command) {
        Validate.notNull(command,command.getConfiguration(),command.getOwner());
        if(!command.getConfiguration().isEnabled()) return;
        if(!(command.getOwner() instanceof net.pretronic.libraries.plugin.Plugin) && !command.getOwner().equals(McNative.getInstance())){
            throw new IllegalArgumentException("Owner is not a plugin.");
        }
        if(command instanceof CommandManager && ((CommandManager)command).getNoPermissionHandler(command.getOwner()) == null) {
            ((CommandManager)command).setNoPermissionHandler(DefaultNoPermissionHandler.DEFAULT);
        }
        this.commandMap.register(command.getOwner().getName().toLowerCase(),new McNativeCommand(this, command));
        this.commands.add(command);
        McNative.getInstance().getInjector().inject(command);
    }

    @Override
    public void unregisterCommand(String name) {
        Command result = Iterators.findOne(this.commands, command -> command.getConfiguration().getName().equalsIgnoreCase(name));
        if(result != null) unregisterCommand(result);
    }

    @Override
    public void unregisterCommand(Command command) {
        if(command instanceof BukkitCommand){
            this.commands.remove(command);
            this.commandMap.unregister(((BukkitCommand) command).getOriginal());
        }else{
            this.commandMap.unregister(command);
        }
    }

    @Override
    public void unregisterCommand(ObjectOwner owner) {
        List<Command> result = Iterators.remove(this.commands, command -> command.getOwner().equals(owner));
        for (Command command : result) {
            if(command instanceof BukkitCommand){
                commandMap.unregister(((BukkitCommand) command).getOriginal());
            }else{
                commandMap.unregister(command);
            }
        }
    }

    public void unregisterCommand(Object command0) {
        if(command0 instanceof  McNativeCommand){
            Iterators.removeSilent(this.commands, command -> command.equals(((McNativeCommand) command0).getOriginal()));
        }
        Iterators.removeSilent(this.commands, command -> command.equals(command0));
    }

    @Override
    public void unregisterCommands() {
        commandMap.clearCommands();
    }

    public void provideCommand(Command command){
        this.commands.add(command);
    }

    public void clearCommands(){
        this.commands.clear();
    }

    @Internal
    public void inject(){
        SimpleCommandMap original = ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"commandMap",SimpleCommandMap.class);
        McNativeCommandMap override = new McNativeCommandMap(this,original);
        ReflectionUtil.changeFieldValue(Bukkit.getPluginManager(),"commandMap",override);
        this.commandMap = override;
    }

    @Internal
    public void reset(){
        ReflectionUtil.changeFieldValue(Bukkit.getPluginManager(),"commandMap",commandMap.getOriginal());
    }


}
