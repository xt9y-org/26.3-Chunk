package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * The private saved-entity loader calls through lambdas. Rather than targeting
 * its internal factory call, replace the LOAD request argument at method entry.
 * This bypasses Peaceful creation checks only for saved positions in allowed
 * chunks, keeping all other loads subject to vanilla checks.
 */
@Mixin(EntityType.class)
public abstract class SavedEntityLoadingMixin {
    @ModifyVariable(
        method = "loadStaticEntity(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Ljava/util/Optional;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private static EntitySpawnRequest chunkspawn$loadInAllowedChunk(
        EntitySpawnRequest request, ValueInput data, Level level, EntitySpawnRequest original) {
        if (request.reason() != EntitySpawnReason.LOAD || request.ignoreChecks()
            || !(level instanceof ServerLevel server) || level.getDifficulty() != Difficulty.PEACEFUL) {
            return request;
        }

        return data.read("Pos", Vec3.CODEC)
            .filter(pos -> SpawnRules.forLevel(server).at(server, BlockPos.containing(pos)) != Difficulty.PEACEFUL)
            .map(pos -> new EntitySpawnRequest(request.reason(), true))
            .orElse(request);
    }
}
