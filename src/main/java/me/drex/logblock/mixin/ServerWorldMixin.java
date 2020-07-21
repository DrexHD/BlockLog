package me.drex.logblock.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "onBlockChanged", at = @At(value = "HEAD"))
    private void onBlockChange(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
//        System.out.println(pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + BlockUtil.toName(oldBlock.getBlock()) + " -> " + BlockUtil.toName(newBlock.getBlock()));
    }

}
