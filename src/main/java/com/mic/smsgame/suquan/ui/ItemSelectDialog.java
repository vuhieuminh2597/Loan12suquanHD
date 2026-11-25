package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.SmallNumberFont;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.MenuSelectionListener;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.SoftKeyButton;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mg.engine.util.ObjectList;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.gfx.ScrollFrameRenderer;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.ItemSlot;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * In-battle inventory dialog ("Túi") with active-buff warning (`[Đang bật!]`),
 * Center primary CTA, and tap-outside-to-dismiss ergonomics.
 */
public class ItemSelectDialog extends BaseDialog implements ActionListener {
    private Image[] scrollFrames;
    private Image dividerImage;
    private Image focusBoxSheet;
    private Image itemIconsSheet;
    private MenuSelectionListener selectionListener;
    private BattleFighter ownerFighter;
    private int selectedIndex;
    private String[] itemDescriptions;
    private String[] wrappedDescription;
    private int blinkTick;
    private ObjectList availableItems;
    private int horizontalGap;
    private SmallNumberFont smallNumberFont;

    public ItemSelectDialog() {
        super(1);
        this.loadSprites();
        this.availableItems = new ObjectList();
        super.b_ = this.scrollFrames[0].getWidth();
        super.c_ = 204;
        super.g = (GameConfig.screenWidth - super.b_) >> 1;
        super.a_ = ((GameConfig.screenHeight - 23) - super.c_) >> 1;
        this.selectedIndex = 0;
        this.itemDescriptions = GameStrings.d;
        this.smallNumberFont = new SmallNumberFont(BattleResourceManager.a().B);
        this.a(new SoftKeyBar());
        this.a((ActionListener) this);
    }

    private void loadSprites() {
        BattleResourceManager res = BattleResourceManager.a();
        this.scrollFrames = res.k;
        this.dividerImage = res.z;
        this.focusBoxSheet = res.A;
        this.itemIconsSheet = res.E;
    }

    protected void e() {
        this.blinkTick = (this.blinkTick < 1) ? 8 : (this.blinkTick - 1);
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0 && this.availableItems.b() > 0) {
            this.a(wheel > 0 ? 15 : 14);
        }
    }

    public void a(ItemSlot[] allSlots, MenuSelectionListener listener) {
        this.a(allSlots, listener, null);
    }

    public void a(ItemSlot[] allSlots, MenuSelectionListener listener, BattleFighter owner) {
        this.selectionListener = listener;
        this.ownerFighter = owner;
        this.availableItems.a();
        this.m();
        this.selectedIndex = 0;

        for (int i = 0; i < allSlots.length; ++i) {
            if (allSlots[i].b > 0 && allSlots[i].a != 2) {
                this.availableItems.a(allSlots[i]);
            }
        }

        int rowCols = this.availableItems.b() > 4 ? 4 : this.availableItems.b();
        this.horizontalGap = rowCols > 0 ? ((super.b_ - 12 - rowCols * 36) / (rowCols + 1)) : 0;
        if (this.horizontalGap < 2) {
            this.horizontalGap = 2;
        }
        if (this.availableItems.b() > 0) {
            ItemSlot first = (ItemSlot) this.availableItems.b(this.selectedIndex);
            this.wrappedDescription = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, this.itemDescriptions[first.a], super.b_ - 22);
        } else {
            this.wrappedDescription = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, "Hiện tại không còn vật phẩm hỗ trợ trong túi.", super.b_ - 22);
        }

        this.a((SoftKeyButton) null);
        this.b(new TextSoftKeyButton("Đóng", -2));
        super.redrawParentScreen = true;
        if (this.availableItems.b() > 0) {
            this.c(new TextSoftKeyButton("Dùng", -1));
        } else {
            this.c((SoftKeyButton) null);
        }
        GUIManager.getInstance().a(this, false);
    }

    private boolean isItemAlreadyActive(int itemId) {
        if (this.ownerFighter == null || this.ownerFighter.a == null) {
            return false;
        }
        if (itemId == 0 && this.ownerFighter.a.length > 0) {
            return this.ownerFighter.a[0].b > 0;
        }
        if (itemId == 3 && this.ownerFighter.a.length > 3) {
            return this.ownerFighter.a[3].b > 0;
        }
        return false;
    }

    public void a(int keyCode) {
        if (this.availableItems.b() < 1) {
            return;
        }
        int prev = this.selectedIndex;
        int count = this.availableItems.b();
        switch (keyCode) {
            case 12:
                this.selectedIndex -= 4;
                if (this.selectedIndex < 0) {
                    this.selectedIndex = prev;
                }
                break;
            case 13:
                this.selectedIndex += 4;
                if (this.selectedIndex >= count) {
                    this.selectedIndex = prev;
                }
                break;
            case 14:
                this.selectedIndex = (this.selectedIndex - 1 + count) % count;
                break;
            case 15:
                this.selectedIndex = (this.selectedIndex + 1) % count;
                break;
        }
        if (prev != this.selectedIndex) {
            this.updateDescription();
        }
    }

    private void updateDescription() {
        ItemSlot slot = (ItemSlot) this.availableItems.b(this.selectedIndex);
        this.wrappedDescription = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, this.itemDescriptions[slot.a], super.b_ - 22);
    }

    public void a(int pointerX, int pointerY) {
        // Tap outside dialog scroll frame -> dismiss immediately
        if (pointerY < GameConfig.screenHeight - 24
                && (pointerX < super.g || pointerX > super.g + super.b_
                || pointerY < super.a_ || pointerY > super.a_ + super.c_)) {
            this.onAction(super.q, -2);
            return;
        }
        int count = this.availableItems.b();
        if (count < 1) {
            return;
        }
        int cellY = super.a_ + 13;
        int rows = count / 4 + (count % 4 != 0 ? 1 : 0);
        int idx = 0;
        for (int row = 0; row < rows; ++row) {
            int cellX = super.g + 6 + this.horizontalGap;
            for (int col = 0; col < 4 && idx < count; ++col) {
                if (pointerY >= cellY && pointerY <= cellY + 36
                        && pointerX >= cellX && pointerX <= cellX + 36) {
                    if (idx != this.selectedIndex) {
                        this.selectedIndex = idx;
                        this.updateDescription();
                    } else {
                        this.onAction(super.q, -1);
                    }
                    return;
                }
                cellX += 36 + this.horizontalGap;
                ++idx;
            }
            cellY += 38;
        }
    }

    public void a(Graphics g) {
        ScrollFrameRenderer.a(g, super.g, super.a_, super.c_, ScrollFrameRenderer.ANTIQUE_SCROLL_BG, this.scrollFrames);
        int cellY = super.a_ + 13;
        int idx = 0;
        int rows = this.availableItems.b() / 4 + (this.availableItems.b() % 4 != 0 ? 1 : 0);

        boolean hd = HdConfig.isHdRemaster() && g.getHdCanvas() != null;
        for (int row = 0; row < rows; ++row) {
            int cellX = super.g + 6 + this.horizontalGap;
            for (int col = 0; col < 4 && idx < this.availableItems.b(); ++col) {
                ItemSlot slot = (ItemSlot) this.availableItems.b(idx);
                boolean selected = (this.selectedIndex == idx);
                boolean hovered = HdInputState.isHovered(cellX, cellY, 36, 36);
                boolean activeBuff = this.isItemAlreadyActive(slot.a);
                if (hd) {
                    HdUiRenderer.drawHdItemSocket(g.getHdCanvas(), cellX, cellY, 36, 36, selected, hovered, activeBuff);
                } else {
                    int focusSrcX = selected ? 0 : 36;
                    DrawUtils.drawRegion(g, this.focusBoxSheet, focusSrcX, 0, 36, 36, cellX, cellY, 0);
                    if (activeBuff) {
                        g.setColor(0xFFD700);
                        g.drawRect(cellX + 1, cellY + 1, 33, 33);
                    }
                }
                DrawUtils.drawRegion(g, this.itemIconsSheet, slot.a * 28, 0, 28, 28, cellX + 4, cellY + 4, 0);
                this.smallNumberFont.a(g, slot.b, cellX + 34, cellY + 29, 2);
                cellX += 36 + this.horizontalGap;
                ++idx;
            }
            cellY += 38;
        }

        int centerX = super.g + (this.scrollFrames[0].getWidth() >> 1);
        if (this.availableItems.b() > 0) {
            if (hd) {
                HdUiRenderer.drawHdDivider(g.getHdCanvas(), centerX, cellY + 2, super.b_ - 26);
            } else {
                g.drawImage(this.dividerImage, centerX, cellY + 2, Graphics.TOP | Graphics.HCENTER);
            }
            ItemSlot selected = (ItemSlot) this.availableItems.b(this.selectedIndex);
            int textY = cellY + 8;
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, GameStrings.c[selected.a],
                    centerX, textY, Graphics.TOP | Graphics.HCENTER,
                    0xFFE680, 0x120804);
            textY += DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 2;

            boolean alreadyActive = this.isItemAlreadyActive(selected.a);
            String limitText = alreadyActive
                    ? "[Đang kích hoạt trên người!]"
                    : ("[Còn dùng: " + selected.b + " lần | 1 món/lượt]");
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, limitText,
                    centerX, textY, Graphics.TOP | Graphics.HCENTER,
                    alreadyActive ? 0xFFAE70 : 0x8CF5E8, 0x120804);
            textY += DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN) + 4;

            int maxDescH = (super.a_ + super.c_ - 10) - textY;
            if (maxDescH > 10) {
                DrawUtils.drawWrappedShadowText(
                        g, DrawUtils.UI_FONT_PLAIN, this.wrappedDescription,
                        super.g + 11, textY, super.b_ - 22, maxDescH,
                        0xFFF4DC, 0x120804);
            }
        } else {
            DrawUtils.drawWrappedShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, this.wrappedDescription,
                    super.g + 11, cellY + 45, super.b_ - 22, 90,
                    0xFFF4DC, 0x120804);
        }
    }

    public void onAction(int screenId, int actionId) {
        switch (actionId) {
            case -1:
                if (this.availableItems.b() > 0 && this.selectedIndex < this.availableItems.b()) {
                    ItemSlot selected = (ItemSlot) this.availableItems.b(this.selectedIndex);
                    selected.a();
                    EngineStorage.g(selected.a);
                    this.selectionListener.a("item", selected.a);
                }
                // Fall through to close dialog.
            case -2:
                GUIManager.getInstance().a(false);
                break;
        }
    }
}
