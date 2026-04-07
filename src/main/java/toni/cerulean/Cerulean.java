package toni.cerulean;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import toni.cerulean.foundation.config.AllConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import toni.cerulean.foundation.ReloadListenerHandler;
import toni.cerulean.impl.ReloadListenerHandlerBase;

#if FABRIC
    import net.fabricmc.api.ClientModInitializer;
    import net.fabricmc.api.ModInitializer;
    import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
    import net.minecraft.server.packs.PackType;
    #if after_21_1
    import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
    import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
    import net.neoforged.neoforge.client.gui.ConfigurationScreen;
    #endif

    #if current_20_1
    import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
    #endif
#endif

#if FORGE
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
#endif


#if NEO
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
#endif


#if FORGELIKE
@Mod("cerulean")
#endif
public class Cerulean #if FABRIC implements ModInitializer, ClientModInitializer #endif
{
    public static final String MODNAME = "Cerulean";
    public static final String ID = "cerulean";
    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    public Cerulean(#if NEO IEventBus modEventBus, ModContainer modContainer #endif) {
        Common.init();

        #if FORGE
        var context = FMLJavaModLoadingContext.get();
        var modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(ReloadListenerHandler.class);
        #endif

        #if FORGELIKE
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        AllConfigs.register((type, spec) -> {
            #if FORGE
            ModLoadingContext.get().registerConfig(type, spec);
            #elif NEO
            modContainer.registerConfig(type, spec);
            //modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            #endif
        });
        #endif

        #if NEO
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> event.addListener(ResourceLocation.fromNamespaceAndPath(Cerulean.ID, "reload_listener"), new ReloadListenerHandlerBase()));
        #endif
    }


    #if FABRIC @Override #endif
    public void onInitialize() {
        #if FABRIC
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ReloadListenerHandler());
            AllConfigs.register((type, spec) -> {
                #if AFTER_21_1
                ConfigRegistry.INSTANCE.register(Cerulean.ID, type, spec);
                #else
                ForgeConfigRegistry.INSTANCE.register(Cerulean.ID, type, spec);
                #endif
            });
        #endif
    }

    #if FABRIC @Override #endif
    public void onInitializeClient() {
        #if AFTER_21_1
            #if FABRIC
            ConfigScreenFactoryRegistry.INSTANCE.register(Cerulean.ID, ConfigurationScreen::new);
            #endif
        #endif
    }

    // Forg event stubs to call the Fabric initialize methods, and set up cloth config screen
    #if FORGELIKE
    public void commonSetup(FMLCommonSetupEvent event) { onInitialize(); }
    public void clientSetup(FMLClientSetupEvent event) { onInitializeClient(); }
    #endif
}
