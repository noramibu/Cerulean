package toni.lib.config;

#if FABRIC
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class ConfigBase {
    public Object specification;
    private final List<ConfigBool> entries = new ArrayList<>();

    protected ConfigGroup group(int order, String name, String comment) {
        return new ConfigGroup(name);
    }

    protected ConfigBool b(boolean defaultValue, String name, String comment) {
        ConfigBool value = new ConfigBool(defaultValue, name, comment);
        entries.add(value);
        return value;
    }

    public void registerAll(Object builder) {
    }

    public void onLoad() {
    }

    public void onReload() {
    }

    public abstract String getName();

    public void loadOrCreateToml(Path path) {
        try {
            if (Files.exists(path)) {
                loadToml(path);
            }
            saveToml(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config " + path, e);
        }
    }

    private void loadToml(Path path) throws IOException {
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("[")) {
                continue;
            }

            int separator = trimmed.indexOf('=');
            if (separator < 0) {
                continue;
            }

            String key = trimmed.substring(0, separator).trim();
            String value = stripInlineComment(trimmed.substring(separator + 1)).trim().toLowerCase();

            for (ConfigBool entry : entries) {
                if (entry.name.equals(key)) {
                    if ("true".equals(value)) {
                        entry.set(true);
                    } else if ("false".equals(value)) {
                        entry.set(false);
                    }
                    break;
                }
            }
        }
    }

    private void saveToml(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("# Cerulean common config").append(System.lineSeparator()).append(System.lineSeparator());

        for (ConfigBool entry : entries) {
            if (entry.comment != null && !entry.comment.isBlank()) {
                builder.append("# ").append(entry.comment).append(System.lineSeparator());
            }
            builder.append(entry.name).append(" = ").append(entry.get()).append(System.lineSeparator()).append(System.lineSeparator());
        }

        Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
    }

    private static String stripInlineComment(String value) {
        int comment = value.indexOf('#');
        return comment < 0 ? value : value.substring(0, comment);
    }

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
        private final String name;
        private final String comment;
        private boolean value;

        private ConfigBool(boolean value, String name, String comment) {
            this.value = value;
            this.name = name;
            this.comment = comment;
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
