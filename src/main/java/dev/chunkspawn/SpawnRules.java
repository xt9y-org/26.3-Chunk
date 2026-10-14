package dev.chunkspawn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;

/** Per-dimension world data, loaded lazily, flushed atomically on command changes. */
public final class SpawnRules {
    private static final Map<ServerLevel, SpawnRules> CACHE = new WeakHashMap<>();
    private final Path path;
    private Difficulty fallback;
    private final Map<Long, Difficulty> chunks = new HashMap<>();

    private SpawnRules(Path path) {
        this.path = path;
        if (!Files.isRegularFile(path)) return;
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            if (properties.containsKey("all")) fallback = parse(properties.getProperty("all"));
            for (String name : properties.stringPropertyNames()) {
                if (!name.startsWith("chunk.")) continue;
                String[] coords = name.split("\\.");
                if (coords.length != 3) continue;
                int x = Integer.parseInt(coords[1]);
                int z = Integer.parseInt(coords[2]);
                chunks.put(ChunkPos.pack(x, z), parse(properties.getProperty(name)));
            }
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("Cannot load ChunkSpawn data: " + path, error);
        }
    }

    public static SpawnRules forLevel(ServerLevel level) {
        return CACHE.computeIfAbsent(level, unused -> {
            String dimension = level.dimension().identifier().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path path = level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("chunkspawn").resolve(dimension + ".properties");
            return new SpawnRules(path);
        });
    }

    public Difficulty at(ServerLevel level, ChunkPos pos) {
        return chunks.getOrDefault(pos.pack(), fallback != null ? fallback : level.getDifficulty());
    }

    public Difficulty at(ServerLevel level, BlockPos pos) {
        return at(level, ChunkPos.containing(pos));
    }

    /** Whether the dimension has any potentially permitted hostile spawning area. */
    public boolean hasAllowedChunks(ServerLevel level) {
        if ((fallback == null ? level.getDifficulty() : fallback) != Difficulty.PEACEFUL) return true;
        return chunks.containsValue(Difficulty.EASY) || chunks.containsValue(Difficulty.NORMAL)
            || chunks.containsValue(Difficulty.HARD);
    }

    public Difficulty defaultDifficulty() {
        return fallback;
    }

    public Map<Long, Difficulty> overrides() {
        return Collections.unmodifiableMap(chunks);
    }

    public void setChunk(ChunkPos pos, Difficulty difficulty) throws IOException {
        Map<Long, Difficulty> updated = new HashMap<>(chunks);
        updated.put(pos.pack(), difficulty);
        persist(fallback, updated);
        chunks.clear();
        chunks.putAll(updated);
    }

    public void setAll(Difficulty difficulty) throws IOException {
        persist(difficulty, Map.of());
        chunks.clear();
        fallback = difficulty;
    }

    private void persist(Difficulty defaultDifficulty, Map<Long, Difficulty> data) throws IOException {
        Files.createDirectories(path.getParent());
        Properties properties = new Properties();
        if (defaultDifficulty != null) properties.setProperty("all", defaultDifficulty.getSerializedName());
        for (Map.Entry<Long, Difficulty> entry : data.entrySet()) {
            ChunkPos pos = ChunkPos.unpack(entry.getKey());
            properties.setProperty("chunk." + pos.x() + "." + pos.z(), entry.getValue().getSerializedName());
        }
        Path tmp = Files.createTempFile(path.getParent(), "chunkspawn-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(tmp)) {
                properties.store(out, "ChunkSpawn: per-dimension mob spawning difficulty");
            }
            try {
                Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private static Difficulty parse(String value) {
        return Difficulty.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
