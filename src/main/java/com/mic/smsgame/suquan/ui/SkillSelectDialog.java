package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.MenuSelectionListener;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.SoftKeyButton;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.gfx.ScrollFrameRenderer;
import com.mic.smsgame.suquan.model.BattleFighter;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * In-battle dialog for viewing and casting the hero's 3 elemental spells ("T.Chiêu")
 * with live Mana/Food cost bar, Center primary CTA, and tap-outside-to-dismiss ergonomics.
 */
public class SkillSelectDialog extends BaseDialog implements ActionListener {
    private static final int[] DAMAGE_MULTIPLIERS = new int[]{
            1, 3, 5, 4, 0, 2, 6, 1, 11,
            1, 13, 23, 4, 0, 9, 4, 0, 3
    };
    private static final int[] DAMAGE_DIVISORS = new int[]{
            1, 2, 2, 5, 1, 1, 5, 1, 5,
            1, 10, 10, 5, 1, 5, 5, 1, 2
    };

    private Image[] scrollFrames;
    private Image dividerImage;
    private Image focusBoxSheet;
    private Image[] skillIcons;
    private int unlockedTierCount;
    private MenuSelectionListener selectionListener;
    private int selectedIndex;
    private int skillCount;
    private String[] skillNames;
    private String[] formattedDescriptions;
    private String[] wrappedDescription;
    private int blinkTick;
    private int cachedAttackPower = -1;
    private int currentMana;
    private int currentFood;
    private int foodCostPerCast = 100;
    private boolean boardTileSelected;
    private String selectedTileName;
    private int[] skillIds;

    public SkillSelectDialog(int[] skillIds) {
        super(1);
        BattleResourceManager res = BattleResourceManager.a();
        this.scrollFrames = res.k;
        this.dividerImage = res.z;
        this.focusBoxSheet = res.A;
        super.b_ = this.scrollFrames[0].getWidth();
        super.c_ = 222;
        super.g = (GameConfig.screenWidth - super.b_) >> 1;
        super.a_ = ((GameConfig.screenHeight - 23) - super.c_) >> 1;
        this.unlockedTierCount = 0;
        this.selectedIndex = 0;
        this.skillIcons = res.i;
        this.formattedDescriptions = new String[skillIds.length];
        this.skillNames = new String[skillIds.length];
        this.skillIds = skillIds;

        for (int i = 0; i < skillIds.length; ++i) {
            this.skillNames[i] = GameStrings.a[skillIds[i]];
            this.formattedDescriptions[i] = GameStrings.b[skillIds[i]];
        }
        this.wrappedDescription = DrawUtils.wrapText(
                DrawUtils.UI_FONT_PLAIN, this.formattedDescriptions[this.selectedIndex], super.b_ - 22);
        this.skillCount = this.skillIcons.length;
        this.a(new SoftKeyBar());
        this.a((ActionListener) this);
    }

    protected void e() {
        this.blinkTick = (this.blinkTick < 1) ? 8 : (this.blinkTick - 1);
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0 && this.skillCount > 0) {
            this.a(wheel > 0 ? 15 : 14);
        }
    }

    public void a(int unlockedTierCount, MenuSelectionListener listener, BattleFighter fighter, boolean tileSelected) {
        this.a(unlockedTierCount, listener, fighter, tileSelected, null);
    }

    public void a(
            int unlockedTierCount,
            MenuSelectionListener listener,
            BattleFighter fighter,
            boolean tileSelected,
            String selectedTileName) {
        this.boardTileSelected = tileSelected;
        this.selectedTileName = selectedTileName;
        this.currentMana = fighter != null ? fighter.o() : 0;
        this.currentFood = fighter != null ? fighter.p() : 1000;
        this.foodCostPerCast = (fighter != null && fighter.isWounded()) ? 175 : 100;
        int atk = fighter != null ? fighter.m() : 10;
        if (this.cachedAttackPower != atk) {
            this.cachedAttackPower = atk;
            for (int i = this.formattedDescriptions.length - 1; i > -1; --i) {
                int skillId = this.skillIds[i];
                String rawDesc = GameStrings.b[skillId];
                int eqPos = rawDesc.indexOf("=");
                if (eqPos >= 0 && skillId < DAMAGE_MULTIPLIERS.length) {
                    int damage = this.cachedAttackPower * DAMAGE_MULTIPLIERS[skillId] / DAMAGE_DIVISORS[skillId];
                    this.formattedDescriptions[i] = rawDesc.substring(0, eqPos) + damage + rawDesc.substring(eqPos + 1);
                } else {
                    this.formattedDescriptions[i] = rawDesc;
                }
            }
        }

        this.unlockedTierCount = unlockedTierCount;
        this.selectionListener = listener;
        this.selectedIndex = 0;
        this.m();
        GUIManager.getInstance().a(this, false);
        super.redrawParentScreen = true;
        this.refreshSoftKeysAndDescription();
    }

    public void a(int keyCode) {
        int prev = this.selectedIndex;
        switch (keyCode) {
            case 14:
                this.selectedIndex = (this.selectedIndex - 1 + this.skillCount) % this.skillCount;
                break;
            case 15:
                this.selectedIndex = (this.selectedIndex + 1) % this.skillCount;
                break;
        }
        if (prev != this.selectedIndex) {
            this.refreshSoftKeysAndDescription();
        }
    }

    private void refreshSoftKeysAndDescription() {
        this.a((SoftKeyButton) null);
        this.b(new TextSoftKeyButton("Đóng", -2));
        if (this.selectedIndex < this.unlockedTierCount) {
            this.c(new TextSoftKeyButton("Dùng", -1));
        } else {
            this.c((SoftKeyButton) null);
        }
        super.redrawParentScreen = true;
        this.wrappedDescription = DrawUtils.wrapText(
                DrawUtils.UI_FONT_PLAIN, this.formattedDescriptions[this.selectedIndex], super.b_ - 22);
    }

    public void a(int pointerX, int pointerY) {
        // Tap outside dialog scroll frame -> dismiss immediately
        if (pointerY < GameConfig.screenHeight - 24
                && (pointerX < super.g || pointerX > super.g + super.b_
                || pointerY < super.a_ || pointerY > super.a_ + super.c_)) {
            this.onAction(super.q, -2);
            return;
        }
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int iconX = super.g + 25;
        int iconY = super.a_ + 11 + plainH;
        if (pointerY >= iconY && pointerY <= iconY + 50) {
            for (int i = 0; i < this.skillCount; ++i) {
                if (pointerX >= iconX && pointerX <= iconX + 38) {
                    if (i != this.selectedIndex) {
                        this.selectedIndex = i;
                        this.refreshSoftKeysAndDescription();
                    } else if (this.selectedIndex < this.unlockedTierCount) {
                        this.onAction(super.q, -1);
                    }
                    return;
                }
                iconX += 45;
            }
        }
    }

    public void a(Graphics g) {
        int x = super.g;
        int y = super.a_;
        ScrollFrameRenderer.a(g, x, y, super.c_, ScrollFrameRenderer.ANTIQUE_SCROLL_BG, this.scrollFrames);

        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int centerX = x + (super.b_ >> 1);

        // Live Mana & Food cost summary strip at top
        String resourceLine;
        int resColor;
        if (this.currentFood <= 0) {
            resourceLine = "Nộ: " + this.currentMana + "/36  |  TUYỆT LƯƠNG (0)!";
            resColor = 0xFF7A66;
        } else {
            resourceLine = "Nộ: " + this.currentMana + "/36  |  Hao: " + this.foodCostPerCast + " Lương";
            resColor = (this.foodCostPerCast > 100 || this.currentFood < this.foodCostPerCast) ? 0xFFAE70 : 0xD8C8B0;
        }
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, resourceLine,
                centerX, y + 8, Graphics.TOP | Graphics.HCENTER,
                resColor, 0x120804);

        boolean hd = HdConfig.isHdRemaster() && g.getHdCanvas() != null;
        int iconX = x + 25;
        int iconY = y + 11 + plainH;
        for (int i = 0; i < this.skillCount; ++i) {
            boolean selected = (this.selectedIndex == i);
            boolean hovered = HdInputState.isHovered(iconX, iconY, 36, 36);
            if (hd) {
                HdUiRenderer.drawHdItemSocket(
                        g.getHdCanvas(), iconX, iconY, 36, 36,
                        selected, hovered, i < this.unlockedTierCount && selected);
            } else {
                int focusSrcX = (selected && this.blinkTick % 2 == 0) ? 0 : 36;
                DrawUtils.drawRegion(g, this.focusBoxSheet, focusSrcX, 0, 36, 36, iconX, iconY, 0);
            }
            g.drawImage(this.skillIcons[i], iconX + 18, iconY + 18, Graphics.HCENTER | Graphics.VCENTER);
            if (i >= this.unlockedTierCount) {
                DrawUtils.drawHatchedOverlay(g, 0x26140E, iconX + 2, iconY + 2, 32, 32);
            }
            String costTag = ((i + 1) * 12) + " Nộ";
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, costTag,
                    iconX + 18, iconY + 37,
                    Graphics.TOP | Graphics.HCENTER,
                    i < this.unlockedTierCount ? 0x7CFF9E : 0xB89B78, 0x120804);
            iconX += 45;
        }

        int divY = iconY + 37 + plainH;
        if (hd) {
            HdUiRenderer.drawHdDivider(g.getHdCanvas(), centerX, divY, super.b_ - 26);
        } else {
            g.drawImage(this.dividerImage, centerX, divY, Graphics.TOP | Graphics.HCENTER);
        }

        int textY = divY + 4;
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, this.skillNames[this.selectedIndex],
                centerX, textY, Graphics.TOP | Graphics.HCENTER,
                0xFFE680, 0x120804);
        textY += DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 2;

        boolean ready = this.selectedIndex < this.unlockedTierCount;
        int neededMana = (this.selectedIndex + 1) * 12;
        int curSkillId = this.skillIds[this.selectedIndex];
        boolean isAimableSkill = (curSkillId == 0 || curSkillId == 3 || curSkillId == 8);
        boolean hasAimedTile = ready && isAimableSkill && this.boardTileSelected && this.selectedTileName != null;
        String statusText;
        int statusColor;
        if (!ready && this.selectedIndex >= 2 && this.currentFood <= 0 && this.currentMana >= neededMana) {
            statusText = "[Tuyệt lương: Khóa Chiêu 3!]";
            statusColor = 0xFF6B6B;
        } else if (!ready) {
            statusText = "[Thiếu Nộ: " + this.currentMana + "/" + neededMana + "]";
            statusColor = 0xFFAE70;
        } else if (hasAimedTile) {
            statusText = "[Nhắm ô chọn: " + this.selectedTileName + "]";
            statusColor = 0xFFE680;
        } else if (isAimableSkill) {
            statusText = "[Tự nhắm - Chọn ô trước để nhắm]";
            statusColor = 0x8CF5E8;
        } else {
            statusText = "[Sẵn sàng - Chạm lần 2 để dùng]";
            statusColor = 0x7CFF9E;
        }
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, statusText,
                centerX, textY, Graphics.TOP | Graphics.HCENTER,
                statusColor, 0x120804);
        textY += plainH + 3;

        int maxDescH = (y + super.c_ - 10) - textY;
        if (maxDescH > 10) {
            DrawUtils.drawWrappedShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, this.wrappedDescription,
                    x + 11, textY, super.b_ - 22, maxDescH,
                    0xFFF4DC, 0x120804);
        }
    }

    public void onAction(int screenId, int actionId) {
        switch (actionId) {
            case -1:
                this.selectionListener.a("skill", this.selectedIndex);
                // Fall through to close dialog.
            case -2:
                GUIManager.getInstance().a(false);
                break;
        }
    }
}
