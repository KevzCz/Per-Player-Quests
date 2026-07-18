package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.pixeldreamstudios.perplayerquests.api.IServerGlobalQuest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Quest.class, remap = false)
public abstract class ServerGlobalQuestExtension implements IServerGlobalQuest {

    @Unique
    private boolean ppq$serverGlobal = false;

    @Override
    public boolean ppq$isServerGlobal() {
        return ppq$serverGlobal;
    }

    @Override
    public void ppq$setServerGlobal(boolean global) {
        this.ppq$serverGlobal = global;
    }

    @Inject(method = "writeData", at = @At("TAIL"), remap = false)
    private void ppq$writeServerGlobal(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (ppq$serverGlobal) nbt.putBoolean("ppq_server_global", true);
    }

    @Inject(method = "readData", at = @At("TAIL"), remap = false)
    private void ppq$readServerGlobal(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        ppq$serverGlobal = nbt.getBoolean("ppq_server_global");
    }

    @Inject(method = "writeNetData", at = @At("TAIL"), remap = false)
    private void ppq$writeNetServerGlobal(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(ppq$serverGlobal);
    }

    @Inject(method = "readNetData", at = @At("TAIL"), remap = false)
    private void ppq$readNetServerGlobal(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        ppq$serverGlobal = buffer.readBoolean();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void ppq$addServerGlobalConfig(ConfigGroup config, CallbackInfo ci) {
        ConfigGroup group = config.getOrCreateSubgroup("ppq_server_global")
                .setNameKey("perplayerquests.config.group.server_global");
        group.addBool("enabled", ppq$serverGlobal, v -> ppq$serverGlobal = v, false)
                .setNameKey("perplayerquests.server_global.quest");
    }
}
