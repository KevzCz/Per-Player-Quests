package net.pixeldreamstudios.perplayerquests.client;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.ftb.mods.ftblibrary.util.TextComponentUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.perplayerquests.network.PerPlayerAdminPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PerPlayerPickerScreen extends AbstractThreePanelScreen<PerPlayerPickerScreen.ListPanel> {
    private static final int ROW = 22;
    private static final int PAD = 8;
    private static final int GAP = 4;

    private final Component title;
    private final QuestObjectBase object;
    private final boolean wholeFile;

    private TextBox searchBox;
    private TextBox nameBox;
    private ListPanel listPanel;
    private String search = "";

    public PerPlayerPickerScreen(QuestObjectBase object) {
        this.object = object;
        this.wholeFile = false;
        this.title = Component.translatable("perplayerquests.per_player.picker.title", object.getTitle());
    }

    private PerPlayerPickerScreen(boolean wholeFile) {
        this.object = null;
        this.wholeFile = wholeFile;
        this.title = Component.translatable("perplayerquests.per_player.picker.title",
                Component.translatable("perplayerquests.per_player.picker.everything"));
    }

    public static PerPlayerPickerScreen forWholeFile() {
        return new PerPlayerPickerScreen(true);
    }

    public Component getTitle() {
        return title;
    }

    @Override
    public boolean onInit() {
        showScrollBar(true);
        return setSizeProportional(0.5f, 0.75f);
    }

    @Override
    protected int getTopPanelHeight() {
        return 30;
    }

    @Override
    protected Panel createTopPanel() {
        return new SearchPanel(this);
    }

    @Override
    protected int getBottomPanelHeight() {
        return 34;
    }

    @Override
    protected Panel createBottomPanel() {
        return new FooterPanel(this);
    }

    @Override
    protected ListPanel createMainPanel() {
        listPanel = new ListPanel(this);
        return listPanel;
    }

    @Override
    protected void doAccept() {
        closeGui(true);
    }

    @Override
    protected void doCancel() {
        closeGui(false);
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        if (listPanel != null && listPanel.scrollPanel(scroll)) return true;
        return super.mouseScrolled(scroll);
    }

    private void send(PerPlayerAdminPacket.Action action, UUID target) {
        long objId = object == null ? 0L : object.id;
        NetworkManager.sendToServer(new PerPlayerAdminPacket(action, target, objId));
        closeGui(true);
    }

    private void sendReset(UUID target) {
        send(wholeFile ? PerPlayerAdminPacket.Action.RESET_ALL : PerPlayerAdminPacket.Action.RESET, target);
    }

    private void sendToTypedName() {
        String name = nameBox == null ? "" : nameBox.getText().trim();
        if (name.isEmpty()) return;
        for (KnownClientPlayer p : allMembers()) {
            if (p.name().equalsIgnoreCase(name)) {
                sendReset(p.id());
                return;
            }
        }
    }

    private static UUID selfId() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? null : mc.player.getUUID();
    }

    private static List<KnownClientPlayer> allMembers() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return List.of();
        UUID selfTeam = FTBTeamsAPI.api().getClientManager().selfTeam().getTeamId();
        List<KnownClientPlayer> out = new ArrayList<>();
        for (KnownClientPlayer p : FTBTeamsAPI.api().getClientManager().knownClientPlayers()) {
            if (p.teamId().equals(selfTeam)) out.add(p);
        }
        out.sort(Comparator.comparing(KnownClientPlayer::online).reversed()
                .thenComparing(p -> p.name(), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private final class SearchPanel extends Panel {
        SearchPanel(Panel parent) { super(parent); }

        @Override
        public void addWidgets() {
            searchBox = new TextBox(this) {
                @Override
                public void onTextChanged() {
                    search = getText().trim().toLowerCase(Locale.ROOT);
                    listPanel.refreshWidgets();
                }
            };
            searchBox.ghostText = Component.translatable("perplayerquests.per_player.picker.search").getString();
            searchBox.textColor = Color4I.WHITE;
            add(searchBox);
        }

        @Override
        public void alignWidgets() {
            searchBox.setPosAndSize(PAD, 6, getWidth() - PAD * 2, 18);
        }

        @Override
        public void drawBackground(GuiGraphics g, Theme t, int x, int y, int w, int h) {
            t.drawPanelBackground(g, x, y, w, h);
        }
    }

    final class ListPanel extends Panel {
        ListPanel(Panel parent) { super(parent); }

        @Override
        public void addWidgets() {
            setOffset(true);
            UUID self = selfId();
            if (self != null) {
                add(new PlayerRow(this,
                        Component.translatable("perplayerquests.per_player.picker.self")
                                .withStyle(ChatFormatting.GOLD),
                        self, true));
            }
            for (KnownClientPlayer p : allMembers()) {
                if (p.id().equals(self)) continue;
                if (!search.isEmpty() && !p.name().toLowerCase(Locale.ROOT).contains(search)) continue;
                add(new PlayerRow(this,
                        Component.literal(p.name()).withStyle(
                                p.online() ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY),
                        p.id(), false));
            }
        }

        @Override
        public void alignWidgets() {
            int w = getWidth() - PAD * 2;
            int y = PAD;
            for (Widget widget : widgets) {
                widget.setPosAndSize(PAD, y, w, ROW);
                y += ROW + GAP;
            }
        }

        @Override
        public boolean mouseScrolled(double scroll) {
            return scrollPanel(scroll) || super.mouseScrolled(scroll);
        }
    }

    private final class PlayerRow extends Button {
        private final UUID target;
        private final boolean isSelf;

        PlayerRow(Panel panel, Component name, UUID target, boolean isSelf) {
            super(panel, name, Icons.PLAYER);
            this.target = target;
            this.isSelf = isSelf;
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            list.add(getTitle());
            list.add(Component.translatable("perplayerquests.per_player.picker.left_reset")
                    .withStyle(ChatFormatting.GRAY));
            if (!wholeFile) {
                list.add(Component.translatable("perplayerquests.per_player.picker.right_complete")
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            if (button.isRight()) {
                if (!wholeFile) send(PerPlayerAdminPacket.Action.COMPLETE, target);
                return;
            }
            sendReset(target);
        }

        @Override
        public void draw(GuiGraphics g, Theme t, int x, int y, int w, int h) {
            Color4I bg = isMouseOver() ? Color4I.rgb(48, 48, 58)
                    : (isSelf ? Color4I.rgb(40, 36, 20) : Color4I.rgb(26, 26, 32));
            bg.draw(g, x, y, w, h);
            Color4I.rgb(60, 60, 68).draw(g, x, y + h - 1, w, 1);
            drawIcon(g, t, x + 4, y + (h - 16) / 2, 16, 16);
            t.drawString(g, getTitle(), x + 24, y + (h - t.getFontHeight()) / 2, Color4I.WHITE, 0);
        }
    }

    private final class FooterPanel extends Panel {
        private Button cancel;

        FooterPanel(Panel parent) { super(parent); }

        @Override
        public void addWidgets() {
            nameBox = new TextBox(this) {
                @Override
                public void onEnterPressed() {
                    sendToTypedName();
                }
            };
            nameBox.ghostText = Component.translatable("perplayerquests.per_player.picker.type_name").getString();
            nameBox.textColor = Color4I.WHITE;
            add(nameBox);

            cancel = SimpleTextButton.cancel(this, b -> closeGui(false),
                    TextComponentUtils.hotkeyTooltip("ESC"));
            add(cancel);
        }

        @Override
        public void alignWidgets() {
            int y = 8;
            cancel.setPos(getWidth() - cancel.width - 5, y);
            nameBox.setPosAndSize(PAD, y, getWidth() - cancel.width - PAD * 2 - 10, 18);
        }

        @Override
        public void drawBackground(GuiGraphics g, Theme t, int x, int y, int w, int h) {
            t.drawPanelBackground(g, x, y, w, h);
            Color4I.GRAY.withAlpha(64).draw(g, x, y, w, 1);
        }
    }
}
