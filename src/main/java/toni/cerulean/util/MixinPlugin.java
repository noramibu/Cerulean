package toni.cerulean.util;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import toni.cerulean.foundation.config.RuntimeOptions;

import java.util.List;
import java.util.Set;

/**
 * This plugin disables loading specific mixins based on settings in config file.
 */
public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean status = switch (mixinClassName) {
            case "toni.cerulean.mixin.AbstractContainerMenuMixin" -> RuntimeOptions.initializeInventoryLastSlots();
            case "toni.cerulean.mixin.InventoryChangeTriggerInstanceMixin" ->
                    RuntimeOptions.optimizeMultiplePredicateTrigger() || RuntimeOptions.checkCountBeforeItemPredicateMatch();
            case "toni.cerulean.mixin.InventoryChangeTriggerMixin" ->
                    RuntimeOptions.ignoreTriggersForEmptiedStacks()
                            || RuntimeOptions.ignoreTriggersForDecreasedStacks()
                            || RuntimeOptions.optimizeTriggersForIncreasedStacks();
            case "toni.cerulean.mixin.ItemStackMixin" ->
                    RuntimeOptions.ignoreTriggersForDecreasedStacks()
                            || RuntimeOptions.optimizeTriggersForIncreasedStacks();
            case "toni.cerulean.mixin.AbstractContainerMenuMixinPlatform" -> RuntimeOptions.ignoreTriggersForDecreasedStacks();
            case "toni.cerulean.mixin.ItemPredicateMixin" -> RuntimeOptions.checkCountBeforeItemPredicateMatch();
            default -> true;
        };
        LogHelper.debug(() -> "Apply mixin %s: %s".formatted(mixinClassName, status));
        return status;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
