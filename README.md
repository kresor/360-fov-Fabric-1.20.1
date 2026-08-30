# 360 FOV Fabric 1.20.1 - Attempt 11

Experimental Fabric 1.20.1 backport with corrected ultrawide projection and a proper off-screen capture performance path.

Attempt 11 is based on the visually working Attempt 8 renderer. Unlike Attempt 10, it does not shrink the viewport inside the real window framebuffer. It renders cube faces into a dedicated square `SimpleFramebuffer`, then reprojects them into the real display framebuffer.

Recommended test:

- 5120x1440
- FOV 120
- shaders off
- `captureScale=0.75`
- `skipBackFace=true`

Config file after launch:

`config/fov360-1.20.1.properties`
