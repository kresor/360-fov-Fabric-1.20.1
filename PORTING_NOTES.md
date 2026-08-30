# Attempt 10 - compile fix for Attempt 9 performance pass

Attempt 9 reached Java compilation but used the wrong package for Minecraft 1.20.1's `Window` class.

Fix:
- `com.mojang.blaze3d.platform.Window` -> `net.minecraft.client.util.Window`

The performance changes from Attempt 9 remain unchanged:
- `captureScale=0.75` balanced default
- square capture viewport/projection
- optional rear-face skipping for the user's 120 FOV ultrawide case
