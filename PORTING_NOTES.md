# Attempt 16 - preserve Attempt 12, fix hand inside front face

Attempts 14/15 tried rendering the hand after panoramic reprojection. On 1.20.1 that produces a gigantic black hand/item because the vanilla first-person pass depends on renderWorld's internal projection/model-view state.

Attempt 16 abandons that path completely and returns to the known-good Attempt 12 architecture:

- full-quality Attempt 8/12 cubemap capture
- dynamic face culling from Attempt 12
- hand rendered only in the front cubemap face

The only new change is a separate **hand-only capture FOV**. On a 32:9 framebuffer the vanilla hand is positioned for the full 5120-wide projection, but only the center 1440x1440 square is copied into the cubemap face. That clips the hand. Rendering the hand with a wider FOV shrinks/recenters it enough to remain inside that square.

Default:

`handCaptureFov=150.0`

Config file:

`config/fov360-1.20.1.properties`

If the hand is still clipped, try 160 or 170. If it looks too small, try 140.
