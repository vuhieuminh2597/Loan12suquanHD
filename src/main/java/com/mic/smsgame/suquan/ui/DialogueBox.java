package com.mic.smsgame.suquan.ui;

import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.sprite.Drawable;
import com.mg.engine.util.ObjectList;
import com.mg.engine.util.StringUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.gfx.AvatarRenderer;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Historical antique wood-and-bronze dialogue box displaying paginated Vietnamese story lines,
 * a horizontal speaker nameplate, page indicator, and bronze-framed speaking character portraits.
 */
public class DialogueBox extends Drawable {
    private static final int LINES_PER_PAGE = 5;

    private int[] speakerIds;
    private String[] scripts;
    private int currentEntryIndex;
    private String[] wrappedLines;
    private int pageStartLine;
    private int pageEndLine;
    private String playerName;
    private int portraitsY;
    private int[] slotXPositions;
    private int[] slotAvatarIds;
    private int activeSlotIndex;
    private int occupiedSlotCount;
    private int playerAvatarId = CampaignData.d();
    private String activeSpeakerName = "";
    private int animTick;

    public DialogueBox(String[][] entries) {
        this.h(GameConfig.screenWidth - 12);
        this.portraitsY = (GameConfig.screenHeight >> 1) + 30;
        this.f((GameConfig.screenWidth - this.k()) >> 1);

        this.speakerIds = new int[entries.length];
        this.scripts = new String[entries.length];
        for (int i = 0; i < entries.length; ++i) {
            if (entries[i][0] == null) {
                this.speakerIds[i] = -1;
            } else {
                this.speakerIds[i] = Integer.parseInt(entries[i][0]);
                if (this.speakerIds[i] == 0) {
                    this.speakerIds[i] = this.playerAvatarId;
                }
            }
            this.scripts[i] = entries[i][1];
        }

        this.currentEntryIndex = 0;
        this.playerName = CampaignData.c();

        this.slotXPositions = new int[4];
        int slotSpan = GameConfig.screenWidth / 4;
        this.slotXPositions[0] = slotSpan / 2 - 18;
        this.slotXPositions[2] = this.slotXPositions[0] + slotSpan;
        this.slotXPositions[3] = this.slotXPositions[2] + slotSpan;
        this.slotXPositions[1] = this.slotXPositions[3] + slotSpan;

        this.slotAvatarIds = new int[4];
        this.occupiedSlotCount = 0;
        this.activeSlotIndex = -1;
        this.loadEntry(this.speakerIds[0], this.scripts[0]);
    }

    /** Advances to the next page or dialogue entry; returns false when finished. */
    public boolean a() {
        this.pageStartLine += LINES_PER_PAGE;
        if (this.pageStartLine >= this.wrappedLines.length) {
            ++this.currentEntryIndex;
            if (this.currentEntryIndex >= this.speakerIds.length) {
                return false;
            }
            this.loadEntry(this.speakerIds[this.currentEntryIndex], this.scripts[this.currentEntryIndex]);
            return true;
        }

        this.pageEndLine = this.pageStartLine + LINES_PER_PAGE - 1;
        if (this.pageEndLine >= this.wrappedLines.length) {
            this.pageEndLine = this.wrappedLines.length - 1;
        }
        this.resizeBox();
        return true;
    }

    private void registerSpeakerSlot(int avatarId) {
        if (avatarId < 0) {
            this.activeSlotIndex = -1;
            return;
        }
        this.activeSlotIndex = -1;
        for (int i = 0; i < this.occupiedSlotCount; ++i) {
            if (this.slotAvatarIds[i] == avatarId) {
                this.activeSlotIndex = i;
            }
        }
        if (this.activeSlotIndex < 0 && this.occupiedSlotCount < this.slotAvatarIds.length) {
            this.activeSlotIndex = this.occupiedSlotCount;
            this.slotAvatarIds[this.occupiedSlotCount++] = avatarId;
        }
    }

    private String[] wrapTextWithFont(String text, Font font, int maxWidth) {
        if (text == null || text.length() == 0) {
            return new String[]{""};
        }
        String[] wrapped = DrawUtils.wrapText(font, text.trim(), maxWidth);
        return (wrapped == null || wrapped.length == 0) ? new String[]{""} : wrapped;
    }

    private void loadEntry(int speakerId, String rawText) {
        if (speakerId >= 0) {
            this.registerSpeakerSlot(speakerId);
            this.activeSpeakerName = AvatarRenderer.a(speakerId);
        } else {
            this.activeSlotIndex = -1;
            this.activeSpeakerName = "Quân sĩ";
        }

        String text = StringUtils.replace(rawText, "{p}", this.playerName);
        this.wrappedLines = this.wrapTextWithFont(text, DrawUtils.UI_FONT_PLAIN, this.width - 20);
        this.pageStartLine = 0;
        this.pageEndLine = LINES_PER_PAGE - 1;
        if (this.pageEndLine >= this.wrappedLines.length) {
            this.pageEndLine = this.wrappedLines.length - 1;
        }
        this.resizeBox();
    }

    private void resizeBox() {
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int lineStep = plainH + 2;
        int visibleLines = this.pageEndLine - this.pageStartLine + 1;
        int boxH = visibleLines * lineStep + plainH + 20;
        if (boxH < 56) {
            boxH = 56;
        }
        this.h(GameConfig.screenWidth - 12);
        this.i(boxH);
        this.portraitsY = (GameConfig.screenHeight >> 1) + 30;
        this.f((GameConfig.screenWidth - this.k()) >> 1);
        int topY = this.portraitsY - this.height - 14;
        if (topY < 26) {
            topY = 26;
        }
        this.g(topY);
    }

    private void drawPortrait(Graphics g, int avatarId, int x, int y, boolean faceRight, boolean isActive) {
        if (avatarId < 0) {
            return;
        }
        if (isActive) {
            DrawUtils.drawAntiquePanel(
                    g, x, y, 36, 36,
                    0x246B5C, 0x16473D, 0x0E2B25,
                    0xFFD700, 0xFFF2A6, 0xFFFFFF);
        } else {
            DrawUtils.drawAntiquePanel(
                    g, x, y, 36, 36,
                    0x382417, 0x20130B, 0x120A06,
                    0x7A592A, 0xA88044, 0xC8963E);
        }
        AvatarRenderer.a(g, avatarId, x + 3, y + 3, faceRight);
    }

    public void b() {
        ++this.animTick;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.wrappedLines == null) {
            return;
        }
        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int plainH = DrawUtils.getFontHeight(plainFont);
        int lineStep = plainH + 2;

        // 1. Main antique parchment-and-wood dialogue frame
        DrawUtils.drawAntiquePanel(
                g, this.x, this.y, this.width, this.height,
                0x3A2618, 0x26170E, 0x160D08,
                0xC29444, 0xF5D27E, 0xFFE680);

        // 2. Speech tail pointing to active speaker portrait
        if (this.speakerIds[this.currentEntryIndex] >= 0 && this.activeSlotIndex >= 0) {
            int tailX = this.slotXPositions[this.activeSlotIndex] + 18;
            int baseY = this.y + this.height - 1;
            g.setColor(0x160D08);
            g.fillTriangle(tailX - 6, baseY, tailX + 6, baseY, tailX, baseY + 8);
            g.setColor(0xC29444);
            g.drawLine(tailX, baseY + 8, tailX - 6, baseY);
            g.drawLine(tailX, baseY + 8, tailX + 6, baseY);
        }

        // 3. Horizontal Speaker Nameplate sitting atop left edge of the dialogue box
        int plaqueH = boldH + 6;
        int plaqueW = DrawUtils.getStringWidth(boldFont, this.activeSpeakerName) + 18;
        if (plaqueW < 76) {
            plaqueW = 76;
        }
        if (plaqueW > this.width - 16) {
            plaqueW = this.width - 16;
        }
        int plaqueX = this.x + 8;
        int plaqueY = this.y - plaqueH + 4;
        if (plaqueY < 2) {
            plaqueY = 2;
        }
        DrawUtils.drawAntiquePanel(
                g, plaqueX, plaqueY, plaqueW, plaqueH,
                0x1A5E54, 0x114039, 0x0A2622,
                0xC8963E, 0xF5D487, 0xFFFFFF);
        DrawUtils.drawFullNameInBox(
                g, this.activeSpeakerName,
                plaqueX + 6, plaqueY + 3, plaqueW - 12, boldH,
                false, 0xFFE680, 0x0E1F1B, this.animTick);

        // 4. Dialogue text lines
        int textY = this.y + 9;
        for (int i = this.pageStartLine; i <= this.pageEndLine; ++i) {
            DrawUtils.drawShadowText(
                    g, plainFont, this.wrappedLines[i],
                    this.x + 10, textY, Graphics.TOP | Graphics.LEFT,
                    0xFFF4DC, 0x120804);
            textY += lineStep;
        }

        // 5. Bottom-right page / entry progress indicator
        int totalPages = (this.wrappedLines.length + LINES_PER_PAGE - 1) / LINES_PER_PAGE;
        int currentPage = (this.pageStartLine / LINES_PER_PAGE) + 1;
        String arrow = ((this.animTick / 8) % 2 == 0) ? " v" : "  ";
        String footerText = (totalPages > 1)
                ? ("Trang " + currentPage + "/" + totalPages + arrow)
                : ((this.currentEntryIndex + 1) + "/" + this.speakerIds.length + arrow);
        DrawUtils.drawShadowText(
                g, plainFont, footerText,
                this.x + this.width - 9, this.y + this.height - plainH - 4,
                Graphics.TOP | Graphics.RIGHT,
                0xFFE07A, 0x120804);

        // 6. Bronze-framed speaking character portraits below the box
        for (int i = 0; i < this.occupiedSlotCount; ++i) {
            this.drawPortrait(
                    g,
                    this.slotAvatarIds[i],
                    this.slotXPositions[i],
                    this.portraitsY,
                    i % 2 == 0,
                    i == this.activeSlotIndex);
        }
    }
}

