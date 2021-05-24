package org.mcnative.runtime.bukkit.event.player;

import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BukkitLegacyTabCompleteEvent implements MinecraftPlayerTabCompleteEvent, MinecraftPlayerTabCompleteResponseEvent {

    private final PlayerChatTabCompleteEvent event;
    private final OnlineMinecraftPlayer player;

    public BukkitLegacyTabCompleteEvent(PlayerChatTabCompleteEvent event, OnlineMinecraftPlayer player) {
        this.event = event;
        this.player = player;
    }

    @Override
    public String getCursor() {
        return event.getChatMessage();
    }

    @Override
    public List<String> getSuggestions() {
        return new MappedList();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.getTabCompletions().clear();
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }

    public class MappedList implements List<String> {

        @Override
        public int size() {
            return event.getTabCompletions().size();
        }

        @Override
        public boolean isEmpty() {
            return event.getTabCompletions().isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return event.getTabCompletions().contains(o);
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return event.getTabCompletions().iterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return event.getTabCompletions().toArray();
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            return event.getTabCompletions().toArray(a);
        }

        @Override
        public boolean add(String s) {
            return event.getTabCompletions().add(s);
        }

        @Override
        public boolean remove(Object o) {
            return event.getTabCompletions().remove(o);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return event.getTabCompletions().containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends String> c) {
            return event.getTabCompletions().addAll(c);
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends String> c) {
            return event.getTabCompletions().addAll(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return event.getTabCompletions().removeAll(c);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return event.getTabCompletions().retainAll(c);
        }

        @Override
        public void clear() {
            event.getTabCompletions().clear();
        }

        @Override
        public String get(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String set(int index, String element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, String element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public ListIterator<String> listIterator() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public ListIterator<String> listIterator(int index) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public List<String> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }
    }
}
