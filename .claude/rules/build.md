# Build Rules

## Branch

Active branch: `port-1.21.11`

## Environment

- Java 21 is required (sourceCompatibility/targetCompatibility = 21)
- Set JAVA_HOME before building:
  ```
  export JAVA_HOME=/opt/homebrew/opt/openjdk@21
  ```

## Project Structure

This is a Minecraft Fabric/NeoForge mod (forked from denisnumb/discord-chat-mod) using the Architectury multi-loader setup. Subprojects: `common`, `fabric`, `neoforge`.

- Minecraft version: 1.21.11
- Mod version: 2.6.2
- Architectury Loom with Mojang mappings
- Shadow plugin for fat JARs

## Build Commands

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
./gradlew build
```

Clean build:
```sh
./gradlew clean build
```
