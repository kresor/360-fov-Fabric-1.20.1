package xpncvr.fov360;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Fov360Config {
    public static final Fov360Config INSTANCE = new Fov360Config();

    private static final float DEFAULT_FOV = 120.0F;
    private float fov = DEFAULT_FOV;

    private Fov360Config() {
    }

    public void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("fov360-1.20.1.properties");
        Properties properties = new Properties();

        if (Files.isRegularFile(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
                fov = clamp(Float.parseFloat(properties.getProperty("fov", Float.toString(DEFAULT_FOV))), 90.0F, 360.0F);
            } catch (Exception e) {
                Main.LOGGER.warn("Could not read {}, using FOV {}", path, DEFAULT_FOV, e);
                fov = DEFAULT_FOV;
            }
        }

        properties.setProperty("fov", Float.toString(fov));
        properties.setProperty("note", "Raw 360-FOV slider equivalent. 120 is the tested 5120x1440 starting point.");
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "360 FOV Fabric 1.20.1 experimental backport");
            }
        } catch (IOException e) {
            Main.LOGGER.warn("Could not write {}", path, e);
        }
    }

    public float getFov() {
        return fov;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
