# Project Context for Qwen Code

## Project Overview

This is a **Minecraft Fabric mod** named "HUD Mod" (internal ID: `hudmod`). Its primary function is to display a Heads-Up Display (HUD) on the client side showing various pieces of information about the player and their environment.

### Key Features Displayed in the HUD:
-   **Player Coordinates:** Precise X, Y, Z position.
-   **Chunk Coordinates:** Relative block position within the current chunk.
-   **Biome:** The name of the current biome.
-   **Direction and Angles:** Cardinal direction (N, S, E, W), Yaw, and Pitch.
-   **Frames Per Second (FPS):** Current game FPS.
-   **Inter-dimensional Coordinates:** Automatically calculates and shows the corresponding coordinates in the Nether (if in Overworld) or Overworld (if in Nether).
-   **Entity Count:** Number of living entities currently within the player's field of view.

The mod is written in **Java** and uses the **Fabric API** for integration with Minecraft. It targets Minecraft version **1.21.8**.

## File Structure

-   `build.gradle` / `gradle.properties`: Define the build system (Gradle), dependencies (Fabric Loader, Fabric API, Minecraft version), and project metadata.
-   `src/main/java/com/kk/CoordinateHud.java`: The main client-side mod class. Implements `ClientModInitializer` and contains all the logic for the HUD rendering, keybinding, and data gathering.
-   `src/main/java/com/kk/mixin/ExampleMixin.java`: An example Mixin class (currently unused boilerplate).
-   `src/main/resources/fabric.mod.json`: The Fabric mod metadata file, defining the mod's ID, name, description, entry point (`com.kk.CoordinateHud`), dependencies, and icon.
-   `src/main/resources/coordinate_hud.mixins.json`: Configuration for the Mixin system.
-   `src/main/resources/assets/coordinate_hud/icon.png`: The mod's icon.

## Building and Running

This project uses **Gradle** with the Fabric Loom plugin for building Minecraft mods.

-   **Setup:** Ensure you have a JDK for Java 21 installed.
-   **Build:** `./gradlew build` (or `gradlew.bat build` on Windows). This compiles the code and packages it into a JAR file located in `build/libs/`.
-   **Run Client (Development):** `./gradlew runClient` (or `gradlew.bat runClient` on Windows). This starts a development instance of Minecraft with the mod loaded.
-   **Run Server (Development):** `./gradlew runServer` (or `gradlew.bat runServer` on Windows).
-   **IDE Setup:** Import the project as a Gradle project into your IDE (e.g., IntelliJ IDEA, VS Code with Java extensions).

## Development Conventions

-   **Language:** Java 21.
-   **Mod Loading:** Fabric Loader.
-   **API:** Fabric API.
-   **Entry Point:** Client-side initialization is handled by the `com.kk.CoordinateHud` class.
-   **HUD Rendering:** Uses `net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry` to register the HUD element.
-   **Keybinding:** Uses `net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper` to register a keybinding (default 'H') to toggle the HUD visibility.
-   **Code Style:** Standard Java conventions. The code is relatively self-contained within the main `CoordinateHud` class.
-   **Mixin:** The project is configured for Mixin, but the provided example is unused.

## Key Details

-   **Default Toggle Key:** The HUD visibility can be toggled in-game using the 'H' key.
-   **Mod ID:** `hudmod`
-   **Minecraft Version:** 1.21.8