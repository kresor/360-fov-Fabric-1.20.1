# Attempt 14 - avoid stale upstream invoker collision

Attempt 13's build log showed GitHub was still compiling the original 26.x `GameRendererInvoker.java`, which contains modern renderer APIs unavailable in 1.20.1.

This pass renames the new 1.20.1 invoker to **HandRendererInvoker.java** and updates all references. The stale upstream file can remain in the repository because `build.gradle` no longer includes it.

Behavior goal remains unchanged from Attempt 13:
- keep Attempt 12's quality-preserving face culling
- render cubemap faces without the first-person hand
- reproject the world
- render the hand/item once afterward as a normal screen-space overlay
