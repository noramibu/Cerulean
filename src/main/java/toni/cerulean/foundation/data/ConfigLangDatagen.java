package toni.cerulean.foundation.data;

#if FABRIC
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import toni.cerulean.Cerulean;
import toni.cerulean.foundation.config.AllConfigs;

import java.util.concurrent.CompletableFuture;

public class ConfigLangDatagen extends FabricLanguageProvider {

    #if mc < 215
    protected ConfigLangDatagen(FabricDataOutput dataOutput) {
        super(dataOutput);
    }
    #else
    protected ConfigLangDatagen(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }
    #endif

    @Override
    public void generateTranslations(#if mc < 215 #else HolderLookup.Provider registryLookup, #endif TranslationBuilder translationBuilder) {
        AllConfigs.generateTranslations(translationBuilder);
    }

    @Override
    public String getName() {
        return "Cerulean Data Gen";
    }
}
#endif
