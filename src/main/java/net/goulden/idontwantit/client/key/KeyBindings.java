package net.goulden.idontwantit.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY = "key.categories.idontwantit";
    public static final String KEY = "key.idontwantit.key";

    public static KeyMapping openMenuKey;

    public static void register(RegisterKeyMappingsEvent event) {
        openMenuKey = new KeyMapping(
                KEY,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                KEY_CATEGORY
        );

        event.register(openMenuKey);
    }
}
