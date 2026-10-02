# Greenward — Automation Update

Target: Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.156.0+26.2, Loom 1.17, Java 25.

## What's in this update

### Machines

Three automation blocks, each with a furnace-style GUI (right-click to open): a fuel slot
(insert fuel by hand or via hopper from any side, with a flame gauge and an operation
progress bar), a 45-slot output storage grid (locked in 9-slot rows beyond your current
storage tier), and two or three upgrade slots. A `lit` blockstate reflects whether the
machine currently has charge. With no fuel, or with storage completely full, a machine
idles harmlessly — no crash, no console spam, no lost items. Fuel, storage, upgrade tiers,
and progress all persist across a world reload.

- **Auto-Harvester** (`greenward:auto_harvester`) — at its base speed every 2 seconds (40
  ticks), scans a 9×9 footprint (radius 4, at its own Y and Y+1) in a stable rotating order
  for a mature crop, harvests it into its own storage (no Fortune, since there's no tool),
  replants at age 0, and applies the fertilized-farmland 2x yield bonus just like the hoe
  does.
- **Auto-Miner** (`greenward:auto_miner`) — at its base speed every 3 seconds (60 ticks),
  mines the single block directly in front of it (placement direction, like a furnace) into
  its own storage. Refuses bedrock/unbreakable blocks, anything with a block entity (chests
  etc.), fluids, and air. **Ores regrow**: any block tagged `#greenward:regenerable_ores`
  (the vanilla ore set) that gets mined is replanted after a delay (base 10s, upgradable).
  Drop an ore block into its dedicated seed slot and, if the spot in front is empty, the
  miner will plant a copy there itself — a bootstrapped, self-sustaining ore point.
- **Auto-Fisher** (`greenward:auto_fisher`) — at its base speed every 10 seconds (200
  ticks), if at least one water source block is orthogonally adjacent (4 sides + below),
  rolls directly against vanilla's fishing loot table (an unenchanted fishing rod as the
  tool, so Luck of the Sea / Lure never apply) and deposits the result into its own storage.

All three craft as a 3×3 frame of iron ingots with a redstone block at bottom-center and a
role tool in the middle (iron hoe / iron pickaxe / fishing rod).

### Upgrades

Drop the matching upgrade item into its slot in the machine's GUI — it's consumed instantly
and the tier increases by 1 (the slot always looks empty; there's nothing to take back out).
Each axis has 5 tiers (0–4, i.e. craft the item 4 times to max a machine out):

| Upgrade | Item | Effect per tier |
|---|---|---|
| Storage | `greenward:storage_upgrade` | +1 row (9 slots) of output storage, up to 45 |
| Speed | `greenward:speed_upgrade` | Shortens the operation interval (see table below) |
| Ore regrowth (Auto-Miner only) | `greenward:regen_upgrade` | Shortens the ore regrowth delay |

Operation interval by speed tier (ticks):

| Machine | Tier 0 | Tier 1 | Tier 2 | Tier 3 | Tier 4 |
|---|---|---|---|---|---|
| Auto-Harvester | 40 | 32 | 24 | 18 | 10 |
| Auto-Miner | 60 | 48 | 36 | 24 | 15 |
| Auto-Fisher | 200 | 160 | 120 | 80 | 50 |

Auto-Miner ore regrowth delay by tier (ticks): 200, 150, 100, 60, 30.

These numbers are a starting balance pass, not a final one — tune the tier arrays in each
`Auto*BlockEntity` class (`TICK_INTERVALS_BY_TIER`) and `AutoMinerBlockEntity`
(`REGEN_TICKS_BY_TIER`) if they feel off, and the recipe files under
`data/greenward/recipe/*_upgrade.json` if the crafting cost needs adjusting.

### Fuel values

| Fuel item | Operations |
|---|---|
| `minecraft:coal` | 8 |
| `minecraft:charcoal` | 8 |
| `minecraft:coal_block` | 80 |
| `minecraft:dried_kelp` | 4 |
| `minecraft:dried_kelp_block` | 40 |
| `greenward:wheat_bale` | 100 |

One "operation" = one work action (one crop harvested-and-replanted, one block mined, one
fishing roll).

### Ungated treasure fishing

`data/minecraft/loot_table/gameplay/fishing.json` overrides vanilla's top-level fishing
table to drop the open-water requirement on the treasure pool, so treasure loot (name tags,
saddles, enchanted books/rods/bows, nautilus shells) can come up anywhere — from a 1×1 hole,
under a roof, wherever. This applies to players and the Auto-Fisher alike. Fish/junk weights
are otherwise untouched vanilla.

### Feature flags

`com.green.ward.GreenwardConfig` — flip a flag to `false` to disable a subsystem without
deleting its code or assets (unregistered content simply never appears in game):

- `ENABLE_CONDENSED_CARROTS` (default `false`) — gilded/radiant carrot tiers. Dormant.
- `ENABLE_WHEAT_COMPRESSION` (default `true`) — wheat sheaf/bale. **Keep this true** — the
  bale is a fuel tier the machines depend on.
- `ENABLE_FERTILIZER` (default `true`) — fertilizer item, fertilized farmland, yield bonus.
- `ENABLE_AUTOMATION` (default `true`) — the three machine blocks.
- `ENABLE_UNGATED_TREASURE_FISHING` (default `true`) — see above. **This one only gates the
  Auto-Fisher's own loot roll.** The player-facing override file is plain data and can't
  read the Java flag — to restore vanilla's open-water gate for players, delete or rename
  `data/minecraft/loot_table/gameplay/fishing.json` yourself.

**If you flip `ENABLE_CONDENSED_CARROTS` back to `true`**, also remove the
`"fabric:load_conditions": [{"condition": "fabric:false"}]` block from
`gilded_carrot.json`, `gilded_carrot_uncraft.json`, `radiant_carrot.json`, and
`radiant_carrot_uncraft.json` under `data/greenward/recipe/` — those blocks were added
because a recipe referencing an unregistered item logs a (harmless but noisy) parse error
at data-pack load. The same caveat applies if you ever flip `ENABLE_WHEAT_COMPRESSION` or
`ENABLE_FERTILIZER` off: their recipe files (`wheat_sheaf.json`, `wheat_bale.json`, etc.,
`fertilizer.json`) will log the same class of error unless you add the same
`fabric:load_conditions` block to them.

### Placeholder textures

`auto_fisher(.png/_lit.png)` and `auto_miner_top/side/front(.png/_front_lit.png)` under
`assets/greenward/textures/block/` are still procedurally-generated placeholders (solid
color + border + inset glow on the lit variant). `auto_harvester(.png/_lit.png)` got a
pass beyond that — a hand-designed pixel pattern (a four-blade reel radiating from a
central hub, on a paneled body; the hub glows gold when lit) meant to actually read as a
harvesting machine at a glance, not just a colored square. None of these are final art —
swap them out whenever the artist is ready; nothing else needs to change (models reference
these filenames directly).

### Field Guide

`greenward:field_guide` (craft: 8 paper around a Wheat Sheaf) is a plain readable book —
right-click to open it — listing every recipe this update adds, page by page. It's not a
real `minecraft:written_book` (no data-component wrangling); the client just opens the
vanilla book-reading screen directly with hardcoded text when you use the item. See
`GreenwardClient.FIELD_GUIDE_PAGES`.

Separately, **every new recipe now also has a matching `data/greenward/advancement/recipes/*.json`**,
so the vanilla recipe book picks them up the normal way (this wasn't automatic — a
recipe with no matching advancement is simply never unlocked in survival, checked against
the decompiled source this session). Each grants on picking up one relevant vanilla
ingredient (e.g. an iron hoe unlocks the Auto-Harvester recipe). This is a separate,
lower-effort path to visibility than the Field Guide — no JEI dependency exists in this
project to integrate with, so the guide book was the more reliable "browse everything at
once" option; the recipe-book advancements are what make the recipes discoverable
in-context while playing.

## Corrections made against the original implementation spec

The spec was written as intent, not gospel, per the project's own instructions — every
unfamiliar symbol was verified against the decompiled 26.2 jar and Fabric API 0.156.0+26.2
jars before use. Notable corrections:

- **Block entity NBT is not `CompoundTag` anymore.** `BlockEntity.loadAdditional` /
  `saveAdditional` take `ValueInput` / `ValueOutput` (a codec-based read/write interface),
  not raw NBT tags. Reads use `getIntOr(key, default)` / `getString(key)` etc.; writes use
  `putInt(key, value)` etc. `ContainerHelper.loadAllItems(ValueInput, NonNullList)` /
  `saveAllItems(ValueOutput, NonNullList)` handle the fuel slot's `ItemStack` persistence.
- **Block entity types are built via `FabricBlockEntityTypeBuilder.create(factory, blocks...).build()`**
  (`net.fabricmc.fabric.api.object.builder.v1.block.entity`), registered under
  `Registries.BLOCK_ENTITY_TYPE` — not a raw `new BlockEntityType<>(...)`.
  `BlockEntityTicker.tick(...)` takes `Level`, not `ServerLevel`; cast inside.
- **`BaseEntityBlock` re-abstracts `codec()`.** Any concrete subclass (all three machine
  blocks) must implement `codec()` returning `Block.simpleCodec(YourBlock::new)`, or it
  won't compile.
- **Hopper insertion is `WorldlyContainer`**, implemented directly on the block entity
  (not `BaseContainerBlockEntity`, which drags in `MenuProvider`/`createMenu` machinery
  the spec explicitly says these blocks don't need — no GUI).
- **Shared blockstate properties**: `BlockStateProperties.LIT` and
  `BlockStateProperties.HORIZONTAL_FACING` already exist and are reused across vanilla
  blocks (furnace, etc.) — no need to declare fresh `BooleanProperty`/`EnumProperty`
  instances per block class.
- **Fishing loot roll**: `level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING)`,
  rolled via `LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, ...)
  .withParameter(LootContextParams.TOOL, itemStack).create(LootContextParamSets.FISHING)`.
  `LootContextParams.TOOL` is typed `ContextKey<ItemInstance>` — `ItemStack` implements
  `ItemInstance` in 26.2, so it drops in directly.
- **Miner block breaking**: `Block.dropResources(state, level, pos)` (no player/tool, so no
  Fortune) + `level.removeBlock(pos, false)`, rather than `Level.destroyBlock`, for full
  control with no dependency on a (possibly null) causing entity.
- **Two pre-existing loot tables were miscategorized and effectively dead**:
  `fertilized_farmland.json` and `wheat_bale.json` lived under
  `assets/greenward/loot_table/blocks/`. Loot tables are data-pack content, loaded from
  `data/<namespace>/loot_table/...` — the resource-pack half of the jar (`assets/`) is
  never read by the loot table system. Both were moved to
  `data/greenward/loot_table/blocks/` with identical content; this means mining
  fertilized farmland or a wheat bale now actually drops something, which — per the
  server log — it silently did not before this update.
- **A dedicated server refuses to load ANY class that references a client-only type
  anywhere in its bytecode — even inside a branch guarded by `level.isClientSide()` that
  never executes there.** This project has a single source set (no split client/common
  jars), so it's easy to assume an `if (isClientSide())` guard is enough; it isn't. The
  server crashed at startup (`Cannot load class net.minecraft.client.gui.screens.Screen
  in environment type SERVER`) the first time the Field Guide's book-opening code lived on
  an `Item` subclass reachable from `ModItems` — because bytecode verification touches
  every type a class file references, guard or no guard. Fix: keep client-only logic
  entirely inside classes only ever reached from the `client` entrypoint (`GreenwardClient`
  and friends), exactly like this project's `AutoHarvesterScreen` etc. already do — reached
  it here via `UseItemCallback.EVENT.register(...)` registered from
  `GreenwardClient.onInitializeClient()` instead of overriding `Item.use()` on a
  commonly-loaded class. Worth grepping `net.minecraft.client` across the package before
  every future release to make sure nothing new leaked in.
- **`fabric:false` beats the "nonexistent mod" load-condition trick.** The spec's §6.4
  fallback (`fabric:all_mods_loaded` with a made-up mod id) works, but Fabric API ships a
  dedicated always-false condition type (`{"condition": "fabric:false"}`,
  `net.fabricmc.fabric.impl.resource.conditions.conditions.FalseResourceCondition`) that
  says what it means. Used on the four condensed-carrot recipes.
- **Fertilizer→fertilized-farmland moisture preservation was listed as existing behavior
  but wasn't actually implemented** — the pre-existing callback just called
  `.defaultBlockState()` (moisture reset to 0) instead of carrying over
  `FarmlandBlock.MOISTURE` from the clicked farmland. Fixed, since the spec's own
  regression checklist requires it.
- The existing fertilizer item's registered id is `greenward:fertilizer` (field
  `ModItems.FERTILIZER`), not `greenward:basic_fertilizer` as the spec's prose implied —
  kept the existing id/field name and just simplified the recipe cost in place.

## Testing performed

- `./gradlew build` — clean compile + resource packaging.
- `./gradlew runClient` — booted to the main menu with the mod loaded, texture atlas built
  with no missing-texture/model warnings.
- `./gradlew runServer` on a fresh world — full data-pack load with **zero** errors
  (1595 recipes loaded, including the 3 machine + 3 upgrade recipes and the simplified
  fertilizer recipe; the 4 disabled condensed-carrot recipes correctly excluded via
  `fabric:load_conditions` instead of erroring), reached `Done`.
- **Live server verification via RCON** (headless — no GUI, so this drives the real block
  entities directly through `/data get`/`/item replace`/`/data merge` rather than a mouse):
  confirmed hopper fuel insertion, ticking, crop scan/harvest/replant, and the fertilized
  2x bonus (see prior spec-implementation testing above); then, for the upgrade system,
  confirmed a harvested crop's drops land in the block's own storage slots (not the world)
  with the correct fertilized doubling, confirmed the machine **stops harvesting once all
  unlocked storage slots are full** (crop stays unharvested, fuel stops depleting) and
  **resumes immediately** once `storage_tier` increases and a newly-unlocked slot exists,
  confirmed a `speed_tier` increase measurably shortens the operation interval, and
  confirmed the Auto-Miner's ore regrowth loop — mining `minecraft:iron_ore` repeatedly
  regrew and got re-mined on its own (raw iron accumulated well past what one ore block
  could drop), with the block correctly absent while its regrowth timer is still counting
  down. Auto-Fisher wasn't independently re-verified this round since its output now
  reuses the exact same `depositOrDrop` storage path already proven on the harvester.

**Not covered** (no GUI automation available in this environment — needs a human pass):
actually opening a machine's screen and looking at it, dragging an upgrade item into its
slot by hand (the RCON tests above set tier fields directly via NBT to prove the
*consequences* of an upgrade, not the slot-consumption interaction itself), the ore seed
slot's plant-a-copy bootstrap from an empty spot, hopper *extraction* from storage slots,
the Auto-Miner's blacklist against a live chest/bedrock, the Auto-Fisher's water-adjacency
check end-to-end, watching the `lit` state and GUI progress bars actually render, and the
world-reload persistence check for the new storage/tier/regen fields. Work
through §8 of the original spec's testing checklist in a live session before calling this
done-done.

---

# Gear & God Potion + Sea Creatures Update

Builds on the Automation Update above. Three armor sets and tool lines — one per pillar
(farming/mining/fishing) — reward automating a pillar with gear that makes doing that
pillar better, and manual fishing gets its own reward track (sea creatures) so the
Auto-Fisher's passive bulk output never fully replaces picking up a rod yourself.

## Armor sets

Each set is 4 pieces (helmet/chest/legs/boots), vanilla-tier protection (never exceeds
netherite), and does nothing unless **all 4 pieces** are worn — checked every 20 ticks by
`SetBonusHandler`, not every tick, so several players in full sets costs nothing measurable.
Every granted effect is ambient with hidden particles — no permanent swirl.

- **Harvester's Garb** (leather-durability, 13 protection, repairs on Wheat Bale) — Haste I
  while holding any hoe; crops you harvest have a 10% chance at one extra drop (rolled
  once, additive with the fertilized-farmland 2x, never compounded); **+1 hoe harvest
  radius** for any hoe (a diamond hoe becomes 7×7).
- **Prospector's Plate** (iron-durability, 17 protection, toughness 1.0, repairs on Iron
  Ingot) — Haste I always; Night Vision below Y0 (fades naturally above it); +1 safe fall
  distance via the real `Attributes.SAFE_FALL_DISTANCE` attribute (no fall damage from a
  4-block drop instead of vanilla's 3).
- **Angler's Wear** (turtle-scute-durability, 13 protection, repairs on Prismarine Shard) —
  Water Breathing; Dolphin's Grace; **+1 Luck attribute** (see substitution note below).

## Tools

One modest upgrade per pillar over diamond — never a netherite replacement — plus, per an
explicit design note in this update, **three fishing rod tiers** instead of one, since a
single "Angler's Line" read as skippable on its own:

- **Harvester's Scythe** — functions as a hoe (existing hoe-harvest logic just works).
  Radius 2 base (diamond-equivalent), 3 with the full Harvester's Garb. Durability 1200.
- **Prospector's Drill** — functions as a pickaxe, diamond mining level, ~15% faster than
  diamond (`ToolMaterial.speed` 9.2 vs. diamond's 8.0). Durability 1400. Deliberately no
  vein-miner/3×3 — that reads as non-vanilla; speed is the whole point.
- **Angler's Line → Deep-Sea Rod → Leviathan Rod** — each a real upgrade craft (Line →
  + prismarine crystals + a nautilus shell → Deep-Sea Rod → + a heart of the sea + more
  nautilus shells/crystals → Leviathan Rod). Durability 256 / 512 / 1024. Each carries
  **genuine baked-in enchantments** (Lure I/II/III, Luck of the Sea I/II on tiers 2–3) —
  not a virtual bonus, an actual default `ENCHANTMENTS` item component, so vanilla's own
  fishing code (which already reads enchantment levels off the held rod) applies them with
  zero mixins. Each tier also raises the sea-creature chance and **gatekeeps which
  creatures it can catch** — see below.

## God Potion

The capstone, requiring output from all three pillars:

**pillar output → catalyst → all three catalysts + Nether Star + Dragon's Breath → Godly
Catalyst → brew with an Awkward Potion in a vanilla brewing stand → God Potion**

- `greenward:golden_harvest` (farming: Wheat Bale + Golden Carrots + Golden Apple)
- `greenward:deepstone_core` (mining: Obsidian + Diamond + Netherite Scrap)
- `greenward:abyssal_pearl` (fishing: Prismarine Crystals + Heart of the Sea + Nautilus
  Shell — both obtainable via treasure fishing, which the Automation Update ungated; this
  is that update's deliberate payoff)
- `greenward:godly_catalyst` (all three catalysts + Nether Star + Dragon's Breath)
- **God Potion**: brewed, not crafted — Awkward Potion + Godly Catalyst in a brewing
  stand, hopper-automatable like any vanilla brew. 8 minutes of Strength I, Resistance II,
  Speed II, Haste II, Regeneration I, Fire Resistance, Water Breathing, Night Vision, and
  Jump Boost I — all ambient/hidden-particle. No splash/lingering variant is registered
  (only the plain-potion mix exists). Re-drinking refreshes rather than stacking (plain
  vanilla effect-application behavior with matching amplifiers).

**Verified against a real brewing stand this session** (RCON-driven: 3 Awkward Potions +
Godly Catalyst + fuel in a `minecraft:brewing_stand`'s NBT slots) — `BrewTime` counted down
from 400 and all three bottles came out as `greenward:god`, with the catalyst consumed.
This proves the registration is correct; it does not by itself prove all 9 effects apply at
the stated amplifiers when actually drunk — that still wants a human pass.

## Sea creatures (manual fishing only)

Sea creatures only ever spawn from a **real player catch** — never from the Auto-Fisher.
This isn't a code-path assumption; it's structural: a real catch is the only caller that
puts the `FishingHook` entity in the loot context as `LootContextParams.THIS_ENTITY`
(confirmed against `FishingHook.retrieve()`'s decompiled source this session), and the
Auto-Fisher's own loot roll never sets that parameter. `SeaCreatureHandler` hooks
`LootTableEvents.MODIFY_DROPS`, filters to the top-level fishing table, and only proceeds if
`THIS_ENTITY` is present and is a `FishingHook` — so even if the event fires more broadly
than expected, the Auto-Fisher path can't match.

**Spawn chance** (base + rod tier + Angler's Wear, capped at 100%):

| Held rod | Chance | + full Angler's Wear |
|---|---|---|
| Vanilla / unlisted rod | 5% | 20% |
| Angler's Line (tier 1) | 10% | 25% |
| Deep-Sea Rod (tier 2) | 20% | 35% |
| Leviathan Rod (tier 3) | 35% | **85%** (tier-3-rod + full-set synergy bonus) |

**Rod gatekeeping** — a rod can only land creatures at or below its tier; a roll for a
creature the held rod can't catch is *re-rolled among only what that rod can catch*, so a
"hit" always produces a creature, never a dud:

| Creature | Weight | Base | Vanilla mob | Tweaks | Extra drops |
|---|---|---|---|---|---|
| Drenched Husk | 45% | any rod | Drowned | — | 1–3 Prismarine Shard |
| Bloated Swarm | 15% | any rod | Pufferfish ×4 | — | 1–2 Pufferfish each |
| Reef Stalker | 25% | Angler's Line+ | Guardian | — | 2–4 Prismarine Crystals |
| Tidecaller | 10% | Deep-Sea Rod+ | Drowned | +50% HP, holds a Trident | 1 Nautilus Shell, 2–5 Prismarine Crystals |
| Abyssal Warden | 5% | Leviathan Rod only | Elder Guardian | +100% HP, Mining Fatigue curse suppressed | 1 Heart of the Sea, 1 Nautilus Shell, 4–8 Prismarine Crystals |

All are plain vanilla mobs (no custom entity types/models/AI) with a custom name, forced
persistence (won't despawn), and a Fabric Data Attachment marker
(`AttachmentRegistry.createPersistent`, not raw NBT — Fabric's sanctioned way to tag an
entity without a mixin) identifying them for the death-drop hook
(`ServerLivingEntityEvents.AFTER_DEATH`). Tidecaller and Abyssal Warden drops are exactly
the Abyssal Pearl's ingredients — manual fishing is the fast route to the fishing catalyst,
the Auto-Fisher's ungated treasure table is the slow passive one. Both work.

**Not independently mixin-required**: the Abyssal Warden's vanilla Mining Fatigue curse
(applied to *nearby players*, not itself, so it can't just be stripped off the entity) is
suppressed by a lightweight periodic check — if an online player has Mining Fatigue and a
tagged Abyssal Warden is within 32 blocks, the effect is removed. Pragmatic, not a mixin
into `ElderGuardian`'s internals.

## New feature flags

- `ENABLE_GEAR_SETS` (default `true`) — the three armor sets and their bonuses.
- `ENABLE_CUSTOM_TOOLS` (default `true`) — Scythe, Drill, and the three rod tiers.
- `ENABLE_GOD_POTION` (default `true`) — the four catalysts, the God Potion, and its brew.
- `ENABLE_SEA_CREATURES` (default `true`) — manual-fishing sea creature spawns.

Same pattern as every other flag in this project: flip to `false`, the content simply
never registers; assets/recipes/advancements stay on disk. If you ever flip one off, the
same `fabric:load_conditions` caveat documented earlier for the condensed carrots applies
to any of this update's recipes that reference an item from a *different* flag that's
still on (e.g. `godly_catalyst`'s recipe references `golden_harvest`/`deepstone_core`/
`abyssal_pearl`, all gated by the same `ENABLE_GOD_POTION` flag, so that particular case is
safe — but cross-flag references, if you ever add any, would need the same treatment).

## Substitutions made against the spec (documented, not silently dropped)

- **Angler's Wear fishing-wait-time reduction → +1 Luck attribute.** `FishingHook`'s bite
  countdown (`nibble`/timing fields) is private with no Fabric event covering it. The spec
  explicitly sanctioned this exact substitution rather than a mixin; applied via
  `Attributes.LUCK` (a real vanilla attribute) through the same set-bonus tick handler.
- **God Potion color → vanilla's automatic blend, not a forced "gold".** Brewing produces
  the result via `PotionContents.createItemStack(item, potionHolder)` with no color
  override hook reachable from `FabricPotionBrewingBuilder`'s recipe registration API.
  Forcing a specific color would need either a mixin or building a custom brewing
  ingredient item with a components override baked into the *input* item, which is more
  moving parts than the payoff justified. The potion still reads as visually distinct
  (auto-blended from nine different effect colors), just not guaranteed literally gold.
- **Deep-Sea Rod / Leviathan Rod are this update's own addition, not the original spec's.**
  The spec asked for exactly one Angler's Line; the brief for *this* update explicitly
  asked to expand rod tiers ("3 different tiers... gatekeep which creatures can be
  caught") and to make tools feel load-bearing rather than skippable, so two more tiers
  were added with their own recipes/advancements, matching the fuel-tier-style upgrade
  pattern already established by the automation machines' storage/speed upgrades.

## Placeholder art

All 21 new item icons (12 armor pieces, 5 tools, 4 catalysts) are procedurally generated
placeholders — armor pieces use a shared silhouette per equipment slot (helmet/chest/
legs/boots) tinted per set (gold/wheat for Harvester, grey/blue for Prospector, teal/green
for Angler); tools use a simple diagonal blade glyph; catalysts reuse the diamond-badge
motif from the automation update's upgrade items. **Worn-armor textures reuse vanilla's own
equipment assets** (`EquipmentAssets.LEATHER` / `IRON` / `TURTLE_SCUTE`) as placeholders —
building real custom equipment assets was out of scope for a first pass; swap the
`assetId` in `ModArmorMaterials` for a real one once art exists. None of this needs a code
change beyond that — models/textures are referenced by filename only.

## Testing performed this session

- `./gradlew build` — clean.
- `./gradlew runServer` on a fresh world — **zero errors**: 1617 recipes (+21), 1721
  advancements (+21) loaded correctly, including every armor/tool/catalyst recipe and its
  matching recipe-book-unlock advancement.
- `./gradlew runClient` — texture atlas built with all 21 new item icons, no missing-
  texture/model warnings, no crash.
- **God Potion brewing chain, live, via RCON** (see above) — genuinely proven end-to-end
  through a real brewing stand, not just "the code compiles."

**Not covered** (no real client/player connection available in this environment — RCON is
a console connection, not a player entity, so anything that needs a `Player` in the world
is untestable headlessly): actually wearing any armor piece and confirming a set bonus
fires or clears on removal, the hoe-radius/extra-drop bonus in practice, fall-damage
reduction, Night Vision fading at the Y0 boundary, the Luck attribute's effect on real
fishing rolls, catching an actual sea creature with each rod tier and confirming the
gatekeeping/chance table, watching the Abyssal Warden's curse-suppression trigger, and
seeing all 21 new item icons and 12 worn-armor pieces rendered in-game. All of this needs a
real playtest before calling the update done-done — the data/registration layer is solid,
the moment-to-moment feel is unverified.

---

# Four Pillars Progression — Farming leg

The first of four planned pillar ladders (Mining/Fishing/Combat come later, one at a time —
building all four before confirming one works risked compounding the same mistake four
times over). Each pillar will get its own two-chain compression ladder feeding three gear
tiers, where **Tier I is already the existing single-tier gear** from the Gear & God Potion
update and Tiers II/III are new.

## Decisions made against the draft spec

The draft named specific compression items "don't follow exactly what items it used... use
items that make sense given the current state of the pack that are farmable, if it is
questionable ask" — three points came back with answers before writing any code:

- **Secondary chain is Potato, not Sugar Cane.** Sugar cane isn't a `CropBlock` (it grows by
  height, not age), so the existing Auto-Harvester — which only recognizes true `CropBlock`
  instances — can't touch it without new automation code. Potato already is a `CropBlock`,
  so it's auto-farmable today with zero engine changes.
- **Rare ingredient is the real Radiant Carrot, not a substitute.** `ENABLE_CONDENSED_CARROTS`
  is now `true` — Gilded Carrot and Radiant Carrot are live, obtainable items for the first
  time since the original automation update shipped them dormant. Their recipe files had
  their `fabric:false` load-condition removed to match (see `GreenwardConfig`'s comment on
  the flag for what to restore if this ever flips back off).
- **Tier III is a real `minecraft:smithing_transform` recipe**, matching vanilla's own
  Diamond→Netherite upgrade exactly: Tier II piece (base) + Harvest Core (addition) +
  Ascension Template (template) → Tier III piece, in a real smithing table. The Ascension
  Template is a **plain `Item`**, not vanilla's `SmithingTemplateItem` class (which needs a
  batch of tooltip Components/icon Identifiers per template) — functionally identical for
  recipe matching, just without the fancy "applies to" tooltip vanilla templates get.
  Shared across all four pillars' future Tier III upgrades rather than one template per
  pillar, craftable (diamond + obsidian + emerald, yields 2) so it's never a hard bottleneck.

## The farming ladder

| Tier | Consumes | Armor | Tool |
|---|---|---|---|
| I *(existing)* | Wheat Sheaf (Primary I) | Harvester's Garb | Harvester's Scythe |
| II | Wheat Bale (Primary II) + Potato Sack (Secondary I) | Cultivator's Garb | Cultivator's Scythe |
| III | Harvest Core (Catalyst), via smithing upgrade from Tier II | Warden's Garb | Harvest Warden |

**Material chains**: Wheat → Wheat Sheaf (9×) → Wheat Bale (9×, existing); Potato → Potato
Sack (9×) → Potato Crate (9×, new); Harvest Core = Wheat Bale + Potato Crate + Radiant
Carrot (shapeless).

**Cultivator's Garb** (Tier II) — diamond-equivalent stats (20 total protection, 2.0
toughness), Haste II while holding any hoe, 20% extra-crop-drop chance (up from Tier I's
10%). Deliberately **no hoe-radius bonus** — see below.

**Warden's Garb** (Tier III) — this is the pillar that's allowed to exceed netherite: 24
total protection, 4.0 toughness, 0.6 knockback resistance (netherite: 20 / 3.0 / 0.4) —
numbers mirror the Combat pillar's own stated Reaper's Aegis table so every pillar's Tier
III lands at the same ceiling. Haste II, plus Regeneration I always on. Repairs on Potato
Crate; Harvest Warden repairs on Harvest Core itself.

### Hoe radius: only the full Tier III combo reaches the cap

The draft says Harvest Warden should reach "full 9×9 harvest (radius 4) — the only tier
that reaches the cap." A single "+1 radius per full set, stacks with any hoe" rule (the
existing Tier I mechanic) would let a Tier II tool paired with old Tier I armor sneak up to
the cap by accident, so the bonus is now scoped per-tier instead of universal:

- Diamond/Netherite hoe, Harvester's Scythe: base 2, **+1 only with full Harvester's Garb** → 3
- Cultivator's Scythe: flat base 3, no armor-driven bonus at all (still caps at 3)
- Harvest Warden: base 3, **+1 only with full Warden's Garb specifically** → 4, the true cap

Mixing tiers (e.g. Harvest Warden with old Harvester's Garb) never reaches 4 — only the
matched Tier III tool *and* armor together do. Same logic for the extra-drop-chance roll:
10% / 20% / 35% by matching armor tier, read fresh at harvest time rather than stacked.

## Not independently verified this session

Brewing was live-testable via RCON (a brewing stand has a real server-side inventory);
**crafting-table and smithing-table recipes are not** — neither block type has a
persistent inventory to manipulate headlessly the way a brewing stand does, so there is no
RCON-only way to prove a smithing_transform actually produces the right item with the
right stats. What *is* confirmed: the exact expected recipe/advancement counts loaded
(1635 recipes, 1739 advancements, +18 over the previous update, zero parse errors) — a
malformed `base`/`addition`/`template` reference (e.g. a typo'd item id) would have thrown
the same kind of visible error the disabled condensed-carrot recipes did earlier this
session, so the absence of any such error is real evidence the schema and ingredient
references are correct. It is not proof the upgrade *feels* right at the smithing table —
that needs a human pass, same as the other unverified-by-necessity items above.

---

# Four Pillars Progression — Mining, Fishing, Combat legs + God Potion rework

Finishes what the Farming leg started: Mining and Fishing each get the same Tier II/III
treatment on top of their existing Tier I gear from the Gear & God Potion update, and
Combat gets a brand-new three-tier ladder built from nothing (it never had a Tier I set).
The God Potion's own recipe was reworked in the same pass — see below.

**Bug fixed in the same pass**: `potato_sack`/`potato_crate` (Farming leg) had accidentally
shipped with a 5-item helmet-shaped crafting pattern copy-pasted from an armor recipe,
instead of the intended full 3×3 (9-item) compression pattern every other compression
recipe in the mod uses. Fixed to `PPP`/`PPP`/`PPP` — both now correctly cost 9 inputs.

## The mining ladder

| Tier | Consumes | Armor | Tool |
|---|---|---|---|
| I *(existing)* | Iron + Diamond | Prospector's Plate | Prospector's Drill |
| II | Cobble Monolith (Primary II) + Deepslate Cluster (Secondary I) | Excavator's Plate | Excavator's Pick |
| III | Bedrock Core (Catalyst), via smithing upgrade from Tier II | Bedrock Plate | Bedrock Reaver |

**Material chains**: Cobblestone → Cobble Cluster (9×) → Cobble Monolith (9×); Cobbled
Deepslate → Deepslate Cluster (9×) → Deepslate Monolith (9×); Bedrock Core = Cobble
Monolith + Deepslate Monolith + Amethyst Shard (shapeless). Amethyst Shard as the Rare
ingredient is deliberately renewable — budding amethyst regrows shards over time, so unlike
a boss drop it's a real (if slow) farmable loop.

**Excavator's Plate** — diamond-equivalent (20 total, 2.0 toughness), Haste II always,
carries Prospector's Night-Vision-below-Y0 and safe-fall bonuses forward on its own
attribute-modifier id (so a mid-run tier swap can't leave a stale modifier applied).
**Bedrock Plate** — same Tier III ceiling as every other pillar (24 total, 4.0 toughness,
0.6 knockback resistance across a full set), adds Fire Resistance. Bedrock Reaver is the
fastest pickaxe in the mod (mining speed 12.0, ahead of Prospector's Drill's 9.2 and
netherite's own 9.0).

**Substitution against the draft spec**: the spec's flavor text for the mining Tier III
pickaxe was "toggleable 3×3 vein mining + auto-smelt." Implementing real vein-mining would
need a `PlayerBlockBreakEvents`-style hook plus a toggle-state UI this pass didn't have
room for safely; Bedrock Reaver ships as a pure stats capstone (speed + durability + attack
damage, all exceeding every other tier) instead. Documented here rather than silently
dropped — worth revisiting as a follow-up if the vein-mining feel is wanted later.

## The fishing ladder

| Tier | Consumes | Armor | Tool |
|---|---|---|---|
| I *(existing)* | Prismarine Shard + Turtle Scute | Angler's Wear | Angler's Line → Deep-Sea Rod → Leviathan Rod |
| II | Cod Shoal (Primary II) + Dried Kelp Block (Secondary I) | Tidal Wear | *(none new)* |
| III | Leviathan's Heart (Catalyst), via smithing upgrade from Tier II | Leviathan's Wear | *(none new)* |

**Material chains**: Cod → Cod School (9×) → Cod Shoal (9×); Dried Kelp Block (vanilla,
reused directly as Secondary I, same as the draft spec) → Kelp Reef (9×, Secondary II);
Leviathan's Heart = Cod Shoal + Kelp Reef + Heart of the Sea (shapeless).

**Deliberately no new rod tier.** The Gear & God Potion update already built a complete,
tested three-tier rod ladder (Angler's Line → Deep-Sea Rod → Leviathan Rod) with its own
sea-creature-gatekeeping mechanic that already reaches this pillar's "near-guaranteed catch"
cap. Re-deriving a fourth rod tier under the Four Pillars naming scheme would only have
added a confusing extra item with no new mechanic behind it, so the existing Leviathan Rod
stands as this pillar's realized Tier III tool. Only the armor got the II/III treatment.

**Tidal Wear** — diamond-equivalent (20 total), Water Breathing + Dolphin's Grace + Luck+2
(own attribute-modifier id, doesn't stack with Tier I's or Tier III's). **Leviathan's Wear**
— same Tier III ceiling as every other pillar, Luck+3, adds Night Vision while underwater.

## The combat ladder (built from nothing)

Combat never got a Tier I set in the Gear & God Potion update (that update shipped exactly
three sets: Harvester/Prospector/Angler). This leg builds all three tiers fresh.

| Tier | Consumes | Armor | Weapon |
|---|---|---|---|
| I | Bone Bundle (Primary I) | Marrowguard | Marrowguard Blade |
| II | Bone Reliquary (Primary II) + Powder Satchel (Secondary I) | Ashwrought | Ashwrought Edge |
| III | Reaper's Core (Catalyst), via smithing upgrade from Tier II | Reaper's Aegis *(spec's own name)* | Reaper's Edge *(spec's own name)* |

**Material chains**: Bone → Bone Bundle (9×) → Bone Reliquary (9×); Gunpowder → Powder
Satchel (9×) → Powder Cache (9×); Reaper's Core = Bone Reliquary + Powder Cache + Ender
Pearl (shapeless).

**Marrowguard** (Tier I) — iron-equivalent (15 total, chainmail-durability), starts at the
same power level the other three pillars' Tier I did, since there's no prior gear to build
on. Attack speed +10% with a full set. **Ashwrought** (Tier II) — diamond-equivalent (20
total), attack speed +15%, attack damage +1. **Reaper's Aegis** (Tier III) — the table
every other pillar's own Tier III was built to match (24 total / 4.0 toughness / 0.6 kb),
attack speed +20%, attack damage +2, plus two named set bonuses straight from the spec:

- **Hunter's Mark Aura** — full Reaper's Aegis jumps The Marked's spawn chance straight to
  its 10% cap, same mechanism as Angler's Wear pushing sea-creature odds to their own cap.
- **Grim Resolve** — once every 5 minutes, a hit that would kill a player wearing full
  Reaper's Aegis instead leaves them at 1 HP. Implemented via Fabric's
  `ServerLivingEntityEvents.ALLOW_DEATH` (returning `false` cancels the death, the same
  sanctioned hook vanilla's own Totem-of-Undying-style saves use) — no mixin. A real Totem
  still saves first if the player is carrying one; this only fires when nothing else did.

### The Marked

Reworked from the spec's "Champions" mechanic it references but doesn't fully restate,
built structurally analogous to Sea Creatures rather than guessed at:

| Mob | Marked title | Bonus drop |
|---|---|---|
| Zombie | Revenant | — |
| Skeleton | Deadeye | — |
| Spider | Widowfang | — |
| Creeper | Ashborn | 2–4 Gunpowder (feeds Powder Satchel directly) |
| Enderman | Wraith | 1 Ender Pearl guaranteed (the Catalyst's Rare ingredient) |

Every Marked also drops 2–4 bonus Bone regardless of type. Trigger: `LivingEntityEvents
.AFTER_DEATH` on one of the five base mobs, gated on `DamageSource.isDirect()` **and**
`DamageSource.getEntity() instanceof ServerPlayer` — structurally identical to how
`SeaCreatureHandler` proves the Auto-Fisher can't trigger sea creatures. A projectile kill
has a direct entity (the arrow/trident) different from its causing entity (the player), so
`isDirect()` is false; a fall/lava/campfire/explosion trap kill has no player entity at all.
Neither can ever satisfy both checks, so there is no automatable path to The Marked's loot —
this falls out of the data the same way the sea-creature gate does, not a code-path
assumption. Spawn chance: base 3%, +1% Marrowguard, +2% Ashwrought (mutually exclusive in
practice — only one full set can be worn at a time), capped at 10%; Reaper's Aegis jumps
straight to that cap via Hunter's Mark Aura.

## God Potion rework: fully automatable-adjacent, still difficult

The original Godly Catalyst needed a Nether Star and Dragon's Breath — genuine one-time
boss gates with no farmable path — plus Golden Harvest leaned on vanilla Golden Carrots,
redundant with the mod's own carrot-compression chain. Reworked so every ingredient
upstream of the Godly Catalyst can be farmed, mined, fished, or fought for repeatably:

- **Golden Harvest** (farming catalyst): now `4x Wheat Bale + 4x Potato Crate` (shaped,
  corners/edges, center empty) — was Wheat Bale + Golden Carrots + Golden Apple. Both
  Wheat Bale and Potato Crate come straight off the Auto-Harvester.
- **Deepstone Core** (mining catalyst): unchanged — Obsidian + Diamond + Netherite Scrap
  was already a reasonable "still difficult but not boss-locked" recipe (Diamond is
  Auto-Miner-farmable with the regrowth upgrade; Obsidian needs no automation at all, just
  a lava+water bucket; Netherite Scrap is a slow grind, not a hard gate).
- **Abyssal Pearl** (fishing catalyst): now `4x Prismarine Crystals + 4x Nautilus Shell +
  1x Heart of the Sea` (shapeless) — was 4x Prismarine Crystals + **4x** Heart of the Sea +
  1x Nautilus Shell. Crystals and Shells both drop from the Auto-Fisher's own ungated
  treasure table (this update's own earlier payoff mechanic); only the single Heart of the
  Sea remains a manual-only gate (buried treasure or an Abyssal Warden sea creature), same
  role Netherite Scrap plays for mining.
- **Godly Catalyst**: now `1x Golden Harvest + 1x Deepstone Core + 1x Abyssal Pearl + 1x
  Reaper's Core` (shaped plus-pattern, center empty) — was three catalysts plus a Nether
  Star and Dragon's Breath. One catalyst per pillar, nothing else. Reaper's Core doubles as
  both Combat's Tier III smithing addition *and* its God Potion contribution — a
  deliberate simplification versus Farming/Mining/Fishing's pattern of a separate
  smithing-catalyst item (Harvest Core/Bedrock Core/Leviathan's Heart) versus God-Potion-
  catalyst item (Golden Harvest/Deepstone Core/Abyssal Pearl); since Combat is new, there
  was no reason to invent a 21st item just to preserve that duplication.

"Automatable-adjacent" rather than fully automatable on its own: this mod has no
auto-crafting block, so a player (or a separate crafting-automation mod) still has to
actually combine the gathered materials at a table. What changed is that nothing in the
chain requires a one-time, unrepeatable action (killing the ender dragon or a wither) —
every raw ingredient can be gathered again if you run out.

## Field Guide overhaul

Rewritten from a flat 9-page automation-only list (barely a third of the mod's actual
recipes) into a ~55-page guide organized **by pillar** instead of by shipping order — every
pillar's Tier I/II/III now sits on consecutive pages, directly addressing the confusion
where Farming's three tiers used to be split across the book with 20+ pages between
Harvester's Garb and Warden's Garb. A new dedicated "The Ascension" section explains the
shared Tier II→III smithing mechanic once (what gets consumed, that the Tier II piece
becomes its Tier III form rather than staying in inventory, and that one Ascension Template
craft yields 2 — enough for two of the five upgrades a full Tier III set needs) instead of
leaving it implicit on every Tier III recipe page. Presentation moved from flat literal
strings to a small internal `Page` builder in `FieldGuideContent.java` producing real
`ChatFormatting`-styled `Component`s — bold green recipe titles, purple section headers,
italic gray notes — still rendered through the same `BookViewScreen` as before (see the
Automation Update section above for why a real `minecraft:written_book` was skipped).

---

# Progression polish + Creative Tab

A balance pass across everything built so far, plus a "Greenward" creative-inventory tab
so every item the mod adds is browsable in one place without JEI.

## Combat pillar rebalanced down

Marrowguard and Ashwrought originally mirrored the exact same piece-cost pattern as every
other pillar's Tier I/II (5 / 2+2 / 6+1 / 7+1 raw materials per Hat-Boots-Leggings-Chest).
That's fine for Farming/Mining/Fishing, since Wheat, Cobblestone, and Cod are all
passively auto-farmable by their matching machine — but Combat has **no automation machine
at all**. Bone and Gunpowder can only come from manually killing mobs, so the identical
cost was a materially harsher grind than any other pillar's equivalent tier, without a
matching reason for it. Cut roughly 35% off both Marrowguard's and Ashwrought's armor and
weapon recipes (new pattern: 3 / 1+1 / 4+1 / 5+1) to compensate — still meaningfully more
expensive than Tier I ever was in the original Gear update, just not punishing relative to
the three pillars that get to automate their grind away.

**Restored to full parity (5 / 2+2 / 6+1 / 7+1) in Design Program Update 4** — Combat's
five Effigies close the automation gap this discount was compensating for, and the
Design Program's own § 4.3 text names this exact condition ("once Effigies ship, restore
Combat recipe costs to parity") for reversing it. See `UPDATING.md`'s Update 4 entry.

## Mining Tier III speed curve smoothed

Bedrock Reaver's mining speed was `12.0`, a 2.4-point jump over Excavator's Pick's `9.6` —
disproportionate next to the smaller steps everywhere else in that same ladder (diamond
`8.0` → Prospector's Drill `9.2` → Excavator's Pick `9.6`). Retuned to `10.5`, still the
clear fastest pickaxe in the mod, on a curve that actually looks like a curve.

## Fuel economy extended to Farming Tier II

`AutomationFuel` previously topped out at Wheat Bale (100 ops/item) — the highest tier of
fuel a player could reach was frozen at whatever the original Automation Update shipped
with, even after the Four Pillars update added a second farming compression chain. Potato
Crate (also a dense, farmable organic bale, same role as Wheat Bale) now burns for the same
100 ops, so the fuel ceiling scales alongside the rest of Farming instead of lagging behind
it.

## Creative Tab

A new "Greenward" tab (`ModCreativeTab.java`) lists every item the mod has registered.
Rather than hand-listing ~150 item fields scattered across `ModItems`/`ModArmor`/
`ModTools` — a list that would silently drift out of sync the next time an item is added —
`displayItems` filters `BuiltInRegistries.ITEM` down to entries in the `greenward`
namespace at render time, so it's automatically exhaustive and automatically respects
every feature flag (a disabled feature's items were never registered, so they simply never
appear — no separate filtering logic needed to match `GreenwardConfig`). Registered via
`net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder()` +
`Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ...)` — note this is a different
package than the older `itemgroup.v1` API some tutorials reference; verified against the
actual `fabric-creative-tab-api-v1` jar contents for this Fabric API version.

## Testing performed this session

- `./gradlew compileJava` — clean after every stage of this pass.
- `./gradlew runServer` on the existing world, three times across this session (after the
  Four Pillars mining/fishing/combat build, after the combat rebalance, and after the
  creative tab) — **zero errors** each time; final recipe/advancement count 1682/1786
  (+47 over the pre-Four-Pillars-Mining/Fishing/Combat baseline), matching the exact new
  item count with nothing orphaned or mis-referenced.
- Creative tab registration itself only fails loudly (a crash at startup) if the
  `Registries.CREATIVE_MODE_TAB` key collided or `displayItems` threw — neither happened,
  and the recipe/advancement counts above already prove every item referenced by name
  elsewhere in the mod (tags, recipes) still resolves correctly.

**Not covered**: actually opening the creative inventory and confirming the tab renders
where expected with the right icon and scroll behavior, and playtesting whether the
Combat rebalance now feels appropriately grindy rather than either trivial or still too
steep — both need a human pass in a real client.

---

# Design Program Revision 3 — Update 1: Permanence Retrofit + Stats & Item Identity

The first update of a ten-update long-term program (`GREENWARD_DESIGN_PROGRAM.md`,
Revision 3). Two halves: bring the mod into compliance with the Permanence
Charter (§ 1 — the world must survive the mod being removed), and add the stat layer
every later update attaches to.

## Permanence retrofit

**Fertilized farmland was a distinct block — the exact violation Appendix C flagged as
highest priority.** `greenward:fertilized_farmland` (`FertilizedFarmlandBlock extends
FarmlandBlock`) replaced vanilla farmland outright when fertilizer was used. Per § 1.1's
severity table, a custom block replacing a vanilla one is the worst category short of
worldgen/dimension/biome damage — removing the mod would have turned every fertilized
tile to air, deleting the farm built on top of it.

**Fixed**: fertilization is now `BlockPos`-keyed side-data (`FertilizedFarmlandData`, a
`SavedData` stored in the level's own save file, outside any chunk/block data vanilla or
another mod would ever read) — the block underneath is real `minecraft:farmland`
forever, and stays that way even with Greenward installed. `HarvestLogic` and the
fertilizer-use handler in `Greenward.java` both read/write this record instead of
touching the block.

**Migration, not deletion**: `FertilizedFarmlandBlock` is *not* removed — deleting the
class outright would have made every existing fertilized tile in an already-shipped
world turn to air on the very update meant to fix exactly that problem. Instead it's now
a pure migration shim: any surviving instance self-converts to vanilla farmland (moisture
preserved) plus a `FertilizedFarmlandData` record the moment it's random-ticked — the
same cadence vanilla farmland itself already ticks on, so existing worlds self-heal
during normal play with no dedicated migration pass. `/greenward decommission` also
sweeps and converts any stragglers immediately. **Safe to delete `FertilizedFarmlandBlock`
entirely in a future update** once enough time has passed that no unconverted world is
expected to remain — noted here rather than left implicit.

**Entity audit**: confirmed zero custom entity types registered anywhere (`grep -rn
"Registries.ENTITY_TYPE\|EntityType.Builder"` across the whole package — no matches).
Sea Creatures and The Marked already used vanilla `EntityTypes.*` with custom names and
attribute modifiers, which the Design Program's § 1.2 rule 5 now makes law rather than
just precedent. Also confirmed `SetBonusHandler`'s existing attribute modifiers already
use `addTransientModifier` (vanilla's own non-persisting API — verified against
`AttributeInstance`'s decompiled source, which tracks `permanentModifiers` in a
*separate* map from transient ones), so § 1.2 rule 6 was already satisfied there without
any change needed.

**`/greenward decommission [world]`**: sweeps chunks, drops every machine's inventory
(`Containers.dropContents`), and converts Greenward blocks to a sensible vanilla
equivalent (Wheat Bale → Hay Block, the three automation machines → Iron Block, any
leftover fertilized-farmland shim → real farmland via its own migration logic). Default
sweeps only already-loaded chunks around online players (fast, since nothing gets
force-generated); `world` widens to a 6-chunk radius around each online player *and*
around world spawn (so it still does something meaningful on a headless/no-player
server) — force-loading chunks via `level.getChunk(...)`. Per-chunk scanning is
section-fast-pathed (`LevelChunkSection.maybeHas`/`hasOnlyAir`) to skip chunk sections
that can't contain a Greenward block before doing the expensive per-block scan.

**Measured live via RCON, not assumed**: an earlier `world` radius of 24 chunks
(~2,400 chunks × 3 dimensions, each force-loaded/generated synchronously on the main
thread) blocked long enough to time out an RCON connection outright — confirmed via a
real hung command, not a guess. Cut to radius 6; a full `world` pass across all three
dimensions from a single spawn point (507 chunks total) now completes in ~10 seconds.
The bottleneck is chunk *generation* for previously-unvisited chunks, not the per-block
scan (which the section fast-path already keeps cheap) — worth knowing before increasing
the radius casually in a future pass.

## Stats

Implemented exactly the eleven stats in the spec table (`GreenwardStat` enum), no more:
Health, Defense, Strength, Crit Chance, Crit Damage, Speed, Mining Speed, Mining
Fortune, Farming Fortune, Sea Creature Chance, Fishing Speed. Magic Find, Ferocity,
Pristine, Breaking Power, Magical Power, and Intelligence/Mana are deliberately absent —
cut from the program, not forgotten.

**Architecture** (`Appendix B`'s "seam" requirement, taken literally): `StatContributor`
is a plain interface (`StatProfile contribute(ServerPlayer)` + a `label()` for
`/greenward stats`'s breakdown display); `PlayerStatManager` holds a list of registered
contributors, sums them plus each stat's base value into a cached `StatProfile` per
player, recomputed on `SetBonusHandler`'s existing 20-tick cadence (never per-tick).
Update 1 registers exactly one contributor — `EquipmentStatContributor`, which reads a
`GreenwardComponents.STATS` data component off every equipped armor piece and the
main-hand item. Skills (Update 2), Heartwood branches (Update 6), and Tempering (Update
8) each register their own contributor later without touching `PlayerStatManager` itself.

**Formulas** (`GreenwardFormulas`), verbatim from the spec and each checked against its
own acceptance numbers by hand before shipping:
- **Fortune**: `applyFortune(base, fortune, random)` — every 100 points guarantees one
  extra multiple of the base amount, the remainder becomes the percentage chance of one
  more. Verified against the spec's own three worked examples (100 → always 2×; 130 →
  always 2×, 30% chance of 3×; 150 → always 2×, 50% chance of 3×) before writing a
  single line of the consuming code.
- **Damage**: `(baseWeaponDamage + 5) × (1 + Strength/100) × (crit ? 1 + CritDamage/100 : 1)`.
- **Defense**: `damageTaken = incoming × (1 − Defense/(Defense + 100))`.

**Mining Fortune and Farming Fortune are wired into real gameplay, not just present as
numbers.** This wasn't explicit in the acceptance criteria's own prose but *is* two of
its six checkable line items ("100 Mining Fortune on diamond ore yields exactly 2, every
trial" / "150 yields 3 at 50%"), so it had to actually multiply drops to pass:
- **Mining**: `GreenwardFortuneHandler` on `PlayerBlockBreakEvents.AFTER` — lets
  vanilla's own break proceed completely untouched (sound, XP, tool durability, any
  vanilla Fortune enchant already on the tool), then adds independently-rolled extra
  copies of `Block.getDrops` using the same tool. For a deterministic drop (diamond ore,
  no vanilla Fortune enchant) this reproduces the acceptance numbers exactly; for a
  probabilistic vanilla drop, each extra copy still gets its own fair vanilla roll
  rather than being cloned from the first.
- **Farming**: the same guaranteed-rolls-plus-chance pattern added directly to
  `HarvestLogic.harvestAndReplant` (the player-facing hoe-harvest path only — the
  machine path, `harvestIntoStorage`, is a structurally separate method that never sees
  a stat lookup). Layered *alongside* the existing Harvester's-Garb-style
  `extraDropChance` set bonus, not replacing it — Update 9 is explicitly where the spec
  retires that mechanic in favor of Farming Fortune outright; Update 1 doesn't reach
  ahead into that.
- **Both are structurally machine-proof**, the same shape as the existing Sea Creature /
  Marked gates: `PlayerBlockBreakEvents` only ever fires with a real
  `net.minecraft.world.entity.player.Player`, and the Auto-Miner removes blocks directly
  via `Block.dropResources`/`level.removeBlock` — it can't reach this event at all, not
  because of a config check but because the event simply never fires for it.

**Damage formula application** (`GreenwardDamageHandler`): there is no Fabric event that
lets a listener *change* a damage amount — `ServerLivingEntityEvents.ALLOW_DAMAGE` is a
boolean gate only (confirmed by reading its actual interface signature, not assumed from
the spec's "avoid mixins" suggestion that it would suffice). Implemented via the same
cancel-and-reapply pattern this project already uses for Reaper's Aegis's Grim Resolve:
cancel the original `hurtServer` call, recompute the amount, re-invoke once with a
`ThreadLocal` re-entrancy guard. Both the outgoing (attacker Strength/Crit) and incoming
(defender Defense) adjustments are combined into **one** handler rather than two separate
cancel-and-reapply hooks on the same event, specifically to avoid the re-entrancy
complexity of composing two independent guards on one event chain.

Deliberately conservative per § 1.8's own caution ("do not remove vanilla crit... only
when the held item carries a Greenward stat component"): both adjustments *layer onto*
vanilla's already-computed amount (which includes enchantment/weapon bonuses) rather than
replacing the calculation outright. **Known, documented approximation**: Greenward Crit
is an independent roll against Crit Chance, and can in principle compound with vanilla's
own fall-based crit (already baked into the amount this code receives, with no clean way
to detect or strip it without a mixin into `Player.attack`) — biases toward more damage,
only in the narrow case of a falling player wielding a Greenward weapon, accepted for
Update 1 and flagged as a mixin candidate if precise replacement is ever required.

**Health and Speed** are the two stats with a direct vanilla attribute equivalent, so
they apply as real transient `AttributeModifier`s (`Attributes.MAX_HEALTH` /
`Attributes.MOVEMENT_SPEED`) computed fresh every 20 ticks in `SetBonusHandler`, rather
than going through the custom formula layer — 100 is each stat's "no bonus" baseline
(matches vanilla's own defaults exactly), so the modifier amount is always relative to
100 (10 Health = 1 heart = 2 HP; Speed is a straight percentage-of-vanilla-base).

## Armor: replaced vanilla armor points

All twelve existing `ArmorMaterial`s (`ModArmorMaterials.java`) now carry
`makeDefense(0,0,0,0,0)` and zero toughness/knockback resistance — Greenward armor
grants **zero** vanilla protection; all mitigation is the Defense stat, applied via a new
`GreenwardComponents.STATS` component on every piece and resolved through
`GreenwardDamageHandler`. Durability, enchantment value, equip sound, repair tag, and
worn-armor asset are all untouched by this — those aren't "protection."

**The spec's own conversion formula undershoots its own stated target — resolved in
favor of the explicit target, not the formula.** The spec gives `Defense = defensePoints
× 8 + toughness × 15` as "roughly" the conversion, then separately states Tier III
should land near 320–380 Defense (≈76–79% reduction). Applying the literal formula to
Warden's Garb/Bedrock Plate/Leviathan's Wear/Reaper's Aegis (24 points, 4.0 toughness)
gives 24×8 + 4×15 = 252 — only ≈72% reduction, short of the stated band. Since the
formula is explicitly hedged ("roughly") and the target number is stated as a firm
acceptance-relevant goal ("Tier III lands near..."), the explicit target wins: every
Tier III set's Defense total is set to exactly **350** (the midpoint of 320–380, ≈78%
reduction), with Tier I/II sets scaled proportionally against the same underlying
per-piece point split rather than recomputed from the literal formula. Full working:

| Set (Tier) | Vanilla points (boots/legs/chest/helm) | Defense total | Reduction |
|---|---|---|---|
| Harvester's Garb / Angler's Wear (I) | 2/4/5/2 = 13 | 100 | 50% |
| Prospector's Plate (I) | 3/5/6/3 = 17 | 140 | 58.3% |
| Marrowguard (Combat I) | 2/5/6/2 = 15 | 120 | 54.5% |
| Cultivator's Garb / Excavator's Plate / Tidal Wear / Ashwrought (II) | 3/6/8/3 = 20 | 220 | 68.75% |
| Warden's Garb / Bedrock Plate / Leviathan's Wear / Reaper's Aegis (III) | 4/7/9/4 = 24 | **350** | **77.8%** |

Per-piece Defense values are each set's own original vanilla point split, scaled to hit
that set total (e.g. Warden's Garb: helm 58 / chest 132 / legs 102 / boots 58, summing to
350) — see the doc comment on `ModArmorMaterials` and `ModArmor.piece()`'s call sites for
the exact per-piece numbers.

## Rarity & tooltips

Vanilla's own `net.minecraft.world.item.Rarity` only has 4 tiers (COMMON/UNCOMMON/RARE/
EPIC) and, being a plain enum, can't be extended with a 5th — so `GreenwardRarity`
(COMMON/UNCOMMON/RARE/EPIC/LEGENDARY) is a parallel Greenward-owned enum, registered as
its own `GreenwardComponents.RARITY` data component rather than reusing vanilla's. Every
armor piece now carries one, assigned by tier (Tier I → UNCOMMON, Tier II → RARE, Tier
III → EPIC; LEGENDARY reserved for something more special later — talismans, most
likely, once Update 6 exists). Tools/weapons/catalysts don't carry Rarity yet — Update 1's
own body text scopes the armor-point conversion specifically to armor; extending Rarity
to the rest of the item roster is a natural, cheap follow-up flagged here rather than
silently left undone.

Tooltip layout (`GreenwardTooltipRenderer`, registered from `GreenwardClient` —
client-only, via Fabric's `ItemTooltipCallback`): stats block (positive values green,
negative red, in a fixed `GreenwardStat` order rather than the component map's iteration
order), blank line, rarity footer in the rarity's own color. The spec's "ability/set
block" between stats and rarity has no data source yet in Update 1 — nothing populates
unique-ability text — so it's simply absent rather than an empty placeholder section;
later updates that add that text insert it in the gap already left for it.

## HUD

`❤ 340/340   ❈ 312` — implemented as a periodic action-bar **system message** sent from
the server (`ServerPlayer.sendSystemMessage(component, true)`) every 20 ticks alongside
the stat recompute, rather than a custom client-side HUD renderer. This reuses vanilla's
existing action-bar overlay exactly as intended and needs zero GUI rendering code at all
— a deliberate simplification against the spec's "Action-bar HUD" wording, which reads
naturally as "use the action bar" rather than "build a new overlay region."

## Commands

- `/greenward stats` — any player; prints the full profile with a per-source breakdown
  (`[Equipment +N]` etc.) computed on demand via `PlayerStatManager.breakdown`, not part
  of the cached hot path.
- `/greenward decommission [world]` — gamemaster-level permission
  (`Commands.LEVEL_GAMEMASTERS`); see the Permanence Retrofit section above.

## Testing performed this session

- `./gradlew compileJava` — clean after every stage of this pass.
- `./gradlew runServer` — multiple full boots across this pass, zero errors; final
  recipe/advancement count unchanged at 1682/1786 (Update 1 touches existing items'
  components and mechanics, not recipe counts).
- **`/greenward decommission` and `/greenward decommission world` verified live via
  RCON** — including catching and fixing the radius-24 hang described above, and
  confirming `/greenward stats` correctly rejects a non-player (RCON console) caller
  rather than crashing.
- Fortune formula **verified by hand against both of the spec's own worked acceptance
  numbers** (100 → always 2×; 150 → 2× always + 50% chance of 3×) before being wired into
  gameplay — this is the same kind of check a unit test would perform, done at
  implementation time since no test harness exists in this project.

**Not covered** (needs a real client + player, same category of gap as every previous
update's "not independently verified" section): actually mining ore or harvesting crops
as a live player and confirming the Fortune statistics hold up over many real trials;
confirming the tooltip renders with the right colors/layout in a real inventory screen;
confirming the action-bar HUD text is readable and doesn't visually collide with
anything; confirming Defense actually reduces incoming damage by the documented
percentage in a real PvP/PvE fight; confirming Grim Resolve and the new combined damage
handler don't interact unexpectedly now that they're both registered on `ALLOW_DAMAGE`.

---

# Design Program Revision 3 — Update 2: The Field Guide — Collections + Proofs

The progression meter: nine tracked Collections (Tiers I–X), four Skills (levels 0–50),
and a Proof for every tier from IV up — the manual gate that keeps § 2.1's doctrine
("you cannot advance a pillar by pointing a machine at it or by a single clever stunt")
actually load-bearing rather than aspirational.

## Architecture

`GreenwardCollection` (9: Wheat, Potato, Carrot, Cobblestone, Deepslate, Diamond, Cod,
Bone, Gunpowder — each the raw vanilla item that already feeds that pillar's compression
chain, so collection progress and material progress are the same lifetime count viewed
two ways) and `GreenwardSkill` (Farming/Mining/Combat/Fishing) are plain enums.
`PlayerProgress` is the facade everything calls through — mirrors `PlayerStatManager`'s
role for the Update 1 stat layer.

**Persistence**: a `copyOnDeath` player attachment (`PlayerProgressData`, codec-backed)
holds collection quantities, skill XP, completed Proofs, and the handful of Proof
counters that are genuinely lifetime-cumulative. Day-scoped and streak-style counters
are deliberately *not* persisted — they live in `PlayerProgress`'s in-memory-only maps
instead, the same call already made for Grim Resolve's cooldown in Update 1: losing a
same-day or in-progress streak counter to a rare server restart is an accepted,
self-healing inconvenience, not lost progress. `copyOnDeath` had to be requested
explicitly (`AttachmentRegistry.Builder.copyOnDeath()`) — persistent attachments do
**not** survive respawn by default, confirmed by reading the builder API rather than
assumed; without it, dying would have silently reset every player's entire collection
history.

**Skill XP curve** (`SkillXpCurve`): a 50-entry cumulative-XP table generated by
geometric (log-space) interpolation between the spec's own four anchor points (level 1
= 50, level 10 ≈ 10k, level 25 ≈ 180k, level 50 = 1.5M), then frozen so those four levels
land on the spec's numbers exactly. Table-driven per the spec's own instruction ("so it
can be tuned") — any entry can be hand-edited without touching the generator.

## Proof tracking — four patterns, not one bespoke mechanic per proof

- **Instant** — completes the moment a qualifying action happens (Full Yield, Storm
  Catch). No counter.
- **Lifetime cumulative** — a gated counter that only ever goes up, persisted (Rainfed,
  Down Deep, Bedrock Bound, Cold Iron, Bare-Knuckle, Graveyard Shift).
- **Day-scoped** — resets whenever the in-game day number changes (`level
  .getOverworldClockTime() / 24000`), in-memory only (Bumper Crop, Dug In, Golden Hour,
  Vein Runner, Quarry Shift, Full Net, Marked Hunter, Powder Trail).
- **Streak** — resets on a specific "broke it" condition rather than a day boundary,
  also in-memory only (Unbroken Harvest, Single Breath, Deep Vein, Patient, Held Nerve).

Stockpile-check proofs (Full Silo, Full Cellar, Fallow No More) need no counter at all —
a live threshold check against current inventory/world state, run every 20 ticks
alongside the Update 1 stat recompute.

## The one genuine contradiction found in the spec, and how it was resolved

**Full Yield** ("harvest a complete mature 9×9 in a single hoe swing") gates **Wheat
Tier IV**, the very first tier with a Proof requirement. Taken literally, a 9×9 area is
radius 4 — but radius 4 is *only* reachable with Harvest Warden plus full Warden's Garb,
which is itself gated behind **Wheat Tier IX** in the same retrofit table (§ 2.4:
"Harvest Core at Wheat IX..."). Reaching Tier IX requires having already completed
Tier IV. The proof as literally written is circular: it requires a tool that requires
the tier it gates.

**Resolved by implementing Full Yield radius-agnostically**: "every tile your
*currently-equipped* hoe's radius covers was mature and got harvested in one swing" —
achievable with a base iron hoe just as validly as with Harvest Warden, preserving the
actual point of the proof (a clean, fully-planned harvest, not a lucky partial one)
without the circular tool dependency. Radius 0 (a single clicked tile) is excluded from
counting, so the proof can't trivialize into "successfully harvest any one crop." Also
completes **Unbroken Harvest** (Wheat IX)'s streak counter on the same check, and resets
that streak on any partial swing. Documented in `GreenwardProof`'s own doc comment, not
just here.

## The recipe retrofit (§ 2.4) — a real advancement trigger, not a stand-in

A new `SimpleCriterionTrigger` subclass, `GreenwardTierCriterion`, registered exactly
like a vanilla one (`Registry.register(BuiltInRegistries.TRIGGER_TYPES, ...)` — a normal
registry mods can add to, no mixin) under `greenward:tier_reached`. `PlayerProgress`
fires it the moment a tier newly becomes fully complete (quantity *and* Proof).
Advancement JSON looks exactly like a vanilla one, just with a custom trigger:

```json
"criteria": {
  "wheat_tier_4": { "trigger": "greenward:tier_reached", "conditions": { "collection": "wheat", "tier": 4 } }
}
```

Every recipe advancement the spec's retrofit table names was rewritten to use this
instead of the pre-existing `minecraft:inventory_changed` trigger — Harvester's/
Cultivator's/Prospector's/Excavator's/Angler's/Marrowguard's/Ashwrought's full sets, the
three automation machines, the Ascension Template, and Harvest Core. Multi-criterion
gates (e.g. Cultivator's needs *both* Wheat VII and Potato V) use vanilla's own
`requirements` AND-grouping (`[["wheat_tier_7", "potato_tier_5"]]`). Recipes the retrofit
table doesn't name (Tier III smithing upgrades, fishing rods, God Potion catalysts, the
potions themselves) were deliberately left on their existing item-pickup advancements —
the table's own "Suggested:" framing hedges it as a concrete worked example, not an
exhaustive list, and inventing gating for dozens more items without spec guidance risked
getting the balance wrong for no real gain.

## 13 invented Proofs, clearly marked as invented

The spec's own Proof table gives 11 worked examples — not all ~63 possible
(collection, tier≥IV) slots, and notably fewer than the retrofit table (§ 2.4) actually
needs (Wheat V/VII/IX, Potato V/VIII, Carrot VII, Diamond VI, Cobblestone V/VII,
Deepslate V, Cod V, Bone VII, Gunpowder V all lack a spec-given Proof but gate a named
recipe). Rather than invent 13 wholly bespoke mechanics, each new Proof reuses one of
the four tracking patterns above with a reskinned name and flavor text — Bumper
Crop/Dug In/Golden Hour/Vein Runner/Quarry Shift/Full Net/Powder Trail (single-day volume
pushes), Full Silo/Full Cellar (stockpile checks), Unbroken Harvest/Deep Vein (streaks),
Bedrock Bound (a depth threshold), Graveyard Shift (a time-of-day-gated counter). Every
one is marked `// invented` at its declaration in `GreenwardProof.java` and listed by
name in that file's own doc comment — never silently presented as spec content.

## Notable implementation substitutions

- **Cold Iron** ("500 deepslate with no torch within 16 blocks") checks the block's own
  light level (`level.getBrightness(LightLayer.BLOCK, pos) == 0`) instead of literally
  searching a 33×33×33 volume for torch blocks — an O(1) lookup against a brute-force
  scan that would run on every qualifying mine, and arguably closer to the proof's real
  intent (mining in genuine darkness) than a search restricted to torches specifically
  (glowstone, lava, and lit ore would all correctly disqualify a spot too).
- **Single Breath** ("without surfacing above Y 0") and **Down Deep/Bedrock Bound**'s
  depth gates check the player's position *at the moment a block is mined*, not on a
  continuous per-tick watch — a trip above Y0 without mining anything in between won't
  register as broken until the next block is mined. A documented simplification, not a
  gap: implementing a true continuous watch would need its own always-on per-player tick
  hook for a difference that only matters in an edge case.
- **Bone/Gunpowder collection counts are a flat "+1 per qualifying kill"**, not an exact
  replica of the mob's own loot-table roll (which drops a small random range) — consistent
  with Farming's own "passes" approximation from Update 1; re-deriving the precise roll
  would mean intercepting the mob's death loot table separately for a lifetime-tally
  precision gain that isn't worth the extra moving part.
- **The Field Guide's live UI is deferred.** § 2.5 asks the book to become "a live
  progression book rather than static documentation," but `PlayerProgressData` lives
  server-side and the book (`BookViewScreen`) renders entirely client-side with zero
  network round-trip today — building a proper sync channel (a custom network payload
  plus a client-side cache) is real, separate infrastructure this pass chose not to rush.
  **`/greenward progress`** ships instead as the fully-functional, zero-sync-risk way to
  check Collections/Skills/Proofs status right now; the in-book view is flagged as the
  natural next step once a sync channel exists, not silently dropped.

## Testing performed this session

- `./gradlew compileJava` / `./gradlew clean build` — clean throughout.
- `./gradlew runServer` — multiple full boots, zero errors; recipe/advancement counts
  unchanged at 1682/1786 (this update rewires existing advancement *criteria*, not
  recipe or advancement *counts*).
- **`/greenward stats` and `/greenward progress` verified live via RCON** — both
  correctly reject a non-player (RCON console) caller rather than crashing, matching the
  established pattern from Update 1's command testing.
- **Not covered** (needs a real client + player, the same category of gap as every prior
  update): actually hand-harvesting/mining/fishing/fighting as a live player and
  confirming collection quantity and skill XP rise correctly while an Auto-Harvester/
  Auto-Miner/Auto-Fisher's output does not; confirming a Proof genuinely can't be
  completed by machine action (the structural argument — these events only ever fire
  with a real `Player` instance — is verified by reading the event's own interface
  signature, the same way Sea Creatures' and The Marked's machine-immunity was verified,
  but the live behavior itself needs a human pass); confirming a full recipe unlock
  actually appears in a real player's recipe book at the moment a gated tier completes;
  confirming progress genuinely survives a real server restart end-to-end.

# Design Program Revision 3 — Update 3: Material Economy

Extends every compression chain to a full 4-rung ladder, gives the three automation
machines a fourth upgrade axis (auto-compression), and adds four passive-collection
Satchels — one per pillar.

## The scope decision that shaped this update

The spec's Press module wording ("cap installed modules at 2 per machine") was
ambiguous — it could plausibly mean a *shared* cap across every upgrade axis, which would
have retrofit a new restriction onto the already-shipped, tested Storage/Speed/Regen
system from the automation update. That was surfaced to the user before implementing
(explicitly requested: "give me a summary of what is in it first"), and resolved in favor
of **dedicated per-axis upgrade slots** — a Compression slot alongside Storage/Speed/Regen,
each independent, matching the user's own clarification ("there is an upgrade slot for
each automation block upgrade... a speed slot, a storage slot, a compression slot, a
regen slot"). The spec's literal "third module refused" acceptance criterion is
superseded by this design and was not implemented as a shared cap.

## Compression ladder: rungs 3 and 4

Every collection except Diamond (which was never given a compression chain — see
`GreenwardCollection`'s own doc comment) now has a full 4-rung ladder, each rung a plain
shapeless 9-of-previous-rung recipe, exactly continuing the existing rung-1/rung-2
convention rather than inventing a new one:

| Collection | Rung 3 | Rung 4 |
|---|---|---|
| Wheat | Rick | Mow |
| Potato | Pallet | Granary |
| Carrot | Sunburst Carrot | Solar Carrot |
| Cobblestone | Cobble Massif | Cobble Batholith |
| Deepslate | Deepslate Massif | Deepslate Batholith |
| Cod | Cod Haul | Cod Trove |
| Bone | Bone Charnel | Bone Ossuary |
| Gunpowder | Powder Magazine | Powder Arsenal |

Cobblestone and Deepslate's rung 3/4 names collide on the bare English word ("Massif",
"Batholith"), so both carry their collection's prefix — the same disambiguation already
used for rung 1/2 (`Cobble Cluster`/`Cobble Monolith` vs. `Deepslate Cluster`/`Deepslate
Monolith`).

**Uncraft recipes** (reversing one rung, e.g. `wheat_mow` → 9× `wheat_rick`) exist only for
Wheat and Carrot's chain, continuing rung 1/2's own existing asymmetry — those two
collections' raw items have off-ramp uses (breeding, eating, composting) that justify
staying reversible; Potato and the four non-farming collections never had uncraft recipes
either, so rungs 3/4 don't invent the pattern for them.

Textures for all 16 new items are procedurally generated placeholders (raw PNG via
`struct`/`zlib`, no PIL — the same approach used throughout this project), extrapolating
each collection's own established rung-1→rung-2 color delta one or two steps further
rather than inventing an unrelated palette.

## Press / Deep Press: auto-compression as a machine upgrade

A new upgrade axis on all three automation machines (`AbstractMachineBlockEntity`'s
`compressionTier`, persisted, synced to the GUI via a new `ContainerData` index) — Press
raises it to 1 (auto-compress output to rung 1 the moment enough raw material accumulates
in storage), Deep Press to 2 (rung 2). The slot accepts either item directly (`setCompressionTier`
is target-based, so dropping in a Deep Press without ever owning a Press jumps straight to
tier 2) via a small `addUpgradeSlot` overload that takes a `Predicate<ItemStack>` +
`Consumer<ItemStack>` instead of a single required `Item` + `Runnable`, needed because this
is the first upgrade axis with two different items mapping to two different target tiers
rather than one item incrementing by exactly one step.

Compression only ever reaches rung 1/2 automatically — rungs 3/4 stay a manual
crafting-table step, matching the spec's own wording ("auto-compresses to rung 1 [rung
2]") rather than "to the highest rung available." The actual compression math
(`CompressionLadder`, plus `applyCompression`/`removeAllFromStorage`/`addAsStacks` in
`AbstractMachineBlockEntity`) scans and re-expresses a machine's *entire* accumulated
storage for a material, not just the newest drop, so partial stock sitting in storage from
before the upgrade was installed still gets folded in correctly.

Press/Deep Press are gated on Cobblestone Tier V/X respectively (via
`greenward:tier_reached`, same retrofit mechanism as Update 2's recipe gates) — a
deliberate choice to tie the "compress smarter" upgrade to the pillar most associated with
raw-volume grinding, rather than gating separately per-pillar.

The Auto-Miner's Compression slot needed a genuinely new 6th special-slot column (it
already fills fuel/storage/speed/regen/seed); the Harvester and Fisher, which never had a
Regen slot, reuse that column instead. The Miner's screen alone widens its rendered panel
past the shared 176px default (`AbstractMachineScreen` gained a second constructor
overload taking an explicit `imageWidth`) to fit the extra column — still inside the
existing 256×256 texture canvas, just cropping more of it. Per this project's established
"placeholder texture, not final art" precedent, the background art itself wasn't
regenerated to visually decorate the new slot position; the slot is fully functional and
labeled, just without custom slot-box artwork yet.

## Satchels: passive, tag-filtered, tier-scaled collection

Four items — Agronomy (Farming), Lithic (Mining), Tidal (Fishing), Ossuary (Combat) — each
vacuuming its pillar's raw drops and every rung of its compression chain (`#greenward:
farming_materials` / `mining_materials` / `fishing_materials` / `combat_materials`, four
new item tags) out of the player's main inventory once a second, up to a live capacity.

**Not built on vanilla's `BundleItem`/`BundleContents`** — read the actual decompiled API
before deciding (see `UPDATING.md`'s Update 3 entry for the full reasoning): its capacity
model is a weight `Fraction` tuned for "how many arbitrary items fit in a bag," not a flat
item-count tier, and it comes with its own manual-open GUI and per-slot toggle-select
interaction that a passive background collector doesn't want. Built a small custom
`SatchelContents` data component instead (`Map<Item, Integer>` counts + a `capacity`
field, both codec- and network-synced), following the exact `DataComponentType` pattern
Update 1 already established for `STATS`/`RARITY` — "custom items — silently dropped from
inventories — acceptable" per the Permanence Charter, so this carries zero world-state
risk regardless of what a Satchel holds.

**Capacity is not a physical upgrade item.** Per the spec's own framing, it's computed
live every vacuum pass off the player's actual Field Guide progress — the highest tier
completed among that pillar's collections maps Tier VI → 256, Tier VIII → 1024, Tier X →
4096 (`SatchelHandler.liveCapacity`). A Satchel a player already owns gets roomier
automatically as their Collections climb; there's nothing separate to craft, lose, or
forget to install. Each Satchel's own recipe is gated at Tier VI of its pillar's primary
collection (Wheat/Cobblestone/Cod/Bone respectively), so a freshly crafted one is never
below the 256 floor.

**Emptying**: sneak-right-click (`SatchelItem.use`) dumps everything back into the
player's inventory, overflow dropped on the ground rather than destroyed. **Fill display**:
the item bar (same 13px indicator vanilla uses for durability) shows fill fraction,
colored per pillar; the tooltip (extending `GreenwardTooltipRenderer`'s existing
component-driven pattern rather than a bespoke per-item override) adds a plain "Holding: X
/ Y" line whenever a stack carries `SATCHEL_CONTENTS`.

The vacuum scan is bounded to `Inventory.INVENTORY_SIZE` (36 — main inventory + hotbar)
so equipped armor and the offhand are never touched.

## Testing performed this session

- `./gradlew compileJava` — clean after every incremental change (Press/Deep Press
  machine wiring, all 16 rung-3/4 items, the full Satchel system).
- `./gradlew runServer` — three separate full boots (after the Compression-slot machine
  wiring, after the 16 rung-3/4 items, after the Satchel system), each checked for
  exceptions/errors in the log in addition to reaching "Done"; all three clean.
- **Not covered** (needs a real client + player): actually placing a Press/Deep Press in a
  machine and confirming stored output visibly re-compresses; hand-crafting any of the 16
  new rung-3/4 items or the 4 Satchels through a real crafting grid; confirming a Satchel
  actually vacuums matching items out of a live player's inventory and that its capacity
  bar/tooltip update as Collections climb; confirming sneak-right-click dumping behaves
  correctly with a full or nearly-full player inventory (overflow-drop path). This is the
  same category of gap disclosed for every prior update — RCON has no simulated player to
  harvest, mine, fish, fight, or craft with.

# Design Program Revision 3 — Update 4: Automation Doctrine

The four fixes from § 4.1, the fuel model rewrite from § 4.2, and the five Effigies from
§ 4.3 — brings the three automation machines under § 2.1's doctrine ("machines are the
floor, players are the ceiling") and closes the God Potion bypass the source document
called out explicitly. Restored to the repo alongside this update: `GREENWARD_DESIGN_
PROGRAM.md` itself, the full ten-update roadmap, recovered from this session's own prior
transcript and committed for the first time — it was referenced throughout Updates 1-3
but had never actually been checked in.

## § 4.1① — Ancient Debris no longer regenerates

`#greenward:regenerable_ores` was a single tag including `minecraft:ancient_debris` — a
tier-4 Regen module against a tier-4 Speed module made it an infinite Netherite tap,
trivializing the Deepstone Core and the God Potion behind it. Ancient Debris is now
excluded from every regrowth tag entirely (see § 4.1② below) — Netherite stays manual,
full stop.

## § 4.1② — Common/precious ore split, gated by a new Deep Regrowth Module

`REGENERABLE_ORES` split into `REGENERABLE_COMMON` (coal, iron, copper, redstone, lapis,
nether gold/quartz — unchanged regrow delay) and `REGENERABLE_PRECIOUS` (gold, diamond,
emerald). Common ores regrow on the base Auto-Miner exactly as before. Precious ores only
regrow with a new **Deep Regrowth Module** installed (a 7th Auto-Miner-only upgrade slot,
boolean install like Compression, not a tiered ladder) — without it, mining a precious
ore is permanent, same as a player mining it by hand. With it, precious ores regrow at
**5× the common delay** at every speed tier, matching § 4.1②'s table exactly.

**Substitution, documented**: the source text gates this module at "Heartwood Mining
branch 4" — Update 6, not built yet. Gated on **Diamond Tier VI** instead as a
same-difficulty-class interim stand-in (a genuinely demanding milestone, using the
collection most thematically tied to "precious" ore) — revisit when the Heartwood ships.

## § 4.1③ — Machine stat-immunity (already true) + new placement caps

Auditing confirmed all three machines' output already ignored every player stat
structurally — the Auto-Miner calls `Block.getDrops(state, level, pos, null)` (no tool,
so no Fortune/Silk Touch), and the Auto-Harvester's machine path
(`harvestIntoStorage`) has always been a separate method from the player-facing
`harvestAndReplant` that applies Farming Fortune. No code changes needed there.

**New**: `MachinePlacementGuard` + `MachineCountData` (a `Set<GlobalPos>`-backed
`SavedData`, anchored to the overworld's own storage so "24 per world" genuinely means
the whole save, not per-dimension) enforce a 24-per-world cap on placement, for all three
machines **and** the five Effigies below — leaving Effigies uncapped would have been an
obvious loophole around the whole point of this cap. **The spec's 4-per-chunk cap was
removed at the user's explicit request** ("I want the peak of automation to be very
powerful") — dense single-chunk automation farms are intentionally allowed; only the
per-world total is still capped. A refused placement is undone immediately (the block
pops back off, the item is returned to the placer) with a chat message naming the cap
that was hit. `AbstractMachineBlockEntity.setRemoved()`
centralizes the removal side for every automation block type in one place, rather than
needing a matching override in each Block subclass.

## § 4.1④ — Auto-Fisher no longer rolls treasure

The Auto-Fisher used to roll the exact same global `BuiltInLootTables.FISHING` table a
real player cast does — and since § 5's data-file override already strips the open-water
gate off the treasure pool for everyone, the machine had unrestricted access to
enchanted books, name tags, saddles, and (critically) Nautilus Shells, a God Potion
ingredient. It now rolls only `FISHING_JUNK`/`FISHING_FISH` at their original relative
weights (10:85), plus an independent ~2% Nautilus Shell chance replacing the treasure
pool's contribution — real player casts are completely unaffected by this change.
(`FieldGuideContent`'s Abyssal Pearl page also had a factual error fixed in passing:
Prismarine Crystals are a Guardian drop, not a fishing drop, and never were.)

## § 4.2 — Fuel is now a speed boost, not an operation budget

The single biggest behavioral change this update: **an unfueled machine now runs forever
at its base rate.** Fuel dropped into the fuel slot is consumed instantly (still
hopper-fed — the slot itself didn't change) for a percentage speed boost that decays over
real time, replacing the old "N operations per item, zero operations with none" model
entirely.

- Each fuel item grants a `(percent, duration)` pair (`AutomationFuel`) — Coal/Charcoal
  +5%/30 min, Coal Block +5%/4 hr, Dried Kelp Block +8%/1 hr, Wheat Bale/Potato Crate
  +10%/2 hr, Wheat Rick/Potato Pallet +15%/6 hr, Lava Bucket +25%/12 hr, matching § 4.2's
  table exactly.
- **Sunwheel** (new item, daylight-only, permanent): installs once rather than ticking
  down a duration; its +25% only applies while `getOverworldClockTime() % 24000 < 12000`
  (a documented approximation of "daytime," the same category of simplification as
  Graveyard Shift's own night-window check in Update 2).
- Refueling only consumes an item when it would actually help (current boost expired, or
  the new item's percent beats the active one) — extends duration and takes the higher
  percent rather than wastefully eating fuel that wouldn't improve anything.
- The flame indicator in the GUI is repurposed to show the *current boost's* remaining
  duration rather than an operation budget — same visual language, different meaning
  underneath. The "lit" blockstate now means "currently boosted," not "has fuel."

**Live-verified via RCON** (see Testing below) — an Auto-Miner with zero fuel mined an
iron ore on schedule; inserting one Coal produced exactly `boost_percent: 5`,
`boost_duration_max: 36000` (30 min × 1200 ticks/min).

## § 4.3 — Five Effigies: combat automation that kills nothing

Rotting (Rotten Flesh), Bonepile (Bone + Arrow), Webbed (String + Spider Eye), Volatile
(Gunpowder), and Void (Ender Pearl) — each generates its mob's base drops on a timer,
spawning and killing nothing, closing the automation asymmetry Combat has had since it
was "built from nothing" in the Four Pillars update. One shared `EffigyBlock`/
`EffigyBlockEntity`/`EffigyMenu`/`EffigyScreen` implementation (parametrized by
`EffigyType`) backs all five rather than five near-identical classes — they reuse the
**exact same** shared machine architecture as the other three (fuel-as-boost, Storage/
Speed/Compression upgrades — no Regen or seed slot; there's no ore concept here), per
§ 4.3's own instruction to use "the same frame and module system as the other machines."
Drop rolls (`EffigyType.rollDrops`) are a flat per-operation approximation of one mob
kill's typical yield, the same documented tradeoff as Bone/Gunpowder collection counting.

**Recipe substitution, documented**: § 4.3 specifies each recipe's center ingredient as
"the mob's rung-2 compressed drop," which only exists for two of the five — Bonepile
(Bone Reliquary) and Volatile (Powder Cache) use their real rung-2 items exactly as
specified; Rotting/Webbed/Void substitute their own raw drop (Rotten Flesh/String/Ender
Pearl) as the center ingredient instead, since Wheat/Potato/Carrot/Cobblestone/Deepslate/
Cod/Bone/Gunpowder are the only tracked compression chains. Gated on **Bone Tier V**
(an interpretation, not a literal spec quote — the source text never specifies a gate for
Effigies at all; Bone Tier V matches the convention the other three automation machines
already use of gating on their own pillar's Tier V).

**Live-verified via RCON**: a Bonepile Effigy with zero fuel produced an Arrow from
nothing within its first 100-tick interval.

## Combat recipe costs restored to parity

Per § 4.3's own explicit condition ("once Effigies ship, restore Combat recipe costs to
parity"), Marrowguard's and Ashwrought's armor recipes are back to the standard 5 / 2+2 /
6+1 / 7+1 pattern every other pillar's Tier I/II already uses — the 35% discount from the
"Progression polish" pass earlier this document was explicitly compensation for Combat
having no automation, and that's no longer true.

## Testing performed this session

- `./gradlew compileJava` — clean after every incremental change.
- `./gradlew runServer` — four separate full boots (after the Ancient-Debris/regrowth/
  placement-cap/treasure-pool fixes; after the fuel-as-boost rework; after the full
  Effigy system) plus a final clean-build boot after clearing ~113 stray duplicate
  `" 2.json"` files out of `build/` (pure Gradle build-cache debris from before this
  session, gitignored, never tracked — see the flag in this session's final summary about
  `bin/` being tracked instead, which `build/` is not). All boots checked for exceptions/
  errors in the log in addition to reaching "Done"; all clean.
- **Live-verified via RCON** (a first for this project — every prior update's fuel/combat/
  machine-output logic could only be reasoned about, not exercised): placed a fueled and
  unfueled Auto-Miner and a Bonepile Effigy via `/setblock`, force-loaded the chunk, and
  read back `/data get block` across real tick cycles — confirmed unfueled operation,
  correct fuel-boost NBT values, and real drop generation from nothing.
- **Not covered** (needs a real client + player — `/setblock` bypasses `setPlacedBy`
  entirely, so the placement-cap refusal path specifically cannot be exercised
  headlessly): confirming a 5th machine or Effigy in one chunk / a 25th in one world is
  actually refused with the chat message, item returned; confirming the Deep Regrowth
  Module's recipe gate fires correctly off a live player's Diamond Tier VI; confirming
  the Auto-Fisher's new roll never produces treasure-pool items over a long real run.

## Post-Update-4 adjustment — per-chunk machine cap removed

At the user's explicit request ("I want the peak of automation to be very powerful"),
the 4-per-chunk placement cap from § 4.1③ was removed entirely — `MachinePlacementGuard`
now only enforces the 24-per-world total. Dense single-chunk automation farms (multiple
machines and/or Effigies stacked in one chunk) are intentionally allowed. `MachineCountData`
lost its now-unused `countInChunk` method in the same pass.

# Design Program Revision 3 — Update 5: Mining Depth

Gives Mining a player-facing expression using **zero worldgen additions** (§ 5.6) — every
new material comes from a loot roll on a genuine vanilla block, gated by tool tier and
Mining skill level, never a new ore block. Closes the "Mining is boring because it's
overpowered" gap the source document names explicitly: Farming has harvest radius,
Fishing has sea creatures, Combat has the Marked — Mining previously had nothing but a
speed number moving slightly across three tiers.

## § 5.1 — Rare drops from vanilla ore

`GreenwardRareOreHandler` hooks the same `PlayerBlockBreakEvents.AFTER` event
`GreenwardFortuneHandler` already uses — structurally machine-proof for the identical
reason (the Auto-Miner removes blocks directly, never through this event). Six new
materials, each gated by tool tier + Mining skill level + (for most) a depth requirement:

| Material | Drops from | Requires | Base rate |
|---|---|---|---|
| Glimmer | Deepslate/Cobbled Deepslate, Y ≤ -40 | Prospector's Drill+, Mining 15 | 1.5% |
| Titanshard | Iron/Copper/Gold ore, Y ≤ -40 | Excavator's Pick+, Mining 25 | 1% |
| Rough Amber | Any ore (`#c:ores`) in a cluster of 5+ | Excavator's Pick+, Mining 30 | 0.6% |
| Rough Jade | Emerald ore, any Y | Excavator's Pick+, Mining 30 | 4% |
| Rough Sapphire | Lapis ore, Y ≤ -40 | Excavator's Pick+, Mining 35 | 0.8% |
| Rough Ruby | Redstone ore, Y ≤ -55 | Bedrock Reaver specifically, Mining 40 | 0.5% |

Amber's cluster-size check is a capped 6-directional flood fill (stops the instant it
confirms 5+, so it's cheap even against a huge natural vein) over Fabric's own `c:ores`
convention tag rather than hand-listing every vanilla ore block again. "Rates are
affected by Mining Fortune" is implemented as `effective = base × (1 + fortune/100)` — a
straight multiplier, not the guaranteed-extra-roll formula `GreenwardFortuneHandler` uses
for ordinary yield (that formula assumes a base yield ≥ 1, which doesn't fit a
sub-1%-chance rare drop). A documented interpretation, not a literal spec formula.

## § 5.2 — Gem cutting and sockets

Four gems (Amber/Jade/Sapphire/Ruby), each cut Rough → Fine → Flawless → Perfect at a
plain crafting table, 5-of-the-previous-cut per step (16 items total, ungated —
unlocked the moment you have one of the previous cut, same convention as the compression
ladder). Each cut grants a stat when socketed, scaled by cut quality:

| Gem | Grants | Rough / Fine / Flawless / Perfect |
|---|---|---|
| Amber | Mining Speed | 5 / 15 / 35 / 75 |
| Jade | Mining Fortune | 5 / 15 / 35 / 75 |
| Sapphire | Fishing Speed + Sea Creature Chance | 5/0.5 · 15/1.5 · 35/3.5 · 75/7.5 |
| Ruby | Strength | 5 / 15 / 35 / 75 |

**Sockets**: Tier II gear gets 2, Tier III gets 3 — a flat rule off the spec's own "1-3"
range, applied uniformly across all 32 Tier II/III armor pieces (via a new overload on
`ModArmor`'s shared `piece()` helper) and all 8 Tier II/III weapons/tools, rather than
inventing per-item variation the source document never specified. Tier I gear gets 0 and
is unaffected.

**Socketing itself needed a genuinely new mechanism**: a static crafting recipe can't
express "merge this gem's stat into whatever stats the gear already has, only if a
socket is free" — the output depends on the input gear's own current state, not a fixed
result. `GemSocketRecipe` is this project's first `CustomRecipe` (the same mechanism
vanilla itself uses for map cloning and repairing two damaged items) — place one
socket-eligible item and one recognized cut gem in a crafting grid, get back the gear
with the gem's stats merged in and one socket consumed. `SocketData` (a new persistent,
networked `DataComponent`) tracks how many sockets are used and which gems are in them.

## § 5.3 — Vein Blast and Stoneflow

Both abilities live on Bedrock Reaver — the source text gates them behind "the
Heartwood's Mining branch" (Update 6, doesn't exist yet), so wielding the pillar's
capstone pickaxe is the interim substitute gate, same category of decision as Update 4's
Deep Regrowth Module. **Sneak + right-click** in empty air throws Vein Blast; **plain
right-click** in empty air triggers Stoneflow. Independent 120s cooldowns each (the spec
gives both the same cooldown value but never says they share one meter).

- **Vein Blast**: throws the pickaxe as `VeinBlastProjectile` — this project's first
  genuinely new `EntityType`, permitted by the Permanence Charter's own explicit
  exception (§ 1.2 rule 5: "Transient projectiles (Vein Blast) are the only exception,
  and they never persist"). On impact, mines every `#c:ores` block within radius 3 by
  directly removing each block and **re-firing `PlayerBlockBreakEvents.AFTER` manually**
  (`.invoker().afterBlockBreak(...)`) for each one — the same event Mining Fortune,
  collection tracking, and § 5.1's rare-ore rolls already listen to, so a Vein Blast kill
  gets full credit exactly as if the player had mined each block by hand. Deliberate: Vein
  Blast is a rare (120s cooldown), aimed, skill-gated player action, not a passive
  multiplier, so crediting it doesn't violate § 2.1's "machines are the floor" doctrine.
  The entity discards itself unconditionally the instant it hits anything, whether or not
  a valid owner was found (confirmed live — see Testing below).
- **Stoneflow**: "+250% Mining Speed for 15s" is computed as `2.5 × current Mining Speed`
  and granted as a temporary buff via a new `AbilityBuffContributor` — a plain
  `StatContributor` like `EquipmentStatContributor`/`SkillStatContributor`, so it plugs
  into `PlayerStatManager`'s existing recompute pipeline with zero special-casing there.
  Expired buffs are pruned lazily on read.

## § 5.4 — The mining-speed ladder, actually implemented

The source document frames this as "retiring" an existing ladder, but auditing found
Prospector's Drill/Excavator's Pick/Bedrock Reaver had **never** granted Mining Speed or
Mining Fortune through the stat system at all — only their vanilla `ToolMaterial` speed
(already modest: 9.2/9.6/10.5, barely above netherite's 9.0). Added the spec's table
exactly: Drill +120/+10, Pick +260/+30, Reaver +500/+60 (Mining Speed/Mining Fortune),
via `GreenwardComponents.STATS` on each tool — the existing `EquipmentStatContributor`
already reads main-hand item stats generically, so this needed no other plumbing.

## Testing performed this session

- `./gradlew compileJava` — clean after every incremental change.
- `./gradlew runServer` — two full boots (after rare ores/gems/sockets/the custom
  recipe; after the full ability system), both clean.
- **Live-verified via RCON**: force-loaded a chunk, placed an Auto-Miner and mined an
  iron ore with zero fuel to confirm it still ran; `/summon`ed a `greenward:vein_blast`
  entity directly and confirmed it discards itself on impact even with no player owner
  (the transient-projectile guarantee holds), with zero exceptions logged.
- **Not covered** (needs a real client + player): the actual rare-ore roll firing on a
  live break at the right tool/level/depth; cutting a gem through all four qualities at a
  real crafting table; socketing a cut gem into real Tier II/III gear via `GemSocketRecipe`
  and confirming the stat merge is additive across multiple gems; sneak-right-clicking
  Bedrock Reaver to actually throw Vein Blast and watching it detonate a real vein: cooldown
  messaging, the radius-3 mine, and Fortune/collection credit on the resulting drops;
  Stoneflow's buff actually showing up in `/greenward stats` and decaying after 15s.

# Design Program Revision 3 — Updates 6-10: The Heartwood through Endgame

Implemented in one continuous pass at the user's request ("work on the rest of the
updates all the way till 10"). Given the scope, each update below is documented more
concisely than Updates 1-5 — see `PROGRESSION_SUMMARY.md` for exact numbers/recipes and
`UPDATING.md` for the version-fragile APIs this pass needed. Every update compiled clean
and boot-tested clean (individually and in one final combined boot); everything needing
a real client + player is disclosed as untested, same as every prior update.

## Update 6 — The Heartwood

One block, one per world (enforced like the machine cap, just with a limit of 1),
animated via a genuine `.png`/`.png.mcmeta` frame strip (zero Java for the pulse). Its
progress — 4 branches × 8 nodes each (a documented simplification of "8-10 nodes across
5 depths": one linear per-branch chain, not a forking tree), plus up to 12 talisman
sockets — lives in `HeartwoodData`, a world-shared `SavedData` rather than per-player
state, matching the spec's own "one per world" framing: whoever pays a node's cost
unlocks it for everyone. The screen (`HeartwoodMenu`/`HeartwoodScreen`) is real Slot-based
talisman sockets plus branch-tab buttons routed through vanilla's own generic
`clickMenuButton` mechanism (the same one the enchanting table uses).

Two Stone-branch nodes are load-bearing outside the Heartwood itself: unlocking
**Reaver's Wrath** is now the real gate for Vein Blast/Stoneflow (previously "wield
Bedrock Reaver" was Update 5's interim substitute), and **Regrowth Mastery** is now the
real second gate for the Deep Regrowth Module actually doing anything. Talismans: only
the 6 of the spec's 20 that don't need Update 8's villagers are shipped (Sheaf-Token,
Cutter's Charm, Angler's Knot, Knucklebone, Wraith's Eye, Deepglass Lens) — the source
document's own Appendix C anticipates this ("the remaining 12 should be filled in as
Proofs, gems, villagers, and bosses are finalised"). Each has one Sigil upgrade step
(doubles the effect), crafted at a plain table rather than "at the Heartwood" — a scope
simplification, since the transformation itself needs no Heartwood state.

**Also found and fixed in this pass**: roughly 60 items across Updates 3-6 (every rung-3/4
item, every Satchel, Press/Deep Press, Sunwheel, the Effigies, every gem, every talisman)
had never gotten a `lang/en_us.json` entry — they would have displayed as raw untranslated
keys in-game. Auto-generated reasonable display names for all of them.

## Update 7 — Threat

A single 0-100 world value (`ThreatManager`, computed live, not cached) from the
most-progressed online player's skill levels (40%), equipped gear tier (20%), unlocked
Heartwood nodes (25%), and bosses defeated (15%, persisted in the new `ThreatData`).
Every hostile mob gets transient `AttributeModifier`s on load scaling health/damage/speed
— "transient" here is a Permanence Charter requirement (§ 1.2 rule 6), not a style choice,
since attribute modifiers do persist in entity NBT otherwise. Stripped on unload, on
server-stop, and now also swept by `/greenward decommission`. Drop/XP bonuses only apply
to direct player kills (`LootTableEvents.MODIFY_DROPS` + a `DIRECT_ATTACKING_ENTITY`
check, the same structural pattern The Marked already established) — XP is a flat
5-per-kill approximation, the same documented tradeoff as Bone/Gunpowder counting.
`/greenward threat` shows the current value and band.

**Scope simplification, documented**: the granular vanilla-AI-behavior flavor (zombies
breaking doors more, skeletons strafing, shorter creeper fuses) needs mixins into
vanilla's own Goal classes, which this project's own stated principle avoids ("stay on
the stable Fabric API"). Only the stat scaling shipped; the Merciless-band "Warden of the
Pillar" elite-spawn hook is left explicitly unfilled, exactly as the spec itself frames it
(a reserved slot for Update 10's later custom bosses).

## Update 8 — Villagers & Seals

Sneak-right-click a Farmer/Mason/Toolsmith/Fisherman/Weaponsmith/Butcher villager to see
or deliver its Commission (`CommissionHandler` + `VillagerCommissionData`) — plain
right-click trading is completely untouched. Seals (`greenward:seal`) are never craftable,
only ever paid out by Commissions. Prosperity is a simplified stand-in for real
village-census logic (nearby-villager count, not bells/beds/workstations), scaling
Commission size. Tempering reuses `GemSocketRecipe`'s exact mechanism (a second
`CustomRecipe`, `TemperingRecipe`) rather than a physical station + adjacent villager —
gear + a Tempering Stone + 1 Seal merges a stat bonus in.

**Not implemented, documented**: Librarian's "complete any outstanding Proof" Commission
(Proofs aren't a deliverable item — different enough mechanic to need its own pass) and
the physical Tempering Station + profession-adjacency requirement (the recipe-based
version reuses proven infrastructure instead).

## Update 9 — Farming & Fishing Depth

The flat 10/20/35% extra-harvest-roll set bonuses are gone — `FarmingSetContributor`
grants +12/+25/+60 Farming Fortune instead (the spec names +25/+60 for Cultivator's/
Warden's directly; Harvester's +12 is this project's own extrapolation), read through the
same stat pipeline as every other Fortune source. Fishing's hardcoded rod-tier switch is
similarly retired: Angler's Line/Deep-Sea Rod/Leviathan Rod now carry real
`GreenwardStat.SEA_CREATURE_CHANCE`/`FISHING_SPEED` components, and `FishingSetContributor`
carries the full-Angler's-set and rod-3-synergy bonuses. Twin Bite (Tidal/Leviathan's Wear
or the Heartwood's Twin Current node) gives a 25% chance at a second creature per catch.
Blight (`BlightData`, side-data like `FertilizedFarmlandData` — never a block) has a small
per-harvest chance to appear on already-fertilized farmland, suppressing that tile's
Farming Fortune entirely until cleared with a hoe or bone meal; the onset roll only ever
fires inside the player-harvest code path, so it structurally cannot appear near an
Auto-Harvester.

**Reduced scope, documented**: the Catch Log announces a graded catch live in chat
(grade derived from the creature's own rod-tier requirement) rather than maintaining the
full persisted, queryable list with milestone rewards the spec describes — real, separate
storage and a Field Guide page this pass's time budget didn't reach. Harvest Fairs are
not implemented at all — a recurring villager-scored event is a genuinely different
mechanic from Commissions, not a small extension of it.

## Update 10 — Endgame

The Ender Dragon and Wither now scale on Threat like any other hostile, just steeper
(health ×6 at Threat 100 vs. ×3 for ordinary mobs) — reusing `ThreatMobHandler`'s exact
same transient-modifier machinery. Killing either increments `ThreatData`'s boss counter
(feeding Threat's own 15%-weight input) and, at Threat 75+, drops the new Boss Essence
item. The God Potion's four catalysts each gained the one non-automatable ingredient
§ 10.3 calls for: Deepstone Core needs 1 of any Perfect gem (a new `#greenward:perfect_gems`
tag — directly implementable now that Update 5's gems exist), Reaper's Core needs 1 Boss
Essence, and Golden Harvest/Abyssal Pearl needed **substitutes** since their spec'd
ingredients (a Harvest Fair award, a Catch Log entry) come from systems this pass
deliberately didn't build — Sheaf-Token and Deepglass Lens (both already Proof/gem-gated,
already non-automatable) stand in instead, documented at each recipe. Potion duration
extended to 60 minutes as specified; the "+100 Strength/+150 Defense" stat addition is
approximated by the God Potion's existing vanilla Strength/Resistance effects rather than
a new parallel GreenwardStat-buff-on-drink system.

**Per § 10.2's own explicit instruction**, the custom-boss architecture (summoning
altar, the Warden of the Pillar spawn, the boss-construction pattern) is a named,
reserved hook and nothing more — building the actual bosses was never in scope for this
update, by the source document's own design.

## Testing performed this session (Updates 6-10)

- `./gradlew compileJava` — clean after every incremental change across all five updates.
- `./gradlew runServer` — one boot after the Heartwood, one after Threat, one after
  Villagers & Seals, one after Farming/Fishing Depth + Endgame together, and one final
  combined boot of the entire Updates 1-10 state — all five clean, zero exceptions.
- **Live-verified via RCON**: placed two Heartwoods via `/setblock` (confirms
  `setPlacedBy`'s one-per-world refusal can't be exercised this way, same `/setblock`
  limitation as every placement-guard test this session) and read back clean block-entity
  NBT from both; summoned a zombie and a wither skeleton and confirmed the Threat
  ENTITY_LOAD hook runs with zero exceptions (Threat correctly computes to 0 with no
  player online, so no scaling was visually observable — expected, not a bug); confirmed
  `greenward:boss_essence` is a real, givable item.
- **Not covered** (needs a real client + player, the same category of gap disclosed for
  every prior update): unlocking any Heartwood node or respeccing a branch; socketing or
  Sigil-upgrading a talisman; the Heartwood's stat bonuses actually reaching
  `/greenward stats`; any Threat-scaled combat at a nonzero Threat value; delivering a
  real Commission or Tempering a real piece of gear; Blight actually appearing and being
  clearable; Twin Bite's second creature; the rescaled Dragon/Wither and Boss Essence
  drop at Threat 75+; brewing an actual God Potion through the four updated catalysts.

## Post-Design-Program additions

User-requested features and fixes built after the 10-update program shipped, outside its
scope but held to the same compile/boot/documentation bar.

**God Potion now lasts 8 hours** (576000 ticks, up from Update 10's own 60-minute spec)
— "since they're hard to get," SkyBlock-style. No other change to its effects/brewing.

**Elytra Fusion**: a real smithing-table recipe (`ElytraFusionRecipe`, a genuine
`SmithingRecipe`, not a crafting-table `CustomRecipe`) — any chestplate (vanilla or
Greenward's own, matched by scanning the item registry for the `Equippable` component's
chest slot, not a hand-kept list) + a vanilla Elytra → the same chestplate back with
`DataComponents.GLIDER` added. That component is a bare marker with no other fields, so
every existing stat/socket/durability/enchantment on the chestplate survives untouched.
Refuses an already-glided chestplate.

**Voidstep Blade**: Hypixel SkyBlock's Aspect of the End. Combat stats sit at Combat
Tier I, since the item's value is entirely its ability. Right-click raycasts up to 8
blocks along your look vector, stopping just short of any solid block, and teleports you
there — 10-second cooldown. No void/suffocation fallback beyond that raycast clamp.
Recipe reads as a normal vanilla sword shape (2 material + 1 stick): a small 2-step Ender
Pearl compression chain (Pearl Cluster, then Pearl Nexus) was added just so "2× material"
could mean something other than 2 raw pearls — 2 Pearl Nexus + 1 Stick, still 8 raw
pearls total end to end (2:1 compression twice), matching the original flat-8 cost.

**Lava Fishing**: a late-game track parallel to water fishing, built entirely without
vanilla's `FishingHook` — it never bites over lava, so there's no vanilla loot roll to
hook into the way Sea Creatures does. Instead, the Scorched Leviathan Rod (crafted from a sacrificed
Leviathan Rod + 4 Blaze Rods + 2 Magma Cream + 1 Obsidian — a plain crafting recipe, so
any gems already socketed into that specific Leviathan Rod are lost, unlike the Four
Pillars' component-preserving ascension) drives a fully self-contained cast/bite/resolve
loop with no bobber entity at all: right-click while looking at lava starts a timed cast,
a server-tick sweep resolves it straight into the inventory. Its own new stat, Lava
Creature Chance (kept separate from Sea Creature Chance on purpose — parallel tracks, not
a shared budget), gates a rare Magma Wyrm catch (a buffed, reskinned Blaze) that drops the
new Cinder Heart plus bonus Blaze Rods/Ghast Tears — calibrated to sit slightly above the
Abyssal Warden's own drop, per spec from the user.

**Real art pass (Retro Diffusion)**: the user supplied an API key and asked for genuine
AI-generated, vanilla-blend art in place of this session's own procedural placeholders,
using the `rd_plus__mc_item` style (a Minecraft-item-specific pixel-art model, 16×16
native output, transparent background, no rescaling needed). Budget was the limit — the
key had $0.38, enough for ~15 images at ~$0.023 each. Covered: all 4 pieces of
Leviathan's Wear (Lovecraftian deep-sea diver theme — brass diving helmet, glowing
cyclopean eye, tentacle/barnacle motifs) and Warden's Garb (overgrown-living-armor theme
— moss, vines, flowers over bark plating), plus Voidstep Blade, Pearl Cluster, Pearl
Nexus, and Boss Essence. Scorched Leviathan Rod and Cinder Heart failed on the API's own
end (transient 502s, then timeouts on retry) and still carry placeholder art; the
remaining 3 tiers' worth of Farming/Mining/Combat gear and every material/catalyst item
also still carries placeholder art — the budget didn't stretch further. A follow-up pass
with a funded key should use the same style and the per-pillar theme language now
established in `PROGRESSION_SUMMARY.md` § 12.

See `PROGRESSION_SUMMARY.md` § 12 for exact numbers/recipes/prompts on all of the above.

## Threat gating, the Coin economy, and Slayers

Threat now stays off — a flat 0, zero mob scaling — until a Heartwood is placed and its
Threat toggle switched on inside the Heartwood menu, per the user's own request during a
SkyBlock-comparison pass. That same pass also produced a Coin economy: a virtual per-
player balance (never a physical item), and a real Shop screen — wear a Coin Purse in
your offhand, sneak-right-click any villager to open it (vanilla trades stay completely
untouched). The Shop has a Sell slot, a Repair slot, and a small Buy catalog (Waystones,
spare Coin Purses) — a proper GUI, replacing an earlier gesture-based version the user
tried and asked to be made more discoverable. Waystones give Coins a fast-travel sink
priced by distance, and Slayers — summon-a-boss quests reusing The Marked's own five
vanilla mob types — pay Coins, a guaranteed drop, and Combat XP for a real melee/ranged
kill. Full numbers in `PROGRESSION_SUMMARY.md` § 14.

## Fishing rod fix, tier upgrades, and a fishing weapon line

Custom fishing rods were fundamentally broken — casting one spawned a bobber that vanished
instantly, never threw a line. Root cause was a hardcoded vanilla check deep inside
`FishingHook` that only ever recognized the literal vanilla Fishing Rod; fixed with the
mod's first mixin, which broadens that one check to any fishing rod. Also fixed: every
pillar's tier-1→2 recipe (Mining, Fishing, Farming, Combat) let you skip straight to tier
2 without ever crafting tier 1 — now all require the tier-1 item, like tier 2→3 already
did. Fishing armor now grants Sea Creature Chance like Farming/Mining armor grants their
own Fortune. Farming's and Mining's top-tier tools now reach the same netherite-and-a-
little-past damage ceiling Combat's Reaper's Edge already had. Sea creatures now visibly
get reeled toward you instead of just appearing at the bobber. And Fishing has its own
weapon line now: three tridents paired with the rod tiers, the final one (Leviathan's
Trident) needing a real Abyssal Warden drop to craft and carrying its own sneak-right-click
ability, Riptide Slash — a 6-block arc of damage with a splash-particle trail. Full
numbers and reasoning in `PROGRESSION_SUMMARY.md` § 16.
