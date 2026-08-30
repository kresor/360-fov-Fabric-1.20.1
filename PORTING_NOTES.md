# Attempt 13 - separate first-person hand overlay

Attempt 12's world projection and dynamic face culling are retained unchanged.

The first-person hand/held item is no longer rendered into the 90-degree front cubemap face. That caused the hand to be visibly chopped at the front-face boundary on a 32:9 display.

Instead:
1. all cubemap world captures run with `renderHand=false`;
2. the world is reprojected exactly as in Attempt 12;
3. vanilla `GameRenderer.renderHand(...)` is invoked once afterward via a Mixin invoker.

Expected result: same clear Attempt-12 world image/performance, but a complete, sharp first-person hand/item with no cubemap-face clipping.
