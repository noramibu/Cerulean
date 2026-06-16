package toni.cerulean.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
#if mc >= 262
import net.minecraft.advancements.triggers.CriteriaTriggers;
#else
import net.minecraft.advancements.CriteriaTriggers;
#endif
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import toni.cerulean.Cerulean;
import toni.cerulean.foundation.config.RuntimeOptions;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReloadListenerHandlerBase extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    private static final Gson GSON = (new GsonBuilder()).create();
    private static final String FOLDER = "advancements";

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        FileToIdConverter converter = FileToIdConverter.json(FOLDER);
        Map<Identifier, JsonElement> prepared = new HashMap<>();

        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry : converter.listMatchingResources(resourceManager).entrySet()) {
            Identifier id = converter.fileToId(entry.getKey());
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement parsed = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                if (parsed != null) {
                    prepared.put(id, parsed);
                }
            } catch (IOException | RuntimeException e) {
                Cerulean.LOGGER.error("Failed to parse advancement {}", id, e);
            }
        }

        return prepared;
    }

    /**
     * Collects all item count thresholds from loaded advancements.
     * For example, if there are advancements for obtaining any amount of stone,
     * 5 emeralds and 64 sticks, thresholds will be [1, 5, 64].
     * We then could use these thresholds to prevent unneeded advancement scanning.
     * For example, there is no need to check advancements when dirt stack size
     * increases from 52 to 53 if there's no advancement for getting 53 dirt.
     */
    @Override
    protected void apply(Map<Identifier, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        if (RuntimeOptions.optimizeTriggersForIncreasedStacks()) {
            StackSizeThresholdManager.clear();

            #if mc >= 211
            String inventoryChangedTriggerId = Objects.requireNonNull(BuiltInRegistries.TRIGGER_TYPES.getKey(CriteriaTriggers.INVENTORY_CHANGED)).toString();
            #else
            String inventoryChangedTriggerId = CriteriaTriggers.INVENTORY_CHANGED.getId().toString();
            #endif

            for (JsonElement advancementElement : object.values()) {
                JsonObject advancementCriteria = advancementElement.getAsJsonObject().getAsJsonObject("criteria");
                if (advancementCriteria != null && !advancementCriteria.isJsonNull()) {
                    for (var criterionEntry : advancementCriteria.entrySet()) {
                        JsonObject criterion = criterionEntry.getValue().getAsJsonObject();
                        JsonElement criterionTrigger = criterion.get("trigger");
                        JsonObject criterionConditions = criterion.getAsJsonObject("conditions");
                        if (criterionTrigger != null && criterionConditions != null
                                && !criterionTrigger.isJsonNull() && !criterionConditions.isJsonNull()
                                && criterionTrigger.getAsString().equals(inventoryChangedTriggerId)
                                && criterionConditions.has("items")) {
                            for (JsonElement itemElement : criterionConditions.getAsJsonArray("items")) {
                                JsonElement itemCount = itemElement.getAsJsonObject().get("count");
                                if (itemCount != null && !itemCount.isJsonNull()) {
                                    int itemCountMinValue = 0;

                                    if (itemCount.isJsonObject()) {
                                        JsonElement itemCountMin = itemCount.getAsJsonObject().get("min");
                                        if (itemCountMin != null && !itemCountMin.isJsonNull()) {
                                            itemCountMinValue = itemCountMin.getAsInt();
                                        }
                                    } else {
                                        itemCountMinValue = itemCount.getAsInt();
                                    }

                                    if (itemCountMinValue > 1) {
                                        StackSizeThresholdManager.add(itemCountMinValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            StackSizeThresholdManager.debugPrint();
        }
    }
}
