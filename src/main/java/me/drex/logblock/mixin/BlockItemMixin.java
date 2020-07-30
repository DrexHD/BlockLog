package me.drex.logblock.mixin;

import me.drex.logblock.BlockLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Shadow @Final @Deprecated private Block block;
    BlockState blockState;

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "HEAD"))
    private void getBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        blockState = context.getWorld().getBlockState(context.getBlockPos());
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void placeBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {

        blockState.getProperties().forEach(property -> {
            property.getName();
            Class<?> clazz = property.getType();
            clazz.getName();

        });

/*        Gson gson = new Gson();
        gson.toJson(blockState);
        System.out.println(gson.toString());*/
        BlockLog.getCache().addEntryAsync(context.getPlayer().getUuidAsString(), context.getBlockPos(), context.getWorld().getDimension(), context.getWorld().getBlockState(context.getBlockPos()), blockState, System.currentTimeMillis(), true);
/*        try {
            Item item = stack.getItem();
            UUID uuid = this.player.getUuid();
            BlockPos hitPos = hitResult.getBlockPos();
            if (item instanceof BlockItem) {
                Block block = this.world.getBlockState(hitPos).getBlock();
                BlockPos pos = hitPos;
                if (!block.canMobSpawnInside() || block instanceof FlowerBlock) {
                    pos = pos.offset(hitResult.getSide());
                }
                LogBlockMod.getCache().addEntry(uuid.toString(), pos, this.world.getDimension(), ItemUtil.toName(item), BlockUtil.toName(this.world.getBlockState(pos).getBlock()), System.currentTimeMillis(), true);
            } else if (item == Items.AIR && LogBlockMod.isInspecting(uuid)) {
                String criteria = "x = " + hitPos.getX() + " AND " + "y BETWEEN " + 0 + " AND " + 256 + " AND " + "z = " + hitPos.getZ();
                ResultSet resultSet = DBUtil.getDataWhere(criteria);
                MessageUtil.send(this.player.getCommandSource(), resultSet, new LiteralText("Block history at ").formatted(Formatting.GRAY).append(new LiteralText(hitPos.getX() + " " + hitPos.getZ() + " " + hitPos.getZ()).formatted(Formatting.WHITE)));
            }
        } catch (SQLException | CommandSyntaxException e) {
            e.printStackTrace();
        }   */
    }

}
