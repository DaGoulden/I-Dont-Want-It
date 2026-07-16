package net.goulden.idontwantit.event;

import net.goulden.idontwantit.IDontWantIt;
import net.goulden.idontwantit.client.key.KeyBindings;
import net.goulden.idontwantit.client.screen.MenuScreen;
import net.goulden.idontwantit.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = IDontWantIt.MODID, value = Dist.CLIENT)
public class PickupEventHandler {
    private static int keyHeldTicks = 0;
    private static final int PickupAtTicks = 10;
    private static boolean overridePickupFilter = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            keyHeldTicks = 0;
            overridePickupFilter = false;
            return;
        }

        if (KeyBindings.openMenuKey.isDown()) {
            keyHeldTicks++;

            if (keyHeldTicks >= PickupAtTicks) {
                overridePickupFilter = true;
            }
        } else {
            if (keyHeldTicks > 0 && keyHeldTicks < PickupAtTicks) {
                mc.setScreen(new MenuScreen());
            }
            keyHeldTicks = 0;
            overridePickupFilter = false;
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null && !event.getPlayer().getUUID().equals(mc.player.getUUID())) return;

        if (overridePickupFilter) return;

        if (ProfileManager.isItemIgnored(event.getItemEntity().getItem().getItem())) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}