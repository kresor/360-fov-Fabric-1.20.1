package xpncvr.fov360;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import xpncvr.fov360.mixin.GameRendererInvoker;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Deliberately small 1.20.1 renderer based on the older Flex-FOV architecture:
 * render six 90 degree cube faces, copy the centre square of the vanilla
 * framebuffer, then reproject those faces with the 360-FOV projection curve.
 *
 * Attempt 12 preserves the proven full-quality Attempt 8 capture path and adds
 * dynamic cubemap face culling so invisible directions are not rendered.
 */
public final class Fov360Renderer {
    public static final Fov360Renderer INSTANCE = new Fov360Renderer();

    public static volatile boolean CAPTURING = false;

    private final int[] faceTextures = new int[6];
    private int faceSize = -1;

    private int program;
    private int vao;
    private int vbo;
    private boolean glReady;
    private boolean failed;

    private int uFovx;
    private int uAspect;
    private int uPitch;
    private int uScales;
    private int uScales2;

    private Fov360Renderer() {
    }

    public boolean shouldRun(MinecraftClient client) {
        return !failed && client != null && client.world != null && client.player != null && client.getCameraEntity() != null;
    }

    public void renderFrame(GameRenderer gameRenderer, float tickDelta, long limitTime, MatrixStack matrices, boolean renderHand) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!shouldRun(client)) {
            gameRenderer.renderWorld(tickDelta, limitTime, matrices);
            return;
        }

        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) {
            gameRenderer.renderWorld(tickDelta, limitTime, matrices);
            return;
        }

        float savedYaw = cameraEntity.getYaw();
        float savedPitch = cameraEntity.getPitch();
        float savedPrevYaw = cameraEntity.prevYaw;
        float savedPrevPitch = cameraEntity.prevPitch;

        float viewYaw = lerpAngle(tickDelta, savedPrevYaw, savedYaw);
        float viewPitch = lerp(tickDelta, savedPrevPitch, savedPitch);

        try {
            Framebuffer framebuffer = client.getFramebuffer();
            int size = Math.max(64, Math.min(framebuffer.textureWidth, framebuffer.textureHeight));
            ensureGl(size);

            float aspect = framebuffer.textureWidth / (float) framebuffer.textureHeight;
            float rawFov = Math.max(30.0F, Math.min(400.0F, client.options.getFov().getValue()));
            float projectedFov = remapBoundaryFovx(rawFov, aspect);
            boolean[] visibleFaces = determineVisibleFaces(projectedFov, aspect, viewPitch);

            CAPTURING = true;

            // Side/back/up/down cube captures must never contain the player's
            // first-person hand. Only the front capture gets it.
            gameRenderer.setRenderHand(false);
            for (int face = 1; face < 6; face++) {
                if (!visibleFaces[face]) {
                    continue;
                }
                setFace(cameraEntity, viewYaw, face);
                gameRenderer.renderWorld(tickDelta, limitTime, new MatrixStack());
                copyFace(framebuffer, face);
            }

            // Front face is world-only as well. Rendering the first-person
            // hand inside the 90 degree front cubemap face clips it at that
            // face boundary on 32:9. We composite the hand once, after the
            // world has been reprojected to the final screen.
            gameRenderer.setRenderHand(false);
            setFace(cameraEntity, viewYaw, 0);
            gameRenderer.renderWorld(tickDelta, limitTime, matrices);
            copyFace(framebuffer, 0);

            // Restore player/camera entity before final reprojection/overlay.
            restoreEntity(cameraEntity, savedYaw, savedPitch, savedPrevYaw, savedPrevPitch);
            CAPTURING = false;

            reproject(framebuffer, projectedFov, aspect, viewPitch);

            // Render the hand/held item once in normal screen space after the
            // panoramic world pass. This keeps the hand sharp and prevents it
            // from being chopped by cubemap face edges. Vanilla renderHand
            // also owns the normal first-person transforms for maps/items.
            gameRenderer.setRenderHand(renderHand);
            if (renderHand) {
                ((GameRendererInvoker) gameRenderer).fov360$renderHand(new MatrixStack(), gameRenderer.getCamera(), tickDelta);
            }
        } catch (Throwable t) {
            failed = true;
            Main.LOGGER.error("360 FOV 1.20.1 renderer failed; falling back to vanilla rendering on later frames", t);
            restoreEntity(cameraEntity, savedYaw, savedPitch, savedPrevYaw, savedPrevPitch);
            gameRenderer.setRenderHand(renderHand);
            CAPTURING = false;
        }
    }

    private void setFace(Entity entity, float baseYaw, int face) {
        float yaw = baseYaw;
        float pitch = 0.0F;
        switch (face) {
            case 1 -> yaw = baseYaw + 90.0F;   // right
            case 2 -> yaw = baseYaw + 180.0F;  // back
            case 3 -> yaw = baseYaw - 90.0F;   // left
            case 4 -> pitch = -90.0F;          // up
            case 5 -> pitch = 90.0F;           // down
            default -> { }
        }
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.prevYaw = yaw;
        entity.prevPitch = pitch;
    }

    private void restoreEntity(Entity entity, float yaw, float pitch, float prevYaw, float prevPitch) {
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.prevYaw = prevYaw;
        entity.prevPitch = prevPitch;
    }

    private void ensureGl(int requestedFaceSize) {
        if (!glReady) {
            initProgramAndQuad();
            for (int i = 0; i < faceTextures.length; i++) {
                faceTextures[i] = GL11.glGenTextures();
            }
            glReady = true;
        }
        if (faceSize == requestedFaceSize) {
            return;
        }
        faceSize = requestedFaceSize;
        for (int texture : faceTextures) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, faceSize, faceSize, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    private void copyFace(Framebuffer framebuffer, int face) {
        framebuffer.beginWrite(false);
        int x = Math.max(0, (framebuffer.textureWidth - faceSize) / 2);
        int y = Math.max(0, (framebuffer.textureHeight - faceSize) / 2);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, faceTextures[face]);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, x, y, faceSize, faceSize);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    private void reproject(Framebuffer framebuffer, float fovx, float aspect, float pitchDegrees) {
        framebuffer.beginWrite(true);

        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int oldProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int oldVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int oldActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL20.glUseProgram(program);
        GL30.glBindVertexArray(vao);

        for (int i = 0; i < 6; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, faceTextures[i]);
        }

        double half = Math.toRadians(fovx) * 0.5;
        float scaleStd = (float) Math.tan(half);
        float scalePan = (float) ((2.0 / (1.0 + Math.cos(half))) * Math.sin(half));
        float scaleSte = (float) Math.tan(half * 0.5);
        float scaleMer = (float) half;
        float scaleEqu = (float) half;
        float scaleFish = (float) half;

        GL20.glUniform1f(uFovx, fovx);
        GL20.glUniform1f(uAspect, aspect);
        GL20.glUniform1f(uPitch, pitchDegrees);
        GL20.glUniform4f(uScales, scaleStd, scalePan, scaleSte, scaleMer);
        GL20.glUniform2f(uScales2, scaleEqu, scaleFish);

        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);

        for (int i = 0; i < 6; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL13.glActiveTexture(oldActiveTexture);
        GL30.glBindVertexArray(oldVao);
        GL20.glUseProgram(oldProgram);

        if (depth) GL11.glEnable(GL11.GL_DEPTH_TEST); else GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (blend) GL11.glEnable(GL11.GL_BLEND); else GL11.glDisable(GL11.GL_BLEND);
        if (cull) GL11.glEnable(GL11.GL_CULL_FACE); else GL11.glDisable(GL11.GL_CULL_FACE);
    }


    /**
     * Determine which cubemap faces can actually be sampled by the final
     * projection. This keeps Attempt 8's full-resolution capture quality but
     * avoids rendering faces that are invisible at the current FOV/pitch.
     *
     * A modest screen-space grid is intentionally conservative. We also render
     * the front face unconditionally because it contains the first-person hand
     * and is the dominant face for normal play.
     */
    private boolean[] determineVisibleFaces(float fovx, float aspect, float pitchDegrees) {
        boolean[] visible = new boolean[6];
        visible[0] = true;

        final int samplesX = 33;
        final int samplesY = 17;
        for (int iy = 0; iy < samplesY; iy++) {
            float sy = -1.0F + 2.0F * iy / (samplesY - 1.0F);
            float y = sy / aspect;
            for (int ix = 0; ix < samplesX; ix++) {
                float x = -1.0F + 2.0F * ix / (samplesX - 1.0F);
                Vec3 ray = screenToRayCpu(x, y, fovx, pitchDegrees);
                visible[classifyCubeFace(ray)] = true;
            }
        }

        return visible;
    }

    private Vec3 screenToRayCpu(float x, float y, float fovx, float pitchDegrees) {
        double half = Math.toRadians(fovx) * 0.5;
        double scaleStd = Math.tan(half);
        double scalePan = (2.0 / (1.0 + Math.cos(half))) * Math.sin(half);
        double scaleSte = Math.tan(half * 0.5);
        double scaleMer = half;
        double scaleEqu = half;
        double scaleFish = half;

        Vec3 ray;
        if (fovx < 90.0F) {
            ray = standardRayCpu(x, y, scaleStd);
        } else if (fovx < 160.0F) {
            double linear = (fovx - 90.0) / 70.0;
            double parabola = 1.0 - (linear - 1.0) * (linear - 1.0);
            Vec3 standard = standardRayCpu(x, y, scaleStd);
            Vec3 hybrid = hybridStereoRayCpu(x, y, scalePan, scaleSte, pitchDegrees);
            ray = mix(standard, hybrid, parabola);
        } else if (fovx < 220.0F) {
            double linear = (fovx - 160.0) / 60.0;
            double parabola = 1.0 - (linear - 1.0) * (linear - 1.0);
            Vec3 hybrid = hybridStereoRayCpu(x, y, scalePan, scaleSte, pitchDegrees);
            Vec3 fish = fisheyeRayCpu(x, y, scaleFish);
            ray = mix(hybrid, fish, parabola);
        } else if (fovx < 300.0F) {
            double linear = (fovx - 220.0) / 80.0;
            double parabola = 1.0 - (linear - 1.0) * (linear - 1.0);
            Vec3 fish = fisheyeRayCpu(x, y, scaleFish);
            Vec3 merc = mercatorRayCpu(x, y, scaleMer);
            ray = mix(fish, merc, parabola);
        } else if (fovx < 340.0F) {
            ray = mercatorRayCpu(x, y, scaleMer);
        } else if (fovx < 360.0F) {
            Vec3 merc = mercatorRayCpu(x, y, scaleMer);
            Vec3 equ = equirectRayCpu(x, y, scaleEqu);
            ray = mix(merc, equ, (fovx - 340.0) / 20.0);
        } else {
            ray = equirectRayCpu(x, y, scaleEqu);
        }

        double p = Math.toRadians(pitchDegrees);
        double cp = Math.cos(p);
        double sp = Math.sin(p);
        return normalize(new Vec3(ray.x, ray.y * cp - ray.z * sp, ray.y * sp + ray.z * cp));
    }

    private Vec3 standardRayCpu(double x, double y, double scale) {
        double px = x * scale;
        double py = y * scale;
        double r = Math.hypot(px, py);
        if (r < 1.0e-9) return new Vec3(0.0, 0.0, 1.0);
        double theta = Math.atan(r);
        double ss = Math.sin(theta) / r;
        return new Vec3(px * ss, py * ss, Math.cos(theta));
    }

    private Vec3 paniniRayCpu(double x, double y, double scale) {
        double px = x * scale;
        double py = y * scale;
        double d = 1.0;
        double k = px * px / ((d + 1.0) * (d + 1.0));
        double disc = Math.max(0.0, k * k * d * d - (k + 1.0) * (k * d * d - 1.0));
        double clon = (-k * d + Math.sqrt(disc)) / (k + 1.0);
        double ss = (d + 1.0) / (d + clon);
        double lon = Math.atan2(px, ss * clon);
        double lat = Math.atan2(py, ss);
        return latlonToRayCpu(lat, lon);
    }

    private Vec3 stereographicRayCpu(double x, double y, double scale) {
        double px = x * scale;
        double py = y * scale;
        double r = Math.hypot(px, py);
        if (r < 1.0e-9) return new Vec3(0.0, 0.0, 1.0);
        double theta = Math.atan(r) / 0.5;
        double ss = Math.sin(theta) / r;
        return new Vec3(px * ss, py * ss, Math.cos(theta));
    }

    private Vec3 hybridStereoRayCpu(double x, double y, double panScale, double stereoScale, double pitchDegrees) {
        double t = Math.min(1.0, Math.abs(pitchDegrees) / 90.0);
        return mix(paniniRayCpu(x, y, panScale), stereographicRayCpu(x, y, stereoScale), t);
    }

    private Vec3 fisheyeRayCpu(double x, double y, double scale) {
        double px = x * scale;
        double py = y * scale;
        double r = Math.hypot(px, py);
        if (r < 1.0e-9) return new Vec3(0.0, 0.0, 1.0);
        double ss = Math.sin(r) / r;
        return new Vec3(px * ss, py * ss, Math.cos(r));
    }

    private Vec3 mercatorRayCpu(double x, double y, double scale) {
        double px = x * scale;
        double py = y * scale;
        return latlonToRayCpu(Math.atan(Math.sinh(py)), px);
    }

    private Vec3 equirectRayCpu(double x, double y, double scale) {
        double px = x * scale;
        double py = y * scale;
        if (Math.abs(py) > Math.PI * 0.5) return new Vec3(0.0, 0.0, 0.0);
        return latlonToRayCpu(py, px);
    }

    private Vec3 latlonToRayCpu(double lat, double lon) {
        return new Vec3(Math.sin(lon) * Math.cos(lat), Math.sin(lat), Math.cos(lon) * Math.cos(lat));
    }

    private int classifyCubeFace(Vec3 r) {
        double ax = Math.abs(r.x);
        double ay = Math.abs(r.y);
        double az = Math.abs(r.z);
        if (az >= ax && az >= ay) return r.z >= 0.0 ? 0 : 2;
        if (ax >= ay) return r.x >= 0.0 ? 1 : 3;
        return r.y >= 0.0 ? 4 : 5;
    }

    private Vec3 mix(Vec3 a, Vec3 b, double t) {
        return new Vec3(a.x * (1.0 - t) + b.x * t,
            a.y * (1.0 - t) + b.y * t,
            a.z * (1.0 - t) + b.z * t);
    }

    private Vec3 normalize(Vec3 v) {
        double length = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        if (length < 1.0e-12) return new Vec3(0.0, 0.0, 1.0);
        return new Vec3(v.x / length, v.y / length, v.z / length);
    }

    private static final class Vec3 {
        final double x;
        final double y;
        final double z;

        Vec3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private void initProgramAndQuad() {
        int vertex = compile(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        GL20.glBindAttribLocation(program, 0, "Position");
        GL20.glBindAttribLocation(program, 1, "UV");
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("360 FOV shader link failed: " + GL20.glGetProgramInfoLog(program));
        }
        GL20.glDetachShader(program, vertex);
        GL20.glDetachShader(program, fragment);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);

        GL20.glUseProgram(program);
        for (int i = 0; i < 6; i++) {
            int location = GL20.glGetUniformLocation(program, "Face" + i + "Sampler");
            GL20.glUniform1i(location, i);
        }
        uFovx = GL20.glGetUniformLocation(program, "fovx");
        uAspect = GL20.glGetUniformLocation(program, "aspect");
        uPitch = GL20.glGetUniformLocation(program, "pitchDegrees");
        uScales = GL20.glGetUniformLocation(program, "Scales");
        uScales2 = GL20.glGetUniformLocation(program, "Scales2");
        GL20.glUseProgram(0);

        float[] vertices = {
            -1.0F, -1.0F, 0.0F, 0.0F,
             1.0F, -1.0F, 1.0F, 0.0F,
            -1.0F,  1.0F, 0.0F, 1.0F,
             1.0F,  1.0F, 1.0F, 1.0F
        };
        FloatBuffer data = BufferUtils.createFloatBuffer(vertices.length);
        data.put(vertices).flip();

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        int stride = 4 * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("360 FOV shader compile failed: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static float remapBoundaryFovx(float rawFov, float aspect) {
        if (rawFov >= 180.0F) {
            return rawFov;
        }
        float boundaryHorizontal = (float) Math.toDegrees(2.0 * Math.atan(aspect));
        float offset = boundaryHorizontal - 90.0F;
        float falloff = (180.0F - rawFov) / 90.0F;
        return rawFov + offset * falloff;
    }

    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    private static float lerpAngle(float delta, float start, float end) {
        float d = (end - start) % 360.0F;
        if (d < -180.0F) d += 360.0F;
        if (d >= 180.0F) d -= 360.0F;
        return start + delta * d;
    }

    private static final String VERTEX_SHADER = """
        #version 150
        in vec2 Position;
        in vec2 UV;
        out vec2 texCoord;
        void main() {
            texCoord = UV;
            gl_Position = vec4(Position, 0.0, 1.0);
        }
        """;

    private static final String FRAGMENT_SHADER = """
        #version 150
        #define M_PI 3.14159265358979323846

        uniform sampler2D Face0Sampler;
        uniform sampler2D Face1Sampler;
        uniform sampler2D Face2Sampler;
        uniform sampler2D Face3Sampler;
        uniform sampler2D Face4Sampler;
        uniform sampler2D Face5Sampler;
        uniform float fovx;
        uniform float aspect;
        uniform float pitchDegrees;
        uniform vec4 Scales;
        uniform vec2 Scales2;

        in vec2 texCoord;
        out vec4 fragColor;

        vec3 safeStandardInverse(vec2 lenscoord) {
            float r = length(lenscoord);
            if (r < 1.0e-7) return vec3(0.0, 0.0, 1.0);
            float theta = atan(r);
            float s = sin(theta) / r;
            return vec3(lenscoord.x * s, lenscoord.y * s, cos(theta));
        }

        vec3 standardRay(vec2 c) {
            return safeStandardInverse(c * Scales.x);
        }

        vec3 latlonToRay(float lat, float lon) {
            return vec3(sin(lon) * cos(lat), sin(lat), cos(lon) * cos(lat));
        }

        vec3 paniniRay(vec2 c) {
            vec2 p = c * Scales.y;
            float x = p.x;
            float y = p.y;
            float d = 1.0;
            float k = x*x / ((d+1.0)*(d+1.0));
            float dscr = max(0.0, k*k*d*d - (k+1.0)*(k*d*d-1.0));
            float clon = (-k*d + sqrt(dscr)) / (k+1.0);
            float S = (d+1.0) / (d+clon);
            float lon = atan(x, S*clon);
            float lat = atan(y, S);
            return latlonToRay(lat, lon);
        }

        vec3 stereographicRay(vec2 c) {
            vec2 p = c * Scales.z;
            float r = length(p);
            if (r < 1.0e-7) return vec3(0.0, 0.0, 1.0);
            float theta = atan(r) / 0.5;
            float s = sin(theta) / r;
            return vec3(p.x * s, p.y * s, cos(theta));
        }

        vec3 hybridStereoRay(vec2 c) {
            return mix(paniniRay(c), stereographicRay(c), clamp(abs(pitchDegrees) / 90.0, 0.0, 1.0));
        }

        vec3 fisheyeRay(vec2 c) {
            vec2 p = c * Scales2.y;
            float r = length(p);
            if (r < 1.0e-7) return vec3(0.0, 0.0, 1.0);
            float s = sin(r) / r;
            return vec3(p.x * s, p.y * s, cos(r));
        }

        vec3 mercatorRay(vec2 c) {
            vec2 p = c * vec2(Scales.w);
            return latlonToRay(atan(sinh(p.y)), p.x);
        }

        vec3 equirectRay(vec2 c) {
            vec2 p = c * vec2(Scales2.x);
            if (abs(p.y) > M_PI * 0.5) return vec3(0.0);
            return latlonToRay(p.y, p.x);
        }

        vec3 screenToRay(vec2 c) {
            vec3 ray;
            if (fovx < 90.0) {
                ray = standardRay(c);
            } else if (fovx < 160.0) {
                float linear = (fovx - 90.0) / 70.0;
                float parabola = 1.0 - (linear - 1.0) * (linear - 1.0);
                ray = mix(standardRay(c), hybridStereoRay(c), parabola);
            } else if (fovx < 220.0) {
                float linear = (fovx - 160.0) / 60.0;
                float parabola = 1.0 - (linear - 1.0) * (linear - 1.0);
                ray = mix(hybridStereoRay(c), fisheyeRay(c), parabola);
            } else if (fovx < 300.0) {
                float linear = (fovx - 220.0) / 80.0;
                float parabola = 1.0 - (linear - 1.0) * (linear - 1.0);
                ray = mix(fisheyeRay(c), mercatorRay(c), parabola);
            } else if (fovx < 340.0) {
                ray = mercatorRay(c);
            } else if (fovx < 360.0) {
                ray = mix(mercatorRay(c), equirectRay(c), (fovx - 340.0) / 20.0);
            } else {
                ray = equirectRay(c);
            }

            // Cube captures are horizon-aligned. Apply Minecraft pitch here:
            // positive pitch looks down.
            float p = radians(pitchDegrees);
            float cp = cos(p);
            float sp = sin(p);
            return vec3(ray.x, ray.y * cp - ray.z * sp, ray.y * sp + ray.z * cp);
        }

        vec4 sampleFace(int face, vec2 uv) {
            uv = clamp(uv, 0.0, 1.0);
            if (face == 0) return texture(Face0Sampler, uv);
            if (face == 1) return texture(Face1Sampler, uv);
            if (face == 2) return texture(Face2Sampler, uv);
            if (face == 3) return texture(Face3Sampler, uv);
            if (face == 4) return texture(Face4Sampler, uv);
            return texture(Face5Sampler, uv);
        }

        vec4 rayToColor(vec3 r) {
            vec3 a = abs(r);
            int face;
            vec2 uv;

            if (a.z >= a.x && a.z >= a.y) {
                if (r.z >= 0.0) {
                    face = 0;
                    uv = vec2(0.5 + r.x/(2.0*r.z), 0.5 + r.y/(2.0*r.z));
                } else {
                    face = 2;
                    float d = -r.z;
                    uv = vec2(0.5 - r.x/(2.0*d), 0.5 + r.y/(2.0*d));
                }
            } else if (a.x >= a.y) {
                if (r.x >= 0.0) {
                    face = 1;
                    uv = vec2(0.5 - r.z/(2.0*r.x), 0.5 + r.y/(2.0*r.x));
                } else {
                    face = 3;
                    float d = -r.x;
                    uv = vec2(0.5 + r.z/(2.0*d), 0.5 + r.y/(2.0*d));
                }
            } else {
                if (r.y >= 0.0) {
                    face = 4;
                    uv = vec2(0.5 + r.x/(2.0*r.y), 0.5 - r.z/(2.0*r.y));
                } else {
                    face = 5;
                    float d = -r.y;
                    uv = vec2(0.5 + r.x/(2.0*d), 0.5 + r.z/(2.0*d));
                }
            }
            return sampleFace(face, uv);
        }

        void main() {
            vec2 screen = (texCoord - vec2(0.5)) * vec2(2.0, 2.0 / aspect);
            vec3 ray = screenToRay(screen);
            if (length(ray) < 1.0e-7) {
                fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            } else {
                fragColor = rayToColor(normalize(ray));
            }
        }
        """;
}
