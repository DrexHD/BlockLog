package me.drex.logblock.mixin;

import me.drex.logblock.BlockLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {

    @Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private void startMoveBlock(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockLog.getCache().addEntryAsync("-gravity", pos, world.getDimension(), Blocks.AIR.getDefaultState(), ((Block) (Object) this).getDefaultState(), System.currentTimeMillis(), false);
    }

    @Inject(method = "onLanding", at = @At(value = "HEAD"))
    private void endMoveBlock(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity, CallbackInfo ci) {
        BlockLog.getCache().addEntryAsync("-gravity", pos, world.getDimension(),  ((Block) (Object) this).getDefaultState(), Blocks.AIR.getDefaultState(), System.currentTimeMillis(), true);

    }

}
