package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.model.HeroStats;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Two-stage antique hero dossier & in-canvas name seal screen ("Chọn Chủ Tướng" + "Đặt Danh Hiệu"),
 * replacing the legacy system {@code javax.microedition.lcdui.TextBox} with a seamless 60 FPS canvas UI.
 */
public class CharacterSelectScreen extends BaseScreen implements ActionListener {
    private static final int ACTION_CONFIRM_PRIMARY = -1;
    private static final int ACTION_BACK = -2;
    private static final int ACTION_LEFT_AUX = -3;
    private static final int ACTION_DISMISS_WARN = -4;

    private static final String KEYPAD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEYPAD_COLS = 9;
    private static final int KEYPAD_ROWS = 4;

    private static final String[] HERO_TITLES = new String[]{
            "Hỏa Tướng (Nam)", "Lôi Tướng (Nam)", "Thủy Tướng (Nam)",
            "Hỏa Phụng (Nữ)", "Lôi Hiệp (Nữ)", "Long Nữ (Nữ)"
    };
    private static final String[] ELEMENT_SKILLS_LINE = new String[]{
            "Cầu Lửa | Thiên Thạch | Lửa Địa Ngục",
            "Chuỗi Sét | Khiên Sét | Sấm Sét",
            "Tên Băng | Cam Lộ Thủy | Băng Phong"
    };
    private static final String[] ELEMENT_PASSIVE_PERK = new String[]{
            "Nội tại Hỏa Linh: +15% Công, +20% ST Kiếm Lửa, Combo x3+ rèn Kiếm Lửa.",
            "Nội tại Lôi Điện: +8 Nộ đầu, hoàn 4 Nộ/chiêu, ghép 4+ nạp +2 Nộ.",
            "Nội tại Băng Tâm: +10~20% Công, +2% Máu & +2 Nộ/lượt, Kiếm 4+ gây Hàn Băng."
    };
    private static final String[] PRESET_NAMES = new String[]{
            "THIÊN LONG", "ĐẠI VIỆT", "VẠN THẮNG", "HOÀNG LONG",
            "LÔI ĐẾ", "HỎA PHỤNG", "BĂNG TÂM", "MINH VƯƠNG",
            "BẮC HẢI", "KIẾM THÁNH"
    };

    private Image[] heroAvatars;
    private Image focusBoxSheet;
    private Image menuBg;
    private int selectedHeroIndex;
    private String[] wrappedBio;
    private String[] heroBios;
    private int[] baseHpByHero;
    private int[] baseAtkByHero;
    private int blinkTick;

    // Stage 2: In-canvas name entry state
    private boolean enteringNameMode = false;
    private String enteredName = "";
    private int keypadIndex = 0;
    private int presetNameCursor = 0;

    public CharacterSelectScreen() {
        GameConfig.textInputActive = false;
        GameConfig.lastTypedKey = 0;
        this.loadResources();
        this.heroBios = GameStrings.e;
        this.baseHpByHero = new int[6];
        this.baseAtkByHero = new int[6];
        for (int i = 0; i < 6; ++i) {
            HeroStats preview = CampaignData.b(i, 1);
            this.baseHpByHero[i] = preview != null ? preview.b : 120;
            this.baseAtkByHero[i] = preview != null ? preview.d : 10;
        }
        this.refreshBioWrap();
        this.a(new SoftKeyBar());
        this.a((ActionListener) this);
        this.syncSoftKeys();
    }

    private void loadResources() {
        Image avatarSheet = MgImageLoader.load("/avatar");
        this.heroAvatars = new Image[6];
        for (int i = 0; i < 6; ++i) {
            this.heroAvatars[i] = Image.createImage(avatarSheet, i * 30, 0, 30, 30, 0);
        }
        this.focusBoxSheet = MgImageLoader.load("/focusitem");
        this.menuBg = MgImageLoader.load("/bkmenu");
    }

    private void refreshBioWrap() {
        this.wrappedBio = DrawUtils.wrapText(
                DrawUtils.UI_FONT_PLAIN, this.heroBios[this.selectedHeroIndex], 204);
    }

    private void syncSoftKeys() {
        if (!this.enteringNameMode) {
            GameConfig.textInputActive = false;
            this.a(new TextSoftKeyButton("Đổi tướng", ACTION_LEFT_AUX));
            this.c(new TextSoftKeyButton("Chọn", ACTION_CONFIRM_PRIMARY));
            this.b(new TextSoftKeyButton("Trở về", ACTION_BACK));
        } else {
            GameConfig.textInputActive = true;
            this.a(new TextSoftKeyButton("Gợi ý", ACTION_LEFT_AUX));
            this.c(new TextSoftKeyButton("Xác nhận", ACTION_CONFIRM_PRIMARY));
            this.b(new TextSoftKeyButton("Chọn lại", ACTION_BACK));
        }
    }

    private void openNameInputMode() {
        this.enteringNameMode = true;
        if (this.enteredName == null || this.enteredName.length() == 0) {
            int defaultIdx = this.selectedHeroIndex == 3 ? 5
                    : (this.selectedHeroIndex == 4 ? 4
                    : (this.selectedHeroIndex == 5 ? 6 : this.selectedHeroIndex));
            this.presetNameCursor = defaultIdx % PRESET_NAMES.length;
            this.enteredName = PRESET_NAMES[this.presetNameCursor];
        }
        this.keypadIndex = 0;
        GameConfig.lastTypedKey = 0;
        this.syncSoftKeys();
    }

    private void cyclePresetName() {
        this.presetNameCursor = (this.presetNameCursor + 1) % PRESET_NAMES.length;
        this.enteredName = PRESET_NAMES[this.presetNameCursor];
    }

    private void appendChar(char ch) {
        if (this.enteredName.length() >= 10) {
            return;
        }
        if (ch >= 'a' && ch <= 'z') {
            ch = (char) (ch - 'a' + 'A');
        }
        if (ch == ' ' && this.enteredName.length() == 0) {
            return;
        }
        this.enteredName = this.enteredName + ch;
    }

    private void deleteLastChar() {
        int len = this.enteredName.length();
        if (len > 0) {
            this.enteredName = this.enteredName.substring(0, len - 1);
        }
    }

    private void confirmHeroAndStartCampaign() {
        String clean = this.enteredName != null ? this.enteredName.trim() : "";
        if (clean.length() < 2) {
            GameConfig.textInputActive = false;
            BaseDialog warn = GUIManager.a(
                    "Chú ý",
                    "Danh hiệu Chủ tướng cần từ 2 đến 10 ký tự. Hãy nhập thêm hoặc bấm nút Gợi ý!",
                    "Đóng", ACTION_DISMISS_WARN, 1);
            warn.a((ActionListener) this);
            GUIManager.getInstance().a(warn, true);
            return;
        }
        GameConfig.textInputActive = false;
        CampaignData.a(clean.toUpperCase());
        CampaignData.c(this.selectedHeroIndex);
        GUIManager.getInstance().e.a(8, 7, true);
    }

    public void a(int keyCode) {
        if (!this.enteringNameMode) {
            int prev = this.selectedHeroIndex;
            int count = this.heroBios.length;
            switch (keyCode) {
                case 12:
                    this.selectedHeroIndex = (this.selectedHeroIndex - 3 + count) % count;
                    break;
                case 13:
                    this.selectedHeroIndex = (this.selectedHeroIndex + 3) % count;
                    break;
                case 14:
                    this.selectedHeroIndex = (this.selectedHeroIndex - 1 + count) % count;
                    break;
                case 15:
                    this.selectedHeroIndex = (this.selectedHeroIndex + 1) % count;
                    break;
            }
            if (this.selectedHeroIndex != prev) {
                this.enteredName = "";
                this.refreshBioWrap();
            }
        } else {
            int col = this.keypadIndex % KEYPAD_COLS;
            int row = this.keypadIndex / KEYPAD_COLS;
            switch (keyCode) {
                case 12: // UP
                    row = (row - 1 + KEYPAD_ROWS) % KEYPAD_ROWS;
                    this.keypadIndex = row * KEYPAD_COLS + col;
                    break;
                case 13: // DOWN
                    row = (row + 1) % KEYPAD_ROWS;
                    this.keypadIndex = row * KEYPAD_COLS + col;
                    break;
                case 14: // LEFT
                    col = (col - 1 + KEYPAD_COLS) % KEYPAD_COLS;
                    this.keypadIndex = row * KEYPAD_COLS + col;
                    break;
                case 15: // RIGHT
                    col = (col + 1) % KEYPAD_COLS;
                    this.keypadIndex = row * KEYPAD_COLS + col;
                    break;
                case 10: // * -> space
                    this.appendChar(' ');
                    break;
                case 11: // # -> backspace
                case 19: // CLEAR / BACKSPACE
                    this.deleteLastChar();
                    break;
                case 0:
                    this.appendChar(KEYPAD_CHARS.charAt(this.keypadIndex));
                    break;
            }
        }
    }

    public void a(int pointerX, int pointerY) {
        int cardX = (super.b_ - 224) >> 1;
        int cardY = 6 + GameConfig.contentOffsetY;
        if (!this.enteringNameMode) {
            int cellY = cardY + 30;
            int idx = 0;
            for (int row = 0; row < 2; ++row) {
                int cellX = cardX + 26;
                for (int col = 0; col < 3; ++col) {
                    if (pointerY >= cellY && pointerY <= cellY + 36
                            && pointerX >= cellX && pointerX <= cellX + 44) {
                        if (idx != this.selectedHeroIndex) {
                            this.selectedHeroIndex = idx;
                            this.enteredName = "";
                            this.refreshBioWrap();
                        } else {
                            this.openNameInputMode();
                        }
                        return;
                    }
                    cellX += 64;
                    ++idx;
                }
                cellY += 40;
            }
        } else {
            // Action pills: [Gợi ý tên] | [Cách] | [Xóa <-]
            int pillY = cardY + 78;
            if (pointerY >= pillY && pointerY <= pillY + 22) {
                if (pointerX >= cardX + 8 && pointerX <= cardX + 82) {
                    this.cyclePresetName();
                    return;
                }
                if (pointerX >= cardX + 86 && pointerX <= cardX + 144) {
                    this.appendChar(' ');
                    return;
                }
                if (pointerX >= cardX + 148 && pointerX <= cardX + 216) {
                    this.deleteLastChar();
                    return;
                }
            }
            // 9x4 character grid
            int gridX = cardX + 9;
            int gridY = cardY + 106;
            int cellW = 23;
            int cellH = 27;
            if (pointerX >= gridX && pointerX < gridX + KEYPAD_COLS * cellW
                    && pointerY >= gridY && pointerY < gridY + KEYPAD_ROWS * cellH) {
                int c = (pointerX - gridX) / cellW;
                int r = (pointerY - gridY) / cellH;
                int kIdx = r * KEYPAD_COLS + c;
                if (kIdx >= 0 && kIdx < KEYPAD_CHARS.length()) {
                    this.keypadIndex = kIdx;
                    this.appendChar(KEYPAD_CHARS.charAt(kIdx));
                }
            }
        }
    }

    public void a(Graphics g) {
        g.setColor(0x160D08);
        g.fillRect(0, 0, super.b_, super.c_);
        if (this.menuBg != null) {
            g.drawImage(this.menuBg, GameConfig.contentOffsetX, GameConfig.contentOffsetY, 0);
        }
        DrawUtils.drawHatchedOverlay(g, 0x120905, 0, 0, super.b_, super.c_);
        int cardX = (super.b_ - 224) >> 1;
        int cardY = 6 + GameConfig.contentOffsetY;
        int cardW = 224;
        int cardH = 289;
        int elem = this.selectedHeroIndex % 3;
        int elemBorder = elem == 0 ? 0xE67E45 : (elem == 1 ? 0xFFD700 : 0x4DF5E8);
        int elemHighlight = elem == 0 ? 0xFFB88C : (elem == 1 ? 0xFFF2A6 : 0xA6FFF6);

        DrawUtils.drawAntiquePanel(
                g, cardX, cardY, cardW, cardH,
                0x362417, 0x22150D, 0x130B06,
                elemBorder, elemHighlight, 0xFFE680);

        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int headerH = boldH + 6;
        DrawUtils.drawAntiquePanel(
                g, cardX + 6, cardY + 5, cardW - 12, headerH,
                0x1A5E54, 0x114039, 0x0A2622,
                0xB8893D, 0xF0C975, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD,
                this.enteringNameMode ? "ĐẶT DANH HIỆU CHỦ TƯỚNG" : "CHỌN CHỦ TƯỚNG XUẤT CHINH",
                cardX + (cardW >> 1), cardY + 5 + ((headerH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        if (!this.enteringNameMode) {
            this.drawHeroDossierView(g, cardX, cardY, cardW, cardH, elem, boldH, plainH);
        } else {
            this.drawNameSealView(g, cardX, cardY, cardW, cardH, elem, boldH, plainH);
        }
    }

    private void drawHeroDossierView(
            Graphics g, int cardX, int cardY, int cardW, int cardH, int elem, int boldH, int plainH) {
        int centerX = cardX + (cardW >> 1);
        int cellY = cardY + 30;
        int idx = 0;
        for (int row = 0; row < 2; ++row) {
            int cellX = cardX + 26;
            for (int col = 0; col < 3; ++col) {
                boolean sel = (this.selectedHeroIndex == idx);
                boolean hov = HdInputState.isHovered(cellX, cellY, 36, 36);
                if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                    HdUiRenderer.drawHdItemSocket(g.getHdCanvas(), cellX, cellY, 36, 36, sel, hov, false);
                    g.drawImage(this.heroAvatars[idx], cellX + 3, cellY + 3, 0);
                } else {
                    int focusSrcX = (sel && this.blinkTick % 2 == 0) ? 0 : 36;
                    DrawUtils.drawRegion(g, this.focusBoxSheet, focusSrcX, 0, 36, 36, cellX, cellY, 0);
                    g.drawImage(this.heroAvatars[idx], cellX + 3, cellY + 3, 0);
                    if (sel) {
                        g.setColor(0xFFD700);
                        g.drawRect(cellX - 1, cellY - 1, 37, 37);
                    }
                }
                cellX += 64;
                ++idx;
            }
            cellY += 40;
        }

        int curY = cardY + 110;
        g.setColor(0x7A592A);
        g.drawLine(cardX + 10, curY, cardX + cardW - 10, curY);
        curY += 3;

        String elemTitle = HERO_TITLES[this.selectedHeroIndex] + "  |  "
                + (elem == 0 ? "Hệ Hỏa (Hỏa Linh)" : (elem == 1 ? "Hệ Lôi (Lôi Điện)" : "Hệ Thủy (Băng Tâm)"));
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, elemTitle,
                centerX, curY, Graphics.TOP | Graphics.HCENTER,
                elem == 0 ? 0xFF9966 : (elem == 1 ? 0xFFE680 : 0x8CF5E8), 0x120804);
        curY += boldH + 1;

        String startStatsLine = "Khởi điểm: Máu " + this.baseHpByHero[this.selectedHeroIndex]
                + "  |  Công " + this.baseAtkByHero[this.selectedHeroIndex];
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, startStatsLine,
                centerX, curY, Graphics.TOP | Graphics.HCENTER,
                0xFFF4DC, 0x120804);
        curY += plainH + 1;

        String breakthroughStepLine = "Mỗi 10 cấp: " + HeroStats.getBreakthroughStepDesc(this.selectedHeroIndex);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, breakthroughStepLine,
                centerX, curY, Graphics.TOP | Graphics.HCENTER,
                0x7CFF9E, 0x120804);
        curY += plainH + 2;

        // 3 Elemental Skills preview box
        int skillBoxH = plainH * 2 + 5;
        DrawUtils.drawAntiquePanel(
                g, cardX + 8, curY, cardW - 16, skillBoxH,
                0x26180F, 0x190F09, 0x0E0804,
                0x8F6932, 0xCCA052, 0xFFE680);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, "3 Tuyệt chiêu đặc trưng (12 / 24 / 36 Nộ):",
                centerX, curY + 2, Graphics.TOP | Graphics.HCENTER,
                0xD8C8B0, 0x100603);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, ELEMENT_SKILLS_LINE[elem],
                centerX, curY + 3 + plainH, Graphics.TOP | Graphics.HCENTER,
                0xFFF5B8, 0x100603);
        curY += skillBoxH + 3;

        String[] passiveLines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, ELEMENT_PASSIVE_PERK[elem], cardW - 22);
        int passiveBoxH = DrawUtils.getWrappedHeight(DrawUtils.UI_FONT_PLAIN, passiveLines) + 6;
        DrawUtils.drawAntiquePanel(
                g, cardX + 8, curY, cardW - 16, passiveBoxH,
                0x1A2E2A, 0x101E1B, 0x091210,
                0x33D6C4, 0x8CF5E8, 0xFFFFFF);
        DrawUtils.drawWrappedShadowText(
                g, DrawUtils.UI_FONT_PLAIN, passiveLines,
                cardX + 12, curY + 3, cardW - 24, passiveBoxH - 4,
                0x99FFF2, 0x091210);
        curY += passiveBoxH + 3;

        int maxBioH = (cardY + cardH - 5) - curY;
        if (maxBioH > plainH) {
            DrawUtils.drawWrappedShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, this.wrappedBio,
                    cardX + 10, curY, cardW - 20, maxBioH,
                    0xFFF4DC, 0x120804);
        }
    }

    private void drawNameSealView(
            Graphics g, int cardX, int cardY, int cardW, int cardH, int elem, int boldH, int plainH) {
        int centerX = cardX + (cardW >> 1);
        int topRowY = cardY + 30;

        // Selected hero badge + name input field
        DrawUtils.drawAntiquePanel(
                g, cardX + 10, topRowY, 38, 38,
                0x3D291A, 0x1C110A, 0x100905,
                0xC29444, 0xF5D27E, 0xFFFFFF);
        g.drawImage(this.heroAvatars[this.selectedHeroIndex], cardX + 14, topRowY + 4, 0);

        int inputX = cardX + 54;
        int inputW = cardW - 64;
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN,
                HERO_TITLES[this.selectedHeroIndex] + " (2-10 ký tự):",
                inputX, topRowY, Graphics.TOP | Graphics.LEFT,
                elem == 0 ? 0xFF9966 : (elem == 1 ? 0xFFE680 : 0x8CF5E8), 0x120804);

        int boxY = topRowY + plainH + 3;
        int boxH = 22;
        DrawUtils.drawAntiquePanel(
                g, inputX, boxY, inputW, boxH,
                0x1A1009, 0x120A06, 0x0A0503,
                0xFFD700, 0xFFF2A6, 0xFFFFFF);
        String cursor = (this.blinkTick < 4 && this.enteredName.length() < 10) ? "_" : "";
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, this.enteredName + cursor,
                inputX + 8, boxY + ((boxH - boldH) >> 1),
                Graphics.TOP | Graphics.LEFT,
                0xFFF5B8, 0x100603);

        // Quick action pills: [Gợi ý tên] | [Cách] | [Xóa <-]
        int pillY = cardY + 78;
        int pillH = 21;
        DrawUtils.drawAntiquePanel(
                g, cardX + 8, pillY, 74, pillH,
                0x1D594E, 0x113831, 0x0A211D,
                0xC89B46, 0xF5D27E, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "Gợi ý tên",
                cardX + 45, pillY + ((pillH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF5B8, 0x100603);

        DrawUtils.drawAntiquePanel(
                g, cardX + 86, pillY, 58, pillH,
                0x3B2718, 0x26180F, 0x140C07,
                0x9E7538, 0xD8AD5E, 0xFFE680);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, "Cách (_)",
                cardX + 115, pillY + ((pillH - plainH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4DC, 0x100603);

        DrawUtils.drawAntiquePanel(
                g, cardX + 148, pillY, 68, pillH,
                0x542026, 0x381418, 0x1F0A0C,
                0xB86B4B, 0xE69E7E, 0xF5C7A9);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "Xóa <-",
                cardX + 182, pillY + ((pillH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFE4D6, 0x140606);

        // 9x4 On-Screen Antique Character Grid
        int gridX = cardX + 9;
        int gridY = cardY + 106;
        int cellW = 23;
        int cellH = 27;
        for (int r = 0; r < KEYPAD_ROWS; ++r) {
            for (int c = 0; c < KEYPAD_COLS; ++c) {
                int kIdx = r * KEYPAD_COLS + c;
                int kx = gridX + c * cellW;
                int ky = gridY + r * cellH;
                boolean hov = HdInputState.isHovered(kx, ky, cellW - 1, cellH - 2);
                boolean focused = (kIdx == this.keypadIndex) || hov;
                DrawUtils.drawAntiquePanel(
                        g, kx, ky, cellW - 1, cellH - 2,
                        focused ? 0x246B5C : 0x332014,
                        focused ? 0x154239 : 0x21140C,
                        0x120A05,
                        focused ? 0xFFD700 : 0x8F6932,
                        focused ? 0xFFF2A6 : 0xCCA052,
                        0xFFFFFF);
                String chStr = String.valueOf(KEYPAD_CHARS.charAt(kIdx));
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, chStr,
                        kx + ((cellW - 1) >> 1), ky + ((cellH - 2 - boldH) >> 1),
                        Graphics.TOP | Graphics.HCENTER,
                        focused ? 0xFFF5B8 : 0xF2E2C4, 0x100603);
            }
        }

        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, "Chạm chữ cái trên bảng hoặc gõ phím trực tiếp",
                centerX, cardY + cardH - plainH - 8,
                Graphics.TOP | Graphics.HCENTER,
                0xD8C8B0, 0x120804);
    }

    public void a() {
        this.blinkTick = (this.blinkTick < 1) ? 8 : (this.blinkTick - 1);
        if (this.enteringNameMode && GameConfig.lastTypedKey != 0) {
            int k = GameConfig.lastTypedKey;
            GameConfig.lastTypedKey = 0;
            if (k == 8) {
                this.deleteLastChar();
            } else {
                this.appendChar((char) k);
            }
        }
    }

    public void onAction(int screenId, int actionId) {
        switch (actionId) {
            case ACTION_DISMISS_WARN:
                GUIManager.getInstance().a(false);
                GameConfig.textInputActive = this.enteringNameMode;
                break;
            case ACTION_LEFT_AUX:
                if (!this.enteringNameMode) {
                    this.selectedHeroIndex = (this.selectedHeroIndex + 1) % this.heroBios.length;
                    this.enteredName = "";
                    this.refreshBioWrap();
                } else {
                    this.cyclePresetName();
                }
                break;
            case ACTION_CONFIRM_PRIMARY:
                if (!this.enteringNameMode) {
                    this.openNameInputMode();
                } else {
                    if (!GameConfig.hasPointerEvents && KEYPAD_CHARS.length() > 0 && this.enteredName.length() == 0) {
                        this.appendChar(KEYPAD_CHARS.charAt(this.keypadIndex));
                    } else {
                        this.confirmHeroAndStartCampaign();
                    }
                }
                break;
            case ACTION_BACK:
                if (this.enteringNameMode) {
                    this.enteringNameMode = false;
                    this.syncSoftKeys();
                } else {
                    GameConfig.textInputActive = false;
                    GUIManager.getInstance().e.a(8, 2, true);
                }
                break;
        }
    }
}
