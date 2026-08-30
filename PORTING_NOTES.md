# 360 FOV -> Fabric 1.20.1 port

This branch is an initial build-system backport scaffold.

Target:
- Minecraft 1.20.1
- Fabric Loader 0.14.22
- Fabric API 0.92.2+1.20.1
- Java 17

The current upstream source was written against Minecraft 26.2 rendering internals, so the Java renderer/mixins still require source-level backporting. The projection shader and projection math should be reusable; the main work is replacing modern GPU pipeline/render-state APIs with 1.20.1 framebuffer/OpenGL rendering APIs and remapping mixin targets.

Use the GitHub Action `Build Fabric 1.20.1 port` to get the first compiler/mixin error set. Do not expect this first scaffold to compile yet.


## Attempt 2
- Replaced unresolved `fabric-loom 1.3-SNAPSHOT` with published `fabric-loom 1.6.12`.
- Downgraded Gradle wrapper from 9.7.1 to 8.8 for Loom 1.6-era compatibility.
- Added official Mojang mappings for Minecraft 1.20.1.
- Switched Fabric Loader/API dependencies to `modImplementation`.
- Updated GitHub Actions Java setup to v5.
- Expected next failure: Java compile errors caused by renderer/API differences between Minecraft 26.2 and 1.20.1. Those errors are the useful input for the real renderer backport.
