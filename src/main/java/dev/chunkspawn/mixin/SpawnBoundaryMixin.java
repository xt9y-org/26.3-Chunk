package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class SpawnBoundaryMixin {
    /** Spawning groups can cross chunk boundaries; validate the candidate position. */
    @Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
    private static void chunkspawn$boundary(ServerLevel level, MobCategory category,
        StructureManager structures, ChunkGenerator generator, MobSpawnSettings.SpawnerData data,
        BlockPos.MutableBlockPos pos, double nearestPlayerDistanceSqr, CallbackInfoReturnable<Boolean> result) {
        if (category == MobCategory.MONSTER && SpawnRules.forLevel(level).at(level, pos) == Difficulty.PEACEFUL) {
            result.setReturnValue(false);
        }
    }
}
