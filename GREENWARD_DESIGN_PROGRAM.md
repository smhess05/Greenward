> **Status note (added mid-Update-3; updated through Update 10 — the full program):**
> All ten updates are implemented and shipped — see `GREENWARD_README.md`'s "Design
> Program Revision 3" sections for what was actually built, including every resolved
> ambiguity and invented/substituted content, clearly marked as such throughout. The
> headline resolutions: Update 3's Press/Deep Press module cap became dedicated per-axis
> slots (explicit user direction); Update 4's per-chunk machine placement cap was
> **removed entirely** post-ship, also at explicit user direction ("I want the peak of
> automation to be very powerful") — only the 24-per-world cap remains; Update 4/5's
> "Heartwood Mining branch" interim substitute gates (Deep Regrowth Module, Vein Blast,
> Stoneflow) were upgraded to the real Heartwood nodes once Update 6 shipped them; Update
> 8's villager-sourced talismans, Update 9's Harvest Fairs and full Catch Log, and Update
> 10's custom-boss architecture were deliberately left unbuilt or reduced in scope under
> real time constraints, each documented at its own section rather than silently skipped.
> This file is the original roadmap text, unedited below this note — it is the source of
> truth for *intent*; the README is the source of truth for *what shipped and how*.

---

# Greenward — Design Program (Revision 3)

**Target:** Minecraft 26.2 / Fabric Loader 0.19.3 / Fabric API 0.156.0+26.2
**Baseline:** `GREENWARD_PROGRESSION_SUMMARY.md`
**Supersedes:** `GREENWARD_SKYBLOCK_ALIGNMENT.md` (Revision 2)

Revision 2 was, honestly, Hypixel SkyBlock with a thesaurus applied to it. This revision keeps the progression *depth* that made SkyBlock worth studying and throws out the borrowed mechanics, names, and numbers. It is also built around a hard constraint that Revision 2 violated: **the world must survive the mod being removed.**

Read § 0 and § 1 before starting any Update. § 1 is not a style guide — it is a set of rules that will cause real damage if broken, and several Revision 2 features are cut outright because of it.

---

## 0. What Greenward is

Hypixel SkyBlock solves progression by building somewhere else to go — custom islands, custom mobs, custom dimensions. Greenward can't do that, because of § 1. So it does the opposite:

> **Greenward never adds places or things to the world. It changes what the world already there does.**

Your deepslate layer starts yielding Glimmer because you got good at mining. Your zombies get faster and hit harder because you got strong. Your villagers stop being a shop and become an employer. The Ender Dragon becomes a repeatable capstone instead of a one-time trophy.

That principle is what makes Greenward distinct from SkyBlock, and — usefully — it is the same principle that makes the world permanent. The constraint generates the identity.

**Practical consequence for every Update:** when a feature needs new content, the first question is always *"can this be an existing vanilla thing behaving differently?"* before *"what block do I add?"*

---

## 1. The Permanence Charter — binding on every Update

**The requirement:** at any point, the player can delete the Greenward mod, load the same save in vanilla Minecraft, and keep playing. Terrain, buildings, and vanilla items are untouched. Greenward's own items and blocks are expected to be lost — that is fine and understood.

### 1.1 What breaks a world, in severity order

| Removed thing | Result on vanilla load |
|---|---|
| **Custom dimension** | `level.dat` references a missing generator — world may fail to load |
| **Custom biome** | Every chunk's biome palette references a missing ID — corruption or failure |
| **Custom worldgen blocks** | Every unmined placement becomes air — permanent scarring across the whole world |
| **Custom structures** | Chunk structure references plus modded blocks — hollow voids |
| **Custom enchantments** | Item component may fail to parse rather than degrade |
| **Custom block replacing a vanilla one** | That block becomes air (e.g. a fertilized-farmland block would delete your farm) |
| **Custom blocks placed by the player** | Become air — acceptable, player-chosen, and recoverable |
| **Custom items** | Silently dropped from inventories — acceptable |
| **Custom entities** | Skipped on load — acceptable, but avoid |
| **Custom player data** | Dropped on next save — acceptable |

### 1.2 Binding rules

1. **No custom dimensions.** Ever.
2. **No custom biomes.** Ever.
3. **No worldgen additions of any kind** — no ore blocks, no structures, no feature injection. Rare materials come from *vanilla* blocks via modified drops (Update 5).
4. **No custom block may replace or shadow a vanilla block the player relies on.** Fertilized farmland must be **side-data keyed by `BlockPos`**, never a distinct block. The farmland stays vanilla farmland forever; the fertilization record is simply ignored when the mod is gone. Apply this pattern anywhere a vanilla block is being augmented.
5. **All Greenward entities are vanilla entity types with attribute modifiers and custom names.** The Marked already do this correctly — it is now law. Transient projectiles (Vein Blast) are the only exception, and they never persist.
6. **All attribute modifiers applied to world entities are transient.** Apply on `ServerEntityEvents.ENTITY_LOAD`, strip on `ENTITY_UNLOAD` and on server stop. Never rely on modifiers persisting in entity NBT.
7. **No custom enchantments.** Cut from the program.
8. **Ship `/greenward decommission`** — sweeps a configurable radius (default: all loaded chunks, with a `--world` flag for a full pass), drops all machine and Heartwood inventories as items, and converts every Greenward block to a sensible vanilla equivalent. Print a summary. This is the clean-exit path.

### 1.3 Standing acceptance criterion

**Every Update that touches the world adds this test, and it is not optional:**

- [ ] Copy the save. Remove the Greenward mod. Load the copy in vanilla Minecraft 26.2. Confirm: world loads without error, terrain is intact, no air pockets in explored chunks, no console errors on chunk load, and mobs behave normally.

If an Update cannot pass this, the feature is cut, not shipped with a caveat.

---

## 2. Doctrine

### 2.1 Machines are the floor. Players are the ceiling.

Every multiplicative stat — Fortune, Sea Creature Chance, Marked chance, Threat bonuses — applies **only to player-performed actions**. Machines produce a flat, unmultiplied trickle.

| Machines DO | Machines DO NOT |
|---|---|
| Produce base materials | Grant Skill XP |
| Feed Collection **quantity** | Satisfy a **Proof** (§ 4) |
| Run unattended forever | Apply any Fortune stat |
| Accept modules that change *what* they output | Produce rare drops or Marked drops |

### 2.2 Player-caused kills only

Any bonus that multiplies combat rewards — Threat drop/XP bonuses, Marked spawn chance, rare drops — requires a kill where `DamageSource.isDirect()` is true and the causing entity is a `ServerPlayer`. This is already how the Marked work. It is now the general rule, and it is what stops a fall-damage grinder from being the optimal way to farm Threat.

### 2.3 Compression follows Minecraft, not Hypixel

The ladder is **9 → 81 → 729 → 6,561**, all the way up, with pillar-specific names. No 160:1, no "Enchanted" tier, no parallel chain. This extends the ladder already in the mod rather than bolting a second one alongside it, which also means existing saves keep working.

### 2.4 Build order

```
1  Permanence retrofit + Stats & Item Identity
2  The Field Guide: Collections + Proofs
3  Material Economy: the 9/81/729/6561 ladder
4  Automation Doctrine  ← small, do early
5  Mining Depth: rare drops from vanilla ore
6  The Heartwood
7  Threat
8  Villagers & Seals
9  Farming & Fishing Depth
10 Endgame: vanilla bosses as capstones
```

**Critical path if only four ship:** 1, 2, 6, 7. Stats, the Field Guide, the Heartwood, and Threat carry most of the feel between them.

---

## Update 1 — Permanence Retrofit + Stats & Item Identity

**Goal:** Bring the existing mod into compliance with § 1, and add the stat layer everything else attaches to.

### 1.1 Permanence retrofit (do this part first)

- Audit every registered block. Any block that augments a vanilla block converts to `BlockPos`-keyed side-data. **Fertilized farmland is the known case** — verify whether it is currently a block; if so, this is a breaking change requiring an `UPDATING.md` note.
- Audit every registered entity. Confirm zero custom entity types.
- Implement `/greenward decommission`.
- Add the § 1.3 test to the CI/manual checklist.

### 1.2 Stats

Implement exactly these. The list is deliberately shorter than Revision 2's.

| Stat | Base | Notes |
|---|---|---|
| Health | 100 | Bonus max HP; 10 = one vanilla heart |
| Defense | 0 | Damage reduction |
| Strength | 0 | Damage multiplier |
| Crit Chance | 5 | % chance of a critical hit |
| Crit Damage | 50 | % bonus damage on crit |
| Speed | 100 | Movement speed |
| Mining Speed | 0 | Block-break rate |
| Mining Fortune | 0 | Extra block drops |
| Farming Fortune | 0 | Extra crop drops |
| Sea Creature Chance | 5 | |
| Fishing Speed | 0 | Reduces bite wait |

**Cut from Revision 2:** Magic Find (its job is Threat's, § 7), Ferocity, Pristine, Breaking Power, Magical Power, Intelligence/Mana.

### 1.3 Formulas

**Fortune:** every 100 points guarantees one additional drop; excess becomes the percentage chance of one more. 130 Mining Fortune = 2× guaranteed, 30% chance of 3×.

**Damage:** `(baseWeaponDamage + 5) × (1 + Strength/100) × (crit ? 1 + CritDamage/100 : 1)`

**Defense:** `damageTaken = incoming × (1 − Defense/(Defense + 100))`

### 1.4 Armor: replace vanilla armor points

Greenward armor carries **zero** vanilla armor and toughness attributes; all mitigation is the Defense stat. Convert existing sets at roughly `Defense = (defensePoints × 8) + (toughness × 15)`. Tier III lands near 320–380 Defense (≈ 76–79% reduction).

This is the decision flagged in Revision 2 § 1.3, now taken. It is what makes Greenward armor a different system rather than "diamond plus," and it is required for Threat's damage scaling to have a coherent curve.

### 1.5 Rarity & tooltips

Five tiers: `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`. Store as a `DataComponent`.

Tooltip layout — stats block, blank line, ability/set block, blank line, rarity footer in the rarity colour. Positive values green, negative red. This is most of the perceived polish and it is pure client-side rendering.

### 1.6 Implementation notes

- `GreenwardStat` enum, `StatProfile`, `StatComponent`, `PlayerStatManager`.
- Recompute on inventory change and on `SetBonusHandler`'s existing 20-tick cadence. Never per-tick.
- Sources register through a `StatContributor` interface so Updates 2, 6, and 8 attach without touching core.
- Action-bar HUD: `❤ 340/340   ❈ 312`.
- Avoid mixins. `ServerLivingEntityEvents`, `AttackEntityCallback`, `PlayerBlockBreakEvents` cover this.

### 1.7 Acceptance criteria

- [ ] § 1.3 permanence test passes.
- [ ] `/greenward decommission` converts all Greenward blocks and drops all inventories.
- [ ] `/greenward stats` prints the profile with labelled contributors.
- [ ] 100 Mining Fortune on diamond ore yields exactly 2, every time, over 50 trials.
- [ ] 150 Mining Fortune yields 3 at 50% ± 5 points over 200 trials.
- [ ] Greenward armor shows zero vanilla armor bar segments.

### 1.8 DO NOT

- Do not add stats beyond the table.
- Do not remove vanilla crit. Greenward Crit replaces the vanilla 1.5× only when the held item carries a Greenward stat component.
- Do not implement mana. It is cut from the program entirely.

---

## Update 2 — The Field Guide: Collections + Proofs

**Goal:** The progression meter, as a hybrid of quantity and demonstrated skill.

**Why the hybrid:** pure collections are a tally you can automate into. Pure achievements are a checklist with no long tail. Together they mean you cannot advance a pillar by pointing a machine at it *or* by a single clever stunt — you need both volume and competence.

### 2.1 Structure

Collections track a resource. Tiers I–X. Each tier needs a **quantity threshold**, and from Tier IV upward, also a **Proof** — a named, specific task.

Thresholds: `50, 250, 1000, 2500, 5000, 10000, 20000, 40000, 75000, 150000`.

**Machines contribute to quantity. Machines never satisfy a Proof.** That is the whole mechanism — the Proof is the manual gate, and it is what keeps § 2.1's doctrine load-bearing rather than aspirational.

### 2.2 Proofs — write them like this

Specific, achievable in one sitting once you're at the right stage, and flavored to the pillar.

| Collection | Tier | Proof | Requirement |
|---|---|---|---|
| Wheat | IV | **Full Yield** | Harvest a complete mature 9×9 in a single hoe swing |
| Wheat | VI | **Rainfed** | Harvest 500 wheat during rainfall |
| Wheat | VIII | **Fallow No More** | Have 256 fertilized farmland blocks active at once |
| Cobblestone | IV | **Down Deep** | Mine 1,000 blocks below Y −50 |
| Cobblestone | VI | **Single Breath** | Mine a 64-block vein without surfacing above Y 0 |
| Deepslate | VII | **Cold Iron** | Mine 500 deepslate with no torch placed within 16 blocks |
| Cod | IV | **Patient** | Land 3 sea creatures without leaving the water |
| Cod | VII | **Storm Catch** | Land a sea creature during a thunderstorm |
| Bone | IV | **Bare-Knuckle** | Kill 50 hostiles wearing no chestplate |
| Bone | VI | **Marked Hunter** | Kill 10 Marked in a single Minecraft day |
| Gunpowder | VII | **Held Nerve** | Kill 25 creepers in melee without taking creeper damage |

Track pending Proofs in the Field Guide UI so the player always knows what is outstanding.

### 2.3 Skills

Four skills matching the pillars: **Farming, Mining, Combat, Fishing**. Levels 0–50.

XP curve: table-driven, not formula-driven, so it can be tuned. Level 1 at 50 XP; level 10 at ~10k cumulative; level 25 at ~180k; level 50 at ~1.5M.

| Skill | Per level |
|---|---|
| Farming | +2 Farming Fortune, +2 Health |
| Mining | +1 Mining Speed, +1 Mining Fortune, +1 Defense |
| Combat | +0.5 Strength, +0.25 Crit Chance |
| Fishing | +0.3 Sea Creature Chance, +2 Health |

XP from player actions only. Machines grant zero.

### 2.4 Retrofit — gate the existing recipes

Every existing Greenward recipe moves behind a collection tier. Suggested: Harvester's set at Wheat IV, Cultivator's at Wheat VII + Potato V, Ascension Template at Diamond VI, Harvest Core at Wheat IX + Potato VIII + Carrot VII, Auto-Harvester at Wheat V, Auto-Miner at Cobblestone V, Auto-Fisher at Cod V, Prospector's at Cobblestone IV, Excavator's at Cobblestone VII + Deepslate V, Angler's at Cod III, Marrowguard at Bone IV, Ashwrought at Bone VII + Gunpowder V.

### 2.5 Implementation notes

- Persist via attached player component. Survives dimension change and respawn.
- Recipe unlocking: fire a custom advancement criterion on tier completion; the advancement's reward grants the recipe. No mixins, and vanilla's Recipe Book works correctly for free.
- The Field Guide is the UI. `FieldGuideContent.java` already exists — extend it into a live progression book rather than static documentation.
- Action-bar popup on XP gain (`+3 Farming XP`).

### 2.6 Acceptance criteria

- [ ] Hand-harvesting wheat raises Farming XP and Wheat quantity; an Auto-Harvester raises quantity only.
- [ ] Wheat IV cannot complete on quantity alone — the Full Yield Proof is required.
- [ ] A Proof completed by machine action does not register.
- [ ] Progress survives restart.
- [ ] § 1.3 permanence test passes.

### 2.7 DO NOT

- Do not gate vanilla recipes. Greenward recipes only.
- Do not make progress retroactive on upgrade. Everyone starts at zero; note it in `UPDATING.md`.
- Do not write Proofs that require luck rather than skill.

---

## Update 3 — Material Economy

**Goal:** Extend the existing compression ladder to four rungs on vanilla ratios, with pillar-specific naming.

### 3.1 The ladder

**9 → 81 → 729 → 6,561.** Each pillar names its own rungs. The first two rungs already exist and do not change — this is purely additive.

| Pillar | ×9 | ×81 | ×729 | ×6,561 |
|---|---|---|---|---|
| Farming (wheat) | Sheaf | Bale | **Rick** | **Mow** |
| Farming (potato) | Sack | Crate | **Pallet** | **Granary** |
| Farming (carrot) | Gilded Carrot | Radiant Carrot | **Sunburst Carrot** | **Solar Carrot** |
| Mining (cobble) | Cluster | Monolith | **Massif** | **Batholith** |
| Mining (deepslate) | Cluster | Monolith | **Massif** | **Batholith** |
| Fishing (cod) | School | Shoal | **Haul** | **Trove** |
| Combat (bone) | Bundle | Reliquary | **Charnel** | **Ossuary** |
| Combat (gunpowder) | Satchel | Cache | **Magazine** | **Arsenal** |

Rick, Mow, Massif, Batholith, Charnel, Ossuary are all real words from the relevant trades — hay storage, geology, and burial respectively. That is the naming register: *occupational, not fantasy.*

Rungs 3 and 4 feed Heartwood branches (Update 6), Tier III catalysts, and Update 10 endgame recipes.

### 3.2 Satchels

Once Fortune multiplies drops, inventory pressure dominates. A held item that vacuums a tagged category directly into itself.

| Satchel | Absorbs | Tiers (per item type) |
|---|---|---|
| Agronomy Satchel | `#greenward:farming_materials` | 256 / 1024 / 4096 |
| Lithic Satchel | `#greenward:mining_materials` | same |
| Tidal Satchel | `#greenward:fishing_materials` | same |
| Ossuary Satchel | `#greenward:combat_materials` | same |

Gate at the relevant collection VI / VIII / X.

### 3.3 Machine modules: the Press

| Module | Effect | Gate |
|---|---|---|
| **Press** | Machine output auto-compresses to rung 1 | Cobblestone V |
| **Deep Press** | Auto-compresses to rung 2 | Cobblestone X |

**Cap installed modules at 2 per machine**, forcing a real choice between throughput, storage, and compaction. Presses count against that cap.

### 3.4 Acceptance criteria

- [ ] 9 Ricks craft 1 Mow; 8 does not.
- [ ] Existing Sheaf and Bale ratios are unchanged and existing saves load cleanly.
- [ ] A machine with a Deep Press produces rung-2 items above threshold stock and raw below it.
- [ ] Installing a third module is refused with a clear message.

### 3.5 DO NOT

- Do not change rungs 1 and 2 — the fuel table and existing saves depend on them.
- Do not make compression reversible above rung 2.

---

## Update 4 — Automation Doctrine ⚠ small, do early

**Goal:** Bring the three machines under § 2.1 and close the God Potion bypass.

### 4.1 The four fixes

**① Remove `minecraft:ancient_debris` from `#greenward:regenerable_ores`.** With a tier-4 regen module against a tier-4 speed module this is an infinite Netherite tap. It trivialises the Deepstone Core, which trivialises the God Potion, and makes Nether exploration pointless. Netherite stays manual.

**② Split the regenerable tag by value.**

| Tag | Contents | Regrow delay | Requires |
|---|---|---|---|
| `#greenward:regenerable_common` | coal, copper, iron, redstone, lapis | current values | base Auto-Miner |
| `#greenward:regenerable_precious` | gold, diamond, emerald | **5× common at every tier** | **Deep Regrowth Module**, gated at Heartwood Mining branch 4 |

**③ Machine output ignores every player stat, and machines are capped.** Base drop only — no Fortune, no Looting, no Silk Touch. Plus: max 4 machines per chunk (`maxMachinesPerChunk`), max 24 per world (`maxMachinesPerWorld`). Enforced at placement with a clear refusal.

**④ Auto-Fisher loses the treasure pool.** The current build removes the open-water requirement from the treasure pool for players *and* machines, putting nautilus shells, enchanted books, and name tags on an infinite tap — and nautilus shells feed the God Potion. Fix: ungated treasure for **players only**; the Auto-Fisher rolls fish and junk plus a ~2% independent nautilus chance. Branch on the existing `THIS_ENTITY`/`FishingHook` predicate that already correctly gates sea creatures. Reuse it; do not write a second one.

### 4.2 Fuel: duration multipliers, not operation counters

`AutomationFuel.java` currently treats fuel as an operation budget. Change to a temporary speed *boost*, with machines running forever at base rate when unfueled. Fuel becomes an optimisation rather than a chore.

| Fuel | Effect | Duration |
|---|---|---|
| Coal / Charcoal | +5% | 30 min |
| Coal Block | +5% | 4 hr |
| Dried Kelp Block | +8% | 1 hr |
| Bale / Crate | +10% | 2 hr |
| Rick / Pallet | +15% | 6 hr |
| **Sunwheel** *(new)* | +25%, daylight only | permanent |
| Lava Bucket | +25% | 12 hr |

### 4.3 Effigies — combat automation

The current spec discounts Combat recipes ~35% for having no automation. Remove the asymmetry rather than preserve it.

An **Effigy** spawns nothing and kills nothing. It generates base mob drops on a timer, on the same frame and module system as the other machines.

| Effigy | Produces | Interval |
|---|---|---|
| Rotting Effigy | Rotten Flesh | 100t |
| Bonepile Effigy | Bone, Arrow | 100t |
| Webbed Effigy | String, Spider Eye | 120t |
| Volatile Effigy | Gunpowder | 140t |
| Void Effigy | Ender Pearl | 400t |

Recipe: 7× Iron Ingot + 1× the mob's rung-2 compressed drop (centre) + 1× Redstone Block.

**Once Effigies ship, restore Combat recipe costs to parity.** Note it in `UPDATING.md` — the discount was deliberate compensation and this is the condition for reversing it.

### 4.4 Acceptance criteria

- [ ] Ancient Debris does not regrow.
- [ ] A player with 200 Mining Fortune beside a running Auto-Miner sees no change in its output.
- [ ] A 5th machine in a chunk is refused, naming the cap.
- [ ] An Auto-Fisher produces zero enchanted books over 500 operations; a player fishing the same water produces them at the vanilla rate.
- [ ] An unfueled machine still runs, at base interval.
- [ ] A Void Effigy produces Ender Pearls and never spawns an Enderman entity.

### 4.5 DO NOT

- Do not add an auto-killer. The Effigy generates loot from nothing — that is deliberate, and it is what keeps Combat un-automatable in the ways that matter.
- Do not let Effigies produce Marked drops.
- Do not replace the existing sea-creature structural check. Extend it.

---

## Update 5 — Mining Depth

**Goal:** Give mining a player-facing expression, using **zero worldgen additions**.

**Why mining is currently the weak pillar:** Farming has harvest radius. Fishing has sea creatures. Combat has the Marked. Mining's only expression is a mining-speed number moving 9.2 → 10.5 across three tiers, which no player can feel — while an uncapped machine does that job better. It is boring *because* it is overpowered.

### 5.1 Rare drops from vanilla ore — the whole mechanism

No new ore blocks. New materials drop from **vanilla blocks**, gated by tool tier and skill level. Every drop table entry is a datapack-style loot modification; remove the mod and the vanilla drop tables return untouched.

| Material | Drops from | Requires | Rate |
|---|---|---|---|
| **Glimmer** | Deepslate, Cobbled Deepslate below Y −40 | Prospector's Drill+, Mining 15 | 1.5% |
| **Titanshard** | Iron / Copper / Gold ore below Y −40 | Excavator's Pick+, Mining 25 | 1% |
| **Amber** | Any ore in a 5-block cluster or larger | Excavator's Pick+, Mining 30 | 0.6% |
| **Jade** | Emerald ore, any Y | Excavator's Pick+, Mining 30 | 4% |
| **Sapphire** | Lapis ore below Y −40 | Excavator's Pick+, Mining 35 | 0.8% |
| **Ruby** | Redstone ore below Y −55 | Bedrock Reaver, Mining 40 | 0.5% |

Rates are affected by Mining Fortune, so gear matters. Machine-broken blocks yield **none of these** — § 2.1.

This replaces Revision 2's Breaking Power entirely. Breaking Power was verbatim Hypixel, and with no custom blocks it has nothing to gate. Tool tier plus skill level does the same job with one fewer stat.

### 5.2 Gem cutting and sockets

Rough gems cut at a crafting table: **Rough → Fine → Flawless → Perfect**, 5:1 each rung. Tier II and Tier III armor and tools carry 1–3 sockets. A cut gem slots in and grants its stat scaled by cut.

| Gem | Grants |
|---|---|
| Amber | Mining Speed |
| Jade | Mining Fortune |
| Sapphire | Fishing Speed + Sea Creature Chance |
| Ruby | Strength |

Sockets are the best late-game stat sink available and cost only small icon textures.

### 5.3 Vein Blast

A right-click pickaxe ability, unlocked on the Heartwood's Mining branch. Throw the pickaxe; on impact it detonates and mines every ore within radius 3. Cooldown 120s.

**This is the correct implementation of the vein-mining flavour the Bedrock Reaver shipped without.** As a thrown projectile resolving a sphere of positions on impact, it needs no block-break mixin and no hook into the break pipeline — which was the original blocker. Second ability, **Stoneflow**: +250% Mining Speed for 15s, 120s cooldown.

### 5.4 Retire the mining-speed ladder

| Tool | Mining Speed | Mining Fortune |
|---|---|---|
| Prospector's Drill | +120 | +10 |
| Excavator's Pick | +260 | +30 |
| Bedrock Reaver | +500 | +60 |

Vanilla tool material speed stays modest; the Greenward stat does the real work.

### 5.5 Acceptance criteria

- [ ] Glimmer drops from deepslate below Y −40 with a Prospector's Drill at Mining 15, and not at Mining 14.
- [ ] An Auto-Miner produces zero Glimmer over 1,000 operations.
- [ ] Vein Blast mines every ore in radius 3 on impact.
- [ ] A Flawless Jade in Excavator's Plate raises Mining Fortune by the documented amount.
- [ ] **§ 1.3 permanence test — with particular attention: no air pockets anywhere, since nothing was ever added to worldgen.**

### 5.6 DO NOT

- Do not add ore blocks, structures, or any worldgen feature. This is the Update most likely to be tempted; do not.
- Do not modify vanilla loot tables destructively — use additive loot modifiers only, so removal is clean.
- Do not grant rare drops from Effigies, machines, or crafting.

---

## Update 6 — The Heartwood

**Goal:** One placed block that holds all long-term progression: four perk branches and the talisman sockets.

**Why one object rather than a menu:** it gives progression a physical location in your world, it is a thing you protect (which matters once Threat exists), and it spans all four pillars rather than being a mining-only tree.

### 6.1 The block

- One per world. Crafted mid-game; gate at Mining VI + Wheat VI + Cod VI + Bone VI, so all four pillars must be underway.
- **Visual:** a static model — a thick trunk section with exposed sap channels. **Animation via animated textures** (`.png` frame strip plus `.png.mcmeta`), giving pulsing sap-veins and a slow glow cycle at essentially zero code cost. Add ambient particles from a block entity tick. A rotating suspended element via a block entity renderer is optional polish, not required to ship.
- Emits light level 7. Placement matters — build it somewhere you'll defend.
- Right-click opens the Heartwood screen.

### 6.2 The four branches — no tokens, no powder

Each branch is fed **directly by its own pillar's materials.** No intermediate currency. This is the deliberate departure from a mining-only tokens-and-powder tree.

| Branch | Fed by | Sample nodes |
|---|---|---|
| **Root** (Farming) | Ricks, Mows, Radiant/Sunburst Carrots | Farming Fortune I–40, hoe radius, Blight resistance, faster crop growth in a radius |
| **Stone** (Mining) | Massifs, Glimmer, Titanshard | Mining Speed I–50, Mining Fortune I–40, Vein Blast, Stoneflow, Deep Regrowth Module unlock |
| **Tide** (Fishing) | Hauls, Troves, Nautilus Shells | Sea Creature Chance, Fishing Speed, Twin Bite, underwater vision |
| **Ash** (Combat) | Charnels, Magazines, Marked drops | Strength, Crit Chance, Marked spawn rate, Grim Resolve |

Each branch has 8–10 nodes across 5 depths. A node unlocks by paying its material cost outright — no token gate, no second currency.

**Respec:** refunds 75% of materials spent on a branch. Costs Seals (Update 8). Free until Seals exist.

### 6.3 Talismans — the anti-grind version

The diagnosis of what makes Hypixel's talismans tedious: the Magical Power meter, which makes you carry twenty accessories you don't care about to feed a number. **Greenward has no Magical Power.** Talismans grant their stated effect and nothing else.

- **~20 total.** Each from one memorable source — a Proof, a rare drop, a villager, a boss.
- **One upgrade step**, not four: `Talisman → Sigil`, infused at the Heartwood with pillar materials. Roughly doubles the effect.
- **Sockets in the Heartwood**, starting at 4, growing to 12 via branch nodes. Socketed talismans apply to the player anywhere in the world.
- Duplicate families do not stack — the higher tier applies.

Twelve slots against twenty talismans means every slot is a real choice, which is the point.

| Talisman | Source | Effect |
|---|---|---|
| Sheaf-Token | Wheat IV Proof | +8 Farming Fortune |
| Cutter's Charm | Cobblestone IV Proof | +12 Mining Speed |
| Angler's Knot | Cod IV Proof | +3 Sea Creature Chance |
| Knucklebone | Bone IV Proof | +8 Strength |
| Wraith's Eye | Wraith drop, 4% | +4 Crit Chance |
| Deepglass Lens | Sapphire, cut Flawless | +15 Fishing Speed |
| Miller's Favour | Farmer villager, high prosperity | +10% crop growth speed nearby |
| Quarryman's Nail | Mason villager | +6 Mining Fortune |
| *(12 more across Proofs, gems, villagers, and Update 10 bosses)* | | |

### 6.4 Acceptance criteria

- [ ] Heartwood animates without a block entity renderer.
- [ ] A Stone branch node unlocks by paying materials directly, with no intermediate currency.
- [ ] Respec refunds 75%.
- [ ] Socketed talismans apply in the Nether and End, not only near the block.
- [ ] Breaking the Heartwood drops all socketed talismans.
- [ ] `/greenward decommission` converts the Heartwood and drops its contents.

### 6.5 DO NOT

- Do not implement Magical Power, or any meter fed by accessory count or rarity.
- Do not exceed 12 sockets or 20 talismans.
- Do not require the player to stand near the Heartwood for talismans to work.
- Do not build a growing/multi-stage block model. One model, animated textures.

---

## Update 7 — Threat

**Goal:** World difficulty that scales with your progression, so vanilla mobs stay relevant into the endgame — using only vanilla entities.

**Why this replaces the Slayer system entirely:** once you have 380 Defense and +500 Strength, vanilla hostiles are furniture. Hypixel solves that with custom mobs on custom islands. Greenward cannot and should not. Scaling the mobs already in your world does the same job, costs a fraction of the work, adds nothing to worldgen, and is completely reversible.

### 7.1 The value

**Per-world**, 0–100. Derived from:

| Source | Contribution |
|---|---|
| Sum of all four skill levels | up to 40 |
| Highest gear tier equipped, per pillar | up to 20 |
| Heartwood branch nodes unlocked | up to 25 |
| Vanilla bosses defeated (Update 10) | up to 15 |

Because Threat is derived from your own progression, it cannot outrun you by construction. Display it on the Heartwood screen with a plain-language band: *Settled → Restless → Hostile → Savage → Merciless.*

### 7.2 What it scales

Applies to **all hostile mobs regardless of origin** — natural spawns, spawner mobs, raid mobs, spawn eggs.

| Property | At Threat 100 | Curve |
|---|---|---|
| Max health | ×3.0 | linear |
| Attack damage | ×1.8 | **linear, and capped** — see below |
| Movement speed | ×1.35 | linear |
| Drops | ×2.5 | linear, **player-caused kills only** |
| Mob XP | ×3.0 | linear, **player-caused kills only** |

**Damage scales more slowly than health, deliberately.** With Update 1.4 removing vanilla armor points, a player mid-progression — high skill levels but not yet in the matching armor — is the one exposed case. Health and speed scaling make fights longer and more dangerous without making them lethal by surprise. Config `threatDamageScaling` (default `true`) lets it be switched off entirely.

**Drop and XP bonuses require a player-caused kill** per § 2.2. Without this, a fall-damage grinder becomes the optimal way to farm Threat's rewards, which would invert the entire doctrine.

### 7.3 Behavior thresholds

Beyond raw stats, unlock behavior at bands — this is what makes Threat *feel* like something rather than a number:

| Band | Change |
|---|---|
| Restless (25+) | Zombies occasionally break turtle eggs and doors on Normal |
| Hostile (50+) | Skeletons strafe more aggressively; creepers have a shorter fuse |
| Savage (75+) | Hostiles have a chance to spawn with a Marked-style modifier; spiders climb toward the player more persistently |
| Merciless (90+) | Small chance a hostile spawns as a **Warden of the Pillar** — reserved hook for Update 10 |

### 7.4 Permanence implementation — read carefully

This is the Update most exposed to § 1. Attribute modifiers **do** persist in entity NBT, so they must be handled transiently:

- Apply on `ServerEntityEvents.ENTITY_LOAD` using modifiers under the `greenward` namespace.
- Strip on `ENTITY_UNLOAD` and on `ServerLifecycleEvents.SERVER_STOPPING`.
- `/greenward decommission` sweeps loaded chunks and strips modifiers from every entity.
- Behavior changes are applied via goal injection at load and are never serialised.

**Do not assume this is safe — test it.** The § 1.3 test for this Update specifically requires loading a save where mobs were mid-scaling and confirming no NBT residue and no console errors.

### 7.5 Acceptance criteria

- [ ] Threat is visible on the Heartwood screen and recomputes when a skill levels.
- [ ] A spawner-spawned zombie carries the same scaling as a natural one.
- [ ] A mob killed by fall damage yields base drops and base XP; the same mob killed in melee yields the Threat multiplier.
- [ ] `threatDamageScaling: false` removes damage scaling and leaves health and speed intact.
- [ ] **§ 1.3 test with mobs loaded and scaled** — vanilla load produces zero errors and mobs have vanilla stats.

### 7.6 DO NOT

- Do not add custom entity types.
- Do not scale passive mobs, villagers, or golems.
- Do not persist any modifier to disk.
- Do not let Threat exceed 100 or scale from anything the player cannot see on the Heartwood screen.

---

## Update 8 — Villagers & Seals

**Goal:** Make villagers the economy, and give them a currency of their own.

**Why:** this replaces Hypixel's Bazaar, NPC merchants, and Commission board in one move, with something that only works in a survival world. Vanilla professions already map cleanly onto all four pillars, which is a gift.

### 8.1 Commissions

A new villager interaction, distinct from trading. A villager posts a **Commission** — a request with a material cost and a Seal payout. Accept it, deliver, get paid.

| Profession | Pillar | Sample commission |
|---|---|---|
| Farmer | Farming | *Bring 3 Ricks of wheat* |
| Mason / Toolsmith | Mining | *Bring 64 Glimmer* |
| Fisherman | Fishing | *Bring 2 Hauls of cod* |
| Weaponsmith / Butcher | Combat | *Bring 1 Charnel of bone* |
| Librarian | Any | *Complete any outstanding Proof* |

Each villager holds one Commission at a time, refreshing on the vanilla restock cycle. This means **commissions scale with your village**, not with a timer you wait out.

### 8.2 Prosperity

A village's **Prosperity** is derived from villager count, average profession level, bed count, and whether it has been raided recently. Higher Prosperity means larger commissions and better payouts, and unlocks the villager-sourced talismans (§ 6.3).

This creates the loop: build a village → it pays better → Threat makes it worth defending → raids and scaled mobs actually threaten it. That loop does not exist anywhere in Hypixel, and it is entirely built from vanilla systems.

### 8.3 Seals

The currency. A simple item, lost harmlessly on uninstall.

**Spent on:** Heartwood respec, Tempering (§ 8.4), talisman infusion, commission rerolls, machine module purchases from villagers.

**Never earned from:** machines, Effigies, or selling items. Commissions only. That keeps Seals tied to player activity and prevents an AFK income.

### 8.4 Tempering

A **Tempering Station** — placed adjacent to a Toolsmith, Weaponsmith, or Armorer villager — applies a stat modifier to gear, scaled by rarity. Costs Seals. Special temperings require materials dropped by Update 10 bosses.

| Tempering | Grants | Applies to |
|---|---|---|
| Keen | Strength, Crit Damage | Weapons |
| Sturdy | Health, Defense | Armor |
| Bountiful | Farming Fortune | Hoes, Armor |
| Deep | Mining Fortune, Mining Speed | Pickaxes |
| Briny | Sea Creature Chance, Fishing Speed | Rods, Armor |
| Swift | Speed, Crit Chance | Armor |
| Grim | Strength, but −10% Health | Weapons |

### 8.5 Acceptance criteria

- [ ] A Farmer villager posts a commission, accepts delivery, and pays Seals.
- [ ] Commission size scales with village Prosperity.
- [ ] Seals cannot be obtained from any machine or by selling.
- [ ] A raided village's Prosperity drops and recovers as it is rebuilt.
- [ ] Tempering requires an adjacent villager of the correct profession.
- [ ] § 1.3 test — villagers behave normally in vanilla, with no residual data.

### 8.6 DO NOT

- Do not modify vanilla villager trades. Commissions are a parallel system.
- Do not build an auction house or player market.
- Do not let a single villager be farmed for repeat commissions — one at a time, restock-gated.

---

## Update 9 — Farming & Fishing Depth

**Goal:** Bring the two remaining pillars up to the depth Mining and Combat now have.

### 9.1 Farming

- **Farming Fortune replaces the flat bonus-drop chance.** The current 10%/20% set bonuses become +25 / +60 Farming Fortune through the Update 1 formula.
- **Harvest Fairs.** A villager-run recurring event: a Farmer villager announces a crop and a window; you deliver as much as you can; you are scored against the village's own expectation, which scales with Prosperity. Rewards are Fortune-granting gear and Seals. This ties farming's skill expression to Update 8 rather than to a leaderboard, which is the singleplayer-appropriate shape.
- **Blight.** Over-fertilized farmland occasionally develops Blight — a **side-data condition**, never a block (§ 1.2 rule 4) — which suppresses Fortune on affected tiles until cleared with a hoe or bone meal. This prevents pure-AFK farming from being optimal, and it degrades to nothing on uninstall because the farmland was always vanilla farmland.
- Hoe radius stays capped at 4 (9×9), exclusive to full Warden's Garb.

### 9.2 Fishing

- **Fishing Speed and Sea Creature Chance become real stats.** Retire the hardcoded rod-tier lookup table; the existing chance values become stat grants on the rods, computed through the stat layer.
- **Twin Bite:** a chance to land two sea creatures from one catch. Granted by Tidal Wear and Leviathan's Wear set bonuses and the Tide branch.
- **The Catch Log.** Notable catches — rare named fish, graded Modest / Fine / Superb / Legendary — recorded in the Field Guide. Milestone rewards grant stats and talismans. Cheap (item icons plus a UI page) and one of the stickiest possible systems.
- Add two Catch Log entries above Abyssal Warden in the existing sea creature roster.

### 9.3 Acceptance criteria

- [ ] 130 Farming Fortune yields 2 wheat guaranteed with a 30% chance of 3.
- [ ] Blight appears only on player-tended farmland above a fertilization threshold, never near an Auto-Harvester.
- [ ] Blighted farmland loads as ordinary vanilla farmland with the mod removed.
- [ ] Twin Bite produces two distinct sea creature entities at the documented rate.
- [ ] The Auto-Fisher produces zero Catch Log entries.

### 9.4 DO NOT

- Do not let the Auto-Harvester trigger Harvest Fairs, Blight, or Farming Fortune.
- Do not implement Blight as a block.

---

## Update 10 — Endgame: Vanilla Bosses as Capstones

**Goal:** Repeatable capstone encounters, built from vanilla entities, plus the architecture for custom bosses later.

### 10.1 The Ender Dragon and Wither, rescaled

Both are already respawnable in vanilla — that makes them natural repeatable capstones with zero custom entities.

- Both scale with Threat, on a steeper curve than ordinary hostiles (health ×6 at Threat 100).
- Drop tables gain Greenward materials at high Threat: Tempering stones, the final talismans, and the God Potion catalyst components.
- The Dragon at Threat 90+ gains an additional phase. Implement via goal injection, never persisted.

### 10.2 Architecture for custom bosses — build the hooks, not the bosses

Custom bosses are deferred to a later update, but the surrounding systems must be built to accept them. Reserve:

- **A summoning mechanism.** A placed altar consuming pillar materials, or an item consumed on use. Do not build it yet; leave the interface.
- **The Warden of the Pillar hook** in Threat's Merciless band (§ 7.3) — a rare elite spawn slot, currently unfilled.
- **Boss construction pattern, now fixed as law:** a vanilla entity type, with attribute modifiers, a custom name, equipped decorative armor (Greenward armor items with distinct textures), and an injected goal set. No custom entity types, ever. This is both a permanence requirement and, conveniently, far less work.
- **Drop table slots** in Tempering and the talisman list marked as boss-sourced and currently unreachable.

Four bosses eventually, one per pillar, thematically tied to the Marked they outrank.

### 10.3 God Potion rework

The current recipe is fully automatable. Update 4 partly closes this; close it fully. Each catalyst component gains one non-automatable ingredient:

| Component | Add |
|---|---|
| Golden Harvest | 1× Harvest Fair award, Superb or better |
| Deepstone Core | 1× Perfect gem, any |
| Abyssal Pearl | 1× Catch Log entry, Superb or better |
| Reaper's Core | 1× Dragon or Wither drop at Threat 75+ |

Extend duration to 60 minutes; add the Update 1 stats (+100 Strength, +150 Defense) to the effect list.

### 10.4 DO NOT

- Do not build the custom bosses in this Update. Build the hooks and stop.
- Do not add a custom entity type for any boss, now or later.
- Do not make the Dragon unkillable at high Threat — verify the DPS curve against Update 1's damage formula before shipping.

---

## Appendix A — What changed from Revision 2, and why

| Revision 2 | Now | Reason |
|---|---|---|
| Deepstrata ores, Geode structures | Rare drops from vanilla ore | § 1 — worldgen additions scar the world permanently |
| Breaking Power | Tool tier + skill level | Verbatim Hypixel; also had nothing left to gate |
| 160:1 "Attuned" chain | 9/81/729/6561, pillar-named rungs | Vanilla's own logic; extends the existing ladder |
| The Deepcore (tokens + powder, mining-only) | The Heartwood (four branches, direct material cost) | Was the Heart of the Mountain with names changed |
| Magical Power, Talisman→Ring→Artifact→Relic | 20 talismans, one upgrade step, 12 sockets | MP is precisely the mechanic that makes talismans tedious |
| Five Slayer branches with custom bosses | Threat + rescaled vanilla bosses | Most derivative system in the document; also the most expensive |
| Magic Find, Ferocity, Pristine, Mana | Cut | Verbatim Hypixel; Threat covers Magic Find's job |
| Coins, Trade Post block | Seals, villager Commissions | Villagers do the job better and only work in a survival world |
| Contests, Pests | Harvest Fairs, Blight | Renamed *and* re-mechanised — Blight is a condition, not a mob |
| Trophy Fish, Double Hook, Telekinesis, Compactor, Solar Panel, Maniac, reforge names | Catch Log, Twin Bite, cut, Press, Sunwheel, cut, Tempering | Verbatim names |
| Custom enchantments | Cut | § 1 — components may fail to parse rather than degrade |
| Delve Board | Villager Commissions | Redundant once villagers issue work |

## Appendix B — Files this program touches

| File | Updates |
|---|---|
| `GreenwardConfig.java` | all |
| `ModArmorMaterials.java` | 1, 5 |
| `ModTools.java` | 1, 5 |
| `SetBonusHandler.java` | 1, 9 — becomes the stat recompute host |
| `AutomationFuel.java` | 4 — rewritten from op-counters to duration multipliers |
| `FieldGuideContent.java` | 2, 9 — becomes the live progression UI, not static docs |
| `ModCreativeTab.java` | no change — registry-filtered tab already handles new items |
| `GREENWARD_README.md` | all |
| `UPDATING.md` | 1, 2, 3, 4 — fertilized farmland change, collection reset, Combat cost restoration |

## Appendix C — Open items

1. **Fertilized farmland** — verify whether it is currently a distinct block. If so, converting it to side-data is a breaking change and needs an `UPDATING.md` migration note. This is the highest-priority permanence audit item.
2. **Threat modifier residue** — § 7.4's approach is sound in principle but must be verified empirically against a real vanilla load, not assumed.
3. **Talisman list** — 8 of 20 are specified. The remaining 12 should be filled in as Proofs, gems, villagers, and bosses are finalised.
4. **Harvest Fair scoring curve** — needs tuning against Prosperity once Update 8 is live.