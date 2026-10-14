package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobDespawnMixin {
    /** Keep a permitted hostile mob alive while its chunk permits spawning. */
    @Redirect(method = "checkDespawn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    private Difficulty chunkspawn$despawnDifficulty(Level level) {
        Mob self = (Mob) (Object) this;
        return level instanceof ServerLevel server
            ? SpawnRules.forLevel(server).at(server, self.blockPosition()) : level.getDifficulty();
    }
}
