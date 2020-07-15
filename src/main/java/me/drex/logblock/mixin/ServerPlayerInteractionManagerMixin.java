package me.drex.logblock.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
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
    public BlockPos blockPos;


    @Inject(method = "processBlockBreakingAction", at = @At(value = "HEAD"))
    private void getBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        this.block = this.world.getBlockState(pos).getBlock();
    }

    @Inject(method = "finishMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void remove(BlockPos pos, PlayerActionC2SPacket.Action action, String reason, CallbackInfo ci) {
        GameProfile profile = this.player.getGameProfile();
        DBUtil.createEntry(profile.getName(), profile.getId(), pos, this.world.getDimension(), BlockUtil.toName(block), false);
    }

    @Inject(method = "interactBlock", at = @At(value = "HEAD"))
    private void getBlockPos(ServerPlayerEntity serverPlayerEntity, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        blockPos = hitResult.getBlockPos().offset(hitResult.getSide());
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 2))
    private void place(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            GameProfile profile = this.player.getGameProfile();
            DBUtil.createEntry(profile.getName(), profile.getId(), hitResult.getBlockPos().offset(hitResult.getSide()), this.world.getDimension(), ItemUtil.toName(item), true);
        }
    }

}
