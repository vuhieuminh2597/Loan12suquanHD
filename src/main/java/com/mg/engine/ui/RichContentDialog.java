package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.GameFont;
import com.mg.engine.graphics.Rect;
import com.mg.engine.util.ObjectList;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Scrollable rich-content dialog combining text blocks, images, and animated sprites.
 */
public class RichContentDialog extends BaseDialog {
    private ContentBlock[] blocks;
    private String title;
    private int targetScrollY;
    private int currentScrollY;
    private Rect contentViewport;
    private int totalContentHeight;
    private Image scrollArrowImage;

    public void a(ContentBlock[] blocks) {
        this.blocks = blocks;
        this.layoutBlocks();
    }

    private static ContentBlock[] parseMarkupBlocks(String markup, ObjectList stringArgs, ObjectList imageBlocks) {
        String[] segments = new String[50];
        int segmentCount = 1;
        int imageIndex = 0;
        if (stringArgs != null) {
            for (int i = 0; i < stringArgs.b(); ++i) {
                int placeholder = markup.indexOf("{s}");
                markup = markup.substring(0, placeholder) + (String) stringArgs.b(i) + markup.substring(placeholder + 2);
            }
        }

        segments[0] = markup;
        for (int i = 0; i < segmentCount; ++i) {
            int splitPos = segments[i].indexOf("@");
            if (splitPos >= 0) {
                for (int j = segmentCount - 1; j > i; --j) {
                    segments[j + 1] = segments[j];
                }
                segments[i + 1] = segments[i].substring(splitPos + 1);
                segments[i] = segments[i].substring(0, splitPos);
                ++segmentCount;
            }
        }

        ContentBlock[] result = new ContentBlock[segmentCount];
        for (int i = 0; i < segmentCount; ++i) {
            if ("{i}".equals(segments[i])) {
                result[i] = (ContentBlock) imageBlocks.b(imageIndex++);
            } else {
                boolean usePrimaryFont = segments[i].length() > 0 && segments[i].charAt(0) == '*';
                if (usePrimaryFont) {
                    segments[i] = segments[i].substring(1);
                }
                result[i] = new TextBlock(segments[i], 0);
                if (segments[i].length() > 0 && usePrimaryFont) {
                    ((TextBlock) result[i]).a(FontManager.primaryFont);
                }
            }
        }
        return result;
    }

    public static ContentBlock[] a(String formatSpec, String[] replacements, ContentBlock[] embeddedBlocks, GameFont unused1, GameFont unused2) {
        int blockCount = Integer.parseInt(formatSpec.substring(0, 3));
        ContentBlock[] result = new ContentBlock[blockCount];
        String remaining = formatSpec.substring(4);
        if (replacements != null) {
            for (int i = 0; i < replacements.length; ++i) {
                int pos = remaining.indexOf("&r");
                remaining = remaining.substring(0, pos) + replacements[i] + remaining.substring(pos + 2);
            }
        }

        int outIndex = 0;
        int embedIndex = 0;
        while (remaining != null) {
            int hashPos = remaining.indexOf("#");
            String token;
            if (hashPos > -1) {
                token = remaining.substring(0, hashPos);
                remaining = remaining.substring(hashPos + 1);
            } else {
                token = remaining;
            }

            String kind = String.valueOf(token.charAt(0));
            if (kind.equals("s")) {
                String styleCode = String.valueOf(token.charAt(1));
                String alignCode = String.valueOf(token.charAt(2));
                int alignment = 0;
                if (alignCode.equals("r")) {
                    alignment = 2;
                } else if (alignCode.equals("c")) {
                    alignment = 1;
                }

                if (!String.valueOf(token.charAt(3)).equals("$")) {
                    result[outIndex] = new TextBlock(token.substring(3), alignment);
                } else {
                    int lineCount = Integer.parseInt(token.substring(4, 6));
                    boolean hasHeader = String.valueOf(token.charAt(4)).equals("t");
                    token = token.substring(7);
                    String[] lines = new String[lineCount];
                    for (int i = 0; i < lines.length; ++i) {
                        int pipePos = token.indexOf("|");
                        if (pipePos > -1) {
                            lines[i] = token.substring(0, pipePos);
                            token = token.substring(pipePos + 1);
                        } else {
                            lines[i] = token;
                        }
                    }
                    result[outIndex] = new TextBlock(lines, hasHeader, alignment);
                }

                if (styleCode.equals("t")) {
                    ((TextBlock) result[outIndex]).a(FontManager.primaryFont);
                }
            } else if (kind.equals("i")) {
                result[outIndex] = embeddedBlocks[embedIndex++];
            }

            ++outIndex;
            if (outIndex >= blockCount) {
                break;
            }
        }
        return result;
    }

    public static final RichContentDialog a(
            String title,
            String markup,
            ObjectList stringArgs,
            ObjectList imageBlocks,
            SoftKeyButton leftButton,
            SoftKeyButton rightButton) {
        RichContentDialog dialog = new RichContentDialog(title, parseMarkupBlocks(markup, stringArgs, imageBlocks));
        if (rightButton == null) {
            dialog.c(leftButton);
        } else if (leftButton == null) {
            dialog.c(rightButton);
        } else {
            dialog.a(leftButton);
            dialog.b(rightButton);
        }
        return dialog;
    }

    public RichContentDialog(String title) {
        this(title, null);
    }

    public RichContentDialog(String title, ContentBlock[] blocks) {
        super(1);
        super.animated = false;
        this.loadScrollArrow();
        this.contentViewport = new Rect(10, 41, super.b_ - 15, super.c_ - 69);
        this.title = title;
        this.blocks = blocks;
        this.layoutBlocks();
    }

    private void layoutBlocks() {
        if (this.blocks == null) {
            return;
        }
        this.totalContentHeight = 0;
        this.targetScrollY = this.currentScrollY = 0;
        for (int i = 0; i < this.blocks.length; ++i) {
            this.blocks[i].a(this.contentViewport);
            this.totalContentHeight += this.blocks[i].a() + 8;
        }
    }

    private void loadScrollArrow() {
        try {
            this.scrollArrowImage = GUIManager.arrowImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void a(int pointerX, int pointerY) {
        super.a(pointerX, pointerY);
        int centerX = super.b_ / 2 + 2;
        if (this.targetScrollY > 0) {
            int arrowY = this.contentViewport.b - 18;
            if (pointerX >= centerX - 24 && pointerX <= centerX + 24 && pointerY >= arrowY && pointerY <= arrowY + 22) {
                GameConfig.keyStates[12] = 2;
                return;
            }
        }

        if (this.totalContentHeight > this.contentViewport.d
                && this.totalContentHeight - this.targetScrollY - this.contentViewport.d > 0) {
            int arrowY = this.contentViewport.b + this.contentViewport.d - 2;
            if (pointerX >= centerX - 24 && pointerX <= centerX + 24 && pointerY >= arrowY && pointerY <= arrowY + 22) {
                GameConfig.keyStates[13] = 2;
            }
        }
    }

    public void c(int deltaY) {
        if (deltaY != 0 && this.totalContentHeight > this.contentViewport.d) {
            int maxScroll = this.totalContentHeight - this.contentViewport.d;
            this.targetScrollY -= deltaY;
            if (this.targetScrollY < 0) {
                this.targetScrollY = 0;
            } else if (this.targetScrollY > maxScroll) {
                this.targetScrollY = maxScroll;
            }
            this.currentScrollY = this.targetScrollY;
        }
    }

    public void a(int keyCode) {
        super.a(keyCode);
        if (keyCode == 12 || keyCode == 2) {
            this.targetScrollY -= this.contentViewport.d / 2;
            if (this.targetScrollY < 0) {
                this.targetScrollY = 0;
            }
        } else if (keyCode == 13 || keyCode == 8) {
            this.targetScrollY += this.contentViewport.d / 2;
            if (this.totalContentHeight - this.targetScrollY < this.contentViewport.d) {
                this.targetScrollY = this.totalContentHeight - this.contentViewport.d;
            }
            if (this.targetScrollY < 0) {
                this.targetScrollY = 0;
            }
        }
    }

    protected void e() {
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0 && this.totalContentHeight > this.contentViewport.d) {
            this.targetScrollY += wheel * 28;
            int maxScroll = this.totalContentHeight - this.contentViewport.d;
            if (this.targetScrollY > maxScroll) {
                this.targetScrollY = maxScroll;
            }
            if (this.targetScrollY < 0) {
                this.targetScrollY = 0;
            }
        }
        if (this.currentScrollY != this.targetScrollY) {
            int delta = this.targetScrollY - this.currentScrollY;
            this.currentScrollY += Math.abs(delta) < 3 ? delta : (delta / 2);
        }
        if (this.blocks != null) {
            for (int i = 0; i < this.blocks.length; ++i) {
                this.blocks[i].b();
            }
        }
    }

    public void a(Graphics g) {
        if (super.r == null) {
            DrawUtils.drawAntiquePanel(
                    g, 2, 2, super.b_ - 4, super.c_ - 26,
                    0x362417, 0x22150D, 0x130B06,
                    0xB8893D, 0xF0C975, 0xFFE680);
        }
        if (this.title != null) {
            int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
            int headerH = boldH + 8;
            DrawUtils.drawAntiquePanel(
                    g, 8, 7, super.b_ - 16, headerH,
                    0x1A5E54, 0x114039, 0x0C2B26,
                    0xB8893D, 0xF0C975, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.title,
                    super.b_ >> 1, 11, Graphics.TOP | Graphics.HCENTER,
                    0xFFE680, 0x120804);
        }
        if (this.blocks != null) {
            g.setClip(0, this.contentViewport.b, super.b_, this.contentViewport.d);
            int drawY = this.contentViewport.b - this.currentScrollY;
            int bottomY = this.contentViewport.b + this.contentViewport.d;
            Rect blockRect = new Rect();
            for (int i = 0; i < this.blocks.length && drawY <= bottomY; ++i) {
                blockRect.a(this.contentViewport.a, drawY, this.contentViewport.c, this.blocks[i].a());
                if (this.contentViewport.a(blockRect)) {
                    this.blocks[i].a(g, this.contentViewport.a, drawY);
                }
                drawY += this.blocks[i].a() + 8;
            }
            g.setClip(0, 0, super.b_, super.c_);
        }

        int centerX = super.b_ / 2 + 2;
        int maxScroll = Math.max(0, this.totalContentHeight - this.contentViewport.d);
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            com.loan12suquan.hd.platform.HdCanvas2D g2 = g.getHdCanvas();
            if (maxScroll > 0) {
                HdUiRenderer.drawHdVerticalScrollbar(
                        g2, super.b_ - 8, this.contentViewport.b, this.contentViewport.d,
                        this.currentScrollY, maxScroll, this.contentViewport.d);
            }
            if (this.targetScrollY > 0) {
                int ay = this.contentViewport.b - 6;
                HdUiRenderer.drawHdDirectionalChevron(
                        g2, centerX, ay, 0, 0xFFE680, false,
                        HdInputState.isHovered(centerX - 8, ay - 8, 16, 16));
            }
            if (maxScroll > 0 && maxScroll - this.targetScrollY > 0) {
                int ay = this.contentViewport.b + this.contentViewport.d + 6;
                HdUiRenderer.drawHdDirectionalChevron(
                        g2, centerX, ay, 1, 0xFFE680, false,
                        HdInputState.isHovered(centerX - 8, ay - 8, 16, 16));
            }
        } else {
            int arrowW = this.scrollArrowImage.getWidth();
            int arrowH = this.scrollArrowImage.getHeight();
            if (this.targetScrollY > 0) {
                g.drawRegion(this.scrollArrowImage, 0, 0, arrowW, arrowH, Sprite.TRANS_ROT270, centerX, this.contentViewport.b - 2, Graphics.HCENTER | Graphics.BOTTOM);
            }
            if (this.totalContentHeight > this.contentViewport.d
                    && this.totalContentHeight - this.targetScrollY - this.contentViewport.d > 0) {
                g.drawRegion(this.scrollArrowImage, 0, 0, arrowW, arrowH, Sprite.TRANS_ROT90, centerX, this.contentViewport.b + this.contentViewport.d + 2, Graphics.HCENTER | Graphics.TOP);
            }
        }
    }
}
