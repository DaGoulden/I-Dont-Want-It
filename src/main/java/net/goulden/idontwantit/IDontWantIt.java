package net.goulden.idontwantit;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(IDontWantIt.MODID)
public class IDontWantIt {

    public static final String MODID = "idontwantit";

    public static final Logger LOGGER = LogUtils.getLogger();

    public IDontWantIt(IEventBus modEventBus) {
        LOGGER.info("Inicializando SelectivePickup Mod");

        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("SelectivePickup: Client setup complete");
    }
}