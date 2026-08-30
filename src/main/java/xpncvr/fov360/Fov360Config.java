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

    private static final float DEFAULT_CAPTURE_SCALE = 0.75F;
    private static final boolean DEFAULT_SKIP_BACK_FACE = true;

    private float captureScale = DEFAULT_CAPTURE_SCALE;
    private boolean skipBackFace = DEFAULT_SKIP_BACK_FACE;

    private Fov360Config() {
    }

    public void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("fov360-1.20.1.properties");
        Properties properties = new Properties();

        if (Files.isRegularFile(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
                captureScale = clamp(Float.parseFloat(properties.getProperty("captureScale", Float.toString(DEFAULT_CAPTURE_SCALE))), 0.25F, 1.0F);
                skipBackFace = Boolean.parseBoolean(properties.getProperty("skipBackFace", Boolean.toString(DEFAULT_SKIP_BACK_FACE)));
            } catch (Exception e) {
                Main.LOGGER.warn("Could not read {}, using default 360-FOV performance settings", path, e);
                captureScale = DEFAULT_CAPTURE_SCALE;
                skipBackFace = DEFAULT_SKIP_BACK_FACE;
            }
        }

        properties.setProperty("note", "Set your normal Minecraft FOV slider to 120 for the tested 5120x1440 baseline. These values control capture performance.");
        properties.setProperty("captureScale", Float.toString(captureScale));
        properties.setProperty("skipBackFace", Boolean.toString(skipBackFace));
        properties.setProperty("captureScale.comment", "0.75 is the default balanced mode. Lower is faster, higher is sharper.");
        properties.setProperty("skipBackFace.comment", "true skips the rear cube face when projected FOV stays below the threshold.");
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "360 FOV Fabric 1.20.1 experimental backport");
            }
        } catch (IOException e) {
            Main.LOGGER.warn("Could not write {}", path, e);
        }
    }

    public float getCaptureScale() {
        return captureScale;
    }

    public boolean isSkipBackFace() {
        return skipBackFace;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
