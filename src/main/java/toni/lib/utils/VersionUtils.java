package toni.lib.utils;

import net.minecraft.resources.ResourceLocation;
import toni.cerulean.Cerulean;

public final class VersionUtils {
    private VersionUtils() {
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(Cerulean.ID, path);
    }
}
