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
public abstract class UniqueQuestExtension implements IUniqueQuest {

    @Unique
    private boolean mqt$uniqueGlobal = false;
    @Unique
    private UniqueLock mqt$uniqueLock = UniqueLock.ON_COMPLETE;
    @Unique
    private UniqueView mqt$uniqueView = UniqueView.MARKED_CLAIMED;
    @Unique
    private boolean mqt$announceClaim = false;
    @Unique
    private ClaimantDisplay mqt$claimantDisplay = ClaimantDisplay.NAME;
    @Unique
    private UniqueInherit mqt$uniqueInherit = UniqueInherit.NONE;

    @Override public boolean mqt$isUniqueGlobal() { return mqt$uniqueGlobal; }
    @Override public void mqt$setUniqueGlobal(boolean unique) { this.mqt$uniqueGlobal = unique; }
    @Override public UniqueInherit mqt$getUniqueInherit() { return mqt$uniqueInherit == null ? UniqueInherit.NONE : mqt$uniqueInherit; }
    @Override public void mqt$setUniqueInherit(UniqueInherit inherit) { this.mqt$uniqueInherit = inherit == null ? UniqueInherit.NONE : inherit; }
    @Override public UniqueLock mqt$getUniqueLock() { return mqt$uniqueLock == null ? UniqueLock.ON_COMPLETE : mqt$uniqueLock; }
    @Override public void mqt$setUniqueLock(UniqueLock lock) { this.mqt$uniqueLock = lock == null ? UniqueLock.ON_COMPLETE : lock; }
    @Override public UniqueView mqt$getUniqueView() { return mqt$uniqueView == null ? UniqueView.MARKED_CLAIMED : mqt$uniqueView; }
    @Override public void mqt$setUniqueView(UniqueView view) { this.mqt$uniqueView = view == null ? UniqueView.MARKED_CLAIMED : view; }
    @Override public boolean mqt$announceClaim() { return mqt$announceClaim; }
    @Override public void mqt$setAnnounceClaim(boolean announce) { this.mqt$announceClaim = announce; }
    @Override public ClaimantDisplay mqt$getClaimantDisplay() { return mqt$claimantDisplay == null ? ClaimantDisplay.NAME : mqt$claimantDisplay; }
    @Override public void mqt$setClaimantDisplay(ClaimantDisplay display) { this.mqt$claimantDisplay = display == null ? ClaimantDisplay.NAME : display; }

    @Inject(method = "writeData", at = @At("TAIL"), remap = false)
    private void mqt$writeUnique(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (mqt$uniqueGlobal) {
            nbt.putBoolean("mqt_unique_global", true);
            nbt.putString("mqt_unique_lock", mqt$getUniqueLock().name());
            nbt.putString("mqt_unique_view", mqt$getUniqueView().name());
            if (mqt$announceClaim) nbt.putBoolean("mqt_unique_announce", true);
            if (mqt$getClaimantDisplay() != ClaimantDisplay.NAME) {
                nbt.putString("mqt_unique_claimant_display", mqt$getClaimantDisplay().name());
            }
        }
        if (mqt$getUniqueInherit() != UniqueInherit.NONE) {
            nbt.putString("mqt_unique_inherit", mqt$getUniqueInherit().name());
        }
    }

    @Inject(method = "readData", at = @At("TAIL"), remap = false)
    private void mqt$readUnique(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        mqt$uniqueGlobal = nbt.getBoolean("mqt_unique_global");
        try {
            mqt$uniqueLock = nbt.contains("mqt_unique_lock")
                    ? UniqueLock.valueOf(nbt.getString("mqt_unique_lock")) : UniqueLock.ON_COMPLETE;
        } catch (Throwable ignored) { mqt$uniqueLock = UniqueLock.ON_COMPLETE; }
        try {
            mqt$uniqueView = nbt.contains("mqt_unique_view")
                    ? UniqueView.valueOf(nbt.getString("mqt_unique_view")) : UniqueView.MARKED_CLAIMED;
        } catch (Throwable ignored) { mqt$uniqueView = UniqueView.MARKED_CLAIMED; }
        mqt$announceClaim = nbt.getBoolean("mqt_unique_announce");
        try {
            mqt$claimantDisplay = nbt.contains("mqt_unique_claimant_display")
                    ? ClaimantDisplay.valueOf(nbt.getString("mqt_unique_claimant_display")) : ClaimantDisplay.NAME;
        } catch (Throwable ignored) { mqt$claimantDisplay = ClaimantDisplay.NAME; }
        try {
            mqt$uniqueInherit = nbt.contains("mqt_unique_inherit")
                    ? UniqueInherit.valueOf(nbt.getString("mqt_unique_inherit")) : UniqueInherit.NONE;
        } catch (Throwable ignored) { mqt$uniqueInherit = UniqueInherit.NONE; }
    }

    @Inject(method = "writeNetData", at = @At("TAIL"), remap = false)
    private void mqt$writeNetUnique(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(mqt$uniqueGlobal);
        buffer.writeEnum(mqt$getUniqueLock());
        buffer.writeEnum(mqt$getUniqueView());
        buffer.writeBoolean(mqt$announceClaim);
        buffer.writeEnum(mqt$getClaimantDisplay());
        buffer.writeEnum(mqt$getUniqueInherit());
    }

    @Inject(method = "readNetData", at = @At("TAIL"), remap = false)
    private void mqt$readNetUnique(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        mqt$uniqueGlobal = buffer.readBoolean();
        mqt$uniqueLock = buffer.readEnum(UniqueLock.class);
        mqt$uniqueView = buffer.readEnum(UniqueView.class);
        mqt$announceClaim = buffer.readBoolean();
        mqt$claimantDisplay = buffer.readEnum(ClaimantDisplay.class);
        mqt$uniqueInherit = buffer.readEnum(UniqueInherit.class);
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void mqt$addUniqueConfig(ConfigGroup config, CallbackInfo ci) {
        ConfigGroup group = config.getOrCreateSubgroup("mqt_unique")
                .setNameKey("perplayerquests.config.group.unique");
        group.addBool("global", mqt$uniqueGlobal, v -> {
            mqt$uniqueGlobal = v;
            if (v && this instanceof IPerPlayerObject ppo) ppo.mqt$setPerPlayer(true);
        }, false).setNameKey("perplayerquests.unique.global");
        var LOCKS = NameMap.of(UniqueLock.ON_COMPLETE, UniqueLock.values()).create();
        group.addEnum("lock", mqt$getUniqueLock(), v -> mqt$uniqueLock = v, LOCKS)
                .setNameKey("perplayerquests.unique.lock");
        var VIEWS = NameMap.of(UniqueView.MARKED_CLAIMED, UniqueView.values()).create();
        group.addEnum("view", mqt$getUniqueView(), v -> mqt$uniqueView = v, VIEWS)
                .setNameKey("perplayerquests.unique.view");
        group.addBool("announce", mqt$announceClaim, v -> mqt$announceClaim = v, false)
                .setNameKey("perplayerquests.unique.announce");
        var DISPLAYS = NameMap.of(ClaimantDisplay.NAME, ClaimantDisplay.values()).create();
        group.addEnum("claimant_display", mqt$getClaimantDisplay(), v -> mqt$claimantDisplay = v, DISPLAYS)
                .setNameKey("perplayerquests.unique.claimant_display");
        var INHERITS = NameMap.of(UniqueInherit.NONE, UniqueInherit.values()).create();
        group.addEnum("inherit", mqt$getUniqueInherit(), v -> mqt$uniqueInherit = v, INHERITS)
                .setNameKey("perplayerquests.unique.inherit");
    }
}
