package org.mcnative.runtime.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

public class Test implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void test(TabCompleteEvent event){
        System.out.println("EVENT "+event.getBuffer()+" | "+event.isCancelled()+" : "+event.getCompletions());

    }

}
