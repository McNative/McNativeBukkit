/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 09.03.20, 19:54
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

package org.mcnative.runtime.bukkit.serviceprovider.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.pretronic.libraries.utility.Validate;
import org.bukkit.OfflinePlayer;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;

import java.util.ArrayList;
import java.util.List;

public class VaultEconomyHook implements Economy {

    private final EconomyProvider economyProvider;

    public VaultEconomyHook(EconomyProvider economyProvider) {
        Validate.notNull(economyProvider);
        this.economyProvider = economyProvider;
    }

    public EconomyProvider getOriginal() {
        return this.economyProvider;
    }

    @Override
    public boolean isEnabled() {
        return this.economyProvider != null;
    }

    @Override
    public String getName() {
        return this.economyProvider.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return this.economyProvider.hasBankSupport();
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double value) {
        return this.economyProvider.formatBalance(value);
    }

    @Override
    public String currencyNamePlural() {
        return this.economyProvider.getCurrencyPluralName();
    }

    @Override
    public String currencyNameSingular() {
        return this.economyProvider.getCurrencySingularName();
    }

    @Override
    public boolean hasAccount(String playerName) {
        return this.economyProvider.hasAccount(McNative.getInstance().getPlayerManager().getPlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return this.economyProvider.hasAccount(McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId()));
    }

    @Override
    public boolean hasAccount(String playerName, String ignored) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String ignored) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public double getBalance(String playerName) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(playerName);
        if(player == null) return 0;
        return this.economyProvider.getPlayerBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId());
        if(player == null) return 0;
        return this.economyProvider.getPlayerBalance(player);
    }

    @Override
    public double getBalance(String playerName, String ignored) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String ignored) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String playerName, double amount) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(playerName);
        if(player == null) return false;
        return this.economyProvider.hasPlayerBalance(player, amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getName());
        if(player == null) return false;
        return this.economyProvider.hasPlayerBalance(McNative.getInstance().getPlayerManager()
                .getPlayer(offlinePlayer.getUniqueId()), amount);
    }

    @Override
    public boolean has(String playerName, String ignored, double amount) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String ignored, double amount) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(playerName);
        if(player == null) return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,playerName+" is not registered in McNative");
        org.mcnative.runtime.api.serviceprovider.economy.EconomyResponse response = this.economyProvider.withdrawPlayerBalance(player, amount);
        return mapEconomyResponse(response);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId());
        if(player == null) return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,offlinePlayer.getName()+" is not registered in McNative");
        org.mcnative.runtime.api.serviceprovider.economy.EconomyResponse response = this.economyProvider
                .withdrawPlayerBalance(player, amount);
        return mapEconomyResponse(response);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String worldName, double amount) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(playerName);
        if(player == null) return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,playerName+" is not registered in McNative");
        return mapEconomyResponse(this.economyProvider.depositPlayerBalance(player, amount));
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId());
        if(player == null) return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,offlinePlayer.getName()+" is not registered in McNative");
        return mapEconomyResponse(this.economyProvider.depositPlayerBalance(player, amount));
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String s1, double amount) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double amount) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public EconomyResponse createBank(String name, String playerName) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(name);
        if(player == null) return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,playerName+" is not registered in McNative");
        boolean success = this.economyProvider.createBank(name,player);
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer offlinePlayer) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId());
        if(player == null) return new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,offlinePlayer.getName()+" is not registered in McNative");
        boolean success = this.economyProvider.createBank(name, McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId()));
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        boolean success = this.economyProvider.deleteBank(name);
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        double balance = this.economyProvider.getBankBalance(name);
        return new EconomyResponse(balance, balance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        boolean success = this.economyProvider.hasBankBalance(name, amount);
        return new EconomyResponse(amount, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        org.mcnative.runtime.api.serviceprovider.economy.EconomyResponse response = this.economyProvider.withdrawBankBalance(name, amount);
        return mapEconomyResponse(response);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        org.mcnative.runtime.api.serviceprovider.economy.EconomyResponse response = this.economyProvider.depositBankBalance(name, amount);
        return mapEconomyResponse(response);
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        boolean success = this.economyProvider.isBankOwner(name, McNative.getInstance().getPlayerManager().getPlayer(playerName));
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer offlinePlayer) {
        boolean success = this.economyProvider.isBankOwner(name, McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId()));
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        boolean success = this.economyProvider.isBankMember(name, McNative.getInstance().getPlayerManager().getPlayer(playerName));
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer offlinePlayer) {
        boolean success = this.economyProvider.isBankMember(name, McNative.getInstance().getPlayerManager().getPlayer(offlinePlayer.getUniqueId()));
        return new EconomyResponse(0, 0, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "Failed");
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>(this.economyProvider.getBanks());
    }

    @Override
    public boolean createPlayerAccount(String name) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public boolean createPlayerAccount(String name, String s1) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        throw new UnsupportedOperationException("EconomyProvider does not support this");
    }

    private EconomyResponse mapEconomyResponse(org.mcnative.runtime.api.serviceprovider.economy.EconomyResponse response) {
        EconomyResponse.ResponseType type = response.isSuccess() ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE;
        return new EconomyResponse(response.getAmount(), response.getNewBalance(), type, response.getMessage());
    }
}
