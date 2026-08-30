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


## Attempt 3 build-system fixes

- Use legacy Gradle plugin id `fabric-loom` for Loom 1.7.4. The newer `net.fabricmc.fabric-loom` id belongs to the newer Loom plugin-id scheme and does not resolve correctly for this older Loom generation.
- Loom updated to 1.7.4, which is aligned with Gradle 8.8.
- Fabric API updated from 0.92.2+1.20.1 to 0.92.11+1.20.1. This also avoids the August 2026 `fabric-api-deprecated` 0.92.2 cache/jar issue reported by Fabric users.
- Fabric Loader updated to 0.16.10.
- CI uses `--refresh-dependencies` to avoid restoring a broken/stale dependency from Gradle cache.

Expected next failure: Java compile errors caused by Minecraft 26.2 rendering APIs that do not exist in 1.20.1. That is progress: it means the build toolchain is finally configured and the renderer backport can begin.


## Attempt 4

The build system now reaches `compileJava`, but the Mixin annotation processor stops early on classes that do not exist in Minecraft 1.20.1 (for example `LevelExtractor`, `SubmitNodeCollection`, `SkyRenderer`, `AtmosphericFogEnvironment`, and `GuiRenderer`).

Attempt 4 is intentionally a diagnostic pass. It disables only the Mixin annotation processor's target validator and raises javac's error limit so GitHub Actions emits the complete 26.2 -> 1.20.1 API mismatch list in one run. This does **not** make invalid mixins load at runtime; it is only to collect the information needed for the real renderer rewrite.

The workflow also uploads `attempt4-build.log` as an artifact even when compilation fails.
