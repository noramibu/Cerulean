package toni.cerulean.mixin;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Unique;
import toni.cerulean.foundation.config.RuntimeOptions;
import toni.cerulean.iface.IItemStackMixin;
import toni.cerulean.impl.StackSizeThresholdManager;
import toni.cerulean.util.LogHelper;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(InventoryChangeTrigger.class)
abstract class InventoryChangeTriggerMixin {

    @Unique
    private Map<UUID, Map<String, Integer>> cerulean$skipTicks = new HashMap<>();

    /**
     * This injection cancels triggering advancement scan for changed slot
     * if this trigger was caused by emptying or decreasing the stack size,
     * or if stack size increased but hasn't crossed any threshold
     * (see {@link toni.cerulean.impl.ReloadListenerHandlerBase#apply(Map, ResourceManager, ProfilerFiller)})
     */
    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "HEAD"), cancellable = true)
    public void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack, CallbackInfo ci) {
        var skipTickMap = cerulean$skipTicks.computeIfAbsent(serverPlayer.getUUID(), k -> new HashMap<>());
        if (RuntimeOptions.enableSkipTicks())
        {
            var itemName = itemStack.getItem().toString();
            var skipTicks = skipTickMap.getOrDefault(itemName, 5);
            if (skipTicks < 4)
            {
                skipTicks++;
                skipTickMap.put(itemName, skipTicks);
                ci.cancel();
                return;
            }

            skipTickMap.put(itemName, 0);
        }

        if ((itemStack.isEmpty() && RuntimeOptions.ignoreTriggersForEmptiedStacks())
                || (RuntimeOptions.ignoreTriggersForDecreasedStacks()
                    && itemStack.getCount() < ((IItemStackMixin) (Object) itemStack).cerulean$getPreviousStackSize())
                || (RuntimeOptions.optimizeTriggersForIncreasedStacks()
                    && !StackSizeThresholdManager.doesStackPassThreshold(itemStack))) {
            ci.cancel();
            LogHelper.debug(() -> "InventoryChangeTrigger cancelled for %s".formatted(itemStack));
        } else {
            LogHelper.debug(() -> "InventoryChangeTrigger passed for %s".formatted(itemStack));
        }
    }
}
