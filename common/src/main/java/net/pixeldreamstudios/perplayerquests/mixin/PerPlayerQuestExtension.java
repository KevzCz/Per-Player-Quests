package net.pixeldreamstudios.perplayerquests.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.pixeldreamstudios.perplayerquests.api.IPerPlayerObject;
import net.pixeldreamstudios.perplayerquests.api.IUniqueQuest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Quest.class, remap = false)
public abstract class PerPlayerQuestExtension implements IPerPlayerObject {

    @Unique
    private boolean mqt$perPlayer = false;
    @Unique
    private Collapse mqt$collapse = Collapse.ANY;
    @Unique
    private boolean mqt$showRosterTooltip = false;
    @Unique
    private ExclusivityScope mqt$exclusivityScope = ExclusivityScope.TEAM;

    @Override
    public boolean mqt$isPerPlayer() {
        if (mqt$perPlayer) return true;
        return this instanceof IUniqueQuest u && u.mqt$isUniqueGlobal();
    }

    @Override
    public void mqt$setPerPlayer(boolean perPlayer) {
        this.mqt$perPlayer = perPlayer;
    }

    @Override
    public Collapse mqt$getCollapse() {
        return mqt$collapse == null ? Collapse.ANY : mqt$collapse;
    }

    @Override
    public void mqt$setCollapse(Collapse collapse) {
        this.mqt$collapse = collapse == null ? Collapse.ANY : collapse;
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
    public ExclusivityScope mqt$getExclusivityScope() {
        return mqt$exclusivityScope == null ? ExclusivityScope.TEAM : mqt$exclusivityScope;
    }

    @Override
    public void mqt$setExclusivityScope(ExclusivityScope scope) {
        this.mqt$exclusivityScope = scope == null ? ExclusivityScope.TEAM : scope;
    }

    @Inject(method = "writeData", at = @At("TAIL"), remap = false)
    private void mqt$writePerPlayer(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (mqt$perPlayer) {
            nbt.putBoolean("mqt_per_player", true);
            nbt.putString("mqt_collapse", mqt$getCollapse().name());
            nbt.putBoolean("mqt_show_roster_tooltip", mqt$showRosterTooltip);
            nbt.putString("mqt_exclusivity_scope", mqt$getExclusivityScope().name());
        }
    }

    @Inject(method = "readData", at = @At("TAIL"), remap = false)
    private void mqt$readPerPlayer(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        mqt$perPlayer = nbt.getBoolean("mqt_per_player");
        Collapse missingTagFallback = mqt$perPlayer ? Collapse.ALL : Collapse.ANY;
        try {
            mqt$collapse = nbt.contains("mqt_collapse")
                    ? Collapse.valueOf(nbt.getString("mqt_collapse")) : missingTagFallback;
        } catch (Throwable ignored) { mqt$collapse = missingTagFallback; }
        mqt$showRosterTooltip = nbt.contains("mqt_show_roster_tooltip") && nbt.getBoolean("mqt_show_roster_tooltip");
        try {
            mqt$exclusivityScope = nbt.contains("mqt_exclusivity_scope")
                    ? ExclusivityScope.valueOf(nbt.getString("mqt_exclusivity_scope")) : ExclusivityScope.TEAM;
        } catch (Throwable ignored) { mqt$exclusivityScope = ExclusivityScope.TEAM; }
    }

    @Inject(method = "writeNetData", at = @At("TAIL"), remap = false)
    private void mqt$writeNetPerPlayer(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(mqt$perPlayer);
        buffer.writeEnum(mqt$getCollapse());
        buffer.writeBoolean(mqt$showRosterTooltip);
        buffer.writeEnum(mqt$getExclusivityScope());
    }

    @Inject(method = "readNetData", at = @At("TAIL"), remap = false)
    private void mqt$readNetPerPlayer(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        mqt$perPlayer = buffer.readBoolean();
        mqt$collapse = buffer.readEnum(Collapse.class);
        mqt$showRosterTooltip = buffer.readBoolean();
        mqt$exclusivityScope = buffer.readEnum(ExclusivityScope.class);
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void mqt$addPerPlayerConfig(ConfigGroup config, CallbackInfo ci) {
        ConfigGroup group = config.getOrCreateSubgroup("mqt_per_player")
                .setNameKey("perplayerquests.config.group.per_player");
        group.addBool("enabled", mqt$perPlayer, v -> mqt$perPlayer = v, false)
                .setNameKey("perplayerquests.per_player.quest");
        var COLLAPSE = NameMap.of(Collapse.ANY, Collapse.values()).create();
        group.addEnum("collapse", mqt$getCollapse(), v -> mqt$collapse = v, COLLAPSE)
                .setNameKey("perplayerquests.per_player.collapse");
        group.addBool("show_roster_tooltip", mqt$showRosterTooltip, v -> mqt$showRosterTooltip = v, false)
                .setNameKey("perplayerquests.per_player.show_roster_tooltip");
        var EXCLUSIVITY = NameMap.of(ExclusivityScope.TEAM, ExclusivityScope.values()).create();
        group.addEnum("exclusivity_scope", mqt$getExclusivityScope(), v -> mqt$exclusivityScope = v, EXCLUSIVITY)
                .setNameKey("perplayerquests.per_player.exclusivity_scope");
    }
}
