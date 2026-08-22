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

## Block entity APIs added in the automation update (26.2) — watch these on the next bump

The three machine blocks (`AutoHarvesterBlock`/`AutoMinerBlock`/`AutoFisherBlock` +
their block entities, `AbstractMachineBlockEntity`, `ModBlockEntities`) touch several
APIs that are newer and more likely to get renamed than the hoe-harvest code. If the next
version bump breaks compilation, check these first:

- **`BlockEntity.loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`** — NBT
  persistence is codec-based (`net.minecraft.world.level.storage.ValueInput` /
  `ValueOutput`), not raw `CompoundTag`, as of this version. Reads:
  `input.getIntOr(key, default)`, `getString(key)` (returns `Optional`), etc. Writes:
  `output.putInt(key, value)`, `putString(...)`, etc.
  `ContainerHelper.loadAllItems(ValueInput, NonNullList<ItemStack>)` /
  `saveAllItems(ValueOutput, NonNullList<ItemStack>)` persist an item list under a fixed
  internal key — don't try to pass a custom tag name to those two.
- **`BlockEntityType` registration** goes through
  `net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder
  .create(YourBlockEntity::new, blockInstance...).build()`, then
  `Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type)` under
  `Registries.BLOCK_ENTITY_TYPE`. The block instances must already be registered before
  you build the type (see `Greenward.onInitialize()` — `ModBlocks.initialize()` before
  `ModBlockEntities.initialize()`).
- **`BlockEntityTicker<T>.tick(...)` takes `Level`, not `ServerLevel`**, as its first
  param — cast to `ServerLevel` inside if you need server-only APIs. `getTicker(...)` on
  `EntityBlock`/`BaseEntityBlock` still takes `(Level, BlockState, BlockEntityType<T>)`.
- **`BaseEntityBlock` re-declares `codec()` as abstract.** Any concrete subclass needs
  `protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }` where
  `CODEC = simpleCodec(YourBlock::new)` (a static helper inherited from `Block`), or it
  won't compile. Plain `Block` subclasses (not `BaseEntityBlock`) don't need this —
  `Block` itself already provides a concrete `codec()`.
- **Hopper-insertable, no-GUI block entities implement `WorldlyContainer` directly** on
  the `BlockEntity` subclass (`getSlotsForFace`, `canPlaceItemThroughFace`,
  `canTakeItemThroughFace`, plus the base `Container`/`Clearable` methods). Don't extend
  `BaseContainerBlockEntity` for this — it drags in `MenuProvider`/`createMenu`, which is
  only needed if the block actually opens a screen.
- **Fishing loot roll**: `serverLevel.getServer().reloadableRegistries()
  .getLootTable(BuiltInLootTables.FISHING)`, then
  `new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, vec3)
  .withParameter(LootContextParams.TOOL, itemStack).create(LootContextParamSets.FISHING)`.
  `LootContextParams.TOOL` is a `ContextKey<ItemInstance>`; `ItemStack` implements
  `ItemInstance` in this version, so pass an `ItemStack` straight in.
- **Shared blockstate properties**: reuse `BlockStateProperties.LIT` and
  `BlockStateProperties.HORIZONTAL_FACING` (`net.minecraft.world.level.block.state
  .properties.BlockStateProperties`) rather than declaring fresh `Property` instances —
  that's the vanilla convention (furnace, etc. all share these).
- **The machine GUIs (added when the automation update grew inventories/upgrades) use
  `AbstractContainerMenu` + `AbstractContainerScreen` as usual, but the actual drawing API
  is new**: `Screen`/`AbstractContainerScreen` no longer has a `render(GuiGraphics, ...)`
  override point, and `GuiGraphics` itself has no `blit`/`drawString`/`fill` methods at
  all. Screens now override `extractBackground(GuiGraphicsExtractor, mouseX, mouseY,
  partialTick)` (and `Screen.extractRenderState(...)` more generally) and draw through a
  `GuiGraphicsExtractor`, whose methods are close analogues (`blit(RenderPipeline,
  Identifier, x, y, u, v, w, h, texW, texH)`, `blitSprite(...)`, `text(Font, ..., x, y,
  color)`, `fill(x0, y0, x1, y1, argbColor)`). Decompile
  `AbstractFurnaceScreen`/`HopperScreen`/`FurnaceMenu` with `./gradlew
  genSourcesWithVineflower` before writing a new screen from scratch — it's the fastest way
  to get this right, since there's no training-data familiarity with this rendering model
  to fall back on.
- **A dedicated server crashes at startup if ANY class it loads references a client-only
  type anywhere in its bytecode — even behind an `if (level.isClientSide())` guard that
  never runs there.** Bytecode verification touches every referenced type at class-load
  time, guard or not. Concretely: never let a class reachable from `Greenward`/`ModItems`/
  `ModBlocks`/etc. import `net.minecraft.client.*` (or Fabric's client-only APIs) — keep
  that code in classes only ever reached from the `client` entrypoint
  (`GreenwardClient.onInitializeClient()` and whatever it directly instantiates/registers,
  like `AutoHarvesterScreen`). `grep -rl "net\.minecraft\.client" src/main/java` and check
  every hit is one of those client-only files before shipping.

## Gear & God Potion + Sea Creatures update (26.2) — new version-fragile APIs

Armor materials, tool materials, and potion/brewing registration are exactly the three
areas that spec called out as the most churn-prone in modern Minecraft, and it was right —
none of them look like any pre-1.20.5 tutorial. All verified against the decompiled jar
this session; if the next bump breaks compilation, check these first.

- **There is no `ArmorItem` class anymore.** Armor pieces are plain `Item`s built with
  `new Item.Properties().humanoidArmor(ArmorMaterial, ArmorType)` — that one call sets up
  durability, the `Equippable` data component, and attribute modifiers together. `ArmorType`
  is `HELMET`/`CHESTPLATE`/`LEGGINGS`/`BOOTS`/`BODY` (BODY is for animal armor, not
  humanoid sets). `ArmorMaterial` is a record: `(durability, Map<ArmorType,Integer> defense,
  enchantmentValue, Holder<SoundEvent> equipSound, toughness, knockbackResistance,
  TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId)` — build the defense
  map with `ArmorMaterials.makeDefense(boots, legs, chest, helm, body)` (note the argument
  order — boots first, despite the vanilla constants listing helmet-first in the table).
  The repair ingredient is a **tag**, not a bare item, so every custom material needs its
  own single-item tag (see `data/greenward/tags/item/repairs_*.json`).
- **There is no `PickaxeItem`/`AxeItem`/`ShovelItem` class either** — those tool types are
  fully data-driven via `Item.Properties.pickaxe(ToolMaterial, attackDamage, attackSpeed)`
  (and `.axe(...)`/`.shovel(...)`), no dedicated Item subclass needed. `HoeItem` and
  `FishingRodItem` still exist as real classes (hoes till dirt, so that can't be pure data).
  `ToolMaterial` is a record too: `(TagKey<Block> incorrectBlocksForDrops, durability,
  speed, attackDamageBonus, enchantmentValue, TagKey<Item> repairItems)` — the `speed`
  field is what actually controls mining speed (the two floats passed to `.pickaxe(...)`
  are attack damage/speed, unrelated to mining). Reuse `BlockTags.INCORRECT_FOR_DIAMOND_TOOL`
  for a diamond-tier custom tool rather than inventing a new incorrect-blocks tag.
- **Both `ArmorMaterials` (armor) and `EntityTypes` (mob spawning) moved their vanilla
  constants into a separate plural-named holder class/interface** — `ArmorMaterial`/
  `EntityType` themselves don't carry the `LEATHER`/`DROWNED`/etc. static fields anymore.
  Grep for a `*s` sibling class before concluding a constant doesn't exist.
- **Baking a default enchantment onto a custom item** (used for the fishing rods' built-in
  Lure/Luck of the Sea) needs `Item.Properties.delayedComponent(DataComponents.ENCHANTMENTS,
  HolderLookup.Provider -> ItemEnchantments)`, not a plain `.component(...)` call — enchant-
  ments are a *reloadable* (data-pack) registry now, unavailable at mod-init time, so the
  component has to be resolved lazily once real registries exist. Inside the lambda:
  `context.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LURE)` gives a real
  `Holder<Enchantment>`; build the map with `new ItemEnchantments.Mutable(ItemEnchantments
  .EMPTY)`, `.set(holder, level)`, `.toImmutable()`. This reuses vanilla's real enchantment-
  reading code paths (FishingHook already reads levels off the held rod) — no mixin.
- **Potions are still a plain built-in registry** (`BuiltInRegistries.POTION`), *unlike*
  enchantments — `Potion` is a plain Java class (`new Potion(name, MobEffectInstance...)`),
  registered exactly like any `ModItems`/`ModBlocks` entry, no deferred-lookup dance needed.
  `Registry.register(...)` returns the raw object; get the `Holder<Potion>` you need
  elsewhere via `BuiltInRegistries.POTION.wrapAsHolder(potion)`.
- **A custom potion's item translation key lives under the `minecraft` namespace**, not
  the mod's own — `item.minecraft.potion.effect.<name>` where `<name>` is the string
  passed to `new Potion(name, ...)` (confirmed against vanilla's own `en_us.json`, e.g.
  `item.minecraft.potion.effect.awkward`), because the actual item is still vanilla's
  `minecraft:potion`; only the `PotionContents` component (which flavor) differs. Adding a
  `minecraft:`-namespaced key from a mod's own lang file is normal and fine.
- **Brewing recipe registration has no vanilla mod-facing API** (`PotionBrewing` is an
  immutable object built once from a `Builder`) **but Fabric API does provide one** —
  `net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder` (in
  `fabric-content-registries-v0`, already a transitive dependency via `fabric-api`).
  Register via `FabricPotionBrewingBuilder.BUILD.register(builder -> ((FabricPotionBrewing
  Builder) builder).registerPotionRecipe(fromPotionHolder, Ingredient.of(item),
  toPotionHolder))` — the vanilla `PotionBrewing.Builder` passed to the callback implements
  the Fabric interface via a Fabric-side mixin, hence the cast. No mixin needed on the mod's
  own end. **Verified live** this session: brewed Awkward + a custom item to a custom
  potion in a real brewing stand via RCON and watched `BrewTime` count down and the result
  land correctly.
- **`LootTableEvents.MODIFY_DROPS`** (`fabric-loot-api-v3`,
  `net.fabricmc.fabric.api.loot.v3.LootTableEvents`) is the hook for intercepting a
  specific loot roll's result — signature
  `modifyLootTableDrops(Holder<LootTable>, LootContext, List<ItemStack>)`, and the list is
  mutable (clear/replace it to suppress or change what actually drops). Filter to a specific
  table with `table.is(SomeResourceKey)` (`Holder<T>.is(ResourceKey<T>)` — note `Holder`
  itself carries `.is(Identifier)`/`.is(ResourceKey)`/`.is(TagKey)`/`.is(Holder)`/
  `.is(Predicate)` overloads now, not just tag checks). To distinguish *who* triggered a
  loot roll (e.g. a real player's `FishingHook` vs. a mod's own manually-built
  `LootParams`), check `LootContext.hasParameter(...)` /
  `.getParameter(LootContextParams.THIS_ENTITY)` — confirmed against
  `FishingHook.retrieve()`'s decompiled source that a real catch is the only caller that
  sets `THIS_ENTITY` to the hook entity.
- **Persistent custom entity/block-entity tagging without NBT plumbing**: `Entity` (and
  `BlockEntity`) implement `net.fabricmc.fabric.api.attachment.v1.AttachmentTarget` in this
  version. `AttachmentRegistry.createPersistent(Identifier, Codec<A>)` gives an
  `AttachmentType<A>`; then `entity.setAttached(type, value)` / `entity.getAttached(type)`
  persist across save/load automatically (`fabric-data-attachment-api-v1`). Simpler and
  safer than a raw NBT tag, no mixin.
- **A real vanilla attribute exists for "how far can I fall before taking damage"**:
  `Attributes.SAFE_FALL_DISTANCE` (also see `Attributes.FALL_DAMAGE_MULTIPLIER` and
  `Attributes.GRAVITY` if a future feature wants either) — apply an `AttributeModifier`
  the same way as any other attribute (`getAttribute(...).addTransientModifier(...)` /
  `.removeModifier(id)`). No mixin needed for "reduce fall damage" style set bonuses.
- **`Mob`/`LivingEntity` spawning helpers renamed**: no `Entity.moveTo(x, y, z, yaw,
  pitch)` — use `setPos(x, y, z)` + `setYRot(...)`/`setXRot(...)` separately.
  `EntityType.create(Level, EntitySpawnReason)` constructs the entity (not yet in-world);
  `mob.finalizeSpawn(ServerLevelAccessor, DifficultyInstance, EntitySpawnReason,
  SpawnGroupData)` then `level.addFreshEntity(mob)` to actually spawn it.
  `EntitySpawnReason.TRIGGERED` reads well for "a game event caused this," not
  `MOB_SUMMONED` (that one's really for the `/summon` command family).

## Four Pillars Progression, Farming leg (26.2) — smithing_transform

New to this update: Tier II→III gear upgrades use a real `minecraft:smithing_transform`
recipe (`data/greenward/recipe/wardens_*.json`, `harvest_warden.json`) — same recipe type,
same three-slot (template/base/addition) shape as vanilla's own Diamond→Netherite upgrade.
No new Java registration needed on the mod's side; it's pure data, exactly like any other
crafting recipe file, verified against a real vanilla example
(`data/minecraft/recipe/netherite_axe_smithing.json`) before writing ours. The one
gotcha: **a smithing table has no server-side block-entity inventory**, unlike a brewing
stand — there is no way to drive a smithing upgrade via `/data merge block`/`/item replace`
the way brewing was verified this session; it can only be tested by a real player opening
the smithing UI. Don't spend time hunting for a headless test path here — there isn't one.

If a future template item needs vanilla's fancier "applies to" tooltip (item icons showing
which base/addition items it accepts), that's `SmithingTemplateItem`, not plain `Item` —
its constructor wants four `Component`s (title/description formatting) plus two
`List<Identifier>`s (base and addition icon sprites). `ModItems.ASCENSION_TEMPLATE`
deliberately skips this for a plain `Item` — functionally identical for recipe matching,
just a plainer tooltip.

## Four Pillars Progression, Mining/Fishing/Combat legs + God Potion rework (26.2)

- **There is no `SwordItem` class**, matching the pickaxe/axe/shovel precedent already noted
  above — `Item.Properties.sword(ToolMaterial, float attackDamage, float attackSpeed)` is
  fully data-driven. Confirmed against vanilla's own `Items.java`: every vanilla sword tier
  passes the *same* `(3.0F, -2.4F)` regardless of material — the per-tier damage difference
  comes entirely from `ToolMaterial.attackDamageBonus` (displayed total attack damage =
  1 base + 3 from the sword's fixed properties call + the material's own bonus; e.g.
  Diamond's bonus is 3.0F for a displayed 7, Netherite's is 4.0F for 8). Reaper's Edge uses
  `5.0F` for the spec's literal "Atk 9" target.
- **`ServerLivingEntityEvents.ALLOW_DEATH`** (`fabric-entity-events-v1`, same module as
  `AFTER_DEATH` already used by Sea Creatures) is a cancellable pre-death hook —
  `boolean allowDeath(LivingEntity, DamageSource, float damageAmount)`; returning `false`
  cancels the death outright, the same mechanism vanilla's own Totem-of-Undying-style saves
  use internally. Used for Reaper's Aegis's Grim Resolve set bonus (set health to 1, apply
  a short Resistance buff, return `false`) — no mixin needed, and a real Totem still takes
  priority since that's a separate, earlier-firing vanilla check this event doesn't touch.
- **`AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL`** is the right operation for a
  percentage-of-final-value bonus (used for the combat pillar's attack-speed set bonuses,
  e.g. `0.10` = +10%) — distinct from `ADD_MULTIPLIED_BASE` (percentage of the attribute's
  *base* value before other modifiers) and `ADD_VALUE` (flat addition, used everywhere else
  in this project's set bonuses, e.g. safe-fall distance and Luck).
- **Multiple tiers of the same set-bonus attribute need distinct `AttributeModifier` ids.**
  Early in this pass a single shared modifier id was nearly reused across Mining's three
  armor tiers for safe-fall distance — the bug: `addModifierIfMissing` only adds when the id
  is absent, so swapping from a lower tier's modifier (still present, same id) to a higher
  tier's would silently keep the *old* value instead of updating it, since the "already has
  this id" check would short-circuit. Fixed by giving every tier (Excavator's vs. Bedrock,
  Tidal vs. Leviathan's, etc.) its own `Identifier`/`AttributeModifier` constant, each
  removed independently when that tier's own full-set check fails. Since armor slots hold
  exactly one item, only one tier's full-set check can ever be true at once anyway — the
  separate ids are what make a mid-run tier swap transition cleanly rather than sticking.

## Principles that keep this easy

- **Stay on the stable Fabric API** (events like UseBlockCallback), never mixins
  into Minecraft internals unless unavoidable. API survives updates; internal
  hooks break every version.
- **All version numbers live in gradle.properties + build.gradle.** Nothing
  version-specific is hardcoded in the Java.
- **Read the bottom of error output first.** The first 40 errors are usually
  cascades from one root cause stated at the very end.
- **Commit before every update.** Rolling back a bad update should be one command.
