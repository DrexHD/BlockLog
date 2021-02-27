package me.drex.logblock.mixin;

import me.drex.logblock.database.entry.HistoryEntry;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
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
import java.util.UUID;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private List<BlockPos> affectedBlocks;

    @Shadow
    @Final
    private Entity entity;

    @Shadow
    @Final
    private World world;

    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void onExplosion(boolean bl, CallbackInfo ci) {
        if (this.affectedBlocks != null) {
            for (BlockPos pos : this.affectedBlocks) {
                BlockState blockState = this.world.getBlockState(pos);
                if (blockState.getBlock() != Blocks.AIR)
                    new HistoryEntry("-" + (this.entity != null ? EntityUtil.toName(this.entity.getType()) : "unknown"), this.world.getDimension(), pos, blockState, Blocks.AIR.getDefaultState(), BlockUtil.getTagAt(world, pos), new CompoundTag(), System.currentTimeMillis(), false).saveAsync();
            }
        }
    }


}
