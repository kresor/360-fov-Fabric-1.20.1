# 360 FOV - Fabric 1.20.1 experimental backport (Attempt 9)

Experimental 1.20.1 port focused on corrected ultrawide rendering.

## What changed in Attempt 9

- normal Minecraft FOV slider still drives the mod
- first performance pass added:
  - configurable square capture scale (`captureScale`, default `0.75`)
  - square projection during capture via `WindowMixin`
  - optional back-face skipping (`skipBackFace=true`)

## Install

1. Build the mod through the included GitHub Actions workflow or local Gradle.
2. Put the produced jar into the Prism Fabric 1.20.1 instance's `mods` folder.
3. Launch the game and set the normal Minecraft FOV slider to your preferred value.

## Recommended first test

- Resolution: **5120x1440**
- Vanilla FOV slider: **120**
- Shader packs: **OFF**
- Pack: **All of Fabric 7** or another large Fabric 1.20.1 pack

## Config file

After first launch:

`config/fov360-1.20.1.properties`

Useful values:
- `captureScale=0.75` ← balanced default
- `captureScale=0.5` ← faster, softer
- `captureScale=1.0` ← sharpest, slowest
- `skipBackFace=true` ← better performance for mid-range FOV values
