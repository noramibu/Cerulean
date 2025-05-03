package toni.cerulean.mixin;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import toni.cerulean.iface.IItemPredicateMixin;
import toni.cerulean.iface.IItemStackMixin;
import toni.cerulean.util.LogHelper;

import java.util.Optional;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateMixin implements IItemPredicateMixin {
    @Final
    @Shadow
    private MinMaxBounds.Ints count;

    #if mc >= 211
    @Shadow
    public abstract boolean test(ItemStack itemStack);
    #else
    @Shadow
    public abstract boolean matches(ItemStack itemStack);
    #endif

    /**
     * Item matching, especially against tag, is very heavy. By comparing the stack count first,
     * we could avoid unneeded tag matching.
     * We also could use previous stack count to avoid even more unneeded matching.
     */
    @Override
    public boolean cerulean$fasterMatches(ItemStack itemStack) {
        #if mc >= 211
        Optional<Integer> minThr = count.min();
        Optional<Integer> maxThr = count.max();
        int stackCount = itemStack.getCount();
        int prevStackCount = ((IItemStackMixin) (Object) itemStack).cerulean$getPreviousStackSize();

        LogHelper.debug(() -> "Checking stack %d for range [%d; %d]".formatted(stackCount, minThr.orElse(null), maxThr.orElse(null)));

        if ((minThr.map(integer -> (prevStackCount < integer && integer <= stackCount)).orElseGet(() -> prevStackCount == 0))
                && (maxThr.isEmpty() || stackCount <= maxThr.get())) {
            return #if mc >= 211 test #else matches #endif (itemStack);
        }
        return false;
        #else
        Integer minThr = count.getMin();
        Integer maxThr = count.getMax();
        int stackCount = itemStack.getCount();
        int prevStackCount = ((IItemStackMixin) (Object) itemStack).cerulean$getPreviousStackSize();

        LogHelper.debug(() -> "Checking stack %d for range [%d; %d]".formatted(stackCount, minThr, maxThr));

        if ((minThr == null ? prevStackCount == 0 :
            (prevStackCount < minThr && minThr <= stackCount))
            && (maxThr == null || stackCount <= maxThr)) {
            return matches(itemStack);
        }
        return false;
        #endif
    }
}
