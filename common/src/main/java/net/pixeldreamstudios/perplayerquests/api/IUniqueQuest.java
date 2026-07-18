package net.pixeldreamstudios.perplayerquests.api;

public interface IUniqueQuest {
    enum UniqueLock { ON_START, ON_COMPLETE }

    enum UniqueView { MARKED_CLAIMED, HIDDEN_FOR_OTHERS }

    enum ClaimantDisplay { NAME, ANONYMOUS }

    enum UniqueInherit { NONE, FROM_DEPENDENCY }

    boolean mqt$isUniqueGlobal();

    void mqt$setUniqueGlobal(boolean unique);

    UniqueInherit mqt$getUniqueInherit();

    void mqt$setUniqueInherit(UniqueInherit inherit);

    UniqueLock mqt$getUniqueLock();

    void mqt$setUniqueLock(UniqueLock lock);

    UniqueView mqt$getUniqueView();

    void mqt$setUniqueView(UniqueView view);

    boolean mqt$announceClaim();

    void mqt$setAnnounceClaim(boolean announce);

    ClaimantDisplay mqt$getClaimantDisplay();

    void mqt$setClaimantDisplay(ClaimantDisplay display);
}
