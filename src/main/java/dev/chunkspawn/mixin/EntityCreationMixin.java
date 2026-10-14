package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PostSpawnProcessor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The vanilla EntityType.create checks global difficulty even after a spawn egg passes. */
@Mixin(EntityType.class)
public abstract class EntityCreationMixin {
    @Redirect(method = "create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PostSpawnProcessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"))
    private Entity chunkspawn$create(EntityType<?> type, Level level, EntitySpawnReason reason,
        ServerLevel server, PostSpawnProcessor<?> config, BlockPos pos,
        EntitySpawnReason spawnReason, boolean moveDown, boolean movedUp) {
        if (server.getDifficulty() == Difficulty.PEACEFUL
            && SpawnRules.forLevel(server).at(server, pos) != Difficulty.PEACEFUL
            && !type.isAllowedInPeaceful()) {
            if (!type.isEnabled(server.enabledFeatures())) return null;
            return type.create(level, new EntitySpawnRequest(reason, true));
        }
        return type.create(level, reason);
    }
}
