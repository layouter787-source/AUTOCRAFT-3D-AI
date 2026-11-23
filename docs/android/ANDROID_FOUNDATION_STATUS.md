# Android foundation — implementation status

**Branch:** `autocraft/android-porting-foundation`  
**Purpose:** establish a small, independently buildable Android ARM64 shell before attempting to compile Blender subsystems.

## Implemented in this increment

- Android application module under `android-app/`.
- Kotlin + Jetpack Compose entry screen.
- Android NDK / CMake shared-library target.
- Minimal JNI call proving that the Kotlin app can load and call native C++.
- GitHub Actions workflow that assembles a debug APK for `arm64-v8a` and uploads it as an artifact.

## Explicitly not implemented

- The JNI library is a smoke-test bridge; it does **not** link Blender.
- The viewport is a placeholder; there is no 3D renderer or interactive scene yet.
- Model, sculpt, materials and animation buttons are visual placeholders.
- No Blender BKE, BMesh, DNA, depsgraph or geometry module has been ported.
- No project serialization, .ac3d format, GLB import/export, undo stack or AI tools exist yet.
- No APK is claimed to have compiled or been tested on a physical device until CI and device validation succeed.

## Next engineering gate

1. Confirm the Android workflow passes and preserve its APK artifact.
2. Add a native unit/smoke test and an instrumentation launch test.
3. Audit the smallest useful Blender dependency slice (math + mesh data) with a reproducible ARM64 build.
4. Integrate one real Blender-backed mesh operation behind the JNI command boundary.
5. Introduce a real viewport only after the native scene can create, mutate and destroy mesh data.

## Build locally

From this directory:

```bash
cd android-app
gradle --version
gradle assembleDebug
```

The CI workflow pins Gradle 8.13, JDK 17, Android NDK 27.2.12479018 and CMake 3.22.1. The first target ABI is `arm64-v8a`.
