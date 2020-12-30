/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.02.20, 19:50
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

package org.mcnative.bukkit.plugin;

import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class McNativePluginWrapperList implements List<Plugin> {

    private final List<Plugin> original;
    private final BukkitPluginManager pluginManager;

    public McNativePluginWrapperList(List<Plugin> original, BukkitPluginManager pluginManager) {
        this.original = original;
        this.pluginManager = pluginManager;
    }

    public List<Plugin> getOriginal() {
        return original;
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return original.contains(o);
    }

    @Override
    public Iterator<Plugin> iterator() {
        return original.iterator();
    }

    @Override
    public Object[] toArray() {
        return original.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return original.toArray(a);
    }

    @Override
    public boolean add(Plugin plugin) {
        pluginManager.registerBukkitPlugin(plugin);
        return original.add(plugin);
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof Plugin) pluginManager.unregisterBukkitPlugin((Plugin) o);
        return original.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return original.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Plugin> c) {
        return original.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Plugin> c) {
        return original.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return original.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return original.retainAll(c);
    }

    @Override
    public void clear() {
        for (Plugin plugin : original) pluginManager.unregisterBukkitPlugin(plugin);
        original.clear();
    }

    @Override
    public Plugin get(int index) {
        return original.get(index);
    }

    @Override
    public Plugin set(int index, Plugin element) {
        pluginManager.registerBukkitPlugin(element);
        return original.set(index, element);
    }

    @Override
    public void add(int index, Plugin element) {
        pluginManager.registerBukkitPlugin(element);
        original.add(index,element);
    }

    @Override
    public Plugin remove(int index) {
        Plugin result =  original.remove(index);
        if(result != null) pluginManager.unregisterBukkitPlugin(result);
        return result;
    }

    @Override
    public int indexOf(Object o) {
        return original.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return original.lastIndexOf(o);
    }

    @Override
    public ListIterator<Plugin> listIterator() {
        return original.listIterator();
    }

    @Override
    public ListIterator<Plugin> listIterator(int index) {
        return original.listIterator();
    }

    @Override
    public List<Plugin> subList(int fromIndex, int toIndex) {
        return original.subList(fromIndex, toIndex);
    }
}
