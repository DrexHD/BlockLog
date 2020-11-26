package me.drex.logblock.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class WorldMixin {

/*    @Inject(method = "onBlockChanged", at = @At(value = "HEAD"))
    private void onBlockChange(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
      //System.out.println(pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + BlockUtil.toName(oldBlock.getBlock()) + " -> " + BlockUtil.toName(newBlock.getBlock()));
    }*/

}
