package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.api.PerPlayerMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Task.class, remap = false)
public abstract class PerPlayerTaskExtension implements IPerPlayerObject {

    @Unique
    private PerPlayerMode mqt$mode = PerPlayerMode.INHERIT;
    @Unique
    private boolean mqt$showRosterTooltip = false;

    @Unique
    private PerPlayerMode mqt$mode() {
        return mqt$mode == null ? PerPlayerMode.INHERIT : mqt$mode;
    }

    @Override
    public boolean mqt$showRosterTooltip() {
        return mqt$showRosterTooltip;
    }

    @Override
    public void mqt$setShowRosterTooltip(boolean show) {
        this.mqt$showRosterTooltip = show;
    }

    @Override
    public boolean mqt$isPerPlayer() {
        switch (mqt$mode()) {
            case FORCE_ON:  return true;
            case FORCE_OFF: return false;
            default:
                Task self = (Task) (Object) this;
                Object quest = self.getQuest();
                return quest instanceof IPerPlayerObject q && q.mqt$isPerPlayer();
        }
    }

    @Override
    public void mqt$setPerPlayer(boolean perPlayer) {
        this.mqt$mode = perPlayer ? PerPlayerMode.FORCE_ON : PerPlayerMode.FORCE_OFF;
    }

    @Inject(method = "writeData", at = @At("TAIL"), remap = false)
    private void mqt$writePerPlayer(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (mqt$mode() != PerPlayerMode.INHERIT) {
            nbt.putString("mqt_per_player_mode", mqt$mode().name());
        }
        if (mqt$showRosterTooltip) {
            nbt.putBoolean("mqt_show_roster_tooltip", true);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"), remap = false)
    private void mqt$readPerPlayer(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (nbt.contains("mqt_per_player") && nbt.getBoolean("mqt_per_player")) {
            mqt$mode = PerPlayerMode.FORCE_ON;
        } else {
            try {
                mqt$mode = nbt.contains("mqt_per_player_mode")
                        ? PerPlayerMode.valueOf(nbt.getString("mqt_per_player_mode")) : PerPlayerMode.INHERIT;
            } catch (Throwable ignored) {
                mqt$mode = PerPlayerMode.INHERIT;
            }
        }
        mqt$showRosterTooltip = nbt.contains("mqt_show_roster_tooltip") && nbt.getBoolean("mqt_show_roster_tooltip");
    }

    @Inject(method = "writeNetData", at = @At("TAIL"), remap = false)
    private void mqt$writeNetPerPlayer(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeEnum(mqt$mode());
        buffer.writeBoolean(mqt$showRosterTooltip);
    }

    @Inject(method = "readNetData", at = @At("TAIL"), remap = false)
    private void mqt$readNetPerPlayer(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        mqt$mode = buffer.readEnum(PerPlayerMode.class);
        mqt$showRosterTooltip = buffer.readBoolean();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void mqt$addPerPlayerConfig(ConfigGroup config, CallbackInfo ci) {
        ConfigGroup group = config.getOrCreateSubgroup("mqt_per_player")
                .setNameKey("perplayerquests.config.group.per_player");
        NameMap<PerPlayerMode> MODES = NameMap.of(PerPlayerMode.INHERIT, PerPlayerMode.values()).create();
        group.addEnum("mode", mqt$mode(), v -> mqt$mode = v, MODES)
                .setNameKey("perplayerquests.per_player.task_mode");
        group.addBool("show_roster_tooltip", mqt$showRosterTooltip, v -> mqt$showRosterTooltip = v, false)
                .setNameKey("perplayerquests.per_player.show_roster_tooltip");
    }
}
