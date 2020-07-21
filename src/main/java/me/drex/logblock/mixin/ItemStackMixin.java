/*
package me.drex.logblock.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.logblock.LogBlockMod;
import me.drex.logblock.database.DBUtil;
import me.drex.logblock.util.BlockUtil;
import me.drex.logblock.util.ItemUtil;
import me.drex.logblock.util.MessageUtil;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerBlock;
import net.minecraft.item.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    Block block;

    @Inject(method = "useOnBlock", at = @At(value = "HEAD"))
    private void getBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        Item item = this.getItem();
        UUID uuid = context.getPlayer().getUuid();
        BlockPos hitPos = context.getBlockPos();
        if (item == Items.AIR && LogBlockMod.isInspecting(uuid)) {
            String criteria = "x = " + hitPos.getX() + " AND " + "y BETWEEN " + 0 + " AND " + 256 + " AND " + "z = " + hitPos.getZ();
            try {
                ResultSet resultSet = DBUtil.getDataWhere(criteria);
                MessageUtil.send(context.getPlayer().getCommandSource(), resultSet, new LiteralText("Block history at ").formatted(Formatting.GRAY).append(new LiteralText(hitPos.getX() + " " + hitPos.getZ() + " " + hitPos.getZ()).formatted(Formatting.WHITE)));
            } catch (SQLException | CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"))
    private void placeBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        Item item = this.getItem();
        UUID uuid = context.getPlayer().getUuid();
        BlockPos hitPos = context.getBlockPos();
        if (item instanceof BlockItem) {
            Block block = context.getWorld().getBlockState(hitPos).getBlock();
            BlockPos pos = hitPos;
            if (!block.canMobSpawnInside() || block instanceof FlowerBlock) {
                pos = pos.offset(context.getSide());
            }
            LogBlockMod.getCache().addEntry(uuid.toString(), pos, context.getWorld().getDimension(), ItemUtil.toName(item), BlockUtil.toName(block), System.currentTimeMillis(), true);
        }
    }

}
*/
