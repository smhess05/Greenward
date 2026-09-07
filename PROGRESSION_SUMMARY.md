# Greenward — Full Progression Summary

Mod id `greenward`, target Minecraft 26.2 / Fabric Loader 0.19.3 / Fabric API
0.156.0+26.2. This document is a complete, order-of-acquisition inventory of every
progression-relevant item in the mod, with exact stats and recipes, intended as a
handoff reference for review — not a player-facing guide (see `GREENWARD_README.md`
for narrative documentation and `FieldGuideContent.java` for the in-game book).

All feature flags (`GreenwardConfig.java`) default to `true`; everything below is live
in a default install. Covers Design Program Revision 3 Updates 1-10 (§§ 0-11) plus every
post-program addition made since (§ 12) — kept current as of the Elytra Fusion/Voidstep
Blade/Lava Fishing/8-hour-God-Potion pass.

---

## 0. Shared Systems

### 0.1 Compression (used by every pillar)

Universal pattern: **9 raw → 1 rung 1** → **9 rung 1 → 1 rung 2** → **9 rung 2 → 1 rung
3** → **9 rung 3 → 1 rung 4** (shapeless 3×3 at every step). Wheat and Carrot chains also
reverse at every rung (1 compressed → 9 of the previous rung/raw item). Every other chain
is one-way. Diamond has no compression chain at all. Rungs 3/4 (Update 3) are a manual
crafting-table step only — machines never auto-compress past rung 2 (see § 0.2's Press/
Deep Press).

| Pillar | Raw | Rung 1 | Rung 2 | Rung 3 | Rung 4 |
|---|---|---|---|---|---|
| Farming (primary) | Wheat | Wheat Sheaf | Wheat Bale | Rick | Mow |
| Farming (secondary) | Potato | Potato Sack | Potato Crate | Pallet | Granary |
| Farming (rare) | Carrot | Gilded Carrot | Radiant Carrot | Sunburst Carrot | Solar Carrot |
| Mining (primary) | Cobblestone | Cobble Cluster | Cobble Monolith | Cobble Massif | Cobble Batholith |
| Mining (secondary) | Cobbled Deepslate | Deepslate Cluster | Deepslate Monolith | Deepslate Massif | Deepslate Batholith |
| Fishing (primary) | Cod | Cod School | Cod Shoal | Cod Haul | Cod Trove |
| Fishing (secondary) | *(Dried Kelp Block, vanilla)* | — | Kelp Reef | — | — |
| Combat (primary) | Bone | Bone Bundle | Bone Reliquary | Bone Charnel | Bone Ossuary |
| Combat (secondary) | Gunpowder | Powder Satchel | Powder Cache | Powder Magazine | Powder Arsenal |

### 0.2 Automation machines

All three craft as 7× Iron Ingot (frame) + 1× role tool (center) + 1× Redstone Block
(bottom-center), shaped 3×3.

| Machine | Recipe center item | Base interval | Action |
|---|---|---|---|
| Auto-Harvester | Iron Hoe | 40 ticks (2s) | Scans 9×9 (radius 4, own Y/Y+1) for a mature crop, harvests + replants at age 0, applies fertilized-farmland 2× bonus |
| Auto-Miner | Iron Pickaxe | 60 ticks (3s) | Mines the block it faces; common ores always regrow, precious ores only with a Deep Regrowth Module (§ 0.2 Upgrades) |
| Auto-Fisher | Fishing Rod | 200 ticks (10s) | Needs adjacent water; rolls fish/junk only (10:85 weight) plus an independent ~2% Nautilus Shell chance — never rolls treasure (Update 4 § 4.1④) |

**Update 4 § 4.2 — all three machines (and the five Effigies below) run at their base
rate with zero fuel.** Fuel is a temporary speed boost, not an operation gate — see the
Fuel table further down.

**Upgrades** (drop into a machine's side slot, consumed instantly, tier 0–4, 5 tiers
each — craft 4× to max, except Compression/Deep Regrowth below, which install once):

| Upgrade | Recipe | Effect/tier |
|---|---|---|
| Storage (`storage_upgrade`) | 8× Iron Ingot + 1× Chest (shaped, ring) | +1 row (9 slots), caps at 45 total |
| Speed (`speed_upgrade`) | 4× Gold Ingot + 4× Redstone + 1× Sugar (shaped, ring) | Shortens operation interval |
| Ore Regrowth, Auto-Miner only (`regen_upgrade`) | 4× Diamond + 1× Bone Meal (shaped, plus) | Shortens **common**-ore regrow delay |
| Deep Regrowth Module, Auto-Miner only (`deep_regrowth_module`) | 4× Diamond + 4× Gold Ingot + 1× Emerald (shaped, ring), gated Diamond Tier VI | Without it, precious ores (gold/diamond/emerald) never regrow; with it, they regrow at 5× the common delay |
| Compression — Press (`press`) | 8× Iron Ingot + 1× Cobble Cluster (shaped, ring), gated Cobblestone Tier V | Machine output auto-compresses to rung 1 |
| Compression — Deep Press (`deep_press`) | 4× Diamond + 4× Iron Ingot + 1× Press (shaped, ring), gated Cobblestone Tier X | Machine output auto-compresses to rung 2; installs directly to tier 2 even without Press first |

Compression and Deep Regrowth are dedicated upgrade slots alongside Storage/Speed/Regen —
not a shared "max 2 modules" cap; the Harvester and Fisher (which never had a Regen slot)
reuse the Compression column, the Miner gets two new slot columns (6th: Compression,
7th: Deep Regrowth).

Operation interval by speed tier (ticks): Harvester 40/32/24/18/10 · Miner
60/48/36/24/15 · Fisher 200/160/120/80/50 — each further shortened by the active fuel
boost percentage, if any. Common-ore regrow delay by Regen tier (ticks): 200, 150, 100,
60, 30 — precious-ore delay (with the Deep Regrowth Module) is 5× those values.

**Placement cap (Update 4 § 4.1③)**: max 24 automation blocks (machines + Effigies
combined) per world (every dimension in the save, not per-dimension) — enforced at
placement via `MachinePlacementGuard`; a refused placement pops the block back off and
returns the item with a chat message. The spec's per-chunk cap was removed at the user's
request ("I want the peak of automation to be very powerful") — dense single-chunk
automation farms are intentionally allowed now.

**Fuel — a speed boost, not an operation budget (Update 4 § 4.2)**: dropped in
instantly, grants `(percent, duration)`. Refueling only consumes an item when it would
help (expired, or the new item beats the active percent) — extends duration and takes
the higher percent otherwise.

| Fuel | Boost | Duration |
|---|---|---|
| Coal / Charcoal | +5% | 30 min |
| Coal Block | +5% | 4 hr |
| Dried Kelp Block | +8% | 1 hr |
| Wheat Bale / Potato Crate | +10% | 2 hr |
| Wheat Rick / Potato Pallet | +15% | 6 hr |
| Lava Bucket | +25% | 12 hr |
| **Sunwheel** (`sunwheel`, new) | +25% | Permanent, but only while `getOverworldClockTime() % 24000 < 12000` ("daytime") |

**Gated (not ungated) treasure fishing, players only**: vanilla's fishing loot table still
has its open-water requirement removed on the treasure pool for real player casts (name
tags, saddles, enchanted books/rods/bows, nautilus shells) — the Auto-Fisher no longer
shares that access at all (§ 4.1④ above).

### 0.2b Effigies (Update 4 § 4.3) — combat automation that kills nothing

One shared `EffigyBlock`/`EffigyBlockEntity`/`EffigyMenu`/`EffigyScreen` implementation
(parametrized by `EffigyType`) backs all five — same fuel-as-boost model, same Storage/
Speed/Compression upgrade slots (no Regen or seed slot) as the three machines above, and
subject to the same placement caps.

| Effigy | Produces | Base interval | Recipe center |
|---|---|---|---|
| Rotting Effigy | Rotten Flesh (0-2) | 100t | Rotten Flesh (raw — no compression chain exists) |
| Bonepile Effigy | Bone (0-2) + Arrow (0-2) | 100t | Bone Reliquary (real rung-2, per spec) |
| Webbed Effigy | String (0-2) + Spider Eye (34% × 1) | 120t | String (raw) |
| Volatile Effigy | Gunpowder (0-2) | 140t | Powder Cache (real rung-2, per spec) |
| Void Effigy | Ender Pearl (50% × 1) | 400t | Ender Pearl (raw) |

Recipe shape (all five): 7× Iron Ingot (ring) + 1× center item + 1× Redstone Block
(shaped 3×3, matching the three machines' own recipe shape). Gated on Bone Tier V (an
interpretation — the source document doesn't specify a gate for Effigies at all; this
matches the convention the three machines already use of gating on their own pillar's
Tier V).

**Combat recipe costs restored to parity** now that Effigies close the automation gap:
Marrowguard's and Ashwrought's armor recipes are back to the standard 5 / 2+2 / 6+1 / 7+1
pattern (see § 1 below's cross-pillar Tier I/II costs) — the 35% discount from the
"Progression polish" pass was explicit, documented compensation for Combat having no
automation, conditioned on exactly this.

### 0.3 The Ascension (shared Tier II → III mechanic, all 4 pillars)

Tier III gear is a **`minecraft:smithing_transform`** at a real smithing table —
`template = Ascension Template`, `base = the Tier II piece`, `addition = that pillar's
Catalyst`. All three consumed; the Tier II piece becomes its Tier III form (it does not
remain in inventory).

**Ascension Template** — shaped 3×3: 4× Diamond (corners) + 4× Obsidian (edges) + 1×
Emerald (center) → yields **2**. A full Tier III set (4 armor + 1 weapon/tool) needs 5
upgrades, so ~3 template crafts per pillar.

### 0.4 Satchels (Update 3)

Four passive-collection items, one per pillar — vacuum matching materials out of the
player's main inventory (36 slots, hotbar included; armor/offhand untouched) once a
second, up to a live capacity. Not a stack of the raw item — a `Map<Item, Integer>` data
component, so a Satchel can hold a mix of raw drops and any rung of that pillar's
compression chain simultaneously.

| Satchel | Pillar | Tag vacuumed | Recipe center | Gate |
|---|---|---|---|---|
| Agronomy Satchel | Farming | `#greenward:farming_materials` | Wheat Bale | Wheat Tier VI |
| Lithic Satchel | Mining | `#greenward:mining_materials` | Cobble Monolith | Cobblestone Tier VI |
| Tidal Satchel | Fishing | `#greenward:fishing_materials` | Cod Shoal | Cod Tier VI |
| Ossuary Satchel | Combat | `#greenward:combat_materials` | Bone Reliquary | Bone Tier VI |

Recipe shape (all four): 4× Leather + 4× String (ring) + 1× center item, shaped 3×3.

Capacity is not a physical upgrade — it's recomputed every vacuum pass from the highest
tier the player has completed among that pillar's collections: Tier VI → 256, Tier VIII →
1024, Tier X → 4096. Sneak-right-click dumps everything back into the player's inventory
(overflow drops on the ground). The item's durability-style bar shows fill fraction.

### 0.5b Gem sockets (Update 5 § 5.2 — shared across all 4 pillars)

Tier II gear gets 2 sockets, Tier III gets 3 (flat rule off the spec's "1-3" range) — all
32 Tier II/III armor pieces and all 8 Tier II/III weapons/tools across every pillar.
Socketing is `greenward:gem_socket`, a `CustomRecipe` (not data-driven — the result
depends on the input gear's current stats): place one socket-eligible item + one cut gem
in a crafting grid, get the gear back with the gem's stat merged in and a socket used.

| Gem | Grants | Rough / Fine / Flawless / Perfect |
|---|---|---|
| Amber | Mining Speed | 5 / 15 / 35 / 75 |
| Jade | Mining Fortune | 5 / 15 / 35 / 75 |
| Sapphire | Fishing Speed + Sea Creature Chance | 5/0.5 · 15/1.5 · 35/3.5 · 75/7.5 |
| Ruby | Strength | 5 / 15 / 35 / 75 |

Each gem cuts up through 4 qualities at a plain crafting table, 5-of-previous-cut per
step, ungated (ladder-continuation convention, same as compression). Rough Amber/Jade/
Sapphire/Ruby themselves only come from mining (see § 2 Mining Pillar) — never crafted
from scratch.

### 0.5 Creative Tab

`ModCreativeTab.java` registers a "Greenward" tab that lists every item under the
`greenward` namespace, filtered live from `BuiltInRegistries.ITEM` — no hand-kept list,
automatically respects disabled feature flags.

---

## 1. Farming Pillar

**Order of acquisition**: Wheat → Wheat Sheaf → Wheat Bale (existing, also fuel) ·
Potato → Potato Sack → Potato Crate · Carrot → Gilded Carrot → Radiant Carrot ·
Fertilizer · **Tier I** (Harvester's Garb + Scythe) · **Tier II** (Cultivator's Garb +
Scythe, needs Wheat Bale + Potato Sack) · Harvest Core (needs Wheat Bale + Potato Crate
+ Radiant Carrot) · **Tier III** (Warden's Garb + Harvest Warden, ascended).

### Fertilizer
Shapeless: 3× Bone Meal + 1× Dirt + 1× Wheat Seeds → 4× Fertilizer. Right-click vanilla
Farmland to convert (preserves moisture); fertilized farmland gives a 2× crop-yield bonus
mirrored by the Auto-Harvester.

### Tier I — Harvester's Garb & Scythe
Armor material `HARVESTERS_GARB`: durability×5 (leather-tier), defense
boots2/legs4/chest5/helm2 (**13 total**), toughness 0.0, kb 0.0, enchant value 15,
repairs on Wheat Bale, sound LEATHER.

| Piece | Recipe |
|---|---|
| Hat | 5× Wheat Sheaf |
| Boots | 2× Wheat Sheaf + 2× Leather |
| Leggings | 6× Wheat Sheaf + 1× Leather |
| Tunic | 7× Wheat Sheaf + 1× Leather |

**Harvester's Scythe** (`HoeItem`, material dur. 1200 / speed 8.0 / atk-bonus 3.0 /
enchant 10, repairs on Wheat Sheaf): 2× Wheat Sheaf + 1× Wheat Bale + 2× Stick.

Set bonus (full set): Haste I while holding any hoe; 10% chance at one extra crop drop
(rolled once, additive with fertilized 2×); hoe radius base 2, **+1 with full set → 3**.

### Tier II — Cultivator's Garb & Scythe
Armor material `CULTIVATORS_GARB`: durability×33 (diamond-tier), defense
boots3/legs6/chest8/helm3 (**20 total**), toughness 2.0, kb 0.0, enchant 10, repairs on
Potato Crate, sound DIAMOND.

| Piece | Recipe |
|---|---|
| Hat | 5× Wheat Bale |
| Boots | 2× Wheat Bale + 2× Potato Sack |
| Leggings | 6× Wheat Bale + 1× Potato Sack |
| Tunic | 7× Wheat Bale + 1× Potato Sack |

**Cultivator's Scythe** (dur. 1800 / speed 8.0 / atk-bonus 3.0 / enchant 10, repairs on
Potato Crate): 2× Wheat Bale + 1× Potato Sack + 2× Stick.

Set bonus: Haste II while holding a hoe; 20% extra-drop chance; hoe radius flat 3 (no
armor-driven bonus).

### Harvest Core (Tier III catalyst)
Shapeless: 1× Wheat Bale + 1× Potato Crate + 1× Radiant Carrot.

### Tier III — Warden's Garb & Harvest Warden (ascended)
Armor material `WARDENS_GARB`: durability×45 (exceeds netherite), defense
boots4/legs7/chest9/helm4 (**24 total**), toughness 4.0, kb 0.15/piece (**0.6 full
set**), enchant 15, repairs on Potato Crate, sound NETHERITE.

Ascend each Cultivator's piece with Harvest Core (template = Ascension Template).

**Harvest Warden** (dur. 2400 / speed 9.0 / atk-bonus 3.0 / enchant 15, repairs on
Harvest Core): ascended from Cultivator's Scythe + Harvest Core.

Set bonus: Haste II while holding a hoe; Regeneration I always; hoe radius base 3,
**+1 only with full Warden's Garb → 4** (the true 9×9 cap — mixing tiers never reaches
it).

---

## 2. Mining Pillar

**Order**: Cobblestone → Cobble Cluster → Cobble Monolith · Cobbled Deepslate →
Deepslate Cluster → Deepslate Monolith · **Tier I** (Prospector's Plate + Drill,
existing) · **Tier II** (Excavator's Plate + Pick) · Bedrock Core (needs Amethyst
Shard, naturally renewable via budding amethyst) · **Tier III** (Bedrock Plate +
Reaver, ascended).

### Rare ore drops (Update 5 § 5.1 — zero worldgen additions, gated by tool tier + Mining skill)

| Material | Drops from | Requires | Base rate |
|---|---|---|---|
| Glimmer | Deepslate/Cobbled Deepslate, Y ≤ -40 | Prospector's Drill+, Mining 15 | 1.5% |
| Titanshard | Iron/Copper/Gold ore, Y ≤ -40 | Excavator's Pick+, Mining 25 | 1% |
| Rough Amber | Any ore (`#c:ores`) in a cluster of 5+ | Excavator's Pick+, Mining 30 | 0.6% |
| Rough Jade | Emerald ore, any Y | Excavator's Pick+, Mining 30 | 4% |
| Rough Sapphire | Lapis ore, Y ≤ -40 | Excavator's Pick+, Mining 35 | 0.8% |
| Rough Ruby | Redstone ore, Y ≤ -55 | Bedrock Reaver specifically, Mining 40 | 0.5% |

Rate scales with Mining Fortune as a straight multiplier: `effective = base × (1 +
fortune/100)`. Machine-mined blocks (Auto-Miner) never roll any of these — same
`PlayerBlockBreakEvents.AFTER` machine-proofing as everything else. Glimmer and
Titanshard have no crafting use yet (reserved for Update 6's Heartwood Stone branch);
the four Rough gems feed § 0.5b's cutting/socket system.

### Tier I — Prospector's Plate & Drill
Armor material `PROSPECTORS_PLATE`: durability×15 (iron-tier), defense
boots3/legs5/chest6/helm3 (**17 total**), toughness 1.0, kb 0.0, enchant 9, repairs on
Iron Ingot, sound IRON.

| Piece | Recipe |
|---|---|
| Helm | 4× Iron Ingot + 1× Diamond |
| Treads | 2× Iron Ingot + 2× Diamond |
| Greaves | 6× Iron Ingot + 1× Diamond |
| Plate | 7× Iron Ingot + 1× Diamond |

**Prospector's Drill** (pickaxe, dur. 1400 / speed 9.2 / atk-bonus 3.0 / enchant 10,
repairs on `ItemTags.DIAMOND_TOOL_MATERIALS`): 2× Diamond + 1× Iron Ingot + 2× Stick.
Total attack 5 (diamond-equal); mining speed exceeds diamond (8.0) and matches near
netherite (9.0). No vein-mining — speed is the whole point. **+120 Mining Speed / +10
Mining Fortune** via the Greenward stat system (Update 5 § 5.4 — see § 0.2b below).

Set bonus: Haste I always; Night Vision below Y0; +1 safe-fall distance
(`Attributes.SAFE_FALL_DISTANCE`).

### Tier II — Excavator's Plate & Pick
Armor material `EXCAVATORS_PLATE`: durability×33, defense boots3/legs6/chest8/helm3
(**20 total**), toughness 2.0, kb 0.0, enchant 10, repairs on Deepslate Monolith,
sound DIAMOND.

| Piece | Recipe |
|---|---|
| Helm | 5× Cobble Monolith |
| Treads | 2× Cobble Monolith + 2× Deepslate Cluster |
| Greaves | 6× Cobble Monolith + 1× Deepslate Cluster |
| Plate | 7× Cobble Monolith + 1× Deepslate Cluster |

**Excavator's Pick** (dur. 1800 / speed 9.6 / atk-bonus 3.0 / enchant 10, repairs on
Deepslate Monolith): 2× Cobble Monolith + 1× Deepslate Cluster + 2× Stick. **+260 Mining
Speed / +30 Mining Fortune** (Update 5 § 5.4).

Set bonus: Haste II always; Night Vision below Y0; +2 safe-fall (own attribute-modifier
id, doesn't stack with Tier I's).

### Bedrock Core (Tier III catalyst)
Shapeless: 1× Cobble Monolith + 1× Deepslate Monolith + 1× Amethyst Shard.

### Tier III — Bedrock Plate & Reaver (ascended)
Armor material `BEDROCK_PLATE`: durability×45, defense boots4/legs7/chest9/helm4
(**24 total**), toughness 4.0, kb 0.15/piece (0.6 full set), enchant 15, repairs on
Bedrock Core, sound NETHERITE.

**Bedrock Reaver** (dur. 2400 / speed 10.5 / atk-bonus 4.0 / enchant 15, repairs on
Bedrock Core): ascended from Excavator's Pick + Bedrock Core. Fastest pickaxe in the
mod (total attack 6, netherite-equal). **+500 Mining Speed / +60 Mining Fortune** (Update
5 § 5.4). *Original substitution note, later resolved*: the draft spec's "toggleable
3×3 vein-mining + auto-smelt" flavor originally shipped as a pure stats capstone with no
vein-mining at all. Update 5 § 5.3 added it back as **Vein Blast** — sneak-right-click
throws the Reaver as a projectile that detonates on impact, mining every ore in a
radius-3 sphere with full Fortune/collection credit — plus **Stoneflow** (plain
right-click: +250% Mining Speed for 15s). Both are gated on wielding Bedrock Reaver
itself (an interim substitute for the source spec's Heartwood-branch gate, which doesn't
exist until Update 6) and share independent 120s cooldowns.

Set bonus: Haste II always; Fire Resistance always; Night Vision below Y0; +3 safe-fall
(own id).

---

## 3. Fishing Pillar

**Order**: Cod → Cod School → Cod Shoal · Dried Kelp Block (vanilla) → Kelp Reef ·
**Tier I** (Angler's Wear + 3 rod tiers, existing) · **Tier II** (Tidal Wear) ·
Leviathan's Heart · **Tier III** (Leviathan's Wear, ascended — **no new rod tier**, see
below).

### Tier I — Angler's Wear & Rod Line
Armor material `ANGLERS_WEAR`: durability×25 (turtle-scute-tier), defense
boots2/legs4/chest5/helm2 (**13 total**), toughness 0.0, kb 0.0, enchant 9, repairs on
Prismarine Shard, sound TURTLE.

| Piece | Recipe |
|---|---|
| Cap | 5× Prismarine Shard |
| Fins | 2× Prismarine Shard + 2× Turtle Scute |
| Waders | 6× Prismarine Shard + 1× Turtle Scute |
| Coat | 7× Prismarine Shard + 1× Turtle Scute |

**Rod ladder** (all `FishingRodItem`, baked-in enchantments via
`delayedComponent(DataComponents.ENCHANTMENTS, ...)` — real components, not a virtual
bonus):

| Rod | Recipe | Durability | Baked enchants |
|---|---|---|---|
| Angler's Line (I) | 3× Prismarine Shard + 2× String | 256 | Lure I |
| Deep-Sea Rod (II) | shapeless: Angler's Line + 4× Prismarine Crystals + 1× Nautilus Shell | 512 | Lure II, Luck of the Sea I |
| Leviathan Rod (III) | shapeless: Deep-Sea Rod + 1× Heart of the Sea + 2× Nautilus Shell + 2× Prismarine Crystals | 1024 | Lure III, Luck of the Sea II |

Set bonus (full Angler's Wear): Water Breathing; Dolphin's Grace; +1 Luck attribute
(substitute for an unhookable fishing-wait-time reduction, documented).

### Tier II — Tidal Wear
Armor material `TIDAL_WEAR`: durability×33, defense boots3/legs6/chest8/helm3
(**20 total**), toughness 2.0, kb 0.0, enchant 10, repairs on Kelp Reef, sound DIAMOND.

| Piece | Recipe |
|---|---|
| Cap | 5× Cod Shoal |
| Fins | 2× Cod Shoal + 2× Dried Kelp Block |
| Waders | 6× Cod Shoal + 1× Dried Kelp Block |
| Coat | 7× Cod Shoal + 1× Dried Kelp Block |

No new tool at this tier. Set bonus: Water Breathing; Dolphin's Grace; +2 Luck (own id).

### Leviathan's Heart (Tier III catalyst)
Shapeless: 1× Cod Shoal + 1× Kelp Reef + 1× Heart of the Sea.

### Tier III — Leviathan's Wear (ascended)
Armor material `LEVIATHANS_WEAR`: durability×45, defense boots4/legs7/chest9/helm4
(**24 total**), toughness 4.0, kb 0.15/piece (0.6 full set), enchant 15, repairs on
Leviathan's Heart, sound NETHERITE.

*Deliberate substitution*: no new "Tier III rod" was built — the existing Leviathan Rod
(Tier I ladder above) already reaches this pillar's tool cap with its own tested
sea-creature-gating mechanic; a 4th rod tier would have been redundant. Documented in
`GREENWARD_README.md`.

Set bonus: Water Breathing; Dolphin's Grace; +3 Luck (own id); Night Vision while
underwater.

### Sea creatures (manual fishing only)

Structural safety: only fires when `LootContext` carries `THIS_ENTITY` as a real
`FishingHook` — the Auto-Fisher's manually-built loot params never set that parameter,
so it can never trigger these, by construction not code-path assumption.

**Spawn chance** (base + rod tier + Angler's Wear, capped 100%):

| Held rod | Chance | +full Angler's Wear |
|---|---|---|
| Vanilla/unlisted | 5% | 20% |
| Angler's Line (I) | 10% | 25% |
| Deep-Sea Rod (II) | 20% | 35% |
| Leviathan Rod (III) | 35% | **85%** |

**Roster** (rod-tier-gated — a roll re-rolls among only what the held rod can catch):

| Creature | Weight | Min rod | Vanilla mob | HP× | Drops |
|---|---|---|---|---|---|
| Drenched Husk | 45 | any | Drowned | 1.0 | 1–3 Prismarine Shard |
| Reef Stalker | 25 | Line+ | Guardian | 1.0 | 2–4 Prismarine Crystals |
| Bloated Swarm | 15 | any | Pufferfish ×4 | 1.0 | 1–2 Pufferfish each |
| Tidecaller | 10 | Deep-Sea+ | Drowned (+Trident) | 1.5 | 1 Nautilus Shell, 2–5 Prismarine Crystals |
| Abyssal Warden | 5 | Leviathan only | Elder Guardian | 2.0 | 1 Heart of the Sea, 1 Nautilus Shell, 4–8 Prismarine Crystals |

---

## 4. Combat Pillar (built from nothing — no prior Tier I)

**Order**: Bone → Bone Bundle → Bone Reliquary · Gunpowder → Powder Satchel → Powder
Cache · **Tier I** (Marrowguard + Blade) · **Tier II** (Ashwrought + Edge) · Reaper's
Core (needs Ender Pearl) · **Tier III** (Reaper's Aegis + Edge, ascended).

**Balance note**: unlike the other three pillars, Combat has **no automation machine**
— Bone/Gunpowder are pure manual-kill grinds. Armor/weapon recipe costs were
deliberately cut ~35% below the mirrored farming/mining/fishing pattern to compensate
(see § below — do not "fix" this back to parity without re-adding some form of
automation support first).

### Tier I — Marrowguard & Blade
Armor material `MARROWGUARD`: durability×15 (iron-tier), defense
boots2/legs5/chest6/helm2 (**15 total**), toughness 0.0, kb 0.0, enchant 9, repairs on
Bone Bundle, sound CHAIN, worn-asset CHAINMAIL.

| Piece | Recipe |
|---|---|
| Helmet | 5× Bone Bundle |
| Boots | 2× Bone Bundle + 2× String |
| Leggings | 6× Bone Bundle + 1× String |
| Chestplate | 7× Bone Bundle + 1× String |

Restored to this (from a temporary 3 / 1+1 / 4+1 / 5+1 discount) by Update 4 once the
Effigies gave Combat automation parity with the other three pillars — see that update's
README section.

**Marrowguard Blade** (sword, dur. 1200 / speed 8.0 / atk-bonus 3.0 / enchant 10,
repairs on Bone Bundle): 1× Bone Bundle + 1× Bone Reliquary + 2× Stick. Total attack 7
(diamond-equal).

Set bonus: Attack Speed +10% (`ADD_MULTIPLIED_TOTAL`).

### Tier II — Ashwrought & Edge
Armor material `ASHWROUGHT`: durability×33, defense boots3/legs6/chest8/helm3
(**20 total**), toughness 2.0, kb 0.0, enchant 10, repairs on Powder Cache, sound
DIAMOND.

| Piece | Recipe |
|---|---|
| Helmet | 5× Bone Reliquary |
| Boots | 2× Bone Reliquary + 2× Powder Satchel |
| Leggings | 6× Bone Reliquary + 1× Powder Satchel |
| Chestplate | 7× Bone Reliquary + 1× Powder Satchel |

Restored from the same temporary discount as Marrowguard, same reason (Update 4).

**Ashwrought Edge** (dur. 1800 / speed 8.0 / atk-bonus 4.0 / enchant 10, repairs on
Powder Cache): 1× Bone Reliquary + 1× Powder Satchel + 2× Stick. Total attack 8
(netherite-equal).

Set bonus: Attack Speed +15%; Attack Damage +1 (flat).

### Reaper's Core (Tier III catalyst — also feeds the God Potion directly)
Shapeless: 1× Bone Reliquary + 1× Powder Cache + 1× Ender Pearl.

### Tier III — Reaper's Aegis & Edge (ascended)
Armor material `REAPERS_AEGIS`: durability×45, defense boots4/legs7/chest9/helm4
(**24 total**), toughness 4.0, kb 0.15/piece (0.6 full set), enchant 15, repairs on
Reaper's Core, sound NETHERITE. *This is the literal target table the draft spec
specified — every other pillar's Tier III was built to match it, not the other way
around.*

**Reaper's Edge** (dur. 2400 / speed 9.0 / atk-bonus 5.0 / enchant 15, repairs on
Reaper's Core): ascended from Ashwrought Edge + Reaper's Core. Total attack 9 (exceeds
netherite's 8 — the spec's exact stated target).

Set bonus: Attack Speed +20%; Attack Damage +2; **Hunter's Mark Aura** (jumps The
Marked's spawn chance straight to its 10% cap); **Grim Resolve** (once per 5 min /
6000 ticks, a lethal hit instead leaves the player at 1 HP + 5s Resistance III — via
`ServerLivingEntityEvents.ALLOW_DEATH`, no mixin; a real Totem still saves first).

### The Marked (melee-kill-only, structurally un-automatable)

Trigger: `AFTER_DEATH` on one of 5 base mobs, gated on `DamageSource.isDirect()` AND
`getEntity() instanceof ServerPlayer` — a projectile kill has a direct entity ≠ causing
entity (fails `isDirect()`); a trap/fall/lava kill has no player entity at all. Neither
path can automate this.

| Mob | Title | HP× | Bonus drop |
|---|---|---|---|
| Zombie | Revenant | 1.5 | — |
| Skeleton | Deadeye | 1.5 | — |
| Spider | Widowfang | 1.5 | — |
| Creeper | Ashborn | 1.5 | 2–4 Gunpowder |
| Enderman | Wraith | 1.5 | 1 Ender Pearl (guaranteed) |

All also drop 2–4 bonus Bone. Chance: base 3% + Marrowguard 1% + Ashwrought 2% (capped
10%); Reaper's Aegis jumps straight to 10%.

---

## 5. The God Potion

Fully reworked this pass to be **automatable-adjacent and boss-drop-free** — every
ingredient can be farmed/mined/fished/fought repeatably; nothing is a one-time kill.

| Item | Recipe | Automation path |
|---|---|---|
| **Golden Harvest** | shaped, corners+edges: 4× Wheat Bale + 4× Potato Crate, center empty | Auto-Harvester (both) |
| **Deepstone Core** | shaped, corners+edges: 4× Obsidian + 4× Diamond + 1× Netherite Scrap (center) | Diamond via Auto-Miner+Deep Regrowth Module; Obsidian trivial; Scrap is **manual only** since Update 4 — Ancient Debris never regrows (§ 4.1①), closing what used to be an infinite Netherite tap here |
| **Abyssal Pearl** | shapeless: 4× Prismarine Crystals + 4× Nautilus Shell + 1× Heart of the Sea | Prismarine Crystals are a Guardian drop (always manual); Nautilus Shells can come from the Auto-Fisher's small ~2% independent chance per catch (Update 4 — no longer treasure-pool access) or a manual catch; the Heart is always manual |
| **Reaper's Core** | shapeless: 1× Bone Reliquary + 1× Powder Cache + 1× Ender Pearl | manual only (no combat automation) — also Combat's own smithing catalyst, reused rather than duplicated |
| **Godly Catalyst** | shaped plus-pattern, center empty: Golden Harvest (top) / Deepstone Core (left) / Abyssal Pearl (right) / Reaper's Core (bottom) | one catalyst per pillar, nothing else |
| **God Potion** | brewed: Awkward Potion + 1× Godly Catalyst | hopper-automatable brewing stand |

**God Potion effects** (all ambient, hidden particles, **576000 ticks = 8 real-time
hours** — see § 12 for the duration's history; originally 9600/8 min):
Strength I, Resistance II, Speed II, Haste II, Regeneration I, Fire Resistance, Water
Breathing, Night Vision, Jump Boost I. No splash/lingering variant registered.

**Catalysts hardened in Update 10** — see § 11 for the exact substitutions (Sheaf-Token,
`#greenward:perfect_gems`, Deepglass Lens, Boss Essence all added on top of the table
above).

*Pre-rework, for contrast*: originally needed 4× Heart of the Sea (Abyssal Pearl),
Golden Carrots + Golden Apple (Golden Harvest), and a Nether Star + Dragon's Breath
(Godly Catalyst) — all removed/reduced this pass.

---

## 6. Master set-bonus table

| Set | Tier | Key bonuses |
|---|---|---|
| Harvester's Garb | Farming I | Haste I (w/ hoe), 10% bonus drop, hoe radius 2→3 |
| Cultivator's Garb | Farming II | Haste II (w/ hoe), 20% bonus drop, hoe radius flat 3 |
| Warden's Garb | Farming III | Haste II (w/ hoe), Regen I, hoe radius 3→4 |
| Prospector's Plate | Mining I | Haste I, Night Vision <Y0, +1 safe-fall |
| Excavator's Plate | Mining II | Haste II, Night Vision <Y0, +2 safe-fall |
| Bedrock Plate | Mining III | Haste II, Fire Resistance, Night Vision <Y0, +3 safe-fall |
| Angler's Wear | Fishing I | Water Breathing, Dolphin's Grace, +1 Luck |
| Tidal Wear | Fishing II | Water Breathing, Dolphin's Grace, +2 Luck |
| Leviathan's Wear | Fishing III | Water Breathing, Dolphin's Grace, +3 Luck, Night Vision underwater |
| Marrowguard | Combat I | +10% attack speed |
| Ashwrought | Combat II | +15% attack speed, +1 attack damage |
| Reaper's Aegis | Combat III | +20% attack speed, +2 attack damage, Hunter's Mark Aura, Grim Resolve |

All checked every 20 ticks per online player (`SetBonusHandler`), not per-tick.

---

## 7. The Heartwood (Update 6)

One placed block (`greenward:heartwood`, `heartwood.json` recipe — gated on
Mining/Wheat/Cod/Bone Tier VI via a 4-criteria advancement), exactly one per world
(`HeartwoodData`, a `SavedData` anchored to the overworld — placing a second refunds the
item and messages the player). Opens a menu with 4 perk branches and a talisman-socket
inventory.

**Branches** — a flat 8-node linear chain per branch (simplification of the source
spec's "8-10 nodes across 5 depths"; a genuine branching-tree UI wasn't built):

| Branch | Notable named nodes |
|---|---|
| Root | Fallow Ward, Fallow Ward II, Verdant Aura |
| Stone | Reaver's Wrath (gates Vein Blast/Stoneflow — see § 2), Regrowth Mastery (gates precious-ore Auto-Miner regrowth) |
| Tide | Tidal Sight, Twin Current (grants Twin Bite — see § 10) |
| Ash | Marked Sense, Undying Resolve |

Each unlockable node costs a `Map<Item, Integer>` (Wheat Rick, Cobble Massif, Cod Haul,
Bone Charnel, vanilla Nautilus Shell, etc. depending on branch/depth) and grants either a
stat bump (`HeartwoodStatContributor` applies every unlocked node's grant to every
player, world-wide — Heartwood perks are shared, not per-player) or a special unlock
flag (`grantsSocket`/ability gates). **Respec** refunds 75% of everything spent so far.
**Talisman sockets**: 4 base + 1 per unlocked `grantsSocket` node.

**Talismans** (`TalismanType`, 6 shipped, each with a Sigil upgrade that doubles the
stat): Sheaf-Token (Farming Proof reward), Cutter's Charm, Angler's Knot, Knucklebone,
Wraith's Eye (4% drop from The Marked's Wraith), Deepglass Lens (crafted: Flawless
Sapphire + Prismarine Shards). Sigils are crafted from their base talisman at a plain
crafting table (documented simplification of "infused at the Heartwood").

## 8. Threat (Update 7)

World-wide difficulty scaling with vanilla mobs — zero new mob types, zero worldgen.
`ThreatManager.computeThreat` takes the max across all online players of a weighted sum,
capped per component: skill (40) + gear tier (20) + Heartwood progress (25) + bosses
defeated (15) = 0-100.

Every hostile mob (`ThreatMobHandler.onLoad`) gets **transient** attribute modifiers
scaled to current Threat (never written to entity NBT, so removing the mod leaves no
trace — Permanence Charter rule 6):

| Attribute | Formula (at Threat T) | Notes |
|---|---|---|
| Max Health | `+T/100 × 2.0` (ordinary) / `× 5.0` (Ender Dragon/Wither, Update 10) | mob healed to full on load |
| Attack Damage | `+T/100 × 0.8` | toggleable via `THREAT_DAMAGE_SCALING`, off by default impact but on by default flag |
| Movement Speed | `+T/100 × 0.35` | always on |

Drops/XP scale too, but **only on a direct player kill** (same rule as The Marked):
drop count `×(1 + T/100×1.5)` (up to ×2.5), bonus XP `×(1 + T/100×2.0)` (up to ×3.0, off
a flat 5-XP baseline approximation). `/greenward threat` reports the current value;
`/greenward decommission` strips every Threat modifier from loaded chunks.

## 9. Villagers & Seals (Update 8)

**Commissions**: sneak-right-click any villager (`CommissionHandler`) to get a
collection request tied to that villager's profession → one of the 4 pillars
(`pillarFor`). Reward is Seals, a currency item that **only ever comes from
Commissions** — never craftable, never from machines/Effigies/selling. Commission
frequency/reward scale with local Prosperity (nearby-villager count × 10, capped 100).

**Tempering** — a 2nd `CustomRecipe` (`TemperingRecipe`, alongside Update 5's
`GemSocketRecipe`): exactly 1 gear item (must already carry `STATS`) + 1 Tempering Stone
+ **exactly 1 Seal** (not a bulk quantity — a crafting grid always consumes 1 item per
occupied slot per craft, so "more Seals" would need repeated re-crafting, not a bigger
stack) → gear back with the Tempering's stat grant merged in.

| Tempering | Grants | Stone recipe (1 Iron Ingot + 2×) |
|---|---|---|
| Keen | (offense-leaning) | Flint |
| Sturdy | (defense-leaning) | — |
| Bountiful | (fortune-leaning) | — |
| Deep | (mining-leaning) | — |
| Briny | (fishing-leaning) | — |
| Swift | (speed-leaning) | — |
| Grim | large offense bump, **-10 Health penalty** | Gunpowder |

(Exact per-tier stat maps live in `TemperingType.java` — this table is a summary, not a
substitute for reading the enum.)

## 10. Farming & Fishing Depth (Update 9)

**Farming**: the old flat +10%/20%/35% harvest-bonus set effect was retired —
`FarmingSetContributor` now grants real Farming Fortune instead (+12 Harvester's, +25
Cultivator's, +60 Warden's, full sets only), read through the same stat layer everything
else uses. **Blight**: a 2% chance (`HarvestLogic.BLIGHT_CHANCE`) per harvest on already
-fertilized soil marks that farmland Blighted (`BlightData`, side-data, same shape as the
existing fertilized-farmland tracker); a Blighted harvest's Farming Fortune is zeroed for
that swing. Clear it with a hoe or bone meal on the blighted farmland block.

**Fishing**: the sea-creature-chance system was retired from a hardcoded rod-tier switch
onto the same stat layer — Angler's Line/Deep-Sea Rod/Leviathan Rod now carry real
`SEA_CREATURE_CHANCE`/`FISHING_SPEED` `STATS` components (5/10, 15/20, 30/35), and
`FishingSetContributor` adds +15% (full Angler's Wear) and a further +35% (also wielding
Leviathan Rod). **Twin Bite**: full Tidal/Leviathan's Wear or the Heartwood Tide branch's
Twin Current node grants a 25% chance at a second creature per catch. **Catch Log**: a
reduced-scope stand-in for the source spec's persisted, milestone-rewarding log — just a
live chat announcement grading the catch Modest/Fine/Superb/Legendary off the creature's
own `minRodTier`, nothing persisted.

## 11. Endgame (Update 10)

**Boss scaling**: the Ender Dragon and Wither are detected in `ThreatMobHandler.onLoad`
(plain `instanceof`, no new entity types) and scale on the steeper ×5.0 health curve
(§ 8) instead of the ordinary ×2.0. Killing either increments `ThreatData`'s
bosses-defeated counter (feeds back into Threat's own formula) and, at Threat ≥ 75,
drops **Boss Essence** — never craftable, only this drop.

**God Potion catalysts hardened** (reversing part of Update 5's original "everything
automatable" rework, deliberately, per the source spec's endgame-gating intent):

| Catalyst | What changed |
|---|---|
| Golden Harvest | center slot now needs a Sheaf-Token (Heartwood talisman, Farming Proof-gated) |
| Deepstone Core | one Obsidian corner now needs `#greenward:perfect_gems` (any Perfect Amber/Jade/Sapphire/Ruby) |
| Abyssal Pearl | Nautilus Shell 4→3, added 1× Deepglass Lens |
| Reaper's Core | added Boss Essence as a 4th shapeless ingredient |

Each substitution stands in for a source-spec ingredient that was never built this
session (Harvest Fair award, Catch Log milestone, etc.) with an already-existing
non-automatable item that gates on comparable effort — see each recipe file's own
in-repo history for the literal mapping.

## 12. Post-Design-Program additions (user-requested, outside the original 10 updates)

### God Potion duration
Originally 8 min (pre-Update-10) → Update 10's own spec text asked for 60 min (72000
ticks, shipped) → the user then asked for it to last much longer still, SkyBlock-style
("since they're hard to get") → now **576000 ticks = 8 real-time hours**
(`ModPotions.DURATION`). No change to the potion's 9 baked effects or its brewing
ingredient.

### Elytra Fusion (smithing table)
`ElytraFusionRecipe` — a genuine `SmithingRecipe` (not a crafting-table `CustomRecipe`
like Gem Socket/Tempering), matching the user's own "datapacks combine them at a
smithing table" suggestion. Base slot accepts **any** item whose `Equippable` component
targets the chest slot (a registry scan built lazily, so every vanilla chestplate and
every Greenward chestplate qualifies — not a hand-kept list); addition slot is a plain
vanilla Elytra; no template. Output is the base chestplate's own stack, copied, with
`DataComponents.GLIDER` added — a bare marker component, so it changes nothing else
about the item (existing `STATS`/`SOCKETS`/durability/enchantments all survive). Refuses
to re-fuse an already-glided chestplate. Recipe JSON: `data/greenward/recipe/
elytra_fusion.json` (trivial — `{"type": "greenward:elytra_fusion"}`, same pattern as
Gem Socket/Tempering).

### Voidstep Blade (teleport weapon)
Hypixel SkyBlock's "Aspect of the End," reskinned. Combat-Tier-I-equivalent sword stats
(dur. 1200 / speed 8.0 / atk-bonus 3.0, repairs on Ender Pearl — its raw damage isn't the
point). Ability (`VoidstepAbilityHandler`, right-click, 10s/200-tick cooldown): raycasts
along the player's look vector up to 8 blocks (`ClipContext.Block.COLLIDER`), stopping
just short of any solid block, and teleports the player there. No suffocation/void
fallback beyond that raycast clamp (chorus fruit's full multi-attempt safety search
wasn't built) — an accepted gap given how short the blink is.

**Recipe, revised to a normal sword shape**: a 2-step Ender Pearl compression chain was
added purely so the blade's own recipe reads as a standard 2-material + 1-stick sword
pattern instead of a 9-slot flat pearl pile — 2× Ender Pearl → 1 **Pearl Cluster**, 2×
Pearl Cluster → 1 **Pearl Nexus** (both shapeless, `ENABLE_VOIDSTEP`-gated). Voidstep
Blade = 2× Pearl Nexus (top two slots) + 1× Stick (bottom), the exact vanilla sword
pattern. Total raw-pearl cost is unchanged at 8 (2:1 compression twice, not the pillars'
own 9:1 — Ender Pearl isn't one of the 9 tracked collections, and 2:1 was chosen
specifically to preserve the original flat-8 cost rather than inflate it).

### Lava Fishing
A late-game fishing track parallel to (not replacing) water fishing, calibrated to
slightly exceed the Leviathan Rod tier per the user's explicit ask.

- **Scorched Leviathan Rod** (plain `Item`, deliberately *not* `FishingRodItem` — vanilla's own
  `FishingHook` never bites in lava, so casting a real bobber would just do nothing and
  fight with this item's own use-handler). Stats: **35 Lava Creature Chance / 40 Fishing
  Speed** (vs. Leviathan Rod's 30/35), 3 sockets, 1024 durability. Recipe: shapeless,
  1× Leviathan Rod + 4× Blaze Rod + 2× Magma Cream + 1× Obsidian. *Caveat*: unlike the
  Four Pillars' Tier II→III ascension (a `smithing_transform`, which carries the base
  item's components forward), this is a plain crafting recipe — any gems already
  socketed into the sacrificed Leviathan Rod are lost. Accepted simplification, not
  mirrored from the ascension mechanic on purpose given scope.
- **New stat**: `LAVA_CREATURE_CHANCE`, kept separate from `SEA_CREATURE_CHANCE` since
  the two rod lines are meant to run in parallel, not share one power budget.
- **The cast loop** (`LavaFishingHandler`) is fully self-contained — no vanilla
  `FishingHook` involved at all, and no new persistent entity (Permanence Charter-safe
  by construction, not by exception). Right-click while looking at lava (raycast,
  `ClipContext.Fluid.ANY` + `FluidTags.LAVA` check, 5-block reach) starts a timed cast
  (60-200 ticks, reduced up to 50% by the Fishing Speed stat); a server-tick sweep
  resolves it with no visible bobber, granting the catch directly to the inventory.
- **Catches**: a junk table (Netherrack, Magma Cream, Glowstone Dust, Quartz, Blaze
  Powder, Ghast Tear, weighted) vs. a rare **Magma Wyrm** (rolled independently against
  the Lava Creature Chance stat, same two-tier structure as Sea Creatures) — a 2.5×-health
  reskinned Blaze that drops **Cinder Heart** (new, never-craftable — the Heart of the
  Sea analog) + 2-3 Blaze Rod + 1-2 Ghast Tear, calibrated to sit slightly above the
  Abyssal Warden's own drop (1 Heart of the Sea + 1 Nautilus Shell + 4-8 Prismarine
  Crystals) per the user's "should slightly surpass late game water fishing."

### Real AI-generated art (Retro Diffusion)

The user supplied a Retro Diffusion API key and asked for real, tailored, vanilla-blend
art in place of this session's earlier procedural-placeholder textures, using the
`rd_plus__mc_item` style preset (a Minecraft-item-specific pixel-art model) at native
16×16 with a transparent background — no upscale/downscale step needed, the API outputs
game-ready pixel grids directly.

**Budget was the hard constraint**: the key had $0.38 remaining, enough for ~15 images
at ~$0.023 each on `rd_plus`. Scoped to the 14 highest-priority pieces per the user's
explicit callouts — Leviathan's Wear (all 4 pieces, Lovecraftian deep-sea diver theme:
brass diving helmet, glowing cyclopean porthole eye, tentacle/barnacle motifs) and
Warden's Garb (all 4 pieces, overgrown-living-armor theme: bark plating wrapped in moss,
vines, flowers) plus 6 items from this session's own new work (Voidstep Blade, Pearl
Cluster, Pearl Nexus, Boss Essence, and an attempted Scorched Leviathan Rod/Cinder Heart).

**12 of 14 succeeded**; **Scorched Leviathan Rod and Cinder Heart failed** on Retro
Diffusion's own end (`inference_failed`, HTTP 502, then repeated timeouts on 3 retries —
a transient service issue during that window, not a prompt problem) and still carry this
session's earlier procedural placeholder art. The account balance is now $0.013 —
retrying those two needs a top-up first.

Every other pillar (Farming Tier I/II, Mining all 3 tiers, Fishing Tier I/II, Combat all
3 tiers) and every material/catalyst/gem/talisman item still carries this session's
earlier hand-written procedural-shape placeholder art, not Retro Diffusion output — the
budget didn't stretch past the 14 prioritized pieces. A follow-up pass with a funded key
should cover those next, using the same `rd_plus__mc_item` style and the per-pillar theme
language established here (Farming: humble → sprouting vines → fully overgrown across
Tiers I→III; Mining: practical iron → ore-encrusted → crystalline bedrock; Combat already
has its own bone/ash/reaper identity from the original design, just needs the same
render-quality pass).

## 13. Playtesting pass (first real client session)

The user's first hands-on playtest (client launched via `./gradlew runClient`) surfaced
five changes, all implemented and boot-verified:

**Defense in intervals of 5**: every armor piece's `GreenwardStat.DEFENSE` value (the
*only* defense that matters — vanilla armor points are hardcoded to zero across every
Greenward material, see `ModArmorMaterials`'s own class javadoc) was re-rounded to the
nearest multiple of 5. Every set's total held exactly or within 5 of its original value:

| Tier | Old (helm/chest/legs/boots) | New (helm/chest/legs/boots) | Total |
|---|---|---|---|
| Farming/Fishing Tier I | 15/39/31/15 | 15/40/30/15 | 100 (unchanged) |
| Mining Tier I | 25/49/41/25 | 25/50/40/25 | 140 (unchanged) |
| Combat Tier I | 16/48/40/16 | 15/50/40/15 | 120 (unchanged) |
| Tier II (all 4 pillars) | 33/88/66/33 | 35/90/65/35 | 225 (was 220) |
| Tier III (all 4 pillars) | 58/132/102/58 | 60/130/100/60 | 350 (unchanged) |

**Farming/Mining Fortune now shown per-piece, not just full-set**: Farming Fortune used
to be an invisible full-set-only bonus (`FarmingSetContributor`, checked "is every piece
worn," +12/+25/+60) — deleted that class entirely and folded its numbers into each
Harvester's/Cultivator's/Warden's piece's own `STATS` component instead (3/3/3/3=12,
6/7/6/6=25, 15/15/15/15=60 per tier), so the bonus now shows on every individual item's
tooltip via the same generic `EquipmentStatContributor` every other stat already goes
through — and a partial set now grants partial credit, where before it granted nothing
short of all 4 pieces. **Mining Fortune was added to Mining armor for the first time**
(previously only Mining *tools* — Prospector's Drill etc. — carried it; Mining armor had
none) using the identical per-tier numbers as Farming, for symmetry.

**A real stats screen**: `/greenward stats` used to dump a wall of chat text. It now
opens a genuine in-game book (vanilla's own book-reading GUI, via
`Player#openItemGui` — the same mechanism a lectern or written book uses, zero custom
Screen/networking code) listing every `GreenwardStat`'s current total plus its
per-source breakdown, paginated ~8 stats per page. The book is never actually placed in
the player's inventory — it's a transient `ItemStack` built fresh each time the command
runs, so it's always current.

**Voidstep Blade's cooldown removed entirely** — shipped with a 10s/200-tick cooldown
mirroring Hypixel SkyBlock's own Aspect of the End; the user asked for it gone after
trying it. Every right-click blinks now, gated only by click speed.

**Leviathan's Wear renamed to "Leviathan Hunter's ⟨piece⟩"** (Cap→Hood, Coat, Waders,
Fins) — display names and the Field Guide entry only; item ids (`leviathans_cap` etc.)
were left alone since renaming ids would also mean renaming every recipe/advancement/
texture file referencing them for a change that's purely cosmetic.

**Noted, not addressed**: worn armor still renders as vanilla netherite's own body-layer
texture in third person / on the player model — only the inventory *icons* have real art
(procedural or Retro Diffusion). A proper worn-armor texture pass (`armor/<set>_layer_1.png`
/`_layer_2.png`) is real, separate work the user explicitly deferred.

---

## 14. The Coin economy, Slayers, and Threat gating

A SkyBlock-vs-Greenward comparison pass led to three more user-requested systems, all
compile/boot-verified together.

### Threat gated behind the Heartwood

Threat (§ 8) now computes to a flat 0 — no mob scaling, no drop/XP bonus — until a
Heartwood has been placed **and** its new Threat toggle switched on inside the Heartwood
menu (a full-width button under the branch buttons, synced live via the menu's existing
`ContainerData` mechanism). Breaking the world's Heartwood resets the toggle back off.
Every other input to Threat's formula (skills, gear, Heartwood nodes, bosses defeated)
still accrues in the background regardless, so flipping it on later reflects real
progress immediately. `/greenward threat` reports "disabled" with a pointer to the
Heartwood when it's off.

### The Coin economy

A **virtual per-player balance** (`PlayerPurseData`, never a physical item — the user's
own explicit call, matching SkyBlock's own Purse) with an action-bar toast on every
change ("+42 Coins (balance: 1,337)"), checkable via `/greenward balance`.

**A real Shop screen** (`VillagerShopHandler`/`VillagerShopMenu`/`VillagerShopScreen`):
wear a **Coin Purse** (craftable, Leather+Gold+Emerald) in your **offhand**, then
sneak-right-click any villager to open a GUI — a Sell slot (drop an item, hit Sell, price
from `SellPriceTable` — ~50 common vanilla + Greenward items, extend as needed), a Repair
slot (drop a damaged item, hit Repair, 1 Coin per missing durability point, no profession
restriction), and a small Buy catalog (Waystone for 300, a spare Coin Purse for 50 —
extend as needed). This replaced an earlier "hold an item and sneak-click for an instant
result" gesture-based version — the user tried it and asked for something that actually
looks and feels like a shop instead, which this is a closer match for. The Purse-in-
offhand check is still the whole mechanism keeping this separate from Commissions'
identical sneak-right-click gesture — Commissions explicitly steps aside (`return PASS`)
whenever a Coin Purse is equipped. **Vanilla villager trades are completely untouched** —
this whole system lives alongside them, never inside them, honoring the same rule
Commissions already established (Design Program Update 8 § 8.6).

**Sinks**, since a faucet with nowhere to spend it isn't a real economy (the user's own
explicit note):

| Sink | How | Cost |
|---|---|---|
| **Repair** | Shop screen's Repair slot | 1 Coin per missing durability point |
| **Buy Waystone** | Shop screen's Buy catalog | 300 Coins flat |
| **Waystone travel** (`WaystoneBlock`, `WaystoneData`) | Place a Waystone (Obsidian+Amethyst+Ender Pearl), right-click to bind it under an auto-generated name, right-click any bound Waystone again for a chat-clickable travel list to every other one you know | `max(10, distance)` Coins same-dimension, flat 250 cross-dimension |
| Reward from Slayers | — | (a faucet, not a sink — see below) |

### Slayers

The deliberate "break from the grind" goal-to-chase SkyBlock's own Slayer quests provide,
which nothing in Greenward covered before this — The Marked (Update 4) is a passive
random bonus, not something a player chooses to trigger. Reuses The Marked's own five
vanilla mob types (fully Permanence Charter-safe, no new entities):

| Horn (craft cost) | Boss title | Guaranteed drop | Coins | Combat XP |
|---|---|---|---|---|
| Rotten Colossus Horn (3 Rotten Flesh + 1 Bone + 1 Seal) | Rotten Colossus (Zombie) | 4-8 Rotten Flesh | 150 | 50 |
| Bonebreaker Horn (3 Bone + 1 Arrow + 1 Seal) | Bonebreaker (Skeleton) | 4-8 Bone | 150 | 50 |
| Broodmother Horn (3 String + 1 Spider Eye + 1 Seal) | Broodmother (Spider) | 4-8 String | 150 | 50 |
| Fulminant Horn (4 Gunpowder + 1 Seal) | Fulminant (Creeper) | 4-8 Gunpowder | 150 | 50 |
| Voidcaller Horn (2 Ender Pearl + 1 Seal) | Voidcaller (Enderman) | 2-4 Ender Pearl | 250 | 75 |

Right-click a Horn to consume it and summon that boss a few blocks ahead (raycast the
same way Voidstep/Lava Fishing already do), buffed 8x health / 2x attack damage via a
transient attribute modifier (same technique as `ThreatMobHandler`, but a flat multiplier
here rather than Threat-scaled, so Slayers are a real goal even before Threat is ever
turned on). Rewards only pay out on `DamageSource.isDirect()` + a real `ServerPlayer` —
the same anti-automation check every kill-triggered system in this mod already uses.

Every Horn recipe needs a Seal, tying Slayers into the villager-reputation economy too —
you can't buy your way into Slayers with Coins alone.

---

## 15. Open items / known caveats for review

- **Not independently verified in this environment**: any actual gameplay feel (RCON
  can drive server-side block-entity state, brewing, and item-give commands, but not a
  real player wearing armor, swinging a weapon, casting a lava line, or opening a
  smithing table). Every number above is what's *registered*, confirmed via clean
  compile + full server boot with zero load errors — not confirmed via live play.
- **Bedrock Reaver** ships without the draft spec's vein-mining/auto-smelt flavor
  (stats-only capstone instead, later given Vein Blast/Stoneflow in Update 5 § 5.3,
  regated onto the Heartwood Stone branch's Reaver's Wrath node in Update 6).
- **Combat's cost discount** (~35% below the mirrored pattern) is a deliberate,
  reasoned compensation for having zero automation support — worth re-examining if a
  future pass ever adds any form of auto-combat.
- **Reaper's Core is dual-purpose** (Combat's own Tier III smithing addition *and* its
  God Potion contribution, now further overloaded as a 4th-ingredient host for Boss
  Essence in Update 10) — asymmetric with Farming/Mining/Fishing's separate
  smithing-catalyst vs. God-Potion-catalyst items. Deliberate simplification, not an
  oversight.
- **Heartwood branches are a flat 8-node chain**, not the source spec's branching
  "8-10 nodes across 5 depths" tree — no in-game UI was built for a real tree structure.
- **The Catch Log, Harvest Fair, and full Villager Commission economy** are all shipped
  in reduced scope (chat announcements / one-off substitutions) rather than the source
  spec's full persisted systems — see §§ 9-11 above for the specific substitutions made
  at each God Potion catalyst.
- **Scorched Leviathan Rod sacrifices the Leviathan Rod via a plain crafting recipe**, not a
  component-preserving smithing_transform like the Four Pillars' own ascension chain —
  any sockets/gems on that specific Leviathan Rod are lost. See § 12.
- **Scorched Leviathan Rod and Cinder Heart still have procedural placeholder art**, not
  Retro Diffusion output — those 2 of the 14-item art batch failed on the API's own end
  and the key ran out of balance before a retry. See § 12's art subsection.
- **§§ 1-4's per-pillar armor tables are stale** — they list a "defense
  boots/legs/chest/helm" line as if it were the piece's real vanilla armor points, but
  every Greenward `ArmorMaterial` has hardcoded zero vanilla defense (see
  `ModArmorMaterials`'s own class javadoc) — the actual mitigation is the custom Defense
  *stat*, whose real current numbers are § 13's table above. This predates this session
  and was never caught until writing § 13 — a real, pre-existing doc/code drift, not
  something introduced by today's rebalance. Worth a follow-up pass to rewrite §§ 1-4's
  armor call-out lines properly rather than patching them piecemeal.
- All armor/tool/stat numbers above were taken directly from the relevant `.java` files
  at time of writing each section, not from memory of the original design pass —
  cross-check against `ModArmorMaterials.java`/`ModArmor.java`/`ModTools.java`/
  `GreenwardStat.java`/etc. if anything here looks stale.
