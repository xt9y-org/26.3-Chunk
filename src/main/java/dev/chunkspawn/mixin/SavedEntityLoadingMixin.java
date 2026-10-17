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
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Vanilla refuses to create most monsters from saved NBT while the world is
 * Peaceful. Read the saved position before creation, and bypass only that
 * difficulty gate for entities saved inside allowed chunks.
 *
 * No global changes to EntityType.isAllowedInPeaceful or to spawn eggs.
 */
@Mixin(EntityType.class)
public abstract class SavedEntityLoadingMixin {
    @ModifyArg(
        method = "loadStaticEntity(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Ljava/util/Optional;",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Lnet/minecraft/world/entity/Entity;"),
        index = 1
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
