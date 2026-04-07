package toni.lib.utils;

#if FABRIC
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
#endif

#if FORGE
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
#endif

#if NEO
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
#endif

public final class PlatformUtils {
    private PlatformUtils() {
    }

    public static boolean isDedicatedServer() {
        #if FABRIC
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
        #elif FORGE
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
        #elif NEO
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
        #else
        return false;
        #endif
    }
}
