package me.drex.logblock.mixin;

import com.mojang.brigadier.CommandDispatcher;
import me.drex.logblock.commands.Manager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Shadow
    private CommandDispatcher<ServerCommandSource> dispatcher;
    @Inject(method = "<init>", at = @At("RETURN"))
    public void addCommand(net.minecraft.server.command.CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
        Manager.dispatcher = this.dispatcher;
        Manager.register();
    }
}
