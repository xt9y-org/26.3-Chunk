package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import java.util.List;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerChunkCache.class)
public abstract class ChunkSpawningMixin {
    @Shadow @Final private ServerLevel level;

    /** Recalculate the hostile category per chunk; keep vanilla mob caps and periodic animal spawns. */
    @ModifyVariable(method = "tickSpawningChunk", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private List<MobCategory> chunkspawn$categories(List<MobCategory> original, LevelChunk chunk,
        List<MobCategory> categories, NaturalSpawner.SpawnState state) {
        Difficulty difficulty = SpawnRules.forLevel(level).at(level, chunk.getPos());
        if (difficulty == Difficulty.PEACEFUL) {
            return original.stream().filter(cat -> cat != MobCategory.MONSTER).toList();
        }
        if (original.contains(MobCategory.MONSTER)
            || !level.getGameRules().get(GameRules.SPAWN_MOBS)
            || !level.getGameRules().get(GameRules.SPAWN_MONSTERS)) return original;
        return NaturalSpawner.getFilteredSpawningCategories(state, true, level.getGameTime() % 400L == 0L);
    }
}
