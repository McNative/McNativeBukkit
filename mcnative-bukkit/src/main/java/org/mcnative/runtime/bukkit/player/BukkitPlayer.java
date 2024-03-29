/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 21.03.20, 13:56
 * @web %web%
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

import io.netty.buffer.ByteBuf;
import net.pretronic.libraries.concurrent.Task;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Convert;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.exception.OperationFailedException;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.bukkit.Bukkit;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.connection.ConnectionState;
import org.mcnative.runtime.api.connection.MinecraftOutputStream;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerCustomClientLoginEvent;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ProxyServer;
import org.mcnative.runtime.api.network.component.server.ServerConnectReason;
import org.mcnative.runtime.api.network.component.server.ServerConnectResult;
import org.mcnative.runtime.api.player.*;
import org.mcnative.runtime.api.player.bossbar.BossBar;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.chat.ChatPosition;
import org.mcnative.runtime.api.player.client.CustomClient;
import org.mcnative.runtime.api.player.client.LabyModClient;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.input.ConfirmResult;
import org.mcnative.runtime.api.player.input.PlayerTextInputValidator;
import org.mcnative.runtime.api.player.input.YesNoResult;
import org.mcnative.runtime.api.player.input.types.MaxStringLengthPlayerTextInputValidator;
import org.mcnative.runtime.api.player.scoreboard.BelowNameInfo;
import org.mcnative.runtime.api.player.scoreboard.sidebar.Sidebar;
import org.mcnative.runtime.api.player.sound.SoundCategory;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacket;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftChatPacket;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftCustomPayloadPacket;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftResourcePackSendPacket;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftTitlePacket;
import org.mcnative.runtime.api.protocol.packet.type.sound.MinecraftSoundEffectPacket;
import org.mcnative.runtime.api.protocol.packet.type.sound.MinecraftStopSoundPacket;
import org.mcnative.runtime.api.service.GameMode;
import org.mcnative.runtime.api.service.MinecraftService;
import org.mcnative.runtime.api.service.advancement.AdvancementProgress;
import org.mcnative.runtime.api.service.entity.Entity;
import org.mcnative.runtime.api.service.entity.living.Player;
import org.mcnative.runtime.api.service.inventory.Inventory;
import org.mcnative.runtime.api.service.inventory.item.ItemStack;
import org.mcnative.runtime.api.service.world.Effect;
import org.mcnative.runtime.api.service.world.location.Location;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.format.TextColor;
import org.mcnative.runtime.api.utils.positioning.Position;
import org.mcnative.runtime.bukkit.BukkitService;
import org.mcnative.runtime.bukkit.McNativeLauncher;
import org.mcnative.runtime.bukkit.entity.BukkitEntity;
import org.mcnative.runtime.bukkit.entity.living.BukkitHumanEntity;
import org.mcnative.runtime.bukkit.event.player.BukkitCustomClientLoginEvent;
import org.mcnative.runtime.bukkit.inventory.BukkitInventory;
import org.mcnative.runtime.bukkit.inventory.item.BukkitItemStack;
import org.mcnative.runtime.bukkit.location.BukkitLocation;
import org.mcnative.runtime.bukkit.player.permission.BukkitPermissionHandler;
import org.mcnative.runtime.bukkit.player.tablist.BukkitTablist;
import org.mcnative.runtime.bukkit.utils.BukkitReflectionUtil;
import org.mcnative.runtime.bukkit.world.BukkitWorld;
import org.mcnative.runtime.common.player.DefaultBossBar;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.common.utils.PlayerRegisterAble;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class BukkitPlayer extends OfflineMinecraftPlayer implements Player, BukkitHumanEntity<org.bukkit.entity.Player> {

    private final org.bukkit.entity.Player original;
    private final PendingConnection connection;

    private ChatChannel chatChannel;
    private Tablist tablist;
    private final Collection<BossBar> bossBars;

    private BukkitWorld world;
    private boolean permissibleInjected;
    private boolean joining;
    private CustomClient customClient;

    private final Map<TablistEntry,String> tablistTeamNames;
    private int tablistTeamIndex;

    private final Deque<PlayerTextInput<?>> inputs;
    private PlayerTextInput<?> currentInput;

    public BukkitPlayer(org.bukkit.entity.Player original, PendingConnection connection,MinecraftPlayerData playerData) {
        super(playerData);
        this.original = original;
        this.connection = connection;
        this.permissibleInjected = false;
        this.joining = false;
        this.world = (BukkitWorld) ((BukkitService)MinecraftService.getInstance()).getMappedWorld(original.getWorld());
        this.tablistTeamNames = new HashMap<>();
        this.tablistTeamIndex = 0;
        this.bossBars = new ArrayList<>();
        this.inputs = new ArrayDeque<>();
    }

    @Override
    public OnlineMinecraftPlayer getAsOnlinePlayer() {
        return this;
    }

    @Override
    public PermissionHandler getPermissionHandler() {
        if(permissionHandler == null){
            permissionHandler = McNative.getInstance().getRegistry().getService(PermissionProvider.class).getPlayerHandler(this);
            if(!permissibleInjected && !(permissionHandler instanceof BukkitPermissionHandler)){
                new McNativePermissible(original,this).inject();
                permissibleInjected = true;
            }
        }else if(!permissionHandler.isCached()){
            permissionHandler = permissionHandler.reload();
        }
        return permissionHandler;
    }

    @Override
    public PendingConnection getConnection() {
        return connection;
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return connection.getProtocolVersion();
    }

    @Override
    public ConnectionState getState() {
        return connection.getState();
    }

    @Override
    public PlayerDesign getDesign(MinecraftPlayer player) {
        return super.getDesign(player);
    }

    @Override
    public InetSocketAddress getAddress() {
        return connection.getAddress();
    }

    @Override
    public void disconnect(MessageComponent<?> reason, VariableSet variables) {
        connection.disconnect(reason,variables);
    }

    @Override
    public DeviceInfo getDevice() {
        return DeviceInfo.JAVA;
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode();
    }

    @Override
    public PlayerClientSettings getClientSettings() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public CustomClient getCustomClient() {
        return customClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CustomClient> T getCustomClient(Class<T> aClass) {
        return (T) customClient;
    }

    @Override
    public boolean isCustomClient() {
        return customClient != null;
    }

    @Override
    public boolean isCustomClient(String name) {
        return customClient != null && customClient.getName().equalsIgnoreCase(name);
    }

    @Override
    public boolean isCustomClient(Class<? extends CustomClient> aClass) {
        return customClient != null && aClass.isAssignableFrom(customClient.getClass());
    }

    @Override
    public void setCustomClient(CustomClient customClient) {
        if(this.customClient != null) throw new OperationFailedException("A custom client is already registered for this player");
        this.customClient = customClient;
        McNative.getInstance().getLocal().getEventBus().callEvent(MinecraftPlayerCustomClientLoginEvent.class,new BukkitCustomClientLoginEvent(this));
    }

    @Override
    public int getPing() {
        return BukkitReflectionUtil.getPing(original);
    }

    @Override
    public CompletableFuture<Integer> getPingAsync() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        McNative.getInstance().getExecutorService().execute(() -> {
            try {
                Thread.sleep(0,10);
            } catch (InterruptedException ignored) { }
            future.complete(getPing());
        });
        return future;
    }

    @Override
    public ProxyServer getProxy() {
        return McNative.getInstance().getNetwork().getOperations().getProxy(this);
    }

    @Override
    public MinecraftServer getServer() {
        return MinecraftService.getInstance();
    }

    @Override
    public void connect(MinecraftServer target, ServerConnectReason reason) {
        McNative.getInstance().getNetwork().getOperations().connect(this,target,reason);
    }

    @Override
    public CompletableFuture<ServerConnectResult> connectAsync(MinecraftServer target, ServerConnectReason reason) {
        return McNative.getInstance().getNetwork().getOperations().connectAsync(this,target,reason);
    }

    @Override
    public void kick(MessageComponent<?> message, VariableSet variables) {
        if(McNative.getInstance().isNetworkAvailable()){
            McNative.getInstance().getNetwork().getOperations().kick(this,message,variables);
        }else{
            disconnect(message,variables);
        }
    }

    @Override
    public void kickLocal(MessageComponent<?> messageComponent, VariableSet variableSet) {
        disconnect(messageComponent,variableSet);
    }

    @Override
    public void performCommand(String command) {
        original.performCommand(command);
    }

    @Override
    public void chat(String message) {
        original.chat(message);
    }

    @Override
    public void sendMessage(ChatPosition position, MessageComponent<?> message, VariableSet variables) {
        MinecraftChatPacket packet = new MinecraftChatPacket();
        packet.setPosition(position);
        packet.setMessage(message);
        packet.setVariables(variables);
        sendPacket(packet);
    }

    @Override
    public void sendTitle(Title title) {
        MinecraftTitlePacket timePacket = new MinecraftTitlePacket();
        timePacket.setAction(MinecraftTitlePacket.Action.SET_TIME);
        timePacket.setTime(title.getTiming());
        sendPacket(timePacket);

        if(title.getTitle() != null){
            MinecraftTitlePacket titlePacket = new MinecraftTitlePacket();
            titlePacket.setAction(MinecraftTitlePacket.Action.SET_TITLE);
            titlePacket.setMessage(title.getTitle());
            titlePacket.setVariables(title.getVariables());
            sendPacket(titlePacket);
        }

        if(title.getSubTitle() != null){
            MinecraftTitlePacket subTitle = new MinecraftTitlePacket();
            subTitle.setAction(MinecraftTitlePacket.Action.SET_SUBTITLE);
            subTitle.setMessage(title.getSubTitle());
            subTitle.setVariables(title.getVariables());
            sendPacket(subTitle);
        }
    }

    @Override
    public void resetTitle() {
        MinecraftTitlePacket packet = new MinecraftTitlePacket();
        packet.setAction(MinecraftTitlePacket.Action.RESET);
        sendPacket(packet);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables) {
        MinecraftChatPacket packet = new MinecraftChatPacket();
        packet.setPosition(ChatPosition.ACTIONBAR);
        packet.setMessage(message);
        packet.setVariables(variables);
        sendPacket(packet);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables, long staySeconds) {
        long timeout = System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(staySeconds);
        sendActionbar(message, variables);
        final Task task = McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                .async().interval(3,TimeUnit.SECONDS).delay(1,TimeUnit.SECONDS).create();
        task.append(() -> {
            if(System.currentTimeMillis() <= timeout){
                task.destroy();
            }else{
                sendActionbar(message, variables);
            }
        });
    }

    @Override
    public void sendPacket(MinecraftPacket packet) {
        connection.sendPacket(packet);
    }

    @Override
    public void sendLocalLoopPacket(MinecraftPacket packet) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void sendRawPacket(ByteBuf byteBuf) {
        connection.sendRawPacket(byteBuf);
    }

    @Override
    public OutputStream sendData(String channel) {
        return new MinecraftOutputStream(channel,this);
    }

    @Override
    public void sendData(String channel, byte[] output) {
        Validate.notNull(output,channel);
        MinecraftCustomPayloadPacket packet = new MinecraftCustomPayloadPacket();
        packet.setChannel(channel);
        packet.setContent(output);
        sendPacket(packet);
    }

    @Override
    public void playSound(String sound, SoundCategory category, float volume, float pitch) {
        MinecraftSoundEffectPacket packet = new MinecraftSoundEffectPacket();
        packet.setSoundName(sound);
        packet.setCategory(category);
        packet.setVolume(volume);
        packet.setPitch(pitch);

        org.bukkit.Location location = original.getLocation();
        packet.setPositionX(location.getBlockX());
        packet.setPositionY(location.getBlockY());
        packet.setPositionZ(location.getBlockZ());

        sendPacket(packet);
    }

    @Override
    public void stopSound() {
        if(getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_13)) return;
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setAction(MinecraftStopSoundPacket.Action.ALL);
        sendPacket(packet);
    }

    @Override
    public void stopSound(String sound) {
        if(getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_13)) return;
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setAction(MinecraftStopSoundPacket.Action.SOUND);
        packet.setSoundName(sound);
        sendPacket(packet);
    }

    @Override
    public void stopSound(SoundCategory category) {
        if(getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_13)) return;
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setAction(MinecraftStopSoundPacket.Action.CATEGORY);
        packet.setCategory(category);
        sendPacket(packet);
    }

    @Override
    public void stopSound(String sound, SoundCategory category) {
        if(getProtocolVersion().isOlder(MinecraftProtocolVersion.JE_1_13)) return;
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setAction(MinecraftStopSoundPacket.Action.BOTH);
        packet.setSoundName(sound);
        packet.setCategory(category);
        sendPacket(packet);
    }

    @Override
    public boolean isBlocking() {
        return original.isBlocking();
    }

    @Override
    public boolean isSleeping() {
        return original.isSleeping();
    }


    @Override
    public org.bukkit.entity.Player getOriginal() {
        return original;
    }

    @Override
    public BukkitWorld getBukkitWorld() {
        return this.world;
    }

    @Override
    public void openBook(ItemStack book) {
        this.original.openBook(((BukkitItemStack)book).getOriginal());
    }

    @Override
    public void hide(OnlineMinecraftPlayer forPlayer) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(forPlayer.getUniqueId());
        if(player != null) {
            this.original.hidePlayer(player);//Switched to deprecated method to support legacy Minecraft versions
        }
    }

    @Override
    public void show(OnlineMinecraftPlayer forPlayer) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(forPlayer.getUniqueId());
        if(player != null) {
            this.original.showPlayer(player);//Switched to deprecated method to support legacy Minecraft versions
        }
    }

    @Override
    public boolean canSee(OnlineMinecraftPlayer forPlayer) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(forPlayer.getUniqueId());
        return player != null && this.original.canSee(player);
    }

    @Override
    public boolean isSneaking() {
        return this.original.isSneaking();
    }

    @Override
    public void setSneaking(boolean sneak) {
        this.original.setSneaking(sneak);
    }

    @Override
    public boolean isSprinting() {
        return  this.original.isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        this.original.setSprinting(sprinting);
    }

    @Override
    public boolean isSleepingIgnored() {
        return  this.original.isSleepingIgnored();
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        this.original.setSleepingIgnored(isSleeping);
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.valueOf(original.getGameMode().name());
    }

    @Override
    public void setGameMode(GameMode mode) {
        org.bukkit.GameMode bukkitGameMode = org.bukkit.GameMode.valueOf(mode.name());
        this.original.setGameMode(bukkitGameMode);
    }

    @Override
    public Location getCompassTarget() {
        return new BukkitLocation(this.original.getCompassTarget(), world);
    }

    @Override
    public void setCompassTarget(Location location) {
        this.original.setCompassTarget(((BukkitLocation)location).getOriginal());
    }

    @Override
    public long getPlayerTime() {
        return this.original.getPlayerTime();
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        this.original.setPlayerTime(time, relative);
    }

    @Override
    public void resetPlayerTime() {
        this.original.resetPlayerTime();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return this.original.isPlayerTimeRelative();
    }

    @Override
    public float getExperience() {
        return this.original.getExp();
    }

    @Override
    public void setExperience(float xp) {
        this.original.setExp(xp);
    }

    @Override
    public void addExperience(float xp) {
        this.original.setExp(this.original.getExp()+xp);
    }

    @Override
    public void removeExperience(float xp) {
        this.original.setExp(this.original.getExp()-xp);
    }

    @Override
    public int getLevel() {
        return this.original.getLevel();
    }

    @Override
    public void setLevel(int level) {
        this.original.setLevel(level);
    }

    @Override
    public void addLevel(int level) {
        this.original.setLevel(this.original.getLevel()+level);
    }

    @Override
    public void removeLevel(int level) {
        this.original.setLevel(this.original.getLevel()-level);
    }

    @Override
    public int getTotalExperience() {
        return this.original.getTotalExperience();
    }

    @Override
    public void setTotalExperience(int exp) {
        this.original.setTotalExperience(exp);
    }

    @Override
    public int getFoodLevel() {
        return this.original.getFoodLevel();
    }

    @Override
    public void setFoodLevel(int food) {
        this.original.setFoodLevel(food);
    }

    @Override
    public float getSaturation() {
        return this.original.getSaturation();
    }

    @Override
    public void setSaturation(float value) {
        this.original.setSaturation(value);
    }

    @Override
    public float getExhaustion() {
        return this.original.getExhaustion();
    }

    @Override
    public void setExhaustion(float value) {
        this.original.setExhaustion(value);
    }

    @Override
    public boolean isAllowFlight() {
        return this.original.getAllowFlight();
    }

    @Override
    public void setAllowFlight(boolean flight) {
        this.original.setAllowFlight(flight);
    }

    @Override
    public boolean isFlying() {
        return this.original.isFlying();
    }

    @Override
    public void setFlying(boolean value) {
        this.original.setFlying(value);
    }

    @Override
    public float getFlySpeed() {
        return this.original.getFlySpeed();
    }

    @Override
    public void setFlySpeed(float value) {
        this.original.setFlySpeed(value);
    }

    @Override
    public float getWalkSpeed() {
        return this.original.getWalkSpeed();
    }

    @Override
    public void setWalkSpeed(float value) {
        this.original.setWalkSpeed(value);
    }

    @Override
    public Entity getSpectatorTarget() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void setSpectatorTarget(Entity entity) {
        this.original.setSpectatorTarget(((BukkitEntity<?>)entity).getOriginal());
    }

    @Override
    public void playEffect(Location location, Effect effect, int data) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public AdvancementProgress getAdvancementProgress() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Inventory getEnderchestInventory() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Location getBedSpawnLocation() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void setBedSpawnLocation(Location location) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Internal
    public void setWorld(BukkitWorld world){
        this.world = world;
    }

    @Override
    public ChatChannel getPrimaryChatChannel() {
        return chatChannel;
    }

    @Override
    public void setPrimaryChatChannel(ChatChannel channel) {
        this.chatChannel = channel;
    }

    @Override
    public Sidebar getSidebar() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void setSidebar(Sidebar sidebar) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Tablist getTablist() {
        return tablist;
    }

    @Override
    public void setTablist(Tablist tablist) {
        if(this.tablist == tablist) return;
        if(this.tablist != null){
            ((BukkitTablist)this.tablist).detachReceiver(this);
        }
        this.tablistTeamNames.clear();
        if(tablist != null){
            this.tablist = tablist;
            ((BukkitTablist)this.tablist).attachReceiver(this);
        }
    }

    @Override
    public BelowNameInfo getBelowNameInfo() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void setBelowNameInfo(BelowNameInfo info) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<BossBar> getActiveBossBars() {
        return this.bossBars;
    }

    @Override
    public void addBossBar(BossBar bossBar) {
        if(!this.bossBars.contains(bossBar)){
            this.bossBars.add(bossBar);
            ((DefaultBossBar)bossBar).attachReceiver(this);
        }
    }

    @Override
    public void removeBossBar(BossBar bossBar) {
        if(this.bossBars.contains(bossBar)){
            this.bossBars.remove(bossBar);
            ((DefaultBossBar)bossBar).detachReceiver(this);
        }
    }

    @Override
    public void sendResourcePackRequest(String url) {
        sendResourcePackRequest(url,"");
    }

    @Override
    public void sendResourcePackRequest(String url, String hash) {
        MinecraftResourcePackSendPacket packet = new MinecraftResourcePackSendPacket();
        packet.setUrl(url);
        packet.setHash(hash);
        sendPacket(packet);
    }

    @Override
    public void requestTextInput(String label, String placeholder, Consumer<String> callback, PlayerTextInputValidator... validators) {
        requestObjectInput(label, placeholder, null, callback, validators);
    }

    @Override
    public void requestBooleanInput(String label, String placeholder, Consumer<Boolean> callback, PlayerTextInputValidator... validators) {
        requestObjectInput(label, placeholder, Convert::toBoolean, callback, appendArray(validators, PlayerTextInputValidator.BOOLEAN));
    }

    @Override
    public void requestNumberInput(String label, String placeholder, Consumer<Long> callback, PlayerTextInputValidator... validators) {
        requestObjectInput(label, placeholder, Convert::toLong, callback, appendArray(validators, PlayerTextInputValidator.NUMBER));
    }

    @Override
    public void requestDecimalInput(String label, String placeholder, Consumer<Double> callback, PlayerTextInputValidator... validators) {
        requestObjectInput(label, placeholder, Convert::toDouble, callback, appendArray(validators, PlayerTextInputValidator.DECIMAL));
    }

    @Override
    public void requestColorInput(String label, String placeholder, Consumer<TextColor> callback, PlayerTextInputValidator... validators) {
        requestObjectInput(label, placeholder, TextColor::make, callback, appendArray(validators, PlayerTextInputValidator.COLOR));
    }

    @Override
    public <T> void requestObjectInput(String label, String placeholder, Function<String, T> converter, Consumer<T> callback, PlayerTextInputValidator... validators) {
        requestObjectInput(label, placeholder, "", converter, callback, validators);
    }

    private <T> void requestObjectInput(String label, String placeholder, String defaultValue, Function<String, T> converter, Consumer<T> callback, PlayerTextInputValidator... validators) {
        PlayerTextInput<T> input = new PlayerTextInput<>(callback, converter, validators);

        if(isCustomClient(CustomClient.LABYMOD)) {
            LabyModClient client = getCustomClient(CustomClient.LABYMOD);
            client.sendInput(label, placeholder, defaultValue,getValidatorMaxLength(validators), value -> {
                MessageComponent<?> error = input.validate(value);
                if(error != null) {
                    sendMessage(error, VariableSet.create()
                            .addDescribed("player", this)
                            .add("value", validators));
                    requestObjectInput(label, placeholder, value, converter, callback, validators);
                    return;
                }
                input.callCallback(value);
            });
        } else {
            sendMessage(Text.parse(label));
            this.inputs.add(input);
        }
    }

    private int getValidatorMaxLength(PlayerTextInputValidator... validators) {
        for (PlayerTextInputValidator validator : validators) {
            if(validator instanceof MaxStringLengthPlayerTextInputValidator) {
                return ((MaxStringLengthPlayerTextInputValidator) validator).getMaxLength();
            }
        }
        return -1;
    }

    @Override
    public void requestConfirmInput(String s, Consumer<ConfirmResult> consumer) {

    }

    @Override
    public void requestYesNoInput(String label, Consumer<YesNoResult> callback) {

    }

    @Override
    public void requestOkInput(String s, Consumer<Boolean> consumer) {

    }

    @Override
    public void requestButtonInput(String s, String s1, Consumer<Boolean> consumer) {

    }

    @Override
    public void openInventory(Inventory inventory) {
        if(isJoining()) {
            Bukkit.getScheduler().runTask(McNativeLauncher.getPlugin(), () -> openInventory(inventory));
            return;
        }

        if(inventory instanceof PlayerRegisterAble) {
            Bukkit.getScheduler().runTask(McNativeLauncher.getPlugin(), ()-> {
                ((PlayerRegisterAble)inventory).registerPlayer(this);
            });
        } else {
            org.bukkit.inventory.Inventory bukkitInventory = ((BukkitInventory<?>)inventory).getOriginal();
            getOriginal().openInventory(bukkitInventory);
        }

    }

    @Internal
    public boolean isJoining() {
        return joining;
    }

    @Internal
    public void setJoining(boolean joining) {
        this.joining = joining;
    }

    @Internal
    public void handleLogout(){
        if(this.permissionHandler != null)this.permissionHandler.onPlayerLogout();
    }

    @Internal
    public Map<TablistEntry, String> getTablistTeamNames() {
        return tablistTeamNames;
    }

    @Internal
    public int getTablistTeamIndexAndIncrement(){
        return tablistTeamIndex++;
    }

    @Override
    public Position getPosition() {
        return getLocation();
    }

    @Internal
    public PlayerTextInput<?> getCurrentInput() {
        if(this.currentInput == null) {
            this.currentInput = this.inputs.poll();
        }
        return this.currentInput;
    }

    @Internal
    public void finishInput() {
        this.currentInput = null;
    }

    private static <T> T[] appendArray(T[] array, T element) {
        final int N = array.length;
        array = Arrays.copyOf(array, N + 1);
        array[N] = element;
        return array;
    }
}
