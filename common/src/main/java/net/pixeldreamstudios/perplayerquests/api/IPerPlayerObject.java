package net.pixeldreamstudios.perplayerquests.api;

public interface IPerPlayerObject {
    enum Collapse { ALL, ANY }

    enum ExclusivityScope { TEAM, PER_ACTOR }

    boolean mqt$isPerPlayer();

    void mqt$setPerPlayer(boolean perPlayer);

    default Collapse mqt$getCollapse() {
        return Collapse.ANY;
    }

    default void mqt$setCollapse(Collapse collapse) {
    }

    default ExclusivityScope mqt$getExclusivityScope() {
        return ExclusivityScope.TEAM;
    }

    default void mqt$setExclusivityScope(ExclusivityScope scope) {
    }

    default boolean mqt$showRosterTooltip() {
        return true;
    }

    default void mqt$setShowRosterTooltip(boolean show) {
    }
}
