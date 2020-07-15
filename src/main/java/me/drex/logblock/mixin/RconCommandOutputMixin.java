package me.drex.logblock.mixin;

import me.drex.logblock.LogBlockMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.rcon.RconCommandOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RconCommandOutput.class)
public class RconCommandOutputMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void saveInstance(MinecraftServer server, CallbackInfo ci) {

        LogBlockMod.server = server;
    }
}
