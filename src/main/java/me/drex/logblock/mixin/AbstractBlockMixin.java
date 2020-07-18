package me.drex.logblock.mixin;


import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

/*    @Inject(method = "onBlockAdded", at = @At(value = "HEAD"))
    private void onBlockUse(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        world.setBlockState(pos.up().up(), Blocks.DIAMOND_BLOCK.getDefaultState());
    }*/

}
