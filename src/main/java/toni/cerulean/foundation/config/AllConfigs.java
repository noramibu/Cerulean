package toni.cerulean.foundation.config;

import toni.lib.config.ConfigBase;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

#if FABRIC
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
#else
    #if NEO
    import net.neoforged.fml.config.ModConfig;
    import net.neoforged.neoforge.common.ModConfigSpec;
    import net.neoforged.neoforge.common.ModConfigSpec.Builder;
    #else
    import net.minecraftforge.fml.config.ModConfig;
    import net.minecraftforge.common.ForgeConfigSpec;
    import net.minecraftforge.common.ForgeConfigSpec.Builder;
    #endif
#endif

public class AllConfigs {
#if FABRIC
    private static final Map<String, ConfigBase> CONFIGS = new LinkedHashMap<>();
#else
    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);
#endif

    private static CClient client;
    private static CCommon common;
    private static CServer server;

    public static CClient client() {
        return client;
    }

    public static CCommon common() {
        return common;
    }

    public static CServer server() {
        return server;
    }

#if FABRIC
    public static ConfigBase byType(String type) {
        return CONFIGS.get(type);
    }
#else
    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }
#endif

#if FABRIC
    private static <T extends ConfigBase> T register(Supplier<T> factory, String key) {
        T config = factory.get();
        config.loadOrCreateToml(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("cerulean-" + key + ".toml"));
        CONFIGS.put(key, config);
        return config;
    }

    public static void register(BiConsumer<Object, Object> registration) {
        client = null;
        common = register(CCommon::new, "common");
        server = null;

        for (ConfigBase config : CONFIGS.values()) {
            registration.accept(null, config.specification);
        }
    }

    public static void generateTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder) {
        // Intentionally no-op on Fabric without ForgeConfigAPIPort/NightConfig wiring.
    }
#else
    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        var specPair = new Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static void register(BiConsumer<ModConfig.Type, #if NEO ModConfigSpec #else ForgeConfigSpec #endif> registration) {
        client = null;
        common = register(CCommon::new, ModConfig.Type.COMMON);
        server = null;

        for (Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet()) {
            registration.accept(pair.getKey(), pair.getValue().specification);
        }
    }
#endif
}
