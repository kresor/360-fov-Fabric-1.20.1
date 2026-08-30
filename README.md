# 360 FOV - Fabric 1.20.1 experimental backport (Attempt 12)

Quality-preserving optimization pass based on the known-good Attempt 8 renderer.

## Attempt 12 strategy

Rather than lowering cubemap resolution, this version keeps the original full-quality capture and skips cube directions that the final projection cannot see.

At **5120x1440 / FOV 120** a level view normally needs only front + left + right. Top/bottom are enabled as pitch requires them. The rear face appears only at sufficiently extreme FOV/projection combinations.

## Test baseline

- Minecraft / Fabric: 1.20.1
- Resolution: 5120x1440
- FOV: 120
- Shaders: OFF
- Compare image quality directly with Attempt 8
- Compare FPS directly with Attempt 8
