# 360 FOV Fabric 1.20.1 - Attempt 16

Quality-preserving branch based on Attempt 12.

- corrected ultrawide world projection
- dynamic cubemap face culling
- first-person hand stays inside the front cubemap capture
- no post-reprojection hand pass

Recommended test:

- 5120x1440
- Minecraft FOV 120
- `handCaptureFov=150.0`

Tune `handCaptureFov` in `config/fov360-1.20.1.properties` if necessary.
