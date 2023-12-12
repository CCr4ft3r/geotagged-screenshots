package com.ccr4ft3r.geotaggedscreenshots.container;

import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class ScreenshotMetadata {

    private final File file;
    private final UUID id;

    private Vec3 coordinates;

    private String dimensionId;

    private UUID worldId;

    public ScreenshotMetadata(File file, UUID id) {
        this.file = file;
        this.id = id;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public ScreenshotMetadata setWorldId(UUID worldId) {
        this.worldId = worldId;
        return this;
    }

    public Vec3 getCoordinates() {
        return coordinates;
    }

    public ScreenshotMetadata setCoordinates(Vec3 coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenshotMetadata metadata = (ScreenshotMetadata) o;
        return Objects.equals(id, metadata.id) && Objects.equals(coordinates, metadata.coordinates) && Objects.equals(worldId, metadata.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, coordinates, worldId);
    }

    public File getFile() {
        return file;
    }

    public String getDimensionId() {
        return dimensionId;
    }

    public ScreenshotMetadata setDimensionId(String dimensionId) {
        this.dimensionId = dimensionId;
        return this;
    }
}