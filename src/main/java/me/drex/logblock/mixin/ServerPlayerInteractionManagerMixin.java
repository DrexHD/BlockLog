package me.drex.logblock.mixin;

import me.drex.logblock.BlockLog;
import me.drex.logblock.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public ServerWorld world;

    public Block block;


    @Inject(method = "processBlockBreakingAction", at = @At(value = "HEAD"))
    private void getBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        this.block = this.world.getBlockState(pos).getBlock();
    }

    @Inject(method = "finishMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void remove(BlockPos pos, PlayerActionC2SPacket.Action action, String reason, CallbackInfo ci) {
        BlockLog.getCache().executeEntry(this.player.getUuid().toString(), pos, this.world.getDimension(), "minecraft:air", BlockUtil.toName(block), System.currentTimeMillis(), false);
    }


    @Inject(method = "interactBlock", at = @At(value = "HEAD"))
    private void place(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
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
        }*/
    }

/*    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 2))
    private void onplace(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            GameProfile profile = this.player.getGameProfile();
            DBUtil.createEntryAsync(profile.getName(), profile.getId(), hitResult.getBlockPos().offset(hitResult.getSide()), this.world.getDimension(), ItemUtil.toName(item), true);
        }
    }*/
}
