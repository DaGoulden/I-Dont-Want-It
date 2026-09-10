package net.goulden.idontwantit.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigBuilder {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    public static class Client {

        // Colors
        protected final ModConfigSpec.ConfigValue<String> primaryColor;
        protected final ModConfigSpec.ConfigValue<String> secondaryColor;
        protected final ModConfigSpec.ConfigValue<String> tertiaryColor;
        protected final ModConfigSpec.ConfigValue<String> hoveredAdditiveColor;
        protected final ModConfigSpec.ConfigValue<String> linesColor;
        protected final ModConfigSpec.ConfigValue<String> goodMeaningColor;
        protected final ModConfigSpec.ConfigValue<String> badMeaningColor;

        // Pixels
        protected final ModConfigSpec.IntValue spacedX;
        protected final ModConfigSpec.IntValue spacedY;
        protected final ModConfigSpec.IntValue spacedText;
        protected final ModConfigSpec.IntValue spaceBetweenButtons;

        // Animations
        protected final ModConfigSpec.IntValue stateButtonDuration;
        protected final ModConfigSpec.IntValue deleteConfirmationDuration;
        protected final ModConfigSpec.IntValue editBoxConfirmationDuration;
        protected final ModConfigSpec.IntValue scrollbarAnimationDuration;
        protected final ModConfigSpec.IntValue openListAnimationDuration;

        Client(ModConfigSpec.Builder builder) {
            primaryColor = builder
                    .comment("Primary color (ARGB)")
                    .define("primaryColor", "99000000");
            secondaryColor = builder
                    .comment("Secondary color (ARGB)")
                    .define("secondaryColor", "55000000");
            tertiaryColor = builder
                    .comment("Tertiary color (ARGB)")
                    .define("tertiaryColor", "22000000");
            hoveredAdditiveColor = builder
                    .comment("Additive color when a button is hovered (ARGB)")
                    .define("hoveredAdditiveColor", "55000000");
            linesColor = builder
                    .comment("Text and simple lines color (RGB)")
                    .define("linesColor", "FFFFFFFF");
            goodMeaningColor = builder
                    .comment("Color that means something is active or right (ARGB)")
                    .define("goodMeaningColor", "FF00AA00");
            badMeaningColor = builder
                    .comment("Color that means something is unactive or wrong (ARGB)")
                    .define("badMeaningColor", "FFAA0000");

            spacedX = builder
                    .comment("Space between the borders of the screen and the menu in the X axis")
                    .defineInRange("spacedX", 200, 0, Integer.MAX_VALUE);
            spacedY = builder
                    .comment("Space between the borders of the screen and the menu in the Y axis")
                    .defineInRange("spacedY", 100, 0, Integer.MAX_VALUE);
            spacedText = builder
                    .comment("Space between the borders of the buttons and the text in it")
                    .defineInRange("spacedText", 3, 0, Integer.MAX_VALUE);
            spaceBetweenButtons = builder
                    .comment("Space between buttons")
                    .defineInRange("spaceBetweenButtons", 2, 0, Integer.MAX_VALUE);

            stateButtonDuration = builder
                    .comment("The duration of the state button animation")
                    .defineInRange("stateButtonDuration", 10, 0, 50);
            deleteConfirmationDuration = builder
                    .comment("The duration of the delete button confirmation")
                    .defineInRange("deleteConfirmationDuration", 25, 5, 100);
            editBoxConfirmationDuration = builder
                    .comment("The duration of the edit box error or success animation in the profile name change")
                    .defineInRange("editBoxConfirmationDuration", 20, 5, 80);
            scrollbarAnimationDuration = builder
                    .comment("The duration of the scrollbar appearing animation")
                    .defineInRange("scrollbarAnimationDuration", 10, 0, 50);
            openListAnimationDuration = builder
                    .comment("The duration of the animation when opening the list")
                    .defineInRange("openListAnimationDuration", 20, 0, 80);
        }
    }
}