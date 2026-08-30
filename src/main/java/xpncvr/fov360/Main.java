package xpncvr.fov360;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fov360");

    @Override
    public void onInitializeClient() {
        Fov360Config.INSTANCE.load();
        LOGGER.info("360 FOV 1.20.1 experimental backport initialized (handCaptureFov={})",
            Fov360Config.INSTANCE.getHandCaptureFov());
    }
}
