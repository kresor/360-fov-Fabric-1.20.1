# Attempt 12 - quality-preserving face culling

Attempt 10/11 reduced capture resolution and caused visible cube-face artifacts. Attempt 12 deliberately abandons that path.

This version is based directly on the visually good **Attempt 8** renderer:

- same full-resolution main framebuffer capture
- same projection shader
- same FOV slider behavior
- same hand behavior

The only optimization is **dynamic cubemap face culling**.

Before rendering the cube faces, the mod evaluates the same projection math on a conservative screen-space grid and determines which of the six cube faces can actually be sampled in the final image. It then renders only those faces.

For the target setup (**5120x1440, FOV slider 120, looking roughly level**) the expected visible set is:

- front
- left
- right

So normal play should often use about **3 world renders instead of 6** while keeping Attempt 8 image quality. Looking up/down dynamically enables the top/bottom face. Very wide FOV values can enable the back face when it becomes genuinely visible.
