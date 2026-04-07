package toni.cerulean.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import toni.cerulean.foundation.config.RuntimeOptions;
import toni.cerulean.iface.IItemPredicateMixin;
import toni.cerulean.util.LogHelper;

import java.util.List;


@Mixin(InventoryChangeTrigger.TriggerInstance.class)
abstract class InventoryChangeTriggerInstanceMixin {
    /**
     * When the game checks if given inventoryChangeTrigger matches changed stack
     * and this trigger has only one predicate, game only checks if said trigger
     * matches changed stack.
     * <br>
     * However, when trigger has multiple predicates, for some reason game doesn't
     * check them against changed stack at first, but against all the items in
     * player inventory, even if changed stack doesn't match any of predicates.
     * <br>
     * So this injected code checks if any of predicates match the changed stack,
     * and if not, cancels the full inventory scan.
     */
    @Inject(method = "matches(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;III)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getContainerSize()I", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)

    #if mc >= 211
    public void matches(Inventory inventory, ItemStack itemStack, int full, int empty, int occupied, CallbackInfoReturnable<Boolean> cir, @Local List<ItemPredicate> predicatesList) {
    #else
    public void matches(Inventory inventory, ItemStack itemStack, int full, int empty, int occupied, CallbackInfoReturnable<Boolean> cir, @Local List<ItemPredicate> predicatesList) {
    #endif
        // If no predicate in list matches the changed item, the trigger not matches
        if (RuntimeOptions.optimizeMultiplePredicateTrigger() &&
            #if mc >= 211
            !predicatesList.removeIf(itemPredicate -> itemPredicate.test(itemStack))
            #else
            !predicatesList.removeIf(itemPredicate -> itemPredicate.matches(itemStack))
            #endif
        ) {
            LogHelper.debug("Trigger has multiple predicates, and none of them matches changed item. Skipping full inventory check");
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /**
     * Use optimized itemPredicate match
     */
    #if mc >= 261
    @Redirect(method = "matches(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;III)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ItemPredicate;test(Lnet/minecraft/world/item/ItemInstance;)Z"))
    #elif mc >= 211
    @Redirect(method = "matches(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;III)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ItemPredicate;test(Lnet/minecraft/world/item/ItemStack;)Z"))
    #else
    @Redirect(method = "matches(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;III)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ItemPredicate;matches(Lnet/minecraft/world/item/ItemStack;)Z"))
    #endif
    public boolean itemPredicateMatches(ItemPredicate itemPredicate, #if mc >= 261 ItemInstance itemInstance #else ItemStack itemStack #endif) {
        if (RuntimeOptions.checkCountBeforeItemPredicateMatch()) {
            #if mc >= 261
            ItemStack itemStack = (ItemStack) itemInstance;
            #endif
            return ((IItemPredicateMixin) (Object) itemPredicate).cerulean$fasterMatches(itemStack);
        } else {
            #if mc >= 261
            return itemPredicate.test(itemInstance);
            #elif mc >= 211
            return itemPredicate.test(itemStack);
            #else
            return itemPredicate.matches(itemStack);
            #endif
        }
    }
}
