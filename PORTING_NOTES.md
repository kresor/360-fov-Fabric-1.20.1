# Attempt 9 - first performance pass

This attempt keeps the working 1.20.1 backport and adds three performance-oriented changes:

1. **Reduced capture resolution**
   - Each cube face is now rendered into a centered square viewport sized from `captureScale`.
   - Default is `captureScale=0.75`, so a 5120x1440 setup captures faces at ~1080x1080 instead of using the full-width 5120x1440 viewport.

2. **Square capture projection during cube-face rendering**
   - `WindowMixin` temporarily reports square framebuffer dimensions during cube capture so Minecraft's projection matrix matches the square viewport.

3. **Optional back-face skipping**
   - `skipBackFace=true` by default.
   - The rear cube face is skipped when the projected FOV remains below the threshold (currently 165 degrees), which should include the user's tested 120 FOV ultrawide case.

## Config

File: `config/fov360-1.20.1.properties`

Relevant properties:
- `captureScale=0.75`
- `skipBackFace=true`

Suggested tests:
- Keep normal Minecraft FOV slider at **120**
- Test AOF7 / giant pack with default config
- If image quality remains good, try `captureScale=0.5` for more speed
- If artifacts appear near extreme edges, set `skipBackFace=false`
