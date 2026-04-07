package toni.cerulean.foundation.config;

import toni.lib.config.ConfigBase;

public class CCommon extends ConfigBase {

    public final ConfigBool debugMode = b(false, "debug_mode", "Enables debug logs. Will reduce performance.");
    public final ConfigBool enableSkipTicks = b(true, "enable_skip_ticks", "Makes advancement checks run less often.");
    public final ConfigBool ignoreTriggersForEmptiedStacks = b(true, "ignore_triggers_for_emptied_stacks", "Cancels advancement checks when stacks become empty.");
    public final ConfigBool ignoreTriggersForDecreasedStacks = b(true, "ignore_triggers_for_decreased_stacks", "Cancels advancement checks when stack size decreases.");
    public final ConfigBool optimizeMultiplePredicateTrigger = b(true, "optimize_multiple_predicate_trigger", "Skips full inventory scans when multi-item predicates cannot match.");
    public final ConfigBool initializeInventoryLastSlots = b(true, "initialize_inventory_last_slots", "Avoids initial slot listener advancement bursts when opening containers.");
    public final ConfigBool optimizeTriggersForIncreasedStacks = b(true, "optimize_triggers_for_increased_stacks", "Skips checks when increased stack size cannot pass advancement thresholds.");
    public final ConfigBool checkCountBeforeItemPredicateMatch = b(true, "check_count_before_item_predicate_match", "Checks count bounds before expensive item predicate matching.");


    @Override
    public String getName() {
        return "common";
    }
}
