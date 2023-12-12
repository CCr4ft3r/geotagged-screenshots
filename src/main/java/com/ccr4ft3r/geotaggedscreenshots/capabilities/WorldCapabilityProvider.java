package com.ccr4ft3r.geotaggedscreenshots.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static Capability<WorldCapability> WORLD_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private WorldCapability worldCapability = null;
    private final LazyOptional<WorldCapability> opt = LazyOptional.of(this::createCapability);

    private WorldCapability createCapability() {
        if (worldCapability == null) {
            worldCapability = new WorldCapability();
        }
        return worldCapability;
    }

    @Override
    public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == WORLD_CAP)
            return opt.cast();
        else
            return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createCapability().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createCapability().deserializeNBT(nbt);
    }
}