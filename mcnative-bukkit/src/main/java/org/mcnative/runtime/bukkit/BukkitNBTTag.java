package org.mcnative.runtime.bukkit;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import org.mcnative.runtime.api.service.NBTTag;

public class BukkitNBTTag implements NBTTag {

    private final NBTCompound nbtCompound;

    public BukkitNBTTag(NBTCompound nbtCompound) {
        Validate.notNull(nbtCompound, "Error while creating BukkitNBTTag");
        this.nbtCompound = nbtCompound;
    }

    @Internal
    public NBTCompound getOriginal() {
        return nbtCompound;
    }

    @Override
    public byte getByte(String key) {
        return this.nbtCompound.getByte(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return this.nbtCompound.getBoolean(key);
    }

    @Override
    public short getShort(String key) {
        return this.nbtCompound.getShort(key);
    }

    @Override
    public int getInt(String key) {
        return this.nbtCompound.getInteger(key);
    }

    @Override
    public long getLong(String key) {
        return this.nbtCompound.getLong(key);
    }

    @Override
    public float getFloat(String key) {
        return this.nbtCompound.getFloat(key);
    }

    @Override
    public double getDouble(String key) {
        return this.nbtCompound.getDouble(key);
    }

    @Override
    public String getString(String key) {
        return this.nbtCompound.getString(key);
    }

    @Override
    public byte[] getByteArray(String key) {
        return this.nbtCompound.getByteArray(key);
    }

    @Override
    public int[] getIntArray(String key) {
        return this.nbtCompound.getIntArray(key);
    }

    @Override
    public boolean hasKey(String key) {
        return this.nbtCompound.hasKey(key);
    }

    @Override
    public void setByte(String key, byte value) {
        this.nbtCompound.setByte(key, value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        this.nbtCompound.setBoolean(key, value);
    }

    @Override
    public void setShort(String key, short value) {
        this.nbtCompound.setShort(key, value);
    }

    @Override
    public void setInt(String key, int value) {
        this.nbtCompound.setInteger(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        this.nbtCompound.setLong(key, value);
    }

    @Override
    public void setFloat(String key, float value) {
        this.nbtCompound.setFloat(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        this.nbtCompound.setDouble(key, value);
    }

    @Override
    public void setString(String key, String value) {
        this.nbtCompound.setString(key, value);
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        this.nbtCompound.setByteArray(key, value);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        this.nbtCompound.setIntArray(key, value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BukkitNBTTag && ((BukkitNBTTag)o).getOriginal().equals(getOriginal());
    }
}
