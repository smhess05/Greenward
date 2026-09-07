# Adding a Collection, a Proof, or a Tier-Gated Recipe

A checklist for extending the Design Program Update 2 progression system (Collections +
Skills + Proofs). Written immediately after building the system, specifically so nothing
gets missed once the details aren't fresh anymore. Read `GREENWARD_README.md`'s "Design
Program Revision 3 — Update 2" section first if you haven't touched this system before —
this file is the checklist, that section is the explanation of *why* it's shaped this way.

---

## Checklist: adding a new Collection

1. **Add the enum entry in `GreenwardCollection.java`**: id string, display name, the
   backing vanilla `Item`, and the `GreenwardSkill` it feeds XP into. Nothing else in the
   enum needs touching — tiers, thresholds, and the Tier IV+ Proof rule are shared by
   every collection automatically.

2. **Decide what "one unit" means** — an actual item obtained, or a flat per-action
   approximation:
   - If the action already computes a real yield count (Farming's `passes`, Mining's
     `copies` from Fortune), use that — it's the more honest number.
   - If it doesn't (a mob's own loot-table roll, e.g. Bone/Gunpowder), a flat "+1 per
     qualifying action" is an accepted, documented approximation. Say so in a comment at
     the call site — don't let it read as "this is the exact count."

3. **Find the right hook — and confirm it is genuinely player-only, not just
   config-gated.** Every existing hook was chosen because the *event itself* cannot fire
   from machine-driven code, not because a flag happens to be checked nearby:

   | Action | Hook | Why it's machine-proof |
   |---|---|---|
   | Crop harvest | `HarvestLogic.harvestAndReplant` (the player-facing overload — **not** `harvestIntoStorage`, which is the Auto-Harvester's own separate method) | Two structurally separate methods; the machine path never calls the player one |
   | Block mining | `GreenwardFortuneHandler.onAfterBreak` (`PlayerBlockBreakEvents.AFTER`) | This event's signature requires a real `Player`; the Auto-Miner removes blocks directly via `Block.dropResources`/`level.removeBlock`, never through this event |
   | Fishing catch | `SeaCreatureHandler.onLootDrops` (`LootTableEvents.MODIFY_DROPS`, filtered to `LootContextParams.THIS_ENTITY instanceof FishingHook`) | A real catch is the only caller that sets `THIS_ENTITY` to the hook entity — confirmed against `FishingHook.retrieve()`'s decompiled source, not assumed |
   | Mob kill | `GreenwardCombatHandler.onAfterDeath` (`ServerLivingEntityEvents.AFTER_DEATH`, gated on `DamageSource.isDirect()` + `getEntity() instanceof ServerPlayer`) | A projectile kill's direct entity ≠ causing entity (fails `isDirect()`); a trap/fall/lava kill has no player entity at all |

   If none of these fit and you need a genuinely new hook, verify the "can a machine
   reach this?" question by reading the event's actual firing site or interface
   signature before trusting it — not by assuming a config flag is enough. A flag can be
   flipped or bypassed; a structurally-impossible code path can't.

4. **Call `PlayerProgress.addCollection(player, YOUR_COLLECTION, amount, true)`** at that
   hook. The `true` (playerAction) is what makes this a real, XP-and-tier-triggering
   action rather than silent bookkeeping — passing `false` is reserved for future
   machine-visible quantity-only tracking and must never grant XP or complete a Proof.
   `addCollection(..., true)` **automatically**:
   - Grants `amount` XP to `collection.skill()`.
   - Checks whether any tier newly became fully complete (quantity *and* Proof, if that
     tier has one) and fires `GreenwardTierCriterion.trigger(...)` if so.

   **⚠ Do not also grant that same skill's XP anywhere else nearby.** This bit a real
   implementation: `GreenwardCombatHandler` originally had a flat `addSkillXp(player,
   COMBAT, 1)` for *any* hostile kill, sitting right next to `addCollection(...,
   GreenwardCollection.BONE, ..., true)` for skeleton kills specifically — which
   *itself* grants Combat XP internally. Result: skeleton and creeper kills silently
   granted double Combat XP versus a zombie or spider kill, and nobody would have
   noticed without specifically doing the arithmetic. The fix was to delete the flat
   grant entirely — every other skill *only* grants XP from its own collection-relevant
   actions (Mining XP never fires for andesite, only cobblestone/deepslate/diamond), so
   there should never be a "flat XP for any pillar-adjacent action" fallback sitting
   alongside a collection call. If a skill needs XP from an action that *doesn't* map to
   any collection, that's a deliberate design call to make explicitly, not a leftover
   from before the collection existed.

5. **If the collection needs day-scoped or streak tracking anywhere**, reuse
   `PlayerProgress.incrementDayCounter` / `incrementStreak` / `incrementLifetimeCounter`
   rather than inventing a fifth pattern. See the Proof checklist below for which one to
   reach for.

6. **Compile, then boot-test.** RCON cannot simulate a player harvesting/mining/fishing/
   fighting — there is no way to headlessly verify a collection actually increments.
   Confirm the server boots clean (registration, codec, and advancement-file errors all
   surface at boot) and say plainly in any summary that live verification needs a real
   client + player, the same disclosure every previous update in this project makes.

7. **Update `PROGRESSION_SUMMARY.md`** if it's meant to stay a living reference — it is
   not automatically regenerated and will drift silently otherwise.

---

## Checklist: adding a new Proof

Reuse one of the five existing patterns. Do not invent a sixth without a specific reason
— the whole point of keeping to a small set is that each one is already implemented,
tested, and understood.

| Pattern | Use when | Storage |
|---|---|---|
| **Instant** | Completes the moment one qualifying action happens | None — call `PlayerProgress.completeProof` directly |
| **Lifetime cumulative** | A gated counter that should never reset (e.g. "mine N blocks below Y −50") | Persisted (`PlayerProgressData.lifetimeProofCounters`) — survives restart |
| **Day-scoped** | "N of X in a single Minecraft day" | In-memory only (`PlayerProgress`'s `DAY_COUNTERS`/`DAY_ANCHORS`) — a restart mid-day loses same-day progress, an accepted inconvenience |
| **Streak** | Breaks on a specific condition, not a day boundary (e.g. "without surfacing," "without taking damage") | In-memory only (`STREAK_COUNTERS`) — same accepted-loss reasoning |
| **Stockpile** | "Hold N of an item / have N of a world condition active at once" | None — a live threshold check, run every 20 ticks in `PlayerProgress.checkStockpileProofs`, called from `SetBonusHandler`'s existing cadence |

Steps:

1. Add the enum entry to `GreenwardProof.java`: which `GreenwardCollection`, which tier
   (must be ≥ IV — Tiers I–III never have a Proof), a short title, and a one-line
   requirement description. If you're inventing this proof (the spec didn't name it),
   say so in a trailing comment, matching the existing invented entries — never let an
   invented proof look like it came from the design document.
2. Wire the tracking call at the natural trigger point, using the table above.
3. For a streak, make sure you also wire the **reset** condition somewhere real — a
   streak with no way to break is not a Proof, it's a delayed Instant. (`SINGLE_BREATH`
   resets when the player's Y exceeds 0 at mine-time; `HELD_NERVE` resets on an
   `AFTER_DAMAGE` event specifically from a creeper; `PATIENT` resets on a periodic tick
   check of `player.isInWater()`.)
4. Remember Proofs are checked at mine/harvest/catch/kill **time**, not continuously —
   e.g. depth and "above Y0" checks look at the player's position at the moment of the
   qualifying action, not via a dedicated per-tick watcher. This is a deliberate,
   documented simplification everywhere it's used, not an oversight — but if a new
   proof's condition is something that can silently change *between* actions in a way
   that matters (not just "did you cross Y0," which self-corrects on the next action),
   think about whether that simplification still holds before copying the pattern.

---

## Checklist: gating a recipe behind a collection tier

1. Decide the exact `(collection, tier)` requirement(s) — check the tier actually has
   (or will have) the Proof it needs if it's ≥ IV; a tier's `isTierComplete` check will
   never return true without one.
2. Rewrite that item's advancement JSON (`data/greenward/advancement/recipes/<item>.json`)
   to use `"trigger": "greenward:tier_reached"` with `"conditions": {"collection":
   "<id>", "tier": <n>}`, replacing whatever trigger it used before (usually
   `minecraft:inventory_changed`). For multiple required tiers, add one criterion per
   requirement and put **all their names in one inner array** under `"requirements"` —
   `[["wheat_tier_7", "potato_tier_5"]]` means AND, not OR. (Multiple *inner* arrays
   would mean OR-of-groups, which is almost never what a multi-collection gate wants.)
3. No Java changes needed for this step alone — `GreenwardTierCriterion` is already
   registered and firing; a new gated recipe is pure data.
4. Boot-test. A malformed criterion reference (typo'd collection id, tier out of range)
   fails loudly at advancement-load time, the same way a bad item reference in a normal
   recipe advancement always has in this project.

---

## Quick reference: where things live

- `GreenwardCollection.java` — the 9 collections, tier thresholds, `tierForQuantity`.
- `GreenwardSkill.java` — the 4 skills and their per-level stat grants.
- `GreenwardProof.java` — every Proof, its collection/tier, title, and requirement text.
- `PlayerProgressData.java` — the persisted payload + codec (collections, skill XP,
  completed Proofs, lifetime counters only — **not** day/streak counters, see above).
- `PlayerProgress.java` — the facade. Everything above routes through here; nothing
  else should touch `PlayerProgressData` or the ephemeral tracking maps directly.
- `GreenwardTierCriterion.java` — the custom advancement trigger recipes gate on.
- `SkillXpCurve.java` — the level table. Hand-editable; regenerate via the geometric-
  interpolation script noted in its own doc comment if the anchor points ever change.
- `GreenwardCommands.java` (`/greenward progress`) — the only way to inspect a player's
  live Collections/Skills/Proofs state right now. The Field Guide book itself still
  shows static reference content only — its live-progress view is blocked on a
  client/server sync channel that doesn't exist yet (see the README's Update 2 section,
  "Notable implementation substitutions").
