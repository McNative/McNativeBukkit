/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 20.02.20, 21:12
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

package org.mcnative.runtime.bukkit.player;

import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveConcurrentHashMap;
import net.pretronic.libraries.utility.map.caseintensive.CaseIntensiveMap;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionResult;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

//@Todo implement child permissions
/*
@Todo create glowstone implementation
 Class.forName("net.glowstone.entity.GlowHumanEntity").getDeclaredField("permissions");
 */
public class McNativePermissible extends PermissibleBase {

    private final static Class<?> CRAFT_HUMAN_ENTITY_CLASS = BukkitReflectionUtil.getCraftClass("entity.CraftHumanEntity");
    private final static Field CRAFT_HUMAN_ENTITY_PERMISSIBLE_FIELD = ReflectionUtil.getField(CRAFT_HUMAN_ENTITY_CLASS,"perm");

    private final Player bukkitPlayer;
    private final MinecraftPlayer player;
    private final CaseIntensiveMap<PermissionAttachmentInfo> permissions;
    private final List<PermissionAttachment> attachments;
    private final Permissible parent;

    static {
        CRAFT_HUMAN_ENTITY_PERMISSIBLE_FIELD.setAccessible(true);
    }

    public McNativePermissible(Player bukkitPlayer,MinecraftPlayer player) {
        super(null);
        this.bukkitPlayer = bukkitPlayer;
        this.player = player;

        this.parent = bukkitPlayer;
        this.permissions = new CaseIntensiveConcurrentHashMap<>();
        this.attachments = new LinkedList<>();

        recalculatePermissions();
    }

    @Override
    public boolean isOp() {
        if(player == null) return false;
        return player.getPermissionHandler().isOperator();
    }

    @Override
    public void setOp(boolean operator) {
        player.getPermissionHandler().setOperator(operator);
    }

    @Override
    public boolean isPermissionSet(String permission) {
        if(player.getPermissionHandler().isPermissionSet(permission)) return true;
        return permissions.containsKey(permission);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        String name = permission.getName().toLowerCase(java.util.Locale.ENGLISH);

        if (isPermissionSet(name)) {
            return permissions.get(name).getValue();
        }
        return permission.getDefault().getValue(isOp());
    }

    @Override
    public boolean hasPermission(String permission) {
        PermissionResult result = player.getPermissionHandler().hasPermissionExact(permission);
        if(result == PermissionResult.NORMAL) {
            PermissionAttachmentInfo attachmentInfo = permissions.get(permission);
            return attachmentInfo != null && attachmentInfo.getValue();
        }
        return result == PermissionResult.ALLOWED;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        Validate.notNull(permission);
        return hasPermission(permission.getName());
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        if (name == null) {
            throw new IllegalArgumentException("Permission name cannot be null");
        } else if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        } else if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        }

        PermissionAttachment result = addAttachment(plugin);
        result.setPermission(name, value);

        recalculatePermissions();

        return result;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        } else if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        }

        PermissionAttachment result = new PermissionAttachment(plugin, parent);

        attachments.add(result);
        recalculatePermissions();

        return result;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        if (name == null) {
            throw new IllegalArgumentException("Permission name cannot be null");
        } else if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        } else if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        }

        PermissionAttachment result = addAttachment(plugin, ticks);

        if (result != null) {
            result.setPermission(name, value);
        }

        return result;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        } else if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        }

        PermissionAttachment result = addAttachment(plugin);

        if (Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemoveAttachmentRunnable(result), ticks) == -1) {
            Bukkit.getServer().getLogger().log(Level.WARNING, "Could not add PermissionAttachment to " + parent + " for plugin " + plugin.getDescription().getFullName() + ": Scheduler returned -1");
            result.remove();
            return null;
        } else {
            return result;
        }
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment cannot be null");
        }

        if (attachments.contains(attachment)) {
            attachments.remove(attachment);
            PermissionRemovedExecutor removedExecutor = attachment.getRemovalCallback();

            if (removedExecutor != null) {
                removedExecutor.attachmentRemoved(attachment);
            }

            recalculatePermissions();
        } else {
            throw new IllegalArgumentException("Given attachment is not part of Permissible object " + parent);
        }
    }

    @Override
    public void recalculatePermissions() {
        if(permissions == null) return;
        clearPermissions();

        for (PermissionAttachment attachment : attachments) {
            calculateChildPermissions(attachment.getPermissions(), false, attachment);
        }
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {//@Todo optimize
        Set<PermissionAttachmentInfo> result = new HashSet<>(permissions.values());
        for (String permission : player.getPermissionHandler().getEffectivePermissions()) {
            result.add(new PermissionAttachmentInfo(this,permission,null,true));
        }
        return result;
    }

    @Override
    public synchronized void clearPermissions() {
        Set<String> permissions = this.permissions.keySet();

        for (String name : permissions) {
            Bukkit.getServer().getPluginManager().unsubscribeFromPermission(name, parent);
        }

        Bukkit.getServer().getPluginManager().unsubscribeFromDefaultPerms(false, parent);
        Bukkit.getServer().getPluginManager().unsubscribeFromDefaultPerms(true, parent);

        this.permissions.clear();
    }

    private void calculateChildPermissions(Map<String, Boolean> children, boolean invert, PermissionAttachment attachment) {
        for (Map.Entry<String, Boolean> entry : children.entrySet()) {
            String name = entry.getKey();

            Permission permission = Bukkit.getServer().getPluginManager().getPermission(name);
            boolean value = entry.getValue() ^ invert;

            permissions.put(name, new PermissionAttachmentInfo(parent, name, attachment, value));
            Bukkit.getServer().getPluginManager().subscribeToPermission(name, parent);

            if (permission != null) {
                calculateChildPermissions(permission.getChildren(), !value, attachment);
            }
        }
    }

    public void inject(){
        try {
            CRAFT_HUMAN_ENTITY_PERMISSIBLE_FIELD.set(bukkitPlayer,this);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to inject McNative Permission Bridge");
        }
    }

    private static class RemoveAttachmentRunnable implements Runnable {

        private final PermissionAttachment attachment;

        public RemoveAttachmentRunnable(PermissionAttachment attachment) {
            this.attachment = attachment;
        }

        @Override
        public void run() {
            attachment.remove();
        }
    }
}
