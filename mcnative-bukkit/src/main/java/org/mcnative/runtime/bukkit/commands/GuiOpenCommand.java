package org.mcnative.runtime.bukkit.commands;

import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.service.inventory.gui.Gui;
import org.mcnative.runtime.api.service.inventory.gui.GuiManager;

public class GuiOpenCommand extends BasicCommand {


    public GuiOpenCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("gui"));
    }

    @Override
    public void execute(CommandSender sender, String[] arguments) {
        if(arguments.length == 0){
            //@Todo error
            return;
        }
        String guiName = arguments[0];
        String page = null;
        if(arguments.length >= 2){
            page = arguments[1];
        }

        GuiManager manager = McNative.getInstance().getRegistry().getService(GuiManager.class);
        Gui<?> gui = manager.getGui(guiName);
        if(gui == null){
            sender.sendMessage("Gui not found");
            return;
        }

        gui.open((ConnectedMinecraftPlayer) sender,page);

    }
}
