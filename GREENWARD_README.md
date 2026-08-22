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
