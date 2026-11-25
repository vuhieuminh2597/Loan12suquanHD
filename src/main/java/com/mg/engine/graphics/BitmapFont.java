package com.mg.engine.graphics;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdFontRenderer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Proportional sprite-sheet bitmap font supporting Vietnamese diacritics. */
public class BitmapFont extends GameFont {
    protected Image atlasImage;
    protected int[] glyphX;
    protected int[] glyphY;
    protected int[] glyphWidth;
    protected int[] glyphHeight;
    protected int[] glyphOffsetY;
    protected String primaryChars = " 0123456789.,:!?()+-*/#$%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDĐEFGHIJKLMNOPQRSTUVWXYZÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴ\\\"@<=>;_&'`^~{}";
    protected String fallbackChars = "";
    protected int lineHeight;

    public void a(Graphics g, String text, int x, int y, int align) {
        if (text == null || text.length() == 0) {
            return;
        }
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            int anchor = Graphics.TOP | (align == 0 ? Graphics.LEFT : (align == 2 ? Graphics.RIGHT : Graphics.HCENTER));
            boolean isSecondary = (this == FontManager.secondaryFont);
            int fgColor = isSecondary ? 0xFFE680 : 0xFFF4DC;
            HdFontRenderer.drawHdShadowText(
                    g.getHdCanvas(),
                    isSecondary,
                    text,
                    x,
                    y,
                    anchor,
                    fgColor,
                    0x120804,
                    this.a(text),
                    this.lineHeight);
            return;
        }
        int len = text.length();
        int totalWidth = this.a(text);
        int drawX;
        if (align == 0) {
            drawX = x;
        } else if (align == 2) {
            drawX = x - totalWidth;
        } else {
            drawX = x - (totalWidth >> 1);
        }

        for (int i = 0; i < len; ++i) {
            char ch = text.charAt(i);
            int glyphIdx = this.primaryChars.indexOf(ch);
            if (glyphIdx == -1) {
                glyphIdx = this.fallbackChars.indexOf(ch);
            }
            if (glyphIdx == -1) {
                glyphIdx = 0;
            }
            if (glyphIdx > -1) {
                DrawUtils.drawRegion(
                        g,
                        this.atlasImage,
                        this.glyphX[glyphIdx],
                        this.glyphY[glyphIdx],
                        this.glyphWidth[glyphIdx],
                        this.glyphHeight[glyphIdx],
                        drawX,
                        y - 1 + this.glyphOffsetY[glyphIdx],
                        Graphics.TOP | Graphics.LEFT
                );
            }
            drawX += this.glyphWidth[glyphIdx] + 1;
        }
    }

    public int a() {
        return this.lineHeight;
    }

    public int a(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ++i) {
            width = width + 1 + this.a(text.charAt(i));
        }
        return width;
    }

    public int a(char ch) {
        int glyphIdx = this.primaryChars.indexOf(ch);
        if (glyphIdx == -1) {
            glyphIdx = this.fallbackChars.indexOf(ch);
        }
        if (glyphIdx == -1) {
            glyphIdx = 0;
        }
        return this.glyphWidth[glyphIdx];
    }
}



