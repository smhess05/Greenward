# Updating Hearty Harvest to a New Minecraft Version

A step-by-step for bumping this mod to a newer Minecraft version. Written after
the 26.1.2 build, so examples use 26.1.2 -> 26.2, but the process is the same
for any future bump.

The core idea: **your Java logic almost never changes. Version numbers in a few
files do, and occasionally a renamed Minecraft method the compiler will flag.**
Updating is a bounded chore, not a rewrite.

---

## Step 0 — Commit the working version FIRST (do not skip)

Before changing anything, snapshot the working state so you can roll back if the
update goes sideways.

    git add .
    git commit -m "Working on <current version>"

If you have not set up git yet:

    git init
    git add .
    git commit -m "Working on <current version>"

This is the safety net. Updating = break things, then fix them. Always have a
known-good commit to return to.

---

## Step 1 — Get the correct version numbers (never guess)

Wrong version numbers cause the mismatch crashes (e.g. "wrong version 69.0",
"Cannot use Mojang mappings", mixin target mismatch). Always pull the current
recommended set from the official source:

  **https://fabricmc.net/develop**

That page shows copy-paste values for the target Minecraft version:
- minecraft_version
- loader_version
- loom_version
- fabric_api_version (Fabric API)
- required Gradle version
- required Java version

Also read the porting guide for the target version, which lists notable code
changes:

  **https://docs.fabricmc.net/develop/porting/**

---

## Step 2 — CHECK: is Fabric API fully released for the target version?

Right after a Minecraft version drops, some Fabric API modules lag behind. If the
API is not fully released for the target version yet, DO NOT UPDATE. Stay on your
current working version until the ecosystem catches up. There is no benefit to
chasing the newest version the day it lands.

Confirm on https://fabricmc.net/develop that a full Fabric API build exists for
the target version before proceeding.

---

## Step 3 — Update the version numbers in 3 files

### 3a. gradle.properties
Update these lines to the values from fabricmc.net/develop:

    minecraft_version=<new version>       e.g. 26.2
    loader_version=<new loader>           e.g. 0.19.3
    fabric_version=<new api version>      e.g. 0.xxx.x+26.2

### 3b. build.gradle
Update the Loom plugin version:

    id 'net.fabricmc.fabric-loom' version '<new loom>'   e.g. '1.17-SNAPSHOT'

Update the Java toolchain ONLY if fabricmc.net/develop says the target version
needs a newer Java. (26.1/26.2 use Java 25.) The block looks like:

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)   // bump if required
        }
    }

If you bump Java, install that JDK first (winget install
EclipseAdoptium.Temurin.<N>.JDK) and confirm Gradle sees it:

    & "<your-gradle>\bin\gradle.bat" -q javaToolchains

### 3c. src/main/resources/fabric.mod.json
Update the Minecraft dependency:

    "minecraft": "~<new version>",        e.g. "~26.2"

---

## Step 4 — Update the Gradle wrapper (if the version changed)

If fabricmc.net/develop lists a different Gradle version, update the wrapper so
the VS Code panel and terminal agree:

    & "<your-gradle>\bin\gradle.bat" wrapper --gradle-version <new gradle>

(Replace <your-gradle> with your Gradle install path, e.g.
G:\Minecraft Modpacks\gradle-9.6.1)

---

## Step 5 — Clean build with refreshed dependencies

The --refresh-dependencies flag is REQUIRED after a version bump. It discards the
cached old libraries and pulls the new ones. Without it you get phantom errors
from stale caches.

    & "<your-gradle>\bin\gradle.bat" clean build --refresh-dependencies

---

## Step 6 — Fix any compile errors (usually few or none)

Two outcomes:

**A) BUILD SUCCESSFUL.** Likely, since this mod uses stable Fabric API and
long-stable Minecraft names. Skip to Step 7.

**B) A handful of "cannot find symbol" / renamed-method errors.** Mojang renamed
something you call. The compiler names the exact file, line, and symbol. For each:
1. Note the class/method it cannot find.
2. Check the porting guide (docs.fabricmc.net/develop/porting) for the new name.
3. If not listed, search "Minecraft <version> <old name> renamed" or browse the
   decompiled source at https://mcsrc.dev.
4. Update the name, rebuild.

This is the same process used to get onto 26.1.2 originally. It is mechanical.

---

## Step 7 — Run and test

    & "<your-gradle>\bin\gradle.bat" runClient

Launches a dev Minecraft with the mod loaded. Test the feature:
- Grow crops to full, hold a hoe, right-click.
- Iron = 3x3, diamond/netherite = 5x5, wood/stone = single.
- Fortune on the hoe should increase yield.

---

## Step 8 — Commit the updated version

Once it builds and runs:

    git add .
    git commit -m "Update to <new version>"

---

## Quick reference: the whole loop

1. git commit (safety net)
2. Get numbers from fabricmc.net/develop
3. Confirm Fabric API is fully released for the target version (else wait)
4. Update versions in: gradle.properties, build.gradle, fabric.mod.json
5. Update the Gradle wrapper if needed
6. clean build --refresh-dependencies
7. Fix any renamed symbols the compiler flags
8. runClient to test
9. git commit

---

## Common errors and what they mean

- **"Cannot use Mojang mappings in a non-obfuscated environment"**
  -> You have a `mappings loom.officialMojangMappings()` line in build.gradle.
     On 26.1+ Minecraft is un-obfuscated; REMOVE that line entirely.

- **"class file has wrong version 69.0, should be 65.0" (or similar numbers)**
  -> Java version mismatch. The libraries need a newer Java than the build is
     using. 65.0 = Java 21, 69.0 = Java 25. Install the required JDK and set the
     toolchain languageVersion to match.

- **"invalid source release: 25"**
  -> You told it to compile for Java 25 but the JDK running the build is older.
     Install JDK 25 and use a toolchain block (not sourceCompatibility) so Gradle
     locates it.

- **Mixin target type mismatch / mixin PREPARE failed in a fabric-* module**
  -> Fabric API version does not match the Minecraft version. Set fabric_version
     to the exact value for your Minecraft version from fabricmc.net/develop, then
     clean build --refresh-dependencies.

- **"plugin net.fabricmc.fabric-loom ... was not found"**
  -> settings.gradle is missing the Fabric maven in pluginManagement, or the Loom
     version does not exist. Confirm the repository block and use a real Loom
     release number.

- **45 "cannot find symbol" errors on imports that were fine before**
  -> Almost always a stale cache or Java-version mismatch, NOT your code.
     Run: clean build --refresh-dependencies. Check the real error at the BOTTOM
     of the output (the first errors are cascades from one root cause).

- **Build uses the wrong Gradle version (e.g. mentions an old version you did
    not choose)**
  -> The wrapper (gradle/wrapper/gradle-wrapper.properties) points at a different
     version than your install. Regenerate: gradle wrapper --gradle-version <N>.
     Or run through the VS Code Gradle panel consistently, not a mix.

---

## Principles that keep this easy

- **Stay on the stable Fabric API** (events like UseBlockCallback), never mixins
  into Minecraft internals unless unavoidable. API survives updates; internal
  hooks break every version.
- **All version numbers live in gradle.properties + build.gradle.** Nothing
  version-specific is hardcoded in the Java.
- **Read the bottom of error output first.** The first 40 errors are usually
  cascades from one root cause stated at the very end.
- **Commit before every update.** Rolling back a bad update should be one command.
