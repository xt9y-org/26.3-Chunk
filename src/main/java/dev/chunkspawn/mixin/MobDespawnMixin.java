package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent Peaceful removal AND vanilla distance/random despawn only inside
 * allowed chunks. Do not mark persistenceRequired: vanilla mob caps must still
 * count these monsters so long-running farms cannot spawn unlimited monsters.
 *
 * Chunks remain normally unloadable, and mobs are saved through vanilla
 * entity-region serialization.
 */
@Mixin(Mob.class)
public abstract class MobDespawnMixin {
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void chunkspawn$keepInAllowedChunk(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.getType().getCategory() != MobCategory.MONSTER
            || !(mob.level() instanceof ServerLevel level)
            || SpawnRules.forLevel(level).at(level, mob.blockPosition()) == Difficulty.PEACEFUL) {
            return;
        }

        ci.cancel();
    }
}
