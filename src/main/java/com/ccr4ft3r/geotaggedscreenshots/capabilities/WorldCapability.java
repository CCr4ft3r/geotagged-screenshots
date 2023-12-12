package com.ccr4ft3r.geotaggedscreenshots.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class WorldCapability implements INBTSerializable<CompoundTag> {

    public static final String ID_KEY = "worldId";

    private String worldId = UUID.randomUUID().toString();

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(ID_KEY, StringTag.valueOf(worldId));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.worldId = tag.getString(ID_KEY);
    }

    public String getWorldId() {
        return worldId;
    }
}