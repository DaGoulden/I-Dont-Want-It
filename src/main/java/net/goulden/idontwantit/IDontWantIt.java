package net.goulden.idontwantit;

import com.mojang.logging.LogUtils;
import net.goulden.idontwantit.config.ConfigBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

@Mod(IDontWantIt.MODID)
public class IDontWantIt {

    public static final String MODID = "idontwantit";

    public static final Logger LOGGER = LogUtils.getLogger();

    public IDontWantIt(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing I Don't Want It Mod");

        modEventBus.addListener(this::clientSetup);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigBuilder.CLIENT_SPEC);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("I Don't Want It: Client setup complete");
    }
}