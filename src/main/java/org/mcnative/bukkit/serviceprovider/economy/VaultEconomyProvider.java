/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 09.03.20, 19:52
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

package org.mcnative.bukkit.serviceprovider.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyResponse;

import java.util.Collection;

public class VaultEconomyProvider implements EconomyProvider {

    private final Economy economy;

    public VaultEconomyProvider(Economy economy) {
        this.economy = economy;
    }

    @Override
    public String getName() {
        return this.economy.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return this.economy.hasBankSupport();
    }

    @Override
    public String getCurrencyName() {
        return getCurrencySingularName();
    }

    @Override
    public String getCurrencySingularName() {
        return this.economy.currencyNameSingular();
    }

    @Override
    public String getCurrencyPluralName() {
        return this.economy.currencyNamePlural();
    }

    @Override
    public String formatBalance(double balance) {
        return this.economy.format(balance);
    }

    @Override
    public boolean hasAccount(MinecraftPlayer player) {
        return this.economy.hasAccount(Bukkit.getOfflinePlayer(player.getUniqueId()));
    }

    @Override
    public double getPlayerBalance(MinecraftPlayer player) {
        return this.economy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
    }

    @Override
    public boolean hasPlayerBalance(MinecraftPlayer player, double amount) {
        return getPlayerBalance(player) >= amount;
    }

    @Override
    public EconomyResponse setPlayerBalance(MinecraftPlayer player, double amount) {
        withdrawPlayerBalance(player, getPlayerBalance(player));
        return depositPlayerBalance(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayerBalance(MinecraftPlayer player, double amount) {
        double oldBalance = getPlayerBalance(player);
        net.milkbowl.vault.economy.EconomyResponse vaultResponse = this.economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
        return new EconomyResponse(vaultResponse.transactionSuccess(),
                vaultResponse.errorMessage, oldBalance, vaultResponse.amount);
    }

    @Override
    public EconomyResponse depositPlayerBalance(MinecraftPlayer player, double amount) {
        double oldBalance = getPlayerBalance(player);
        net.milkbowl.vault.economy.EconomyResponse vaultResponse = this.economy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
        return new EconomyResponse(vaultResponse.transactionSuccess(),
                vaultResponse.errorMessage, oldBalance, vaultResponse.amount);
    }

    @Override
    public Collection<String> getBanks() {
        return this.economy.getBanks();
    }

    @Override
    public boolean doesBankExist(String name) {
        return getBanks().contains(name);
    }

    @Override
    public boolean createBank(String name) {
        return this.economy.createBank(name, (OfflinePlayer) null).transactionSuccess();
    }

    @Override
    public boolean createBank(String name, MinecraftPlayer owner) {
        return this.economy.createBank(name, Bukkit.getOfflinePlayer(owner.getUniqueId())).transactionSuccess();
    }

    @Override
    public boolean deleteBank(String name) {
        return this.economy.deleteBank(name).transactionSuccess();
    }

    @Override
    public double getBankBalance(String name) {
        return this.economy.bankBalance(name).amount;
    }

    @Override
    public boolean hasBankBalance(String name, double amount) {
        return getBankBalance(name) >= amount;
    }

    @Override
    public EconomyResponse setBankBalance(String name, double amount) {
        withdrawBankBalance(name, getBankBalance(name));
        return depositBankBalance(name, amount);
    }

    @Override
    public EconomyResponse withdrawBankBalance(String name, double amount) {
        double oldBalance = getBankBalance(name);
        net.milkbowl.vault.economy.EconomyResponse vaultResponse = this.economy.bankWithdraw(name, amount);
        return new EconomyResponse(vaultResponse.transactionSuccess(),
                vaultResponse.errorMessage, oldBalance, vaultResponse.amount);
    }

    @Override
    public EconomyResponse depositBankBalance(String name, double amount) {
        double oldBalance = getBankBalance(name);
        net.milkbowl.vault.economy.EconomyResponse vaultResponse = this.economy.bankDeposit(name, amount);
        return new EconomyResponse(vaultResponse.transactionSuccess(),
                vaultResponse.errorMessage, oldBalance, vaultResponse.amount);
    }

    @Override
    public boolean isBankOwner(String name, MinecraftPlayer owner) {
        return this.economy.isBankOwner(name, Bukkit.getOfflinePlayer(owner.getUniqueId())).transactionSuccess();
    }

    @Override
    public boolean isBankMember(String name, MinecraftPlayer member) {
        return this.economy.isBankMember(name, Bukkit.getOfflinePlayer(member.getUniqueId())).transactionSuccess();
    }
}
