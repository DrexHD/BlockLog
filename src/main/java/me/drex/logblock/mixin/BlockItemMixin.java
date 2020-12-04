package me.drex.logblock.mixin;

import me.drex.logblock.database.entry.HistoryEntry;
import me.drex.logblock.util.BlockUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    BlockState blockState;
    CompoundTag tag;

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "HEAD"))
    private void BlockLog$getBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        blockState = context.getWorld().getBlockState(context.getBlockPos());
        tag = BlockUtil.getTagAt(context.getWorld(), context.getBlockPos());

    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void BlockLog$placeBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getPlayer() != null) {
            new HistoryEntry(context.getPlayer().getUuidAsString(), context.getWorld().getDimension(), context.getBlockPos(), blockState, context.getWorld().getBlockState(context.getBlockPos()), tag, BlockUtil.getTagAt(context.getWorld(), context.getBlockPos()), System.currentTimeMillis(), true).saveAsync();
        }
    }

}
