package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ServerLevel.class)
public abstract class LocalDifficultyMixin {
    /** Preserve original time/moon arguments and override only local difficulty. */
    @ModifyArgs(method = "getCurrentDifficultyAt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DifficultyInstance;<init>(Lnet/minecraft/world/Difficulty;JJF)V"))
    private void chunkspawn$localDifficulty(Args args, BlockPos pos) {
        ServerLevel self = (ServerLevel) (Object) this;
        Difficulty effective = SpawnRules.forLevel(self).at(self, pos);
        args.set(0, effective);
    }
}
