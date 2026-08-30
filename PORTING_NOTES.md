# Attempt 11 - proper off-screen capture optimization

Attempt 10 proved that shrinking the viewport inside the real 5120x1440 window framebuffer is not safe: the screenshot showed the cube faces themselves as warped panels.

Attempt 11 returns to the known-good Attempt 8 projection path and changes only how the cube faces are rendered.

## Architecture

- The real window framebuffer remains untouched while cube faces are captured.
- A reusable `SimpleFramebuffer` is created at a square resolution.
- During capture, `MinecraftClient.getFramebuffer()` is temporarily redirected to that square framebuffer.
- `Window.getFramebufferWidth/Height()` also report the square capture size while rendering a cube face, so Minecraft builds a true square 90-degree projection.
- Each completed face is copied into the six face textures.
- The main 5120x1440 framebuffer is rebound only for the final reprojection pass.

## Performance settings

`config/fov360-1.20.1.properties`

- `captureScale=0.75` (default)
- `skipBackFace=true` (default)

At 5120x1440, captureScale 0.75 means approximately 1080x1080 per face instead of rendering each pass at the full 5120x1440 window resolution.

At the tested FOV 120, the rear cube face is skipped because the remapped projected FOV remains below the current 165 degree safety threshold.

## Test order

1. 5120x1440
2. FOV 120
3. shaders off
4. captureScale=0.75
5. skipBackFace=true

If visuals are correct, compare FPS to Attempt 8. Then optionally test captureScale=0.5.
