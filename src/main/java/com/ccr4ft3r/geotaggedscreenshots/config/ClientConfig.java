package com.ccr4ft3r.geotaggedscreenshots.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Data CONFIG_DATA = new Data(BUILDER);
    public static final ForgeConfigSpec CONFIG = BUILDER.build();

    public static class Data {

        public ForgeConfigSpec.BooleanValue useSeparateSetForGeotaggedScreenshots;
        public ForgeConfigSpec.ConfigValue<String> waypointSetName;
        public ForgeConfigSpec.BooleanValue displayThumbnailRegenerationInChat;
        public ForgeConfigSpec.BooleanValue hideUiAtTakingScreenshots;
        public ForgeConfigSpec.BooleanValue disableScreenshotChatMessage;
        public ForgeConfigSpec.BooleanValue useJpgForScreenshots;

        public Data(ForgeConfigSpec.Builder builder) {
            useSeparateSetForGeotaggedScreenshots = builder.comment("Determines whether geotagged screenshots should be added to a separate set of waypoints. " +
                    "If this option is disabled screenshots will be added to the default set.")
                .define("useSeparateSetForGeotaggedScreenshots", true);
            waypointSetName = builder.comment("Specifies the name of the waypoint set which geotagged screenshots should be added.")
                .define("waypointSetName", "Geotagged Screenshots");
            displayThumbnailRegenerationInChat = builder.comment("Determines whether to show a chat message for starting and completing the regeneration of thumbnails.")
                .define("displayThumbnailRegenerationInChat", true);
            hideUiAtTakingScreenshots = builder.comment("Determines whether to hide the whole ui at taking screenshots.")
                .define("hideUiAtTakingScreenshots", true);
            disableScreenshotChatMessage = builder.comment("Determines whether to disable the success message after taking a screenshot.")
                .define("disableScreenshotChatMessage", true);
            useJpgForScreenshots = builder.comment("Specifies whether to use jpg instead of png for screenshots. When enabled metadata is being saved in separates files (/screenshots-metadata/*). Screenshots and metadata will then be loaded/identified by their file names! (renaming them will annul their connection)")
                .define("useJpgForScreenshots", true);
        }
    }
}