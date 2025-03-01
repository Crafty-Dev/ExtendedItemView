package de.crafty.eiv.mixin.server;

import com.mojang.datafixers.DataFixer;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.*;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements ServerInfo, ChunkIOErrorReporter, CommandSource {

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Inject(method = "setPlayerList", at = @At("RETURN"))
    private void setServer(PlayerList playerList, CallbackInfo ci) {
        ServerRecipeManager.INSTANCE.setServer((MinecraftServer) (Object) this);
    }
}
