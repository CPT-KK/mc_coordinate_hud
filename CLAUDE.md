# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **client-side Fabric mod** for Minecraft 26.1 that displays a comprehensive HUD overlay with player coordinates, biome information, entity counts, FPS, and other environmental data. The mod is intentionally simple - all functionality is contained in a single Java class.

**Key architectural points:**
- Client-side only (no server components)
- Single-class design (`CoordinateHud.java`)
- Uses Fabric API's `HudElementRegistry` for rendering
- Configuration persistence via Java Properties file
- Full internationalization support (en_us, zh_cn)
- Uses **Mojang Official Mappings** (Yarn is no longer supported for 26.1+)

## Development Commands

```bash
# Build the mod JAR
./gradlew build
# Output: build/libs/coordinate-hud-<version>.jar

# Run development client for testing
./gradlew runClient

# Run on Windows (if above doesn't work)
gradlew.bat build
gradlew.bat runClient
```

Note: `genSources` is no longer needed for 26.1+ since Minecraft is unobfuscated.

## Technology Stack

- **Java 25**: Required (26.1 minimum JVM version)
- **Gradle 9.4.0**: Build system
- **Fabric Loom 1.16-SNAPSHOT**: Gradle plugin for Minecraft modding (unobfuscated mode)
- **Fabric Loader 0.18.6**: Mod loader
- **Fabric API 0.145.1+26.1**: Core modding framework
- **Mojang Official Mappings**: No separate mappings dependency needed

## Code Architecture

### Single-Class Pattern

The entire mod is implemented in `src/main/java/com/kk/CoordinateHud.java`. This is intentional, not accidental. When adding features:
- Prefer adding new methods to the existing class
- Do not create new classes unless absolutely necessary
- Keep the simple, focused design

### Core Components

**1. Initialization (`onInitializeClient`)**
- Registers key bindings (F10 toggle)
- Registers HUD renderer via `HudElementRegistry.addLast()`

**2. HUD Rendering (`renderHud`)**
- Main render loop called every frame
- Early returns if: HUD hidden, player/level null, or F3 debug overlay open
- Gathers data from `minecraft.player` and `minecraft.level`
- Renders 7 text lines at fixed positions (4px offset, 10px line height)

**3. Data Gathering Methods**
- `getEntitiesInViewAndTotal()`: Calculates entities in player's FOV using vector angle math
- `getPlayerViewVector()`: Converts yaw/pitch to normalized direction vector
- `getAngleBetweenVectors()`: Vector dot product for angle calculation
- `getSurfaceHeight()`: Uses `level.getHeight()` with `MOTION_BLOCKING` heightmap
- `getDirectionFromYaw()`: Maps yaw angle to cardinal direction
- `wrapAngleTo180()`: Normalizes angles to [-180, 180)

**4. Configuration Persistence**
- `loadConfig()`: Reads `config/coordinate_hud.properties` on startup
- `saveConfig()`: Writes `hudVisible` state when toggled
- Silent failure handling (uses defaults on error)

### Localization System

All user-facing text uses `I18n.get()` with keys from `assets/coordinate_hud/lang/*.json`:
- Biome names are constructed as `"biome." + namespace + "." + path`
- Fallback to `coordinate_hud.biome.unknown` if translation missing
- Both English and Chinese translations provided
- Mod Menu integration via `modmenu.*` keys

### Key Bindings

- **F10**: Toggle HUD visibility
- Uses `KeyMapping.Category.create()` with custom identifier
- Registered via `KeyBindingHelper.registerKeyBinding()`

## Important Patterns

**Mojang Mappings Naming Conventions:**
- `Minecraft` (not `MinecraftClient`)
- `GuiGraphics` (not `DrawContext`)
- `KeyMapping` (not `KeyBinding`)
- `Player` (not `PlayerEntity`)
- `Vec3` (not `Vec3d`)
- `ResourceLocation` (not `Identifier`)
- `Holder` (not `RegistryEntry`)
- `Level` (not `World`)
- `I18n.get()` (not `I18n.translate()`)
- `I18n.exists()` (not `I18n.hasTranslation()`)

**Dimension Handling:**
- Check via `minecraft.level.dimension().location().toString()`
- Overworld/Nether coordinate conversion (multiply/divide by 8)
- Other dimensions show "N/A"

**Biome Lookup:**
```java
Holder<Biome> biomeEntry = minecraft.level.getBiome(playerBlockPos);
String translationKey = "biome." + namespace + "." + path;
```

**Vector Math for FOV:**
- Player view vector derived from yaw/pitch with trigonometry
- FOV angle uses `minecraft.options.fov().get() / 2.0`
- Dot product used for angle comparison

**Rendering:**
```java
graphics.drawString(minecraft.font, text, x, y, color, shadow);
```

## File Structure Notes

```
src/main/
├── java/com/kk/
│   └── CoordinateHud.java    # Single source file
└── resources/
    ├── fabric.mod.json        # Mod metadata (id, version, dependencies)
    ├── coordinate_hud.mixins.json  # Mixin config (not currently used)
    └── assets/coordinate_hud/
        ├── icon.png
        └── lang/
            ├── en_us.json     # English translations
            └── zh_cn.json     # Chinese translations
```

## Version Management

- Version defined in `gradle.properties` (`mod_version`)
- Automatically injected into `fabric.mod.json` during build
- `fabric.mod.json` has hardcoded version that gets expanded

## Testing Notes

Use `runClient` task for testing. The mod will:
- Hide when F3 debug overlay is open
- Persist HUD visibility state across game sessions
- Display entity counts based on current FOV settings
- Automatically convert coordinates between Overworld/Nether
