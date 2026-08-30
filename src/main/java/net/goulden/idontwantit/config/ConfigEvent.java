package net.goulden.idontwantit.config;

import net.goulden.idontwantit.IDontWantIt;
import net.goulden.idontwantit.util.GUIVariables;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

import static net.goulden.idontwantit.config.ConfigBuilder.CLIENT;

@EventBusSubscriber(modid = IDontWantIt.MODID)
public class ConfigEvent {

    @SubscribeEvent
    public static void configLoading(ModConfigEvent.Loading event) {
        refreshConfig();
    }

    @SubscribeEvent
    public static void configReloading(ModConfigEvent.Reloading event) {
        refreshConfig();
    }

    private static void refreshConfig() {
        GUIVariables.primaryColor = (int) Long.parseLong(CLIENT.primaryColor.get(), 16);
        GUIVariables.secondaryColor = (int) Long.parseLong(CLIENT.secondaryColor.get(), 16);
        GUIVariables.tertiaryColor = (int) Long.parseLong(CLIENT.tertiaryColor.get(), 16);
        GUIVariables.hoveredAdditiveColor = (int) Long.parseLong(CLIENT.hoveredAdditiveColor.get(), 16);
        GUIVariables.linesColor = (int) Long.parseLong(CLIENT.linesColor.get(), 16);
        GUIVariables.goodMeaningColor = (int) Long.parseLong(CLIENT.goodMeaningColor.get(), 16);
        GUIVariables.badMeaningColor = (int) Long.parseLong(CLIENT.badMeaningColor.get(), 16);

        GUIVariables.spacedX = CLIENT.spacedX.get();
        GUIVariables.spacedY = CLIENT.spacedY.get();
        GUIVariables.spacedText = CLIENT.spacedText.get();
        GUIVariables.spaceBetweenButtons = CLIENT.spaceBetweenButtons.get();

        GUIVariables.stateButtonDuration = CLIENT.stateButtonDuration.get();
        GUIVariables.deleteConfirmationDuration = CLIENT.deleteConfirmationDuration.get();
        GUIVariables.editBoxConfirmationDuration = CLIENT.editBoxConfirmationDuration.get();
    }
}