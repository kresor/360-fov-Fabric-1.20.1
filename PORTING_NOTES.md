# Attempt 15 - restore vanilla hand projection

Attempt 14 proved that drawing the hand as a final overlay is the correct architectural direction, but the hand was rendered using a stale cubemap/world projection matrix. The result was a giant black hand/item covering much of the screen.

Attempt 15 keeps the Attempt 12 panoramic world renderer and face-culling optimization unchanged, but before invoking Minecraft 1.20.1's private `renderHand(...)` method it now:

1. obtains Minecraft's own hand FOV using `getFov(camera, tickDelta, false)`;
2. backs up the current RenderSystem projection matrix;
3. loads `getBasicProjectionMatrix(handFov)`;
4. clears only the depth buffer so the hand can render over the reprojected world;
5. renders the hand once;
6. restores the previous projection matrix.

This should preserve the clean panoramic world while making the hand/item use normal vanilla first-person perspective.
