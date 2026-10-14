package dev.chunkspawn;

import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;

/** Server-only commands; the vanilla world difficulty is never modified. */
public final class ChunkSpawn implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("chunkspawn").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Difficulty difficulty : Difficulty.values()) {
            String name = difficulty.getSerializedName();
            root.then(Commands.literal(name).executes(ctx -> setChunk(ctx.getSource(), difficulty)));
            root.then(Commands.literal("all")
                .then(Commands.literal(name).executes(ctx -> setAll(ctx.getSource(), difficulty))));
        }
        root.then(Commands.literal("list").executes(ctx -> list(ctx.getSource())));
        dispatcher.register(root);
    }

    private static int setChunk(CommandSourceStack source, Difficulty difficulty) {
        ServerLevel level = source.getLevel();
        ChunkPos pos = atSource(source);
        SpawnRules rules = SpawnRules.forLevel(level);
        try {
            rules.setChunk(pos, difficulty);
        } catch (IOException e) {
            source.sendFailure(Component.literal("ChunkSpawn: unable to save settings: " + e.getMessage()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Chunk (" + pos.x() + ", " + pos.z() + ") now "
            + difficulty.getSerializedName() + " for mob spawning [" + level.dimension().identifier() + "]"), true);
        return 1;
    }

    private static int setAll(CommandSourceStack source, Difficulty difficulty) {
        ServerLevel level = source.getLevel();
        try {
            SpawnRules.forLevel(level).setAll(difficulty);
        } catch (IOException e) {
            source.sendFailure(Component.literal("ChunkSpawn: unable to save settings: " + e.getMessage()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("All chunks in " + level.dimension().identifier()
            + " now use " + difficulty.getSerializedName()
            + " for mob spawning; previous chunk overrides cleared."), true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        SpawnRules rules = SpawnRules.forLevel(level);
        String fallback = rules.defaultDifficulty() == null ? "vanilla (" + level.getDifficulty().getSerializedName() + ")"
            : rules.defaultDifficulty().getSerializedName();
        source.sendSuccess(() -> Component.literal("ChunkSpawn [" + level.dimension().identifier()
            + "]: default " + fallback + ", overrides " + rules.overrides().size()), false);
        rules.overrides().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                ChunkPos pos = ChunkPos.unpack(entry.getKey());
                source.sendSuccess(() -> Component.literal("  (" + pos.x() + ", " + pos.z() + ") "
                    + entry.getValue().getSerializedName()), false);
            });
        return rules.overrides().size();
    }

    private static ChunkPos atSource(CommandSourceStack source) {
        return new ChunkPos(Math.floorDiv((int) Math.floor(source.getPosition().x), 16),
            Math.floorDiv((int) Math.floor(source.getPosition().z), 16));
    }
}
