# Hearty Harvest — Fabric mod (Minecraft 26.1.2)

The datapack harvest system, rebuilt as a mod. The entire feature is in
HeartyHarvest.java — ~50 lines of actual logic. No interaction entities,
raycasts, advancements, or cursor tags. A real right-click event replaces
all of it.

## Prerequisites (install once)
1. **JDK 21** — Fabric requires exactly Java 21. Get Temurin/Adoptium 21.
   Verify: `java -version` shows 21.
2. **VS Code extensions:**
   - "Extension Pack for Java" (Microsoft)
   - "Gradle for Java"
   These give you Java language support and Gradle task running.

## First build
This project ships without the gradle wrapper JAR (it's binary). Generate it:
1. Have Gradle installed once (or use the wrapper from any Fabric example).
2. In the project folder: `gradle wrapper --gradle-version 9.4.0`
   That creates gradle-wrapper.jar and the gradlew scripts.
3. Then: `./gradlew build`  (first run downloads Minecraft + deps — slow once)

The built mod lands in  build/libs/heartyharvest-0.1.0.jar

## Run it in a dev client (test without installing)
`./gradlew runClient`
Loom launches a dev Minecraft with your mod loaded. Grow a crop, hold a hoe,
right-click. Iron = 3x3, diamond/netherite = 5x5, wood/stone = single.

## Install into a real game
1. Install **Fabric Loader 0.18.4** for 26.1.2 (fabricmc.net/use).
2. Download **Fabric API** (the fabric_version in gradle.properties) into mods/.
3. Drop your built jar into .minecraft/mods/.

## Future-proofing notes (your priority)
- **Official Mojang mappings, no Yarn.** Since 26.1, Minecraft is un-obfuscated;
  we use net.fabricmc.fabric-loom (no remap). This kills the single biggest
  source of version breakage. Old tutorials using modImplementation/remapJar/
  Yarn are pre-26.1 — don't follow them.
- **Stable Fabric API only.** We use UseBlockCallback (public, maintained), not
  mixins into Minecraft internals. API events survive updates; internal hooks
  don't. Keep to the API wherever possible.
- **All version numbers live in gradle.properties.** On a MC bump, change them
  there (check fabricmc.net/develop for the set), refresh Gradle, fix any
  renamed methods the compiler flags. That's the whole upgrade loop.
- **Java 21** is current for this era. A future MC may bump it; that's a
  one-line change in build.gradle + a new JDK.

## Where to grow this
- Next feature -> new file (e.g. CookingPotBlock.java), registered in
  onInitialize. Keep features in their own classes.
- Custom items/blocks: use the Registry + RegistryKey pattern (the blessed way).
- Config: a JSON config file read at load, not hardcoded values.
- The "Neptune's Blessing" radius-3 hoe: a custom item with radius logic added
  to radiusForHoe(). That's the natural next build.
