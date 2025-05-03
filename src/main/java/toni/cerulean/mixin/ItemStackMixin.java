package toni.cerulean.mixin;

import toni.cerulean.iface.IItemStackMixin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IItemStackMixin {
    @Unique
    private int cerulean$previousStackSize;

    @Override
    public void cerulean$setPreviousStackSize(int value) {
        cerulean$previousStackSize = value;
    }

    @Override
    public int cerulean$getPreviousStackSize() {
        return cerulean$previousStackSize;
    }
}
