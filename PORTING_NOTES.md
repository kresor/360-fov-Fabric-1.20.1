# Attempt 5: architecture reset for Minecraft 1.20.1

The full Attempt 4 CI log confirmed that the forked upstream source targets the modern 26.x renderer. The old direct-backport approach depended on render-state classes that do not exist in Minecraft 1.20.1.

Attempt 5 therefore stops porting those classes line by line.

Instead it recreates the older Flex-FOV style architecture on the 1.20.1 renderer:

1. Intercept the normal `GameRenderer.render -> renderWorld` call.
2. Render six 90 degree cube directions using the vanilla 1.20.1 `GameRenderer.renderWorld` method.
3. Copy the centre square of each render into six OpenGL textures.
4. Reproject the cube textures in a full-screen GLSL pass.
5. Preserve the 360-FOV projection transition used around the user's tested 120 degree setting.
6. Let vanilla render the HUD normally after reprojection.

## Intentional limitations of this first functional rewrite

- First-person world rendering is the primary target.
- The first-person hand is disabled during cube capture in this build. We will add a clean post-projection hand pass after the core world projection is proven.
- Modern 26.x fixes for particles, nametags, billboards, entity outlines, split-screen GUI and special renderer states are not included yet.
- FOV is configured in `config/fov360-1.20.1.properties`; default is 120.
- Texture face orientation may need one calibration pass after the first successful in-game launch.

## Why Yarn mappings now

The older 1.20.1 renderer APIs and the Flex-FOV-era code are much easier to target with Yarn named mappings than by trying to translate modern Mojang-named 26.x rendering classes.
