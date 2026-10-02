# Texture Guide

How item/block art actually gets displayed in Greenward, and the exact steps to swap
placeholder art for real art. Written after the 2026-09-10 placeholder pass, where most
custom tools/weapons/rods/tridents were pointed at vanilla items' own textures and held
poses as stand-ins until real art exists.

## The two files that matter per item

Every item has (at minimum) two files, and both matter — a texture alone does nothing:

1. **Texture** — `src/main/resources/assets/greenward/textures/item/<name>.png`
   16×16, matching vanilla's own resolution. Just the flat icon artwork.
2. **Model** — `src/main/resources/assets/greenward/models/item/<name>.json`
   Says *how* that texture gets displayed — flat in a GUI, tilted in-hand like a tool,
   bent like a fishing rod, etc. This is the file that actually matters for "why does
   this look flat/wrong when held" — the texture is rarely the problem.

The model's `"parent"` field picks the display behavior:

| Parent | Looks like | Use for |
|---|---|---|
| `minecraft:item/generated` | Flat 2D icon everywhere | Materials, food, ingots, most non-equipment items |
| `minecraft:item/handheld` | Tilted diagonally in hand (the normal tool/sword look) | Pickaxes, axes, hoes, swords |
| `minecraft:item/handheld_rod` | The fishing-rod-specific bend | Fishing rods only |

A plain custom item (own texture, standard pose) looks like this — this is
`anglers_line.json` right now:

```json
{
  "parent": "minecraft:item/handheld_rod",
  "textures": {
    "layer0": "greenward:item/anglers_line"
  }
}
```

Swap `handheld_rod` for `handheld` for a tool/sword, or drop the file down to just
`"parent": "minecraft:item/generated"` with the same `textures` block for a flat item.

## The placeholder trick (what most tools/weapons use right now)

You can parent a model directly at an *existing vanilla item's own model* instead of
`item/generated`/`item/handheld`. That pulls in both its texture **and** its correct
held pose in one line — no `textures` block needed at all:

```json
{
  "parent": "minecraft:item/netherite_pickaxe"
}
```

That's the entire file for `excavators_pick.json` right now. This is how every custom
tool/weapon/rod/trident except Angler's Line currently looks — each one points at
whichever vanilla item is the closest power-tier match (tier 1 → diamond-tier vanilla
item, tiers 2–3 → netherite-tier). The mapping as of this pass:

| Greenward item | Vanilla placeholder |
|---|---|
| Harvester's Scythe | `diamond_hoe` |
| Cultivator's Scythe / Harvest Warden | `netherite_hoe` |
| Prospector's Drill | `diamond_pickaxe` |
| Excavator's Pick / Bedrock Reaver | `netherite_pickaxe` |
| Marrowguard Blade / Voidstep Blade | `diamond_sword` |
| Ashwrought Edge / Reaper's Edge | `netherite_sword` |
| Deep-Sea Rod / Leviathan Rod / Scorched Leviathan Rod | `fishing_rod` |
| Barbed/Tidal/Leviathan's Trident | `trident` |

**To replace one with real art**: swap that file back to the two-field form above
(own `parent` type + `textures` block pointing at your own PNG in
`textures/item/`), the same way `anglers_line.json` already was swapped back.

## Armor — worn appearance is separate from the item icon

Armor is different: the inventory icon is a normal item texture/model like above, but
what renders *on the player's body* when worn comes from a completely different system —
`ArmorMaterial`'s `EquipmentAssets` reference, set in `ModArmorMaterials.java`, not from
any texture file in `textures/item/`.

```java
public static final ArmorMaterial TIDAL_WEAR = new ArmorMaterial(
        33, ZERO, 10, SoundEvents.ARMOR_EQUIP_IRON,
        0.0F, 0.0F, REPAIRS_TIDAL_WEAR, EquipmentAssets.IRON);
```

That last argument is what you're actually changing to reskin worn armor — it points at
one of vanilla's own equipment asset sets (`EquipmentAssets.COPPER`, `IRON`, `GOLD`,
`DIAMOND`, `NETHERITE`, `TURTLE_SCUTE`, …). **Watch out**: a couple of these
(`TURTLE_SCUTE` is the one that bit us) only define a *helmet* layer, because that's all
vanilla ever needed from them — using one of those for a full 4-piece set leaves the
chestplate/leggings/boots with no texture at all when worn, even though the inventory
icon looks fine. `COPPER`/`IRON`/`GOLD`/`DIAMOND`/`NETHERITE` all define full 4-piece
sets and are safe to use for a whole set.

**Mixing looks per slot** (e.g. the fishing helmets keeping the Turtle Shell head shape
while the body pieces go copper/iron/gold): give that one slot its own `ArmorMaterial`
with the same stats but a different `EquipmentAssets` value, and register just that
piece with it — see `ANGLERS_WEAR_HELM` next to `ANGLERS_WEAR` in
`ModArmorMaterials.java`, and how `ModArmor.java`'s `ANGLERS_CAP = piece(...)` call uses
the `_HELM` material while the other three pieces use the regular one.

Real custom worn-armor art (not reusing a vanilla equipment asset) means registering a
brand new `EquipmentAsset` with its own texture layers — bigger job, not covered here
yet.

## Blocks

Blocks use a third pattern: a **blockstate** file
(`assets/greenward/blockstates/<name>.json`) mapping each blockstate property
combination to a model, e.g. `fertilized_farmland.json` maps `moisture=0..6` to a dry
model and `moisture=7` to a wet one. Each of those models then points at its own texture
in `textures/block/`. Same idea as items, just with a blockstate layer added for
property-dependent looks.

## Checking a change

1. Edit the texture/model file(s).
2. From the project root: `./gradlew build` (or `compileJava` if only Java changed).
3. `./gradlew runClient` to actually look at it in-hand/in-inventory — `/give` yourself
   the item, or find it in the creative tab.
4. If you only touched resources (no `.java` changes), `processResources` alone is
   enough, but `build` is the safe default.

A genuinely custom 3D held model (new geometry, not just a flat texture tilted by
`handheld`) needs a full model with real `elements`/`faces` — a bigger job than anything
in this guide, worth its own pass later.
