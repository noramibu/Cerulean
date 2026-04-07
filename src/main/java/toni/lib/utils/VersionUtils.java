package toni.lib.utils;

import net.minecraft.resources.Identifier;
import toni.cerulean.Cerulean;

public final class VersionUtils {
    private VersionUtils() {
    }

    public static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath(Cerulean.ID, path);
    }
}
