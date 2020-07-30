package me.drex.logblock.mixin;

import me.drex.logblock.BlockLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {

    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private void onLeaveDecay(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockLog.getCache().addEntryAsync("-decay", pos, world.getDimension(), Blocks.AIR.getDefaultState(), world.getBlockState(pos), System.currentTimeMillis(), false);
    }

}
