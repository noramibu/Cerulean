package toni.cerulean.foundation;


import net.minecraft.resources.ResourceLocation;
import toni.cerulean.impl.ReloadListenerHandlerBase;
import toni.lib.utils.VersionUtils;

#if fabric
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
#elif forge
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
#endif

public class ReloadListenerHandler extends ReloadListenerHandlerBase #if fabric implements IdentifiableResourceReloadListener #endif {
    #if fabric
    @Override
    public ResourceLocation getFabricId() {
        return VersionUtils.resource("cerulean");
    }

    #elif forge
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ReloadListenerHandlerBase());
    }
    #endif
}