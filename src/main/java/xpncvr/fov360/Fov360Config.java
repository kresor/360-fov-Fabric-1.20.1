package xpncvr.fov360;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Fov360Config {
	public boolean splitScreen = false;

	public boolean invertSplitScreen = false;

	public int faceSizeCap = 2048;

	public boolean lowResTopBottomFaces = false;

	public int antialiasSamples = 4;

	public static Fov360Config load() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Path path = FabricLoader.getInstance().getConfigDir().resolve("fov360.json");
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				Fov360Config config = gson.fromJson(reader, Fov360Config.class);
				if (config != null) {
					return config;
				}
				Main.LOGGER.warn("Empty config at {}; using defaults", path);
			} catch (IOException | RuntimeException e) {
				Main.LOGGER.warn("Failed to read config at {}; using defaults", path, e);
			}
			return new Fov360Config();
		}
		Fov360Config config = new Fov360Config();
		try (Writer writer = Files.newBufferedWriter(path)) {
			gson.toJson(config, writer);
		} catch (IOException e) {
			Main.LOGGER.warn("Failed to write default config to {}", path, e);
		}
		return config;
	}
}
