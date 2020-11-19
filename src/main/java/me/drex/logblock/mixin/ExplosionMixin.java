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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private List<BlockPos> affectedBlocks;

    @Shadow @Final private Entity entity;

    @Shadow @Final private World world;

    @Inject(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;affectedBlocks:Ljava/util/List;"))
    private void onExplosion(boolean bl, CallbackInfo ci) {
        this.affectedBlocks.forEach(pos -> {
            BlockState blockState = this.world.getBlockState(pos);
            BlockLog.getCache().addEntryAsync("-" + EntityUtil.toName(this.entity.getType()), pos, this.world.getDimension(), Blocks.AIR.getDefaultState(), blockState, System.currentTimeMillis(), false);
        });
    }


}
