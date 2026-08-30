# 360 FOV Fabric 1.20.1 - Attempt 15

Quality-preserving 1.20.1 ultrawide projection backport.

Attempt 15 fixes the oversized/black first-person hand introduced by Attempt 14 by restoring Minecraft's normal hand projection matrix for the final hand overlay.

Recommended test:
- 5120x1440
- FOV 120
- All of Fabric 7
- shaders off initially
- compare world quality/performance to Attempt 12
- test empty hand, tool, block, and map
