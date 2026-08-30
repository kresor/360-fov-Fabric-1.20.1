# 360 FOV Fabric 1.20.1 backport - Attempt 7

Attempt 7 builds on the first confirmed in-game working 1.20.1 renderer (Attempt 6).

Changes:

- Extends the normal Minecraft FOV slider from 30..110 to 30..400 via `GameOptionsMixin`.
- The renderer now reads the live vanilla FOV option instead of the temporary properties-file FOV.
- First-person hand/held item is disabled on the five auxiliary cube faces and enabled once on the front cube face.
- The old `fov360-1.20.1.properties` file is no longer authoritative for FOV. It can remain on disk harmlessly.

Expected test:

1. Install the Attempt 7 jar over Attempt 6 in a clean Fabric 1.20.1 instance.
2. Open Options. The FOV slider should continue past `Quake Pro` and reach 400.
3. Set FOV to 120 and enter a world at 5120x1440.
4. Confirm the projection still looks like Attempt 6 and that the player's hand/held item renders once.

Known limitation:

The hand is presently captured in the front cube face and therefore passes through the reprojection shader with the world. At the user's tested 120-degree ultrawide setting this should be visually reasonable, but it is not yet the ideal separate post-reprojection hand pass used by newer renderer architectures.
