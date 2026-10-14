package dev.chunkspawn.mixin;

import dev.chunkspawn.SpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Permit eggs only where the actual placement position is allowed. */
@Mixin(SpawnEggItem.class)
public abstract class SpawnEggMixin {
    @Redirect(method = "useOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/EntityType;canSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private boolean chunkspawn$onBlock(EntityType<?> type, Level level, UseOnContext context) {
        if (type.canSpawn(level)) return true;
        if (!(level instanceof ServerLevel server)) return level.isClientSide();
        BlockPos clicked = context.getClickedPos();
        BlockPos destination = level.getBlockEntity(clicked) instanceof net.minecraft.world.level.Spawner
            || level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty()
            ? clicked : clicked.relative(context.getClickedFace());
        return type.isEnabled(level.enabledFeatures())
            && SpawnRules.forLevel(server).at(server, destination) != Difficulty.PEACEFUL;
    }

    @Redirect(method = "use", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/EntityType;canSpawn(Lnet/minecraft/world/level/Level;)Z"))
    private boolean chunkspawn$onLiquid(EntityType<?> type, Level target, Level level,
        Player player, InteractionHand hand) {
        if (type.canSpawn(target)) return true;
        if (!(level instanceof ServerLevel server)) return level.isClientSide();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
            ClipContext.Fluid.SOURCE_ONLY, player));
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
            && type.isEnabled(level.enabledFeatures())
            && SpawnRules.forLevel(server).at(server, hit.getBlockPos()) != Difficulty.PEACEFUL;
    }
}
