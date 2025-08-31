# Coordinate HUD Mod

![Mod Version](https://img.shields.io/badge/version-1.0.0-blue) ![Minecraft Version](https://img.shields.io/badge/minecraft-1.21.8-brightgreen) ![Environment](https://img.shields.io/badge/environment-client-orange)

A client-side Fabric mod for Minecraft that displays a comprehensive Heads-Up Display (HUD) with various pieces of information about the player and their environment.

## Features

The HUD displays the following information in real-time:

- **Player Coordinates (XYZ)**: Precise player position in the world.
- **Chunk Relative Coordinates**: Player's position within the current chunk (0-15 for each axis).
- **Biome**: The name of the current biome the player is in.
- **Direction & Angles**:
  - Cardinal direction (North, South, East, West).
  - Yaw (horizontal rotation).
  - Pitch (vertical rotation).
- **Frames Per Second (FPS)**: Current game FPS for performance monitoring.
- **Inter-dimensional Coordinates**:
  - Automatically calculates and shows corresponding coordinates in the Nether (if in Overworld) or Overworld (if in Nether).
  - Displays "N/A" for other dimensions.
- **Entity Count**:
  - Number of living entities currently within the player's field of view.
  - Total number of living entities in the world.
- **Surface Height**: The Y-coordinate of the block at the top of the world at the player's XZ position.
- **Temperature**: The temperature of the current biome.

## Controls

- **Toggle HUD Visibility**: Press the `F10` key to show or hide the HUD.

## Language Support

The mod supports multiple languages:

- English (en_us)
- 简体中文 (zh_cn)

The mod's name and description are also localized for [Mod Menu](https://github.com/TerraformersMC/ModMenu).

## Requirements

- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.15.11 or higher
- **Fabric API**: 0.133.0+1.21.8 or higher

## Installation

1. Download and install the [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.8.
2. Download the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) mod jar file.
3. Download the latest `coordinate-hud-<version>.jar` file from the [Releases](https://github.com/CPT-KK/mc-coordinate-hud/releases) page.
4. Place the downloaded `fabric-api-<version>.jar` and `coordinate-hud-<version>.jar` files into your `.minecraft/mods` folder.
5. Launch Minecraft with the Fabric profile.

## Building from Source

This project uses Gradle with the Fabric Loom plugin.

### Prerequisites

- Java Development Kit (JDK) 21 or higher.

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/CPT-KK/mc-coordinate-hud.git
   cd mc-coordinate-hud
   ```
2. Set up the development environment:
   ```bash
   ./gradlew genSources
   ```
3. To build the mod:
   ```bash
   ./gradlew build
   ```
   The built jar file will be located in `build/libs/`.

4. To run a development client:
   ```bash
   ./gradlew runClient
   ```

## License

This mod is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Acknowledgements

- [Fabric](https://fabricmc.net/)
- [Fabric API](https://github.com/FabricMC/fabric)
