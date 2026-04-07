package toni.cerulean;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import toni.cerulean.foundation.config.AllConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import toni.cerulean.foundation.ReloadListenerHandler;
import toni.cerulean.impl.ReloadListenerHandlerBase;
import java.lang.reflect.Method;
import java.util.function.Consumer;

#if FABRIC
    import net.fabricmc.api.ClientModInitializer;
    import net.fabricmc.api.ModInitializer;
    import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
    import net.minecraft.server.packs.PackType;
    #if after_21_1
    import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
    import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
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
        registerNeoReloadListenerBridge();
        #endif
    }


    #if FABRIC @Override #endif
    public void onInitialize() {
        #if FABRIC
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ReloadListenerHandler());
            AllConfigs.register((type, spec) -> {
                #if AFTER_21_1
                NeoForgeConfigRegistry.INSTANCE.register(Cerulean.ID, type, spec);
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

    #if NEO
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerNeoReloadListenerBridge() {
        String[] eventCandidates = {
            "net.neoforged.neoforge.event.AddServerReloadListenersEvent",
            "net.neoforged.neoforge.event.AddReloadListenerEvent"
        };

        for (String eventName : eventCandidates) {
            try {
                Class<?> eventClass = Class.forName(eventName);
                NeoForge.EVENT_BUS.addListener((Class) eventClass, (Consumer) Cerulean::onNeoReloadListenerEvent);
                LOGGER.info("Registered Cerulean reload listener hook for {}", eventName);
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }

        LOGGER.warn("Could not find NeoForge reload listener event class; Cerulean reload listener was not registered.");
    }

    private static void onNeoReloadListenerEvent(Object event) {
        ReloadListenerHandlerBase listener = new ReloadListenerHandlerBase();
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Cerulean.ID, "reload_listener");

        for (Method method : event.getClass().getMethods()) {
            if (!method.getName().equals("addListener")) continue;

            try {
                if (method.getParameterCount() == 2) {
                    method.invoke(event, id, listener);
                    return;
                }

                if (method.getParameterCount() == 1) {
                    method.invoke(event, listener);
                    return;
                }
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed invoking {} on {}", method.getName(), event.getClass().getName(), e);
                return;
            }
        }

        LOGGER.warn("No compatible addListener method found on {}", event.getClass().getName());
    }
    #endif
}
