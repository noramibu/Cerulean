package toni.lib.config;

#if FABRIC
public abstract class ConfigBase {
    public Object specification;

    protected ConfigGroup group(int order, String name, String comment) {
        return new ConfigGroup(name);
    }

    protected ConfigBool b(boolean defaultValue, String name, String comment) {
        return new ConfigBool(defaultValue);
    }

    public void registerAll(Object builder) {
    }

    public void onLoad() {
    }

    public void onReload() {
    }

    public abstract String getName();

    public static final class ConfigGroup {
        private final String name;

        private ConfigGroup(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class ConfigBool {
        private boolean value;

        private ConfigBool(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }
}
#else
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

#if NEO
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
#else
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
#endif

public abstract class ConfigBase {
    public #if NEO ModConfigSpec #else ForgeConfigSpec #endif specification;

    private final List<Consumer<Builder>> pendingEntries = new ArrayList<>();
    private String groupName;
    private String groupComment;

    protected ConfigGroup group(int order, String name, String comment) {
        if (groupName == null) {
            groupName = name;
            groupComment = comment;
        }
        return new ConfigGroup(name);
    }

    protected ConfigBool b(boolean defaultValue, String name, String comment) {
        ConfigBool value = new ConfigBool(defaultValue);
        pendingEntries.add(builder -> {
            if (comment != null && !comment.isBlank()) {
                value.bind(builder.comment(comment).define(name, defaultValue));
            } else {
                value.bind(builder.define(name, defaultValue));
            }
        });
        return value;
    }

    public void registerAll(Builder builder) {
        if (groupName != null) {
            if (groupComment != null && !groupComment.isBlank()) {
                builder.comment(groupComment);
            }
            builder.push(groupName);
        }

        for (Consumer<Builder> entry : pendingEntries) {
            entry.accept(builder);
        }

        if (groupName != null) {
            builder.pop();
        }
    }

    public void onLoad() {
    }

    public void onReload() {
    }

    public abstract String getName();

    public static final class ConfigGroup {
        private final String name;

        private ConfigGroup(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class ConfigBool {
        private BooleanValue value;
        private boolean fallback;

        private ConfigBool(boolean fallback) {
            this.fallback = fallback;
        }

        private void bind(BooleanValue value) {
            this.value = value;
        }

        public boolean get() {
            return value != null ? value.get() : fallback;
        }

        public void set(boolean value) {
            if (this.value != null) {
                this.value.set(value);
            }
            this.fallback = value;
        }
    }
}
#endif
