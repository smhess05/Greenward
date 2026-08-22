package com.green.ward;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Static page content for the Field Guide book (see GreenwardClient's UseItemCallback,
 * which opens these via BookViewScreen). Kept in its own class purely for size — the
 * book is long enough that inlining it into GreenwardClient would bury the actual client
 * init logic. Component/ChatFormatting are shared classes, but this file is still only
 * ever referenced from client entrypoint code, so it stays safely off the server's
 * classloading path regardless.
 *
 * Organized by PILLAR rather than by the order features shipped in — every pillar's
 * Tier I/II/III sits on consecutive pages, and a dedicated "The Ascension" page explains
 * the shared Tier II->III smithing mechanic once instead of leaving it implicit on every
 * Tier III recipe page.
 */
public final class FieldGuideContent {
    private FieldGuideContent() {}

    public static List<Component> pages() {
        List<Component> p = new ArrayList<>();

        p.add(new Page()
                .title("Greenward")
                .title("Field Guide")
                .blank()
                .body("A grower's record of every recipe known to these lands.")
                .blank()
                .note("(turn the page)")
                .build());

        p.add(new Page()
                .heading("Contents")
                .blank()
                .body("I.    Compression")
                .body("II.   Automation")
                .body("III.  The Ascension")
                .body("IV.   Farming Pillar")
                .body("V.    Mining Pillar")
                .body("VI.   Fishing Pillar")
                .body("VII.  Combat Pillar")
                .body("VIII. The God Potion")
                .body("IX.   Field Reference")
                .build());

        // ---------------- I. Compression ----------------

        p.add(sectionHeader("I", "Compression",
                "Every pillar bulks its raw goods down the same way. Learn the pattern once."));

        p.add(new Page()
                .heading("The Pattern")
                .blank()
                .body("9 of a raw good, crafted 3x3 shapeless, makes 1 Tier-1 compressed good.")
                .blank()
                .body("9 of THOSE makes 1 Tier-2.")
                .blank()
                .note("Wheat and Carrot reverse (break back down); everything else is one-way.")
                .build());

        p.add(new Page()
                .heading("Farming Chains")
                .blank()
                .body("Wheat -> Wheat Sheaf -> Wheat Bale")
                .blank()
                .body("Potato -> Potato Sack -> Potato Crate")
                .blank()
                .body("Carrot -> Gilded Carrot -> Radiant Carrot")
                .build());

        p.add(new Page()
                .heading("Mining & Fishing Chains")
                .blank()
                .body("Cobblestone -> Cobble Cluster -> Cobble Monolith")
                .blank()
                .body("Cobbled Deepslate -> Deepslate Cluster -> Deepslate Monolith")
                .blank()
                .body("Cod -> Cod School -> Cod Shoal")
                .blank()
                .body("Dried Kelp Block -> Kelp Reef")
                .build());

        p.add(new Page()
                .heading("Combat Chains")
                .blank()
                .body("Bone -> Bone Bundle -> Bone Reliquary")
                .blank()
                .body("Gunpowder -> Powder Satchel -> Powder Cache")
                .build());

        p.add(new Page()
                .title("Fertilizer")
                .type("Shapeless")
                .blank()
                .body("3x Bone Meal")
                .body("1x Dirt")
                .body("1x Wheat Seeds")
                .blank()
                .body("Yields 4 Fertilizer")
                .blank()
                .note("Right-click vanilla Farmland to convert it, no tilling required.")
                .build());

        // ---------------- II. Automation ----------------

        p.add(sectionHeader("II", "Automation",
                "Iron-tier machines that do the work of a tool for you. Each needs the fuel that tool would use, and takes upgrades in its side slots."));

        p.add(new Page()
                .title("Auto-Harvester")
                .type("Shaped, 3x3")
                .blank()
                .body("7x Iron Ingot")
                .body("1x Iron Hoe")
                .body("1x Redstone Block")
                .blank()
                .note("Hoe in the center, Redstone Block bottom-center. Scans a 9x9 area for mature crops.")
                .build());

        p.add(new Page()
                .title("Auto-Miner")
                .type("Shaped, 3x3")
                .blank()
                .body("7x Iron Ingot")
                .body("1x Iron Pickaxe")
                .body("1x Redstone Block")
                .blank()
                .note("Mines whatever block it faces; fed ores slowly regrow.")
                .build());

        p.add(new Page()
                .title("Auto-Fisher")
                .type("Shaped, 3x3")
                .blank()
                .body("7x Iron Ingot")
                .body("1x Fishing Rod")
                .body("1x Redstone Block")
                .blank()
                .note("Needs adjacent water; rolls the vanilla fishing table on its own. Never triggers sea creatures or The Marked.")
                .build());

        p.add(new Page()
                .title("Machine Upgrades")
                .blank()
                .body("Storage: 8x Iron Ingot, 1x Chest (unlocks a row)")
                .blank()
                .body("Speed: 4x Gold Ingot, 4x Redstone, 1x Sugar")
                .blank()
                .body("Regrowth: 4x Diamond, 1x Bone Meal (Auto-Miner only)")
                .blank()
                .note("All three shaped 3x3, keyed around a center item. Drop the finished upgrade into a machine's side slot.")
                .build());

        p.add(new Page()
                .title("Field Guide")
                .type("Shaped, 3x3")
                .blank()
                .body("8x Paper")
                .body("1x Wheat Sheaf")
                .blank()
                .note("The very book in your hands. Sheaf in the center, Paper around it.")
                .build());

        // ---------------- III. The Ascension ----------------

        p.add(sectionHeader("III", "The Ascension",
                "How every pillar's Tier III gear is actually made — read this before the pillar sections."));

        p.add(new Page()
                .heading("Not Crafted - Ascended")
                .blank()
                .body("Tier III gear never comes from a crafting table. It's upgraded at a SMITHING TABLE, the same way vanilla turns Diamond gear into Netherite.")
                .blank()
                .note("Bring three things to the smithing table:")
                .build());

        p.add(new Page()
                .heading("The Three Ingredients")
                .blank()
                .body("1. Your Tier II piece (the base)")
                .body("2. 1x Ascension Template")
                .body("3. That pillar's Catalyst")
                .blank()
                .note("All three are consumed. Your Tier II piece becomes its Tier III form — it doesn't stay in your inventory.")
                .build());

        p.add(new Page()
                .title("Ascension Template")
                .type("Shaped, 3x3")
                .blank()
                .body("4x Diamond")
                .body("4x Obsidian")
                .body("1x Emerald")
                .blank()
                .body("Yields 2 Templates")
                .blank()
                .note("Shared by all four pillars. A full Tier III set (4 armor + 1 weapon/tool) needs 5 templates — plan on 3 crafts of this recipe per pillar.")
                .build());

        // ---------------- IV. Farming Pillar ----------------

        p.add(sectionHeader("IV", "Farming Pillar",
                "Three tiers, one ladder: Harvester -> Cultivator -> Warden."));

        p.add(tierPage("Farming - Tier I", "Harvester's Garb & Scythe",
                "Hat: 5x Wheat Sheaf",
                "Boots: 2x Wheat Sheaf, 2x Leather",
                "Leggings: 6x Wheat Sheaf, 1x Leather",
                "Tunic: 7x Wheat Sheaf, 1x Leather",
                "Scythe: 2x Wheat Sheaf, 1x Wheat Bale, 2x Stick",
                "Haste while holding a hoe; +1 hoe radius with full set."));

        p.add(tierPage("Farming - Tier II", "Cultivator's Garb & Scythe",
                "Hat: 5x Wheat Bale",
                "Boots: 2x Wheat Bale, 2x Potato Sack",
                "Leggings: 6x Wheat Bale, 1x Potato Sack",
                "Tunic: 7x Wheat Bale, 1x Potato Sack",
                "Scythe: 2x Wheat Bale, 1x Potato Sack, 2x Stick",
                "Haste II while holding a hoe; 20% bonus-drop chance."));

        p.add(new Page()
                .title("Farming - Tier III")
                .type("Warden's Garb & Harvest Warden")
                .blank()
                .body("Ascend a Cultivator's piece with:")
                .body("1x Harvest Core")
                .blank()
                .body("Harvest Core (shapeless):")
                .body("1x Wheat Bale, 1x Potato Crate, 1x Radiant Carrot")
                .blank()
                .note("Full set: Haste II + Regeneration. Harvest Warden reaches the true 9x9 harvest radius.")
                .build());

        // ---------------- V. Mining Pillar ----------------

        p.add(sectionHeader("V", "Mining Pillar",
                "Three tiers, one ladder: Prospector -> Excavator -> Bedrock."));

        p.add(new Page()
                .title("Mining - Tier I")
                .type("Prospector's Plate & Drill")
                .blank()
                .body("Helm: 4x Iron Ingot, 1x Diamond")
                .body("Treads: 2x Iron Ingot, 2x Diamond")
                .body("Greaves: 6x Iron Ingot, 1x Diamond")
                .body("Plate: 7x Iron Ingot, 1x Diamond")
                .body("Drill: 2x Diamond, 1x Iron Ingot, 2x Stick")
                .blank()
                .note("Haste always; Night Vision below Y0; softer falls.")
                .build());

        p.add(tierPage("Mining - Tier II", "Excavator's Plate & Pick",
                "Helm: 5x Cobble Monolith",
                "Treads: 2x Cobble Monolith, 2x Deepslate Cluster",
                "Greaves: 6x Cobble Monolith, 1x Deepslate Cluster",
                "Plate: 7x Cobble Monolith, 1x Deepslate Cluster",
                "Pick: 2x Cobble Monolith, 1x Deepslate Cluster, 2x Stick",
                "Haste II always; deeper Night Vision & fall-safety carry over."));

        p.add(new Page()
                .title("Mining - Tier III")
                .type("Bedrock Plate & Reaver")
                .blank()
                .body("Ascend an Excavator's piece with:")
                .body("1x Bedrock Core")
                .blank()
                .body("Bedrock Core (shapeless):")
                .body("1x Cobble Monolith, 1x Deepslate Monolith, 1x Amethyst Shard")
                .blank()
                .note("Full set: Haste II + Fire Resistance. Bedrock Reaver is the fastest pickaxe in the mod.")
                .build());

        // ---------------- VI. Fishing Pillar ----------------

        p.add(sectionHeader("VI", "Fishing Pillar",
                "Three armor tiers: Angler -> Tidal -> Leviathan's. The rod ladder from the Gear update already stands as this pillar's tool tiers."));

        p.add(new Page()
                .title("Fishing - Tier I")
                .type("Angler's Wear & Rods")
                .blank()
                .body("Cap: 5x Prismarine Shard")
                .body("Fins: 2x Prismarine Shard, 2x Turtle Scute")
                .body("Waders: 6x Prismarine Shard, 1x Turtle Scute")
                .body("Coat: 7x Prismarine Shard, 1x Turtle Scute")
                .blank()
                .body("Angler's Line (I): 3x Prismarine Shard, 2x String")
                .body("Deep-Sea Rod (II): Line + 4x Crystals + 1x Nautilus Shell")
                .body("Leviathan Rod (III): Deep-Sea Rod + 1x Heart of the Sea + 2x Shell + 2x Crystals")
                .build());

        p.add(tierPage("Fishing - Tier II", "Tidal Wear",
                "Cap: 5x Cod Shoal",
                "Fins: 2x Cod Shoal, 2x Dried Kelp Block",
                "Waders: 6x Cod Shoal, 1x Dried Kelp Block",
                "Coat: 7x Cod Shoal, 1x Dried Kelp Block",
                null,
                "Water Breathing, Dolphin's Grace, Luck +2."));

        p.add(new Page()
                .title("Fishing - Tier III")
                .type("Leviathan's Wear")
                .blank()
                .body("Ascend a Tidal piece with:")
                .body("1x Leviathan's Heart")
                .blank()
                .body("Leviathan's Heart (shapeless):")
                .body("1x Cod Shoal, 1x Kelp Reef, 1x Heart of the Sea")
                .blank()
                .note("Full set: Luck +3, Night Vision underwater. Pair with a Leviathan Rod for near-guaranteed sea creature catches.")
                .build());

        // ---------------- VII. Combat Pillar ----------------

        p.add(sectionHeader("VII", "Combat Pillar",
                "The only pillar built from nothing — Marrowguard -> Ashwrought -> Reaper's Aegis. Feeds The Marked, five elite hostiles only a real melee kill can call."));

        p.add(tierPage("Combat - Tier I", "Marrowguard & Blade",
                "Helmet: 3x Bone Bundle",
                "Boots: 1x Bone Bundle, 1x String",
                "Leggings: 4x Bone Bundle, 1x String",
                "Chestplate: 5x Bone Bundle, 1x String",
                "Blade: 1x Bone Bundle, 1x Bone Reliquary, 2x Stick",
                "Lighter costs than the other pillars' Tier I on purpose — Bone has no automation machine behind it. Attack speed +10% with full set."));

        p.add(tierPage("Combat - Tier II", "Ashwrought & Edge",
                "Helmet: 3x Bone Reliquary",
                "Boots: 1x Bone Reliquary, 1x Powder Satchel",
                "Leggings: 4x Bone Reliquary, 1x Powder Satchel",
                "Chestplate: 5x Bone Reliquary, 1x Powder Satchel",
                "Edge: 1x Bone Reliquary, 1x Powder Satchel, 2x Stick",
                "Attack speed +15%, attack damage +1 with full set."));

        p.add(new Page()
                .title("Combat - Tier III")
                .type("Reaper's Aegis & Edge")
                .blank()
                .body("Ascend an Ashwrought piece with:")
                .body("1x Reaper's Core")
                .blank()
                .body("Reaper's Core (shapeless):")
                .body("1x Bone Reliquary, 1x Powder Cache, 1x Ender Pearl")
                .blank()
                .note("Full set: attack speed +20%, damage +2, Hunter's Mark Aura (see Field Reference), and Grim Resolve.")
                .build());

        p.add(new Page()
                .heading("Grim Resolve")
                .blank()
                .body("Once every 5 minutes, a hit that would kill you while wearing full Reaper's Aegis instead leaves you at a sliver of health.")
                .blank()
                .note("A safety net, not a Totem replacement — a real Totem still saves you first if you're carrying one.")
                .build());

        // ---------------- VIII. The God Potion ----------------

        p.add(sectionHeader("VIII", "The God Potion",
                "Now built entirely from what the four pillars already produce — no dragon fight, no wither farm."));

        p.add(new Page()
                .title("Golden Harvest")
                .type("Shaped, 3x3 - Farming")
                .blank()
                .body("4x Wheat Bale")
                .body("4x Potato Crate")
                .blank()
                .divider()
                .title("Deepstone Core")
                .type("Shaped, 3x3 - Mining")
                .blank()
                .body("4x Obsidian, 4x Diamond, 1x Netherite Scrap")
                .build());

        p.add(new Page()
                .title("Abyssal Pearl")
                .type("Shapeless - Fishing")
                .blank()
                .body("4x Prismarine Crystals")
                .body("4x Nautilus Shell")
                .body("1x Heart of the Sea")
                .blank()
                .note("The Crystals and Shells come straight off the Auto-Fisher's own ungated treasure table — only the single Heart of the Sea needs a manual catch.")
                .build());

        p.add(new Page()
                .title("Godly Catalyst")
                .type("Shaped, 3x3")
                .blank()
                .body("1x Golden Harvest (top)")
                .body("1x Deepstone Core (left)")
                .body("1x Abyssal Pearl (right)")
                .body("1x Reaper's Core (bottom)")
                .blank()
                .note("One catalyst per pillar, center left empty. Every ingredient upstream of this recipe can be farmed, mined, fished, or fought for — nothing is a one-time boss drop.")
                .build());

        p.add(new Page()
                .title("God Potion")
                .type("Brewed")
                .blank()
                .body("Awkward Potion")
                .body("+ 1x Godly Catalyst")
                .blank()
                .note("Brew it like any vanilla potion — hopper-automatable, same as any brew.")
                .build());

        // ---------------- IX. Field Reference ----------------

        p.add(sectionHeader("IX", "Field Reference",
                "Not recipes, but worth knowing before you go looking for either."));

        p.add(new Page()
                .heading("Set Bonuses - Farming & Mining")
                .blank()
                .body("Harvester's / Cultivator's / Warden's: Haste (II at Tier II+) while holding a hoe; Warden's adds Regeneration")
                .blank()
                .body("Prospector's / Excavator's / Bedrock: Haste, Night Vision below Y0, softer falls; Bedrock adds Fire Resistance")
                .build());

        p.add(new Page()
                .heading("Set Bonuses - Fishing & Combat")
                .blank()
                .body("Angler's / Tidal / Leviathan's: Water Breathing, Dolphin's Grace, rising Luck; Leviathan's adds Night Vision underwater")
                .blank()
                .body("Marrowguard / Ashwrought / Reaper's Aegis: rising attack speed & damage; Reaper's Aegis adds Hunter's Mark Aura + Grim Resolve")
                .build());

        p.add(new Page()
                .heading("Sea Creatures")
                .blank()
                .body("Manual fishing only — the Auto-Fisher can never trigger these. Chance rises with rod tier and a full Angler's Wear.")
                .blank()
                .note("A Leviathan Rod plus full Angler's Wear makes a catch on nearly every cast.")
                .build());

        p.add(new Page()
                .heading("The Marked")
                .blank()
                .body("Zombie -> Revenant, Skeleton -> Deadeye, Spider -> Widowfang, Creeper -> Ashborn, Enderman -> Wraith.")
                .blank()
                .body("Only a genuine melee kill can trigger one — never a trap, farm, or projectile. Chance rises with combat armor tier, capped at 10%; Reaper's Aegis sits at the cap outright.")
                .blank()
                .note("Ashborn drops bonus Gunpowder, Wraith a guaranteed Ender Pearl — the fast route to Reaper's Core.")
                .build());

        p.add(new Page()
                .title("Fair Winds")
                .blank()
                .body("That's every recipe known to Greenward, from a single sheaf of wheat to the God Potion itself.")
                .blank()
                .note("Go raise something.")
                .build());

        return p;
    }

    private static Component sectionHeader(String numeral, String name, String blurb) {
        return new Page()
                .divider()
                .heading(numeral + ".  " + name)
                .divider()
                .blank()
                .note(blurb)
                .build();
    }

    private static Component tierPage(String tierLabel, String setName, String piece1, String piece2, String piece3, String piece4, String weapon, String note) {
        Page page = new Page()
                .title(tierLabel)
                .type(setName)
                .blank()
                .body(piece1)
                .body(piece2)
                .body(piece3)
                .body(piece4);
        if (weapon != null) {
            page.body(weapon);
        }
        if (note != null) {
            page.blank().note(note);
        }
        return page.build();
    }

    /** Small fluent builder so each page reads as a flat list of lines instead of nested append() calls. */
    private static final class Page {
        private final MutableComponent root = Component.empty();
        private boolean empty = true;

        private Page append(Component part) {
            if (!empty) {
                root.append("\n");
            }
            root.append(part);
            empty = false;
            return this;
        }

        Page title(String text) {
            return append(Component.literal(text).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        }

        Page heading(String text) {
            return append(Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }

        Page type(String text) {
            return append(Component.literal(text).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        Page body(String text) {
            return append(Component.literal(text).withStyle(ChatFormatting.BLACK));
        }

        Page note(String text) {
            return append(Component.literal(text).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        Page divider() {
            return append(Component.literal("* * *").withStyle(ChatFormatting.GOLD));
        }

        Page blank() {
            return append(Component.literal(""));
        }

        Component build() {
            return root;
        }
    }
}
