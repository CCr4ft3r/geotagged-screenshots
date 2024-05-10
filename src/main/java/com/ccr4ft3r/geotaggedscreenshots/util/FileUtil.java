package com.ccr4ft3r.geotaggedscreenshots.util;

import com.ccr4ft3r.geotaggedscreenshots.container.ImageType;
import com.ccr4ft3r.geotaggedscreenshots.container.ScreenshotMetadata;
import com.mojang.logging.LogUtils;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ccr4ft3r.geotaggedscreenshots.ModConstants.*;
import static javax.imageio.metadata.IIOMetadataFormatImpl.*;

public class FileUtil {

    public static final String PRAEFIX = "geotagged_screenshot_";
    private static final String ID = PRAEFIX + "id";
    private static final String X = PRAEFIX + "x";
    private static final String Y = PRAEFIX + "y";
    private static final String Z = PRAEFIX + "z";
    private static final String DIMENSION_ID = PRAEFIX + "dimensionId";
    private static final String WORLD_ID = PRAEFIX + "worldId";

    public static final String VALUE = "value";
    public static final String KEYWORD = "keyword";
    public static final String TEXT = "Text";
    public static final String TEXT_ENTRY = "TextEntry";

    public static ScreenshotMetadata getScreenshotMetadata(File file) {
        if (file.toString().endsWith("jpg"))
            return getScreenshotMetadataForJpg(file);
        return getScreenshotMetadataFromPng(file);
    }

    private static ScreenshotMetadata getScreenshotMetadataForJpg(File file) {
        try {
            String fileName = file.getName().substring(0, file.getName().length() - 3);
            File metadataFile = new File(METADATA_DIR, fileName + "ser");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadataFile));
            return (ScreenshotMetadata) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LogUtils.getLogger().error("Couldn't read metadata of {}", file, e);
            return null;
        }
    }

    @Nullable
    private static ScreenshotMetadata getScreenshotMetadataFromPng(File file) {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(file)) {
            ImageReader imageReader = ImageIO.getImageReaders(inputStream).next();
            imageReader.setInput(inputStream);
            IIOMetadata metadata = imageReader.getImageMetadata(0);
            String id = getAttribute(metadata, ID);
            String x = getAttribute(metadata, X);
            String y = getAttribute(metadata, Y);
            String z = getAttribute(metadata, Z);
            String dimensionId = getAttribute(metadata, DIMENSION_ID);
            String worldId = getAttribute(metadata, WORLD_ID);
            if (id == null || x == null || y == null || z == null || worldId == null || dimensionId == null) {
                LogUtils.getLogger().error("{} contains broken metadata: id: {}, x: {}, y: {}, z: {}, worldId: {}, dimensionId: {}", file, id, x, y, z, worldId, dimensionId);
                return null;
            }
            ScreenshotMetadata screenshotMetadata = new ScreenshotMetadata(file, UUID.fromString(id));
            screenshotMetadata.setDimensionId(dimensionId);
            screenshotMetadata.setCoordinates(new Vec3(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z)));
            screenshotMetadata.setWorldId(UUID.fromString(worldId));
            return screenshotMetadata;
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't read metadata of {}", file, e);
            return null;
        }
    }

    public static void saveMetadata(File file, ScreenshotMetadata screenshotMetadata) {
        if (file.toString().endsWith("jpg"))
            saveMetadataForJpg(file, screenshotMetadata);
        addMetadataToPng(file, screenshotMetadata);
    }

    private static void saveMetadataForJpg(File file, ScreenshotMetadata screenshotMetadata) {
        try {
            String fileName = file.getName().substring(0, file.getName().length() - 3);
            File metadataFile = new File(METADATA_DIR, fileName + "ser");
            if (!metadataFile.getParentFile().exists() && !metadataFile.getParentFile().mkdirs())
                throw new IOException("Couldn't create dir: " + metadataFile.getParentFile());
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(metadataFile));
            ois.writeObject(screenshotMetadata);
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't save metadata for {}", file, e);
        }
    }

    private static void addMetadataToPng(File file, ScreenshotMetadata screenshotMetadata) {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(file); ImageOutputStream outputStream = ImageIO.createImageOutputStream(file)) {
            ImageReader reader = ImageIO.getImageReaders(inputStream).next();
            reader.setInput(inputStream);
            IIOImage image = reader.readAll(0, null);
            addGeotaggedScreenshotMetadata(image, screenshotMetadata);

            ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(outputStream);
            writer.write(image);
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't add metadata for {}", file, e);
        }
    }

    public static File findFile(ScreenshotMetadata screenshotMetadata, ImageType imageType) {
        File[] files = imageType.getDir().listFiles();
        if (files == null)
            return null;
        for (File file : files) {
            ScreenshotMetadata metadata = FileUtil.getScreenshotMetadata(file);
            if (screenshotMetadata.equals(metadata))
                return file;
        }
        return null;
    }

    public static List<ScreenshotMetadata> getScreenshotMetadata(UUID worldId, ImageType imageType) {
        List<ScreenshotMetadata> result = new ArrayList<>();
        File[] files = imageType.getDir().listFiles();
        if (files == null)
            files = new File[]{};
        for (File file : files) {
            if (file.isDirectory() || !(file.toString().endsWith(".png") || file.toString().endsWith(".jpg")))
                continue;
            ScreenshotMetadata metadata = FileUtil.getScreenshotMetadata(file);
            if (metadata != null && worldId.equals(metadata.getWorldId()))
                result.add(metadata);
        }
        return result;
    }

    private static String getAttribute(final IIOMetadata metadata, final String name) {
        IIOMetadataNode standardNode = (IIOMetadataNode) metadata.getAsTree(standardMetadataFormatName);
        NodeList elements = standardNode.getElementsByTagName(TEXT_ENTRY);
        for (int i = 0; i < elements.getLength(); i++) {
            IIOMetadataNode node = (IIOMetadataNode) elements.item(i);
            if (node.getAttribute(KEYWORD).equals(name)) {
                return node.getAttribute(VALUE);
            }
        }
        return null;
    }

    private static void addGeotaggedScreenshotMetadata(IIOImage image, ScreenshotMetadata metadata) {
        appendEntry(image.getMetadata(), ID, metadata.getId());
        appendEntry(image.getMetadata(), X, metadata.getCoordinates().x());
        appendEntry(image.getMetadata(), Y, metadata.getCoordinates().y());
        appendEntry(image.getMetadata(), Z, metadata.getCoordinates().z());
        appendEntry(image.getMetadata(), WORLD_ID, metadata.getWorldId());
        appendEntry(image.getMetadata(), DIMENSION_ID, metadata.getDimensionId());
    }

    private static void appendEntry(final IIOMetadata metadata, final String key, final Object value) {
        IIOMetadataNode textEntryNode = new IIOMetadataNode(TEXT_ENTRY);
        textEntryNode.setAttribute(KEYWORD, key);
        textEntryNode.setAttribute(VALUE, value + "");
        IIOMetadataNode textNode = new IIOMetadataNode(TEXT);
        textNode.appendChild(textEntryNode);

        IIOMetadataNode root = new IIOMetadataNode(standardMetadataFormatName);
        root.appendChild(textNode);
        try {
            metadata.mergeTree(standardMetadataFormatName, root);
        } catch (IIOInvalidTreeException e) {
            LogUtils.getLogger().error("Couldn't merge standard metadata node with own", e);
        }
    }
}