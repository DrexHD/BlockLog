package me.drex.logblock.mixin;

import me.drex.logblock.BlockLog;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final
    private Entity entity;

    @Redirect(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/explosion/Explosion;)V"))
    private void destroyBlock(Block block, World world, BlockPos pos, Explosion explosion) {
        BlockLog.getCache().executeEntry("-" + EntityUtil.toName(this.entity.getType()), pos, world.getDimension(), "minecraft:air", BlockUtil.toName(block), System.currentTimeMillis(), false);
        block.onDestroyedByExplosion(world, pos, (Explosion) (Object) this);
    }
}
