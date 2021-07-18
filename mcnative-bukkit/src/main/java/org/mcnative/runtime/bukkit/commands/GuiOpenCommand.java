package org.mcnative.runtime.bukkit.commands;

import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.event.injection.annotations.Inject;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.service.inventory.gui.Gui;
import org.mcnative.runtime.api.service.inventory.gui.GuiManager;
import org.mcnative.runtime.api.service.inventory.gui.Page;
import org.mcnative.runtime.common.Messages;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

public class GuiOpenCommand extends BasicCommand implements Completable {

    private final GuiManager guiManager;

    public GuiOpenCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("gui"));
        this.guiManager = McNative.getInstance().getRegistry().getService(GuiManager.class);
    }

    @Override
    public void execute(CommandSender sender, String[] arguments) {
        if(arguments.length == 0){
            sender.sendMessage(Messages.COMMAND_MCNATIVE_GUI_LIST, VariableSet.create()
                    .addDescribed("guis",guiManager.getGuis()));
            return;
        }
        String guiName = arguments[0];
        String page = null;
        if(arguments.length >= 2){
            page = arguments[1];
        }

        Gui<?> gui = guiManager.getGui(guiName);

        if(gui == null){
            sender.sendMessage(Messages.COMMAND_MCNATIVE_GUI_NOT_FOUND, VariableSet.create()
                    .addDescribed("gui",arguments[0]));
            return;
        }

        gui.open((ConnectedMinecraftPlayer) sender,page);

    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String[] arguments) {
        if(arguments.length == 0){
            return Iterators.map(this.guiManager.getGuis(), Gui::getName);
        }else if(arguments.length == 1){
            return Iterators.map(this.guiManager.getGuis(), Gui::getName, gui -> gui.getName().toLowerCase().startsWith(arguments[0]));
        }else if(arguments.length == 2) {
            Gui<?> gui = guiManager.getGui(arguments[0]);
            if(gui == null) return Collections.emptyList();
            else return Iterators.map(gui.getPages(), Page::getName, page -> page.getName().toLowerCase().startsWith(arguments[1]));
        }
        return Collections.emptyList();
    }
}
