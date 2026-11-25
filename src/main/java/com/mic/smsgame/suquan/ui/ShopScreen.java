package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.SmallNumberFont;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.gfx.ScrollFrameRenderer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * In-game shop screen ("Cửa hàng") with pack-quantity badges, per-battle carry limits,
 * safe confirmation prompt, and instant inline purchase toast feedback (no double-popup friction).
 */
public class ShopScreen extends BaseScreen implements ActionListener {
    private static final int ACTION_PROMPT_BUY = -1;
    private static final int ACTION_BACK = -3;
    private static final int ACTION_CONFIRM_BUY = -4;
    private static final int ACTION_DISMISS_DIALOG = -5;
    private static final int ACTION_OPEN_RELICS = -7;

    /** Quantity granted per purchase for each of the 6 shop items. */
    public static final int[] c = new int[]{3, 2, 1, 3, 3, 3};

    private static final String[] PACK_BADGES = new String[]{"x3", "x2", "Gói", "x3", "x3", "x3"};

    private Image backgroundBuffer;
    private Image itemIconsSheet;
    private Image[] scrollFrames;
    private Image dividerImage;
    private Image focusBoxSheet;
    private Image goldCoinIcon;
    private final int[] itemPrices = new int[]{300, 450, 280, 240, 120, 150};
    private boolean hidden;
    private String[] wrappedDescription;
    private int selectedIndex;
    private String[] itemDescriptions;
    private int pendingPurchaseIndex;
    private int returnScreenId;
    private SmallNumberFont smallNumberFont;
    private int[] inventoryCounts;
    private String priceLabel;
    private String purchaseToastText;
    private int purchaseToastTicks;
    private int flashItemIndex = -1;

    public ShopScreen(int returnScreenId) {
        this.returnScreenId = returnScreenId;
        this.initResources();
        super.g = (super.b_ - this.scrollFrames[0].getWidth()) >> 1;
        super.a_ = ((super.c_ - 180) >> 1) - 10;
        this.itemDescriptions = GameStrings.d;
        this.inventoryCounts = EngineStorage.v();
        if (this.inventoryCounts == null) {
            this.inventoryCounts = new int[6];
        }
        this.smallNumberFont = new SmallNumberFont();
        this.updateSelectedDescription();
        this.initSoftKeys();
        super.q = 4;
    }

    private void initResources() {
        this.backgroundBuffer = MgImageLoader.load("/bkmenu");
        this.itemIconsSheet = MgImageLoader.load("/itemshops");
        this.scrollFrames = new Image[]{
                MgImageLoader.load("/frame1"),
                MgImageLoader.load("/frame2")
        };
        this.dividerImage = MgImageLoader.load("/frame3");
        this.focusBoxSheet = MgImageLoader.load("/focusitem");
        this.goldCoinIcon = MgImageLoader.load("/gold");
        this.a(new SoftKeyBar());
        this.a((ActionListener) this);
    }

    public void a(Graphics g) {
        g.setColor(0x160D08);
        g.fillRect(0, 0, super.b_, super.c_);
        if (this.backgroundBuffer != null) {
            g.drawImage(this.backgroundBuffer, GameConfig.contentOffsetX, GameConfig.contentOffsetY, 0);
        }
        DrawUtils.drawHatchedOverlay(g, 0x160D08, 0, 0, super.b_, super.c_);
        ScrollFrameRenderer.a(g, super.g, super.a_, 208, ScrollFrameRenderer.ANTIQUE_SCROLL_BG, this.scrollFrames);
        if (this.hidden) {
            return;
        }
        boolean hd = HdConfig.isHdRemaster() && g.getHdCanvas() != null;
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int cellY = super.a_ + 12;
        int itemIdx = 0;
        for (int row = 0; row < 2; ++row) {
            int cellX = super.g + 25;
            for (int col = 0; col < 3; ++col) {
                boolean selected = (this.selectedIndex == itemIdx);
                boolean hovered = HdInputState.isHovered(cellX, cellY, 36, 36);
                boolean justBought = (this.purchaseToastTicks > 0 && this.flashItemIndex == itemIdx && (this.purchaseToastTicks & 2) == 0);
                if (hd) {
                    HdUiRenderer.drawHdItemSocket(g.getHdCanvas(), cellX, cellY, 36, 36, selected, hovered, justBought);
                } else {
                    int focusSrcX = selected ? 0 : 36;
                    DrawUtils.drawRegion(g, this.focusBoxSheet, focusSrcX, 0, 36, 36, cellX, cellY, 0);
                    if (justBought) {
                        g.setColor(0x7CFF9E);
                        g.drawRect(cellX - 1, cellY - 1, 37, 37);
                    }
                }
                DrawUtils.drawRegion(g, this.itemIconsSheet, itemIdx * 28, 0, 28, 28, cellX + 4, cellY + 4, 0);

                // Top-left pack quantity badge (x3 / x2 / Gói)
                String packTag = PACK_BADGES[itemIdx];
                int tagW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, packTag) + 4;
                if (hd) {
                    HdCanvas2D g2 = g.getHdCanvas();
                    int s = HdConfig.HD_SCALE;
                    g2.setColorRgba(14, 8, 5, 220);
                    g2.fillRoundRect((cellX + 1) * s, (cellY + 1) * s, tagW * s, plainH * s, 7);
                    g2.setColor(0xB8893D);
                    g2.setStrokeWidth(1.3f);
                    g2.drawRoundRect((cellX + 1) * s, (cellY + 1) * s, tagW * s, plainH * s, 7);
                } else {
                    g.setColor(0x160D08);
                    g.fillRect(cellX + 1, cellY + 1, tagW, plainH);
                }
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_PLAIN, packTag,
                        cellX + 3, cellY, Graphics.TOP | Graphics.LEFT,
                        0xFFE680, 0x0A0503);

                if (itemIdx != 2) {
                    this.smallNumberFont.a(g, this.inventoryCounts[itemIdx], cellX + 34, cellY + 29, 2);
                }
                cellX += 45;
                ++itemIdx;
            }
            cellY += 39;
        }

        int centerX = super.g + this.scrollFrames[0].getWidth() / 2;
        int scrollW = this.scrollFrames[0].getWidth();
        if (hd) {
            HdUiRenderer.drawHdDivider(g.getHdCanvas(), centerX, cellY, scrollW - 26);
        } else {
            g.drawImage(this.dividerImage, centerX, cellY, Graphics.TOP | Graphics.HCENTER);
        }
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, GameStrings.c[this.selectedIndex],
                centerX, cellY + 4, Graphics.TOP | Graphics.HCENTER,
                0xFFE680, 0x120804);
        int descY = cellY + 5 + boldH;
        int priceY = super.a_ + 180;
        DrawUtils.drawWrappedShadowText(
                g, DrawUtils.UI_FONT_PLAIN, this.wrappedDescription,
                super.g + 11, descY, scrollW - 22, priceY - descY - 3,
                0xFFF4DC, 0x120804);

        if (this.purchaseToastTicks > 0 && this.purchaseToastText != null) {
            int toastW = scrollW - 18;
            int toastH = plainH + 6;
            int toastX = super.g + 9;
            int toastY = priceY - 2;
            DrawUtils.drawAntiquePanel(
                    g, toastX, toastY, toastW, toastH,
                    0x1D6355, 0x113D34, 0x0A241E,
                    0x7CFF9E, 0xC4FFD4, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.purchaseToastText,
                    centerX, toastY + ((toastH - boldH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    0xFFF5B8, 0x0A1F1A);
        } else {
            boolean canAfford = this.inventoryCounts[2] >= this.itemPrices[this.selectedIndex];
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.priceLabel,
                    centerX, priceY, Graphics.TOP | Graphics.HCENTER,
                    canAfford ? 0x7CFF9E : 0xFF8C7A, 0x120804);
        }

        // Top-left Gold badge + Top-right title plaque
        String goldText = "" + this.inventoryCounts[2];
        int goldBadgeW = 28 + DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, goldText);
        if (goldBadgeW < 58) {
            goldBadgeW = 58;
        }
        DrawUtils.drawAntiquePanel(
                g, 3, 3, goldBadgeW, 20,
                0x362417, 0x22150D, 0x130B06,
                0xB8893D, 0xF0C975, 0xFFE680);
        g.drawImage(this.goldCoinIcon, 3, 1, 0);
        int goldTextY = 3 + ((20 - boldH) >> 1);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, goldText,
                26, goldTextY, Graphics.TOP | Graphics.LEFT,
                0xFFE680, 0x120804);

        String shopTag = "CỬA HÀNG QUÂN NHU";
        int tagW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, shopTag) + 12;
        int tagX = GameConfig.screenWidth - tagW - 3;
        DrawUtils.drawAntiquePanel(
                g, tagX, 3, tagW, 20,
                0x1A5E54, 0x114039, 0x0A2622,
                0xB8893D, 0xF0C975, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, shopTag,
                tagX + (tagW >> 1), 3 + ((20 - plainH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);
    }

    private void initSoftKeys() {
        this.m();
        if (!this.hidden) {
            String relicLabel = EngineStorage.canCraftOrUpgradeAnyRelic() ? "Báu vật!" : "Báu vật";
            this.a(new TextSoftKeyButton(relicLabel, ACTION_OPEN_RELICS));
            this.c(new TextSoftKeyButton("Mua", ACTION_PROMPT_BUY));
            this.b(new TextSoftKeyButton("Trở về", ACTION_BACK));
        }
    }

    public void a() {
        if (this.purchaseToastTicks > 0) {
            --this.purchaseToastTicks;
        }
        if (!this.hidden && (GUIManager.getController() == null || GUIManager.getController().d == null)) {
            int wheel = HdInputState.consumeWheelDelta();
            if (wheel != 0) {
                int count = this.itemPrices.length;
                this.selectedIndex = (this.selectedIndex + (wheel > 0 ? 1 : -1) + count) % count;
                this.purchaseToastTicks = 0;
                this.updateSelectedDescription();
            }
        }
    }

    public void a(int keyCode) {
        if (this.hidden) {
            return;
        }
        int prev = this.selectedIndex;
        int count = this.itemPrices.length;
        switch (keyCode) {
            case 12:
                this.selectedIndex = (this.selectedIndex - 3 + count) % count;
                break;
            case 13:
                this.selectedIndex = (this.selectedIndex + 3) % count;
                break;
            case 14:
                this.selectedIndex = (this.selectedIndex - 1 + count) % count;
                break;
            case 15:
                this.selectedIndex = (this.selectedIndex + 1) % count;
                break;
        }
        if (this.selectedIndex != prev) {
            this.purchaseToastTicks = 0;
            this.updateSelectedDescription();
        }
    }

    private void updateSelectedDescription() {
        this.wrappedDescription = DrawUtils.wrapText(
                DrawUtils.UI_FONT_PLAIN, this.itemDescriptions[this.selectedIndex], this.scrollFrames[0].getWidth() - 22);
        int price = this.itemPrices[this.selectedIndex];
        if (price > 0) {
            String packInfo = (this.selectedIndex == 2) ? "Gói 5 món" : ("Gói x" + c[this.selectedIndex]);
            this.priceLabel = packInfo + " - Giá: " + price + " vàng";
        } else {
            this.priceLabel = "Miễn phí (Offline)";
        }
    }

    public void a(int pointerX, int pointerY) {
        if (this.hidden) {
            return;
        }
        int gridTop = super.a_ + 12;
        int cellX = super.g + 25;
        if (pointerY >= gridTop && pointerY <= gridTop + 78) {
            for (int col = 0; col < 3; ++col) {
                if (pointerX >= cellX && pointerX <= cellX + 38) {
                    int clickedIndex = ((pointerY - gridTop) / 39) * 3 + col;
                    if (clickedIndex >= 0 && clickedIndex < this.itemPrices.length) {
                        if (clickedIndex != this.selectedIndex) {
                            this.selectedIndex = clickedIndex;
                            this.purchaseToastTicks = 0;
                            this.updateSelectedDescription();
                        } else {
                            this.onAction(super.q, ACTION_PROMPT_BUY);
                        }
                    }
                    return;
                }
                cellX += 45;
            }
        }
    }

    public void onAction(int screenId, int actionId) {
        switch (actionId) {
            case ACTION_DISMISS_DIALOG:
                GUIManager.getInstance().a(false);
                break;
            case ACTION_OPEN_RELICS:
                GUIManager.getController().a(4, 3, true);
                break;
            case ACTION_CONFIRM_BUY:
                GUIManager.getInstance().a(false);
                int price = this.itemPrices[this.selectedIndex];
                if (price > 0) {
                    if (this.inventoryCounts[2] < price) {
                        String notEnoughMsg = "Bạn không đủ vàng để mua (cần " + price + " vàng). Hãy chiến đấu hoặc làm nhiệm vụ để thu thập thêm vàng!";
                        BaseDialog prompt = GUIManager.a("Chú ý", notEnoughMsg, "Đóng", ACTION_DISMISS_DIALOG, 1);
                        prompt.a((ActionListener) this);
                        GUIManager.getInstance().a(prompt, true);
                    } else {
                        this.inventoryCounts[2] -= price;
                        EngineStorage.b(2, price);
                        this.pendingPurchaseIndex = this.selectedIndex;
                        this.completePurchase();
                    }
                } else {
                    this.pendingPurchaseIndex = this.selectedIndex;
                    this.grantFreeOfflineItem(this.pendingPurchaseIndex);
                }
                break;
            case ACTION_BACK:
                GUIManager.getController().a(4, this.returnScreenId, false);
                break;
            case ACTION_PROMPT_BUY:
                int askPrice = this.itemPrices[this.selectedIndex];
                if (askPrice > 0 && this.inventoryCounts[2] < askPrice) {
                    String notEnoughMsg = "Không đủ vàng! Cần " + askPrice + " vàng (hiện có " + this.inventoryCounts[2] + " vàng).";
                    BaseDialog prompt = GUIManager.a("Chú ý", notEnoughMsg, "Đóng", ACTION_DISMISS_DIALOG, 1);
                    prompt.a((ActionListener) this);
                    GUIManager.getInstance().a(prompt, true);
                    break;
                }
                String promptText = GameStrings.c[this.selectedIndex] + ": "
                        + this.itemDescriptions[this.selectedIndex]
                        + " Mua với giá " + askPrice + " vàng?";
                BaseDialog confirm = GUIManager.a("Xác nhận mua", promptText, "Mua", ACTION_CONFIRM_BUY, "Không", ACTION_DISMISS_DIALOG, 1);
                confirm.a((ActionListener) this);
                GUIManager.getInstance().a(confirm, true);
                break;
            default:
                break;
        }
    }

    private void grantFreeOfflineItem(int itemIndex) {
        new ShopPurchaseCallback(this).execute();
    }

    private void completePurchase() {
        int idx = this.pendingPurchaseIndex;
        if (idx == 2) {
            // Túi Quân Nhu combo: +1 Long Thần Kiếm (0), +1 Quỷ Diện Giáp (3), +2 Bình Thuốc (4), +1 Ngọc Ấn (5).
            this.inventoryCounts[0] += 1;
            EngineStorage.a(0, 1, 6);
            this.inventoryCounts[3] += 1;
            EngineStorage.a(3, 1, 6);
            this.inventoryCounts[4] += 2;
            EngineStorage.a(4, 2, 6);
            this.inventoryCounts[5] += 1;
            EngineStorage.a(5, 1, 6);
            this.purchaseToastText = "Đã mua Gói Quân Nhu (+5 món)!";
        } else {
            int gained = c[idx];
            this.inventoryCounts[idx] += gained;
            EngineStorage.a(idx, gained, 6);
            this.purchaseToastText = "Đã mua +" + gained + " " + GameStrings.c[idx] + " (Có: " + this.inventoryCounts[idx] + ")";
        }
        this.flashItemIndex = idx;
        this.purchaseToastTicks = 65;
        this.initSoftKeys();
    }

    static void a(ShopScreen screen) {
        screen.completePurchase();
    }
}
