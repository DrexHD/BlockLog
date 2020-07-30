package me.drex.logblock.mixin;

import me.drex.logblock.BlockLog;
import me.drex.logblock.util.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

    @Shadow @Final private World world;


    @Redirect(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 0))
    private BlockState onBlockExplosion(World world, BlockPos pos) {
        BlockState blockState = this.world.getBlockState(pos);
        BlockLog.getCache().addEntryAsync("-" + EntityUtil.toName(this.entity.getType()), pos, world.getDimension(), Blocks.AIR.getDefaultState(), blockState, System.currentTimeMillis(), false);
        return blockState;
    }


}
