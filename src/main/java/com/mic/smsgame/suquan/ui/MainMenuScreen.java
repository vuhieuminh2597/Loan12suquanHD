package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.sprite.Drawable;
import com.mg.engine.storage.CampaignStorage;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.ImageBackground;
import com.mg.engine.ui.ImageBlock;
import com.mg.engine.ui.MenuSelectionListener;
import com.mg.engine.ui.RichContentDialog;
import com.mg.engine.ui.ScreenBackground;
import com.mg.engine.ui.SlidingMenuList;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.TextBlock;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mg.engine.util.ByteUtils;
import com.mg.engine.util.ObjectList;
import com.mg.smsgame.main.MGMIDlet;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.data.WorldMapData;
import com.mic.smsgame.suquan.gfx.MenuItemSprite;
import com.mic.smsgame.suquan.gfx.TitleLogoSprite;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Main menu screen for <em>Loạn 12 Sứ Quân HD</em> upgraded with 100% Vector HD Imperial
 * Calligraphic Title Banner, Lacquer Menu Plaque Buttons, Mouse Hover auto-focus, and 1-Click UX.
 */
public class MainMenuScreen extends BaseScreen implements ActionListener, MenuSelectionListener {
    private static final int PAGE_NEW_PLAYER = 0;
    private static final int PAGE_HAS_SAVE = 1;
    private static final int PAGE_INFO_SUBMENU = 2;

    private static final int ACTION_PROMPT_EXIT = 1;
    private static final int ACTION_BACK_TO_ROOT = 2;
    private static final int ACTION_CONFIRM_EXIT = 3;
    private static final int ACTION_DISMISS_DIALOG = 4;
    private static final int ACTION_CLOSE_INFO = 6;
    private static final int ACTION_CONFIRM_NEW_GAME = 9;

    private static final String[] HD_MENU_LABELS = new String[]{
            "TIẾP TỤC CHIẾN DỊCH",
            "KHỞI ĐẦU MỚI",
            "BÁU VẬT HỘ QUỐC",
            "CỬA HÀNG QUÂN NHU",
            "CẨM NANG & THÔNG TIN",
            "",
            "",
            "",
            "CẨM NANG CHIẾN TRẬN",
            "ĐỘI NGŨ PHÁT TRIỂN"
    };

    private static final int HD_BTN_W = 184;
    private static final int HD_BTN_H = 22;

    protected TextSoftKeyButton exitButton;
    protected TextSoftKeyButton backButton;
    private SlidingMenuList menuList;
    private int[] activeItemIds;
    private boolean noSavedHero;
    private Drawable[] allMenuSprites;
    private TitleLogoSprite titleLogo;
    private Image swordCursorImage;
    private int swordPulseOffset;
    private boolean swordPulseInward;
    private ScreenBackground menuBackground;
    private int currentPage;
    private String savedProgressSummary;
    private boolean hasPendingBattleSnapshot;
    private int animTick;
    private int hdSelectedIndex = 0;

    public MainMenuScreen() {
        GameConfig.mainMenuReady = true;
        this.initSoftKeys();
        this.menuList = new SlidingMenuList(true);
        this.menuList.a(this);
        this.loadMenuSprites();
        this.menuBackground = new ImageBackground(
                MgImageLoader.load("/bkmenu"),
                GameConfig.contentOffsetX,
                GameConfig.contentOffsetY,
                Graphics.TOP | Graphics.LEFT);
        this.titleLogo = new TitleLogoSprite(super.b_ / 2, GameConfig.contentOffsetY + 98);
        this.swordCursorImage = MgImageLoader.load("/swordfocus");
        this.noSavedHero = !CampaignStorage.hasHeroSave();
        this.savedProgressSummary = this.noSavedHero ? null : buildSavedProgressSummary();
        this.hasPendingBattleSnapshot = !this.noSavedHero && CampaignStorage.hasBattleSnapshot();
        this.setMenuPage(this.noSavedHero ? PAGE_NEW_PLAYER : PAGE_HAS_SAVE);

        this.a(this.menuBackground);
        this.a((ActionListener) this);
        this.a(new SoftKeyBar());
        this.c(new TextSoftKeyButton("Chọn", 100));
        BgmPlayer.getInstance().load("menu", -1);
        BgmPlayer.getInstance().setTempo(120000);
        BgmPlayer.getInstance().playAsync();
        super.q = 7;
    }

    private static String buildSavedProgressSummary() {
        try {
            String heroName = CampaignData.c();
            if (heroName == null || heroName.length() == 0) {
                heroName = "Chủ tướng";
            }
            int heroAvatar = CampaignData.d();
            int elem = (heroAvatar < 0 ? 0 : heroAvatar) % 3;
            String elemTag = elem == 0 ? "Hỏa" : (elem == 1 ? "Lôi" : "Thủy");
            int totalExp = CampaignData.b();
            int lv = 1;
            while (lv < 99 && CampaignData.c(lv + 1, totalExp)) {
                ++lv;
            }
            String loc = "Hoa Lư";
            byte[] heroBytes = CampaignStorage.loadHeroData();
            if (heroBytes != null && heroBytes.length >= 4) {
                int nodeIdx = ByteUtils.a(heroBytes, 0);
                loc = WorldMapData.getLocationName(nodeIdx);
            }
            String summary = "Lưu: " + heroName + " (C." + lv + " " + elemTag + ") | " + loc;
            if (CampaignStorage.hasBattleSnapshot()) {
                summary = summary + " [Đang đánh]";
            }
            return summary;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void loadMenuSprites() {
        String[] itemNames = new String[]{
                "strcontinuegame",
                "strnewgame",
                "strrecord",
                "strshop",
                "strinformation",
                "strgivegame",
                "strchatola",
                "strothergame",
                "strguide",
                "strauthor"
        };
        this.allMenuSprites = new MenuItemSprite[itemNames.length];
        for (int i = 0; i < this.allMenuSprites.length; ++i) {
            Image[] pair = new Image[]{
                    MgImageLoader.load("/" + itemNames[i]),
                    MgImageLoader.load("/" + itemNames[i] + "focus")
            };
            this.allMenuSprites[i] = new MenuItemSprite(pair);
        }
    }

    protected void setMenuPage(int page) {
        this.currentPage = page;
        this.hdSelectedIndex = 0;
        switch (page) {
            case PAGE_NEW_PLAYER:
                this.activeItemIds = new int[]{1, 2, 3, 4};
                break;
            case PAGE_HAS_SAVE:
                this.activeItemIds = new int[]{0, 1, 2, 3, 4};
                break;
            case PAGE_INFO_SUBMENU:
                this.activeItemIds = new int[]{8, 9};
                break;
        }

        this.b(page == PAGE_INFO_SUBMENU ? this.backButton : this.exitButton);
        this.layoutActiveMenuItems();
    }

    protected void initSoftKeys() {
        this.exitButton = new TextSoftKeyButton("Thoát", ACTION_PROMPT_EXIT);
        this.backButton = new TextSoftKeyButton("Trở về", ACTION_BACK_TO_ROOT);
    }

    protected void layoutActiveMenuItems() {
        Drawable[] visibleSprites = new Drawable[this.activeItemIds.length];
        int itemY = (this.savedProgressSummary != null ? 116 : 126) + GameConfig.contentOffsetY;
        int stepY = this.savedProgressSummary != null ? 26 : 28;
        int centerX = super.b_ / 2;
        for (int i = 0; i < visibleSprites.length; ++i) {
            visibleSprites[i] = this.allMenuSprites[this.activeItemIds[i]];
            visibleSprites[i].d(centerX, itemY);
            visibleSprites[i].k(Graphics.TOP | Graphics.HCENTER);
            visibleSprites[i].a(false);
            itemY += stepY;
        }
        this.menuList.a(visibleSprites, 0);
    }

    private int getHdMenuStartY() {
        if (this.currentPage == PAGE_INFO_SUBMENU) {
            return 146;
        }
        return (this.savedProgressSummary != null) ? 108 : 122;
    }

    private int getHdMenuStepY() {
        return (this.savedProgressSummary != null && this.currentPage == PAGE_HAS_SAVE) ? 27 : 31;
    }

    public final void a(Graphics g) {
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdCanvas2D g2 = g.getHdCanvas();
            int W = HdConfig.PLAYFIELD_HD_WIDTH;
            int H = HdConfig.PLAYFIELD_HD_HEIGHT;
            g2.setLinearGradient(
                    0, 0, 0, H,
                    new float[]{0f, 1f},
                    new int[]{HdUiRenderer.rgba(0x201008, 55), HdUiRenderer.rgba(0x0A0503, 175)});
            g2.fillRect(0, 0, W, H);
        }

        this.drawLogo(g);
        this.drawMenuAndCursors(g);
        this.drawSoundBadge(g);
        this.drawDisplayModeBadge(g);
        this.drawSavedProgressStrip(g);
    }

    private void drawSoundBadge(Graphics g) {
        int vol = GameConfig.soundEnabled ? GameConfig.soundVolume : 0;
        String label = vol > 0 ? ("Âm: " + vol + "%") : "Âm: Tắt";
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int bw = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, label) + 10;
        int bh = plainH + 5;
        int bx = 3;
        int by = 3;
        boolean hov = HdInputState.isHovered(bx, by, bw, bh);
        DrawUtils.drawAntiquePanel(
                g, bx, by, bw, bh,
                hov ? 0x247A68 : (vol > 0 ? 0x1A594E : 0x382518),
                vol > 0 ? 0x103831 : 0x24170E,
                0x100805,
                hov ? 0xFFD700 : (vol > 0 ? 0xC89B46 : 0x7A592A),
                vol > 0 ? 0xF5D27E : 0xA88044,
                0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, label,
                bx + (bw >> 1), by + 2,
                Graphics.TOP | Graphics.HCENTER,
                vol > 0 ? 0xFFF4DC : 0xC8B098, 0x120804);
    }

    private void drawSavedProgressStrip(Graphics g) {
        if (this.currentPage != PAGE_HAS_SAVE || this.savedProgressSummary == null) {
            return;
        }
        int stripW = GameConfig.screenWidth - 16;
        int stripX = 8;
        String[] lines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, this.savedProgressSummary, stripW - 12);
        int textH = DrawUtils.getWrappedHeight(DrawUtils.UI_FONT_PLAIN, lines);
        int stripH = textH + 6;
        int stripY = GameConfig.screenHeight - 24 - stripH - 2;
        boolean hov = HdInputState.isHovered(stripX, stripY, stripW, stripH);
        DrawUtils.drawAntiquePanel(
                g, stripX, stripY, stripW, stripH,
                (this.hasPendingBattleSnapshot || hov) ? 0x1D5449 : 0x332114,
                (this.hasPendingBattleSnapshot || hov) ? 0x11362E : 0x1F130B,
                0x100804,
                (this.hasPendingBattleSnapshot || hov) ? 0xFFD700 : 0x9E7538,
                0xF2CC79,
                0xFFFFFF);
        DrawUtils.drawWrappedCenteredShadowText(
                g, DrawUtils.UI_FONT_PLAIN, lines,
                stripX + (stripW >> 1), stripY + 3, textH + 2,
                this.hasPendingBattleSnapshot ? 0xFFF5B8 : 0xFFE680,
                0x120804);
    }

    private void drawDisplayModeBadge(Graphics g) {
        String modeLabel = HdConfig.getModeBadgeLabel();
        boolean isHdActive = HdConfig.isHdUpscaleActive();
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int bw = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, modeLabel) + 10;
        int bh = plainH + 5;
        int bx = GameConfig.screenWidth - bw - 3;
        int by = 3;
        boolean hov = HdInputState.isHovered(bx, by, bw, bh);
        DrawUtils.drawAntiquePanel(
                g, bx, by, bw, bh,
                hov ? 0x247A68 : (isHdActive ? 0x1A594E : 0x382518),
                isHdActive ? 0x103831 : 0x24170E,
                0x100805,
                (isHdActive || hov) ? 0xFFD700 : 0x8F6932,
                (isHdActive || hov) ? 0xFFF2A6 : 0xCCA052,
                0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, modeLabel,
                bx + (bw >> 1), by + 2,
                Graphics.TOP | Graphics.HCENTER,
                isHdActive ? 0xFFF5B8 : 0xE6D5BD, 0x120804);
    }

    private void cycleDisplayMode() {
        HdConfig.cycleVisualMode();
        GameConfig.highFpsEnabled = true;
        GameConfig.hdUpscaleEnabled = HdConfig.isHdUpscaleActive();
        EngineStorage.saveDisplaySettings();
    }

    protected void drawLogo(Graphics g) {
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdImperialTitleLogo(g.getHdCanvas(), super.b_ >> 1, 102, this.animTick);
            return;
        }
        this.titleLogo.a(g);
    }

    protected void drawMenuAndCursors(Graphics g) {
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdCanvas2D g2 = g.getHdCanvas();
            int bx = (super.b_ - HD_BTN_W) >> 1;
            int by = getHdMenuStartY();
            int stepY = getHdMenuStepY();

            for (int i = 0; i < this.activeItemIds.length; ++i) {
                int itemId = this.activeItemIds[i];
                String label = HD_MENU_LABELS[itemId];
                if (itemId == 2 && EngineStorage.canCraftOrUpgradeAnyRelic()) {
                    label = "BÁU VẬT HỘ QUỐC (!)";
                }
                boolean hovered = HdInputState.isHovered(bx, by, HD_BTN_W, HD_BTN_H);
                if (hovered) {
                    this.hdSelectedIndex = i;
                }
                boolean selected = (i == this.hdSelectedIndex);
                HdUiRenderer.drawHdMenuButton(
                        g2, bx, by, HD_BTN_W, HD_BTN_H,
                        label, selected, hovered, itemId == 0);
                by += stepY;
            }
            return;
        }

        this.menuList.a(g);
        if (!this.menuList.c()) {
            Drawable selected = this.menuList.a();
            int itemX = selected.i();
            int itemY = selected.j() + 3;
            int w = this.swordCursorImage.getWidth();
            int h = this.swordCursorImage.getHeight();
            int pulse = this.swordPulseOffset;
            if (GameConfig.highFpsEnabled) {
                int wave = (GameConfig.visualFrameCounter >> 2) & 7;
                pulse = (wave <= 4 ? wave : (8 - wave));
            }
            DrawUtils.drawRegion(g, this.swordCursorImage, 0, 0, w, h, itemX - 2 - pulse, itemY, Graphics.TOP | Graphics.RIGHT);
            g.drawRegion(this.swordCursorImage, 0, 0, w, h, Sprite.TRANS_MIRROR, itemX + selected.k() + 2 + pulse, itemY, Graphics.TOP | Graphics.LEFT);
        }
    }

    public void a() {
        ++this.animTick;
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0 && this.activeItemIds != null && this.activeItemIds.length > 0) {
            int len = this.activeItemIds.length;
            this.hdSelectedIndex = (this.hdSelectedIndex + (wheel > 0 ? 1 : -1) + len) % len;
        }
        if (this.titleLogo.m()) {
            this.titleLogo.a();
        }
        this.menuList.b();
        if (this.swordPulseInward) {
            --this.swordPulseOffset;
            if (this.swordPulseOffset == 0) {
                this.swordPulseInward = false;
            }
        } else {
            ++this.swordPulseOffset;
            if (this.swordPulseOffset == 3) {
                this.swordPulseInward = true;
            }
        }
    }

    protected void promptExitGame() {
        BaseDialog confirm = GUIManager.a("Chú ý", "Bạn có muốn thoát trò chơi không?", "Có", ACTION_CONFIRM_EXIT, "Không", ACTION_DISMISS_DIALOG, 1);
        confirm.a((ActionListener) this);
        GUIManager.getInstance().a(confirm, true);
    }

    protected void exitApplication() {
        MGMIDlet.f().d();
    }

    protected void openCollectionScreen() {
        GUIManager.getInstance().e.a(2, 3, true);
    }

    protected void openShopScreen() {
        GUIManager.getInstance().e.a(2, 4, true);
    }

    protected void showCreditsDialog(String[][] creditsSections) {
        if (creditsSections == null) {
            return;
        }
        TextBlock[] blocks = new TextBlock[creditsSections.length];
        for (int i = 0; i < blocks.length; ++i) {
            blocks[i] = new TextBlock(creditsSections[i], true, 1);
            blocks[i].d = -1;
        }
        RichContentDialog dialog = new RichContentDialog("Thông tin", blocks);
        dialog.c(new TextSoftKeyButton("Đóng", ACTION_CLOSE_INFO));
        dialog.a((SoftKeyBar) null);
        dialog.a(this.menuBackground);
        dialog.a((ActionListener) this);
        GUIManager.getInstance().a(dialog);
    }

    protected void promptOverwriteNewGame(int confirmActionId, int cancelActionId) {
        BaseDialog confirm = GUIManager.a("Chú ý", "Bạn có muốn chơi lại từ đầu không?", "Có", confirmActionId, "Không", cancelActionId, 1);
        confirm.a((ActionListener) this);
        GUIManager.getInstance().a(confirm, true);
    }

    public void a(int keyCode) {
        if (keyCode == 10) {
            BgmPlayer.getInstance().cycleLiveVolumePreset();
            return;
        }
        if (keyCode == 11) {
            this.cycleDisplayMode();
            return;
        }
        if (HdConfig.isHdRemaster() && this.activeItemIds != null && this.activeItemIds.length > 0) {
            int len = this.activeItemIds.length;
            if (keyCode == 12 || keyCode == 14) {
                this.hdSelectedIndex = (this.hdSelectedIndex - 1 + len) % len;
                return;
            }
            if (keyCode == 13 || keyCode == 15) {
                this.hdSelectedIndex = (this.hdSelectedIndex + 1) % len;
                return;
            }
            if (keyCode == 16) {
                this.a("hd", this.hdSelectedIndex);
                return;
            }
        }
        this.menuList.a(keyCode);
    }

    public void a(int pointerX, int pointerY) {
        if (pointerX <= 82 && pointerY <= 24) {
            BgmPlayer.getInstance().cycleLiveVolumePreset();
            return;
        }
        if (pointerX >= GameConfig.screenWidth - 96 && pointerY <= 24) {
            this.cycleDisplayMode();
            return;
        }
        if (this.currentPage == PAGE_HAS_SAVE
                && this.savedProgressSummary != null
                && pointerY >= GameConfig.screenHeight - 46
                && pointerY <= GameConfig.screenHeight - 24) {
            this.a("continue", 0);
            return;
        }
        if (HdConfig.isHdRemaster() && this.activeItemIds != null) {
            int bx = (super.b_ - HD_BTN_W) >> 1;
            int by = getHdMenuStartY();
            int stepY = getHdMenuStepY();
            for (int i = 0; i < this.activeItemIds.length; ++i) {
                if (pointerX >= bx && pointerX <= bx + HD_BTN_W
                        && pointerY >= by && pointerY <= by + HD_BTN_H) {
                    this.hdSelectedIndex = i;
                    // 1-Click immediate activation on HD Main Menu!
                    this.a("hd", i);
                    return;
                }
                by += stepY;
            }
            return;
        }
        this.menuList.a(pointerX, pointerY);
    }

    public void onAction(int screenId, int actionId) {
        switch (actionId) {
            case 100:
                this.a("hd", HdConfig.isHdRemaster() ? this.hdSelectedIndex : 0);
                break;
            case ACTION_PROMPT_EXIT:
                this.promptExitGame();
                break;
            case ACTION_BACK_TO_ROOT:
                this.setMenuPage(this.noSavedHero ? PAGE_NEW_PLAYER : PAGE_HAS_SAVE);
                break;
            case ACTION_CONFIRM_EXIT:
                this.exitApplication();
                break;
            case ACTION_DISMISS_DIALOG:
                GUIManager.getInstance().a(true);
                break;
            case ACTION_CLOSE_INFO:
                GUIManager.getInstance().a(false);
                break;
            case ACTION_CONFIRM_NEW_GAME:
                CampaignStorage.clearCampaignSave();
                EngineStorage.k();
                EngineStorage.x();
                GUIManager.getInstance().a(false);
                BgmPlayer.getInstance().destroy();
                GUIManager.getInstance().a(new StoryCutsceneScreen(true), false);
                break;
        }
    }

    private void showGuideDialog() {
        ObjectList icons = new ObjectList();
        icons.a(new ImageBlock(MgImageLoader.load("/sword")));
        icons.a(new ImageBlock(MgImageLoader.load("/yinyang")));
        icons.a(new ImageBlock(MgImageLoader.load("/rice")));
        icons.a(new ImageBlock(MgImageLoader.load("/gold")));
        icons.a(new ImageBlock(MgImageLoader.load("/book")));
        icons.a(new ImageBlock(MgImageLoader.load("/heart")));
        icons.a(new ImageBlock(MgImageLoader.load("/swordred")));
        RichContentDialog guide = RichContentDialog.a(
                "Cẩm nang chiến trận",
                "**Cốt truyện lấy bối cảnh lịch sử Loạn 12 Sứ Quân tại Việt Nam thế kỷ X.@*Điều khiển & Bàn cờ HD:@- Click chuột trực tiếp hoặc kéo vuốt nhẹ giữa 2 ô kề nhau trên bàn cờ 8x8 để tráo ngọc (hỗ trợ cả phím W/A/S/D hoặc Mũi tên + Enter, con lăn chuột để cuộn).@- Phím F2 đổi chế độ Đồ họa HD, F3 đổi Âm lượng, F4 bật/tắt Hạt ánh sáng, F11 Toàn màn hình.@- Xếp từ 4 ngọc thẳng hàng hoặc chữ L/T (>= 5 ô) được thưởng thêm lượt đi; ghép 5+ ô hoặc chuỗi Combo x4+ có tỉ lệ rớt Vật phẩm & Mảnh Cường Hóa.@{i}@- Kiếm: Tấn công đối phương (ghép 4+ Kiếm rèn thêm 1 Kiếm Lửa).@{i}@- Âm Dương (Nộ): Tích Nộ khí mở 3 bậc Tuyệt chiêu (12 / 24 / 36 Nộ).@{i}@- Đùi Gà (Lương): Bổ sung Lương thực (ghép 4+ viên cướp Lương địch; thiếu lương <=25% giảm 15% Công & 25% Nộ; cạn 0 Lương giảm 30% Công, mất 6% Máu/lượt & khóa Chiêu 3).@{i}@- Thỏi Vàng: Thu thập Vàng mua vật phẩm & nâng cấp Báu vật.@{i}@- Sách Cổ: Tích lũy EXP lên cấp & mở Đột Phá mỗi 10 cấp.@{i}@- Trái Tim: Hồi phục Sinh lực (Máu).@{i}@- Kiếm Lửa: Ghép chung với Kiếm thường, nổ lan 3x3, phá Giáp Ảo & đốt Lương nếu nổ Đùi Gà.@*Đấu Boss & Ấn Đỏ:@- Các Sứ Quân và Boss có Khuôn mẫu riêng, định kỳ đặt 1-2 ô Ấn Đỏ đếm ngược 3 lượt trên bàn cờ.@- Ghép nổ hoặc dùng Tuyệt chiêu phá ô Ấn trước khi hết lượt sẽ hóa giải đòn Boss, thưởng +6 Nộ, hồi 5% Máu & kích hoạt Khắc chế!@- Tuyệt chiêu sát thương và Kiếm 4+/Kiếm Lửa phá Giáp Ảo của Boss với hiệu suất 150%.@*Phát triển Chủ tướng:@- Mỗi trận thắng ngoài Đấu Trường thưởng thêm Vàng & EXP theo cấp địch (nhân cao hơn khi hạ Boss, đánh vượt cấp hoặc giữ >70% Máu).@- Tại thành trì đã giải phóng, bấm nút Nghỉ ngơi góc trên phải để hồi 100% Máu.@- Thu thập Mảnh Cường Hóa để ghép & nâng cấp 4 Báu Vật lên tối đa +5 (có bảo hiểm cộng dồn tỉ lệ khi trượt).",
                null,
                icons,
                null,
                null);
        guide.c(new TextSoftKeyButton("Đóng", ACTION_DISMISS_DIALOG));
        guide.a((ActionListener) this);
        GUIManager.getInstance().a(guide, true);
    }

    public void f(int previousIndex, int currentIndex) {
    }

    public void a(String itemLabel, int selectedIndex) {
        int menuId = this.activeItemIds[selectedIndex];
        switch (menuId) {
            case 0:
                BgmPlayer.getInstance().destroy();
                if (CampaignStorage.hasBattleSnapshot()) {
                    GUIManager.getInstance().e.a(2, 5, false);
                } else {
                    GUIManager.getInstance().e.a(2, 7, false);
                }
                break;
            case 1:
                if (!this.noSavedHero) {
                    this.promptOverwriteNewGame(ACTION_CONFIRM_NEW_GAME, ACTION_DISMISS_DIALOG);
                    return;
                }
                BgmPlayer.getInstance().destroy();
                EngineStorage.k();
                EngineStorage.x();
                GUIManager.getInstance().a(new StoryCutsceneScreen(true), false);
                break;
            case 2:
                this.openCollectionScreen();
                break;
            case 3:
                this.openShopScreen();
                break;
            case 4:
                this.setMenuPage(PAGE_INFO_SUBMENU);
                break;
            case 8:
                this.showGuideDialog();
                break;
            default:
                this.showCreditsDialog(GameStrings.f);
                break;
        }
    }
}
