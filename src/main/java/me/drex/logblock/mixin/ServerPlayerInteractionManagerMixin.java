package me.drex.logblock.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.BlockLog;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.database.entry.HistoryEntry;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.HistoryColumn;
import me.drex.logblock.util.MessageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public ServerWorld world;

    public BlockState blockstate;
    public CompoundTag tag;


    @Inject(method = "processBlockBreakingAction", at = @At(value = "HEAD"))
    private void BlockLogger$getBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        this.blockstate = this.world.getBlockState(pos);
        tag = BlockUtil.getTagAt(this.world, pos);
    }

    @Inject(method = "finishMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void BlockLogger$destroyBlock(BlockPos pos, PlayerActionC2SPacket.Action action, String reason, CallbackInfo ci) {
        new HistoryEntry(this.player.getUuidAsString(), this.world.getDimension(), pos, blockstate, Blocks.AIR.getDefaultState(), BlockUtil.getTagAt(this.world, pos), tag, System.currentTimeMillis(), false).saveAsync();
    }


    @Inject(method = "processBlockBreakingAction", at = @At(value = "HEAD"))
    private void BlockLogger$clickBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (this.player.getMainHandStack().getItem() == Items.AIR && BlockLog.isInspecting(this.player.getUuid())) {
            CompletableFuture.runAsync(() -> {
                try {
                    String criteria = HistoryColumn.XPOS + " = " + pos.getX() + " AND " + HistoryColumn.YPOS + " = " + pos.getY() + " AND " + HistoryColumn.ZPOS + "= " + pos.getZ();
                    ResultSet resultSet = DBUtil.getDataWhere(criteria, false);
                    MessageUtil.send(this.player.getCommandSource(), resultSet, new LiteralText("(").formatted(Formatting.GRAY).append(new LiteralText(pos.getX() + " " + pos.getY() + " " + pos.getZ() + ")").formatted(Formatting.GRAY)));
                } catch (CommandSyntaxException | SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
