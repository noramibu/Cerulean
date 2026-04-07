package toni.cerulean.foundation.config;

public final class RuntimeOptions {

    private RuntimeOptions() {
    }

    public static boolean debugMode() {
        var common = AllConfigs.common();
        return common == null ? false : common.debugMode.get();
    }

    public static boolean enableSkipTicks() {
        var common = AllConfigs.common();
        return common == null ? true : common.enableSkipTicks.get();
    }

    public static boolean ignoreTriggersForEmptiedStacks() {
        var common = AllConfigs.common();
        return common == null ? true : common.ignoreTriggersForEmptiedStacks.get();
    }

    public static boolean ignoreTriggersForDecreasedStacks() {
        var common = AllConfigs.common();
        return common == null ? true : common.ignoreTriggersForDecreasedStacks.get();
    }

    public static boolean optimizeMultiplePredicateTrigger() {
        var common = AllConfigs.common();
        return common == null ? true : common.optimizeMultiplePredicateTrigger.get();
    }

    public static boolean initializeInventoryLastSlots() {
        var common = AllConfigs.common();
        return common == null ? true : common.initializeInventoryLastSlots.get();
    }

    public static boolean optimizeTriggersForIncreasedStacks() {
        var common = AllConfigs.common();
        return common == null ? true : common.optimizeTriggersForIncreasedStacks.get();
    }

    public static boolean checkCountBeforeItemPredicateMatch() {
        var common = AllConfigs.common();
        return common == null ? true : common.checkCountBeforeItemPredicateMatch.get();
    }
}
