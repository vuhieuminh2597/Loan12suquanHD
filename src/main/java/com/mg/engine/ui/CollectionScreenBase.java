package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.sprite.StaticImageSprite;
import com.mg.engine.storage.EngineStorage;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Sacred Relic crafting and enhancement screen ("Báu vật").
 * Uses unified Enhancement Shards ("Mảnh Cường Hóa") + Gold to craft (0 -> +1) and enhance (+1 -> +5)
 * each of the 4 Sacred Relics with diminishing success rates and +10% fail-pity protection.
 */
public abstract class CollectionScreenBase extends BaseScreen implements ActionListener {
    private static final int ACTION_PROMPT_UPGRADE = 1;
    private static final int ACTION_CONFIRM_UPGRADE = 3;
    private static final int ACTION_CANCEL_UPGRADE = 4;
    private static final int ACTION_BACK = 5;
    private static final int ACTION_HELP = 6;
    private static final int ACTION_CLOSE_HELP = 7;

    private static final int CARD_W = 108;
    private static final int CARD_H = 74;
    private static final int CARD_COL0_X = 8;
    private static final int CARD_COL1_X = 124;
    private static final int CARD_ROW0_Y = 28;
    private static final int CARD_ROW1_Y = 108;

    protected int[] c;
    protected int d;
    protected int e = 0;
    protected int f;
    protected int[][] t;
    protected boolean u = false;
    private String[] relicNames;
    protected TextSoftKeyButton v;
    private int returnScreenId;
    protected StaticImageSprite[] w;
    protected int[] x;
    protected int[] y;
    private int flashRelicIdx = -1;
    private int flashTicks = 0;
    private boolean flashSuccess = false;
    private String statusFeedbackText = null;
    private int statusFeedbackColor = 0xE6D8C0;
    private int animTick = 0;
    protected Image z;
    protected Image A;
    protected Image[] B;
    protected RichContentDialog C;
    protected int D = 33;

    public CollectionScreenBase(int returnScreenId, String[] relicNames, int spriteAnchor) {
        super.q = 3;
        this.D = spriteAnchor;
        this.returnScreenId = returnScreenId;
        if (relicNames == null || relicNames.length < 4) {
            relicNames = new String[]{"Kính Bát Quái", "Khánh Kim Long", "Ngọc Bội", "Tượng Phật"};
        }
        this.relicNames = relicNames;
        this.b();
        this.u();
        this.C = new RichContentDialog("Hướng dẫn Báu Vật");
        this.C.a(new TextSoftKeyButton("Đóng", ACTION_CLOSE_HELP));
        this.v();
        this.a(new SoftKeyBar());
        this.a((ActionListener) this);
    }

    public final void v() {
        this.c = new int[EngineStorage.RELIC_COUNT];
        for (int i = 0; i < EngineStorage.RELIC_COUNT; ++i) {
            this.c[i] = EngineStorage.getRelicLevel(i);
        }
        if (this.e < 0 || this.e >= EngineStorage.RELIC_COUNT) {
            this.e = 0;
        }
        this.rebuildSoftKeys();
    }

    protected abstract void b();

    /** Hook invoked when a relic is crafted or enhanced so campaign hero stats update immediately. */
    protected void onRelicStatsUpdated() {
    }

    private void rebuildSoftKeys() {
        this.a(new TextSoftKeyButton("Cần biết", ACTION_HELP));
        this.b(new TextSoftKeyButton("Trở về", ACTION_BACK));
        int lv = EngineStorage.getRelicLevel(this.e);
        if (lv <= 0) {
            this.v = new TextSoftKeyButton("Ghép", ACTION_PROMPT_UPGRADE);
            this.c(this.v);
        } else if (lv < EngineStorage.RELIC_MAX_LEVEL) {
            this.v = new TextSoftKeyButton("Nâng cấp", ACTION_PROMPT_UPGRADE);
            this.c(this.v);
        } else {
            this.c((SoftKeyButton) null);
        }
    }

    protected void u() {
        int ox = GameConfig.contentOffsetX;
        int oy = GameConfig.contentOffsetY;
        this.x = new int[]{ox + CARD_COL0_X, ox + CARD_COL1_X, ox + CARD_COL0_X, ox + CARD_COL1_X};
        this.y = new int[]{oy + CARD_ROW0_Y, oy + CARD_ROW0_Y, oy + CARD_ROW1_Y, oy + CARD_ROW1_Y};
    }

    public void a(int keyCode) {
        int prev = this.e;
        switch (keyCode) {
            case 14: // LEFT
                this.e = (this.e % 2 == 1) ? (this.e - 1) : (this.e + 1);
                break;
            case 15: // RIGHT
                this.e = (this.e % 2 == 0) ? (this.e + 1) : (this.e - 1);
                break;
            case 12: // UP
                this.e = (this.e >= 2) ? (this.e - 2) : (this.e + 2);
                break;
            case 13: // DOWN
                this.e = (this.e < 2) ? (this.e + 2) : (this.e - 2);
                break;
        }
        if (this.e != prev) {
            this.statusFeedbackText = null;
            this.rebuildSoftKeys();
        }
    }

    public void a(int pointerX, int pointerY) {
        for (int i = 0; i < EngineStorage.RELIC_COUNT; ++i) {
            int cx = this.x[i];
            int cy = this.y[i];
            if (pointerX >= cx && pointerX <= cx + CARD_W && pointerY >= cy && pointerY <= cy + CARD_H) {
                if (this.e == i) {
                    if (EngineStorage.getRelicLevel(i) < EngineStorage.RELIC_MAX_LEVEL) {
                        this.onAction(super.q, ACTION_PROMPT_UPGRADE);
                    }
                } else {
                    this.e = i;
                    this.statusFeedbackText = null;
                    this.rebuildSoftKeys();
                }
                return;
            }
        }
    }

    public void a(Graphics g) {
        this.e(g);
        this.b(g);
    }

    protected void e(Graphics g) {
        if (super.r == null) {
            g.setColor(GameConfig.frameBackgroundColor);
            g.fillRect(0, 0, super.b_, super.c_);
        }
    }

    protected void b(Graphics g) {
        int ox = GameConfig.contentOffsetX;
        int oy = GameConfig.contentOffsetY;
        int shards = EngineStorage.getEnhancementShards();
        int gold = EngineStorage.f(2);
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);

        // 1. Top Resource Bar: Enhancement Shards & Gold
        DrawUtils.drawAntiquePanel(
                g, ox + 6, oy + 4, 228, 20,
                0x1A4D45, 0x10332D, 0x0A1F1B,
                0xB8893D, 0xF0C975, 0xFFE680);
        int topTextY = oy + 4 + ((20 - boldH) >> 1);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "Mảnh CH: " + shards,
                ox + 13, topTextY, Graphics.TOP | Graphics.LEFT,
                0x8CF5E8, 0x100805);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "Vàng: " + gold,
                ox + 227, topTextY, Graphics.TOP | Graphics.RIGHT,
                0xFFE680, 0x100805);

        // 2. 2x2 Sacred Relic Cards
        for (int i = 0; i < EngineStorage.RELIC_COUNT; ++i) {
            int cx = this.x[i];
            int cy = this.y[i];
            int lv = EngineStorage.getRelicLevel(i);
            boolean selected = (this.e == i);
            boolean hovered = HdInputState.isHovered(cx, cy, CARD_W, CARD_H);
            boolean flashing = (this.flashTicks > 0 && this.flashRelicIdx == i && (this.flashTicks & 2) == 0);

            int topBg;
            int botBg;
            int border;
            if (flashing) {
                topBg = this.flashSuccess ? 0x2A7A58 : 0x7A2424;
                botBg = this.flashSuccess ? 0x174D36 : 0x4D1414;
                border = this.flashSuccess ? 0x7CFF9E : 0xFF6B6B;
            } else if (selected) {
                topBg = lv > 0 ? 0x26594E : 0x4A331E;
                botBg = lv > 0 ? 0x173B33 : 0x2E1E10;
                border = 0xFFD700;
            } else if (hovered) {
                topBg = lv > 0 ? 0x3F2B1D : 0x362418;
                botBg = lv > 0 ? 0x291A10 : 0x21150D;
                border = 0xF5D27E;
            } else {
                topBg = lv > 0 ? 0x332216 : 0x261A12;
                botBg = lv > 0 ? 0x21150D : 0x170F0A;
                border = lv >= EngineStorage.RELIC_MAX_LEVEL ? 0xE6B84C : (lv > 0 ? 0x8F6932 : 0x5C4326);
            }
            DrawUtils.drawAntiquePanel(g, cx, cy, CARD_W, CARD_H, topBg, botBg, 0x100805, border, 0xF5D27E, 0xFFFFFF);

            // Medallion socket + relic icon
            int medX = cx + ((CARD_W - 34) >> 1);
            int medY = cy + 4;
            if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                HdUiRenderer.drawHdItemSocket(g.getHdCanvas(), medX, medY, 34, 34, selected, hovered, lv >= EngineStorage.RELIC_MAX_LEVEL);
            } else {
                DrawUtils.drawAntiquePanel(
                        g, medX, medY, 34, 34,
                        lv > 0 ? 0x1E3D36 : 0x1A120D,
                        lv > 0 ? 0x112621 : 0x100A07,
                        0x0A0604,
                        lv > 0 ? 0xC99B44 : 0x5A4329, 0xF2CC79, 0xFFFFFF);
            }
            if (this.B != null && i < this.B.length && this.B[i] != null) {
                g.drawImage(this.B[i], medX + 17, medY + 17, Graphics.HCENTER | Graphics.VCENTER);
            }
            if (lv == 0) {
                DrawUtils.drawHatchedOverlay(g, 0x120A06, medX + 2, medY + 2, 30, 30);
            }

            // Relic Name
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.relicNames[i],
                    cx + (CARD_W >> 1), cy + 40, Graphics.TOP | Graphics.HCENTER,
                    selected ? 0xFFF5B8 : (lv > 0 ? 0xFFE680 : 0xC8B8A2), 0x120804);

            // Level / Craft readiness line
            String lvLabel;
            int lvColor;
            if (lv == 0) {
                int need = EngineStorage.getRelicUpgradeShardCost(0);
                if (shards >= need) {
                    lvLabel = "Đủ ghép (" + need + "M)";
                    lvColor = 0x7CFF9E;
                } else {
                    lvLabel = "Chưa mở (" + shards + "/" + need + "M)";
                    lvColor = 0xB0A08E;
                }
            } else if (lv >= EngineStorage.RELIC_MAX_LEVEL) {
                lvLabel = "TỐI ĐA (+5)";
                lvColor = 0xFFE680;
            } else {
                int sCost = EngineStorage.getRelicUpgradeShardCost(lv);
                int gCost = EngineStorage.getRelicUpgradeGoldCost(lv);
                boolean ready = shards >= sCost && gold >= gCost;
                lvLabel = ready ? ("Cấp +" + lv + " [Nâng!]") : ("Cấp +" + lv + " / 5");
                lvColor = ready ? 0x7CFF9E : 0x8CF5E8;
            }
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, lvLabel,
                    cx + (CARD_W >> 1), cy + 56, Graphics.TOP | Graphics.HCENTER,
                    lvColor, 0x120804);
        }

        // 3. Bottom Detail & Enhancement Card (y = 185..293, static wrapped descriptions)
        int detX = ox + 6;
        int detY = oy + 185;
        int detW = 228;
        int detH = 108;
        DrawUtils.drawAntiquePanel(
                g, detX, detY, detW, detH,
                0x362417, 0x22150D, 0x130B06,
                0xB8893D, 0xF0C975, 0xFFE680);

        int sel = this.e;
        int selLv = EngineStorage.getRelicLevel(sel);
        int curY = detY + 5;
        String headerTitle;
        if (selLv == 0) {
            headerTitle = this.relicNames[sel] + "  [Chưa mở]";
        } else if (selLv >= EngineStorage.RELIC_MAX_LEVEL) {
            headerTitle = this.relicNames[sel] + "  [Tối đa +5]";
        } else {
            headerTitle = this.relicNames[sel] + "  (+" + selLv + " -> +" + (selLv + 1) + ")";
        }
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, headerTitle,
                detX + (detW >> 1), curY, Graphics.TOP | Graphics.HCENTER,
                0xFFE680, 0x120804);
        curY += boldH + 2;

        String statLine;
        if (selLv == 0) {
            statLine = "Ghép mở: " + EngineStorage.getRelicStatDesc(sel, 1) + " vĩnh viễn";
        } else if (selLv >= EngineStorage.RELIC_MAX_LEVEL) {
            statLine = "Hiệu lực: " + EngineStorage.getRelicStatDesc(sel, selLv) + " vĩnh viễn";
        } else {
            statLine = EngineStorage.getRelicStatDesc(sel, selLv) + "  ->  " + EngineStorage.getRelicStatDesc(sel, selLv + 1);
        }
        String[] statLines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, statLine, detW - 16);
        int statTextH = DrawUtils.getWrappedHeight(DrawUtils.UI_FONT_PLAIN, statLines);
        DrawUtils.drawWrappedShadowText(
                g, DrawUtils.UI_FONT_PLAIN, statLines,
                detX + 8, curY, detW - 16, statTextH + 2,
                0x7CFF9E, 0x120804);
        curY += statTextH + 3;

        g.setColor(0x7A592A);
        g.drawLine(detX + 8, curY, detX + detW - 8, curY);
        curY += 3;

        if (selLv < EngineStorage.RELIC_MAX_LEVEL) {
            int sCost = EngineStorage.getRelicUpgradeShardCost(selLv);
            int gCost = EngineStorage.getRelicUpgradeGoldCost(selLv);
            boolean enoughS = shards >= sCost;
            boolean enoughG = gold >= gCost;
            String costLine = "Cần: " + sCost + " Mảnh CH" + (gCost > 0 ? (" + " + gCost + " Vàng") : " (0 Vàng)");
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, costLine,
                    detX + 8, curY, Graphics.TOP | Graphics.LEFT,
                    (enoughS && enoughG) ? 0xFFF4DC : 0xFF8C7A, 0x120804);
            curY += plainH + 2;

            int effChance = EngineStorage.getRelicUpgradeSuccessChance(sel);
            int fails = EngineStorage.getRelicFailCount(sel);
            String rateLine = "Tỉ lệ thành công: " + effChance + "%"
                    + (fails > 0 ? (" (+" + (fails * EngineStorage.RELIC_FAIL_PITY_STEP) + "% chúc phúc)") : "");
            int rateColor = effChance >= 80 ? 0x7CFF9E : (effChance >= 50 ? 0xFFE680 : 0xFFAA44);
            String[] rateLines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, rateLine, detW - 16);
            int rateTextH = DrawUtils.getWrappedHeight(DrawUtils.UI_FONT_PLAIN, rateLines);
            DrawUtils.drawWrappedShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, rateLines,
                    detX + 8, curY, detW - 16, rateTextH + 2,
                    rateColor, 0x120804);
            curY += rateTextH + 3;
        } else {
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, "Báu Vật đã đạt cấp tối đa (+5)!",
                    detX + (detW >> 1), curY + 2, Graphics.TOP | Graphics.HCENTER,
                    0xFFE680, 0x120804);
            curY += boldH + plainH + 5;
        }

        g.setColor(0x7A592A);
        g.drawLine(detX + 8, curY, detX + detW - 8, curY);
        curY += 3;

        String footText = this.statusFeedbackText != null
                ? this.statusFeedbackText
                : "Kiếm Mảnh: Ghép 5+/L-T, Combo x4+, Thắng (+/-12 cấp), Nhiệm vụ.";
        int footColor = this.statusFeedbackText != null ? this.statusFeedbackColor : 0xD8C8B0;
        String[] footLines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, footText, detW - 16);
        DrawUtils.drawWrappedShadowText(
                g, DrawUtils.UI_FONT_PLAIN, footLines,
                detX + 8, curY, detW - 16, (detY + detH - 4) - curY,
                footColor, 0x120804);
    }

    public void a() {
        ++this.animTick;
        if (this.flashTicks > 0) {
            --this.flashTicks;
        }
        this.f = (this.f < 4) ? (this.f + 1) : 0;
        if (GUIManager.getController() == null || GUIManager.getController().d == null) {
            int wheel = HdInputState.consumeWheelDelta();
            if (wheel != 0) {
                int count = EngineStorage.RELIC_COUNT;
                this.e = (this.e + (wheel > 0 ? 1 : -1) + count) % count;
                this.statusFeedbackText = null;
                this.rebuildSoftKeys();
            }
        }
    }

    public void e(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < EngineStorage.RELIC_COUNT) {
            this.e = slotIndex;
        }
        this.v();
    }

    public void onAction(int screenId, int actionId) {
        if (actionId == ACTION_PROMPT_UPGRADE) {
            int lv = EngineStorage.getRelicLevel(this.e);
            if (lv >= EngineStorage.RELIC_MAX_LEVEL) {
                return;
            }
            int sCost = EngineStorage.getRelicUpgradeShardCost(lv);
            int gCost = EngineStorage.getRelicUpgradeGoldCost(lv);
            int shards = EngineStorage.getEnhancementShards();
            int gold = EngineStorage.f(2);
            if (shards < sCost) {
                this.statusFeedbackText = "Thiếu Mảnh Cường Hóa (cần " + sCost + ", hiện có " + shards + ")!";
                this.statusFeedbackColor = 0xFF7A7A;
                return;
            }
            if (gCost > 0 && gold < gCost) {
                this.statusFeedbackText = "Thiếu Vàng (cần " + gCost + "v, hiện có " + gold + "v)!";
                this.statusFeedbackColor = 0xFF7A7A;
                return;
            }
            int chance = EngineStorage.getRelicUpgradeSuccessChance(this.e);
            String title = lv == 0 ? "Ghép Báu Vật" : "Cường Hóa Báu Vật";
            String msg;
            if (lv == 0) {
                msg = "Dùng " + sCost + " Mảnh Cường Hóa để ghép mở " + this.relicNames[this.e]
                        + " (" + EngineStorage.getRelicStatDesc(this.e, 1) + ", tỉ lệ 100%)?";
            } else {
                msg = "Cường hóa " + this.relicNames[this.e] + " từ +" + lv + " lên +" + (lv + 1)
                        + " với " + sCost + " Mảnh CH & " + gCost + " Vàng? Tỉ lệ thành công: " + chance
                        + "% (thất bại không tụt cấp, +10% chúc phúc).";
            }
            BaseDialog confirm = GUIManager.a(title, msg, lv == 0 ? "Ghép" : "Nâng cấp", ACTION_CONFIRM_UPGRADE, "Không", ACTION_CANCEL_UPGRADE, 1);
            confirm.a((ActionListener) this);
            GUIManager.getInstance().a(confirm);
        } else if (actionId == ACTION_CONFIRM_UPGRADE) {
            GUIManager.getInstance().a(true);
            int res = EngineStorage.attemptUpgradeRelic(this.e);
            this.v();
            if (res == 1) {
                int newLv = EngineStorage.getRelicLevel(this.e);
                this.flashRelicIdx = this.e;
                this.flashTicks = 18;
                this.flashSuccess = true;
                this.statusFeedbackText = "THÀNH CÔNG! " + this.relicNames[this.e] + " đạt Cấp +" + newLv
                        + " (" + EngineStorage.getRelicStatDesc(this.e, newLv) + ")!";
                this.statusFeedbackColor = 0x7CFF9E;
                this.onRelicStatsUpdated();
            } else if (res == 0) {
                int nextChance = EngineStorage.getRelicUpgradeSuccessChance(this.e);
                this.flashRelicIdx = this.e;
                this.flashTicks = 18;
                this.flashSuccess = false;
                this.statusFeedbackText = "Thất bại! Không tụt cấp, tích +10% chúc phúc (lần sau: " + nextChance + "%).";
                this.statusFeedbackColor = 0xFFAA44;
            } else if (res == -1) {
                this.statusFeedbackText = "Không đủ Mảnh Cường Hóa!";
                this.statusFeedbackColor = 0xFF7A7A;
            } else if (res == -3) {
                this.statusFeedbackText = "Không đủ Vàng!";
                this.statusFeedbackColor = 0xFF7A7A;
            }
        } else if (actionId == ACTION_CANCEL_UPGRADE) {
            GUIManager.getInstance().a(true);
        } else if (actionId == ACTION_BACK) {
            if (this.returnScreenId == -1) {
                this.returnScreenId = 2;
            }
            this.f(this.returnScreenId);
        } else if (actionId == ACTION_HELP) {
            ContentBlock[] blocks = new ContentBlock[]{
                    new TextBlock("Dùng Mảnh Cường Hóa để ghép mở (+1) và nâng cấp 4 Báu Vật lên tối đa Cấp +5:", 0),
                    new TextBlock("- Nguồn kiếm Mảnh: ghép 5+ / L-T (12-25%), combo liên hoàn x4+ (15-30%, kể cả từ tuyệt chiêu), thắng địch +/-12 cấp (15-50%), và thưởng Nhiệm Vụ.", 0),
                    new TextBlock("- Tỉ lệ nâng cấp: +1 (100%), +2 (80%), +3 (60%), +4 (40%), +5 (25%). Thất bại không tụt cấp và cộng +10% chúc phúc cho lần đập tiếp theo!", 0),
                    new TextBlock(this.relicNames[0] + ": +4/7/10/13/16% Công vĩnh viễn.", 0),
                    new ImageBlock(this.B[0]),
                    new TextBlock(this.relicNames[1] + ": +5/8/12/16/20% Máu tối đa vĩnh viễn.", 0),
                    new ImageBlock(this.B[1]),
                    new TextBlock(this.relicNames[2] + ": +2/3/4/5/6 Nộ khởi đầu mỗi trận.", 0),
                    new ImageBlock(this.B[2]),
                    new TextBlock(this.relicNames[3] + ": +150..600 Lương & +6..25% hồi Tim.", 0),
                    new ImageBlock(this.B[3])
            };
            this.C.m();
            this.C.b(new TextSoftKeyButton("Đóng", ACTION_CLOSE_HELP));
            this.C.a(blocks);
            this.C.a((ActionListener) this);
            GUIManager.getInstance().a(this.C);
        } else if (actionId == ACTION_CLOSE_HELP) {
            GUIManager.getInstance().a(false);
        }
    }

    public void f(int targetScreenId) {
        GUIManager.getInstance().e.a(3, this.returnScreenId, true);
    }

    static void a(CollectionScreenBase screen, String label) {
    }

    static void a(CollectionScreenBase screen, boolean completed) {
    }

    static void a(CollectionScreenBase screen) {
        screen.v();
    }
}
