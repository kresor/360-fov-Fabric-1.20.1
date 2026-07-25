package xpncvr.fov360;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fov360");

	@Override
	public void onInitialize() {
		LOGGER.info("360 FOV initialised");
	}
}
