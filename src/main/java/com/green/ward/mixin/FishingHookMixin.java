package com.green.ward.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The mod's only mixin, added after a user-reported bug where casting with any of the
 * three Greenward fishing rods spawned a bobber with no momentum that vanished within a
 * single tick. Root cause: {@code FishingHook.shouldStopFishing(Player)} — a private
 * vanilla method, called every tick — checks "is the held item literally {@code
 * Items.FISHING_ROD}" by identity (not type, not a tag), and discards the hook the instant
 * that's false. A vanilla rod always passes; any modded {@link FishingRodItem} (Greenward's
 * three tiers included) never does, so every custom-rod cast self-destructed on its very
 * first server tick — before it could ever move, get retrieved, or lose durability. There
 * is no Fabric event, tag, or data-driven hook that reaches this check; broadening it here
 * is the only fix. Redirects both {@code ItemStack.is(Items.FISHING_ROD)} calls inside that
 * method (main hand, off hand) to accept any {@link FishingRodItem}, vanilla's own included,
 * so behavior for the real Fishing Rod is unchanged.
 *
 * <p>Bytecode note: {@code ItemStack.is(Items.FISHING_ROD)} decompiles as if it called an
 * {@code is(Item)} overload, but per {@code javap -c} the actual invocation is the erased
 * bridge {@code ItemStack.is(Ljava/lang/Object;)Z} — confirmed against the real class file,
 * not trusted from the decompiled source — so the redirect target and handler both use
 * {@code Object}, not {@code Item}, or the injection silently matches zero call sites.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Redirect(method = "shouldStopFishing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean greenward$acceptAnyFishingRod(ItemStack stack, Object item) {
        return stack.getItem() instanceof FishingRodItem;
    }
}
