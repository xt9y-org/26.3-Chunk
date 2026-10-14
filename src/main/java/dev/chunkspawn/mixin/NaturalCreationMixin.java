package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A monster's natural-spawn constructor is normally rejected globally on Peaceful. */
@Mixin(NaturalSpawner.class)
public abstract class NaturalCreationMixin {
    @Inject(method = "getMobForSpawn", at = @At("HEAD"), cancellable = true)
    private static void chunkspawn$create(ServerLevel level, EntityType<?> type,
        CallbackInfoReturnable<Mob> result) {
        if (level.getDifficulty() != Difficulty.PEACEFUL || type.isAllowedInPeaceful()
            || type.getCategory() != MobCategory.MONSTER
            || !SpawnRules.forLevel(level).hasAllowedChunks(level)) return;
        Entity entity = type.isEnabled(level.enabledFeatures())
            ? type.create(level, new EntitySpawnRequest(EntitySpawnReason.NATURAL, true)) : null;
        result.setReturnValue(entity instanceof Mob mob ? mob : null);
    }
}
