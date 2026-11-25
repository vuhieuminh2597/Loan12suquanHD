package com.mg.engine.graphics;

import com.mg.engine.util.ObjectList;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Horizontal strip bitmap font supporting the full Vietnamese character set and word wrapping.
 */
public abstract class StripBitmapFont {
    private static final String CHARSET =
            " 0123456789.,:!?()+-*/#$%abcdefghijklmnopqrstuvwxyz"
            + "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "ÁÀẢÃẶĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ";

    protected int charSpacing;
    protected int[] glyphWidths;
    protected int[] glyphOffsetsX;
    protected Image fontStripImage;

    protected StripBitmapFont(String resourcePath) {
        this.fontStripImage = MgImageLoader.load(resourcePath);
    }

    public int a() {
        return this.fontStripImage.getHeight();
    }

    public int a(String text) {
        int totalWidth = 0;
        for (int i = text.length() - 1; i >= 0; --i) {
            int glyphIdx = CHARSET.indexOf(text.charAt(i));
            if (glyphIdx == -1) {
                glyphIdx = 0;
            }
            totalWidth += this.glyphWidths[glyphIdx] + this.charSpacing;
        }
        return totalWidth - this.charSpacing;
    }

    public void a(Graphics g, String text, int x, int y, int alignment) {
        int drawX;
        int len = text.length();
        switch (alignment) {
            case 1:
                drawX = x - this.a(text);
                break;
            case 2:
                drawX = x - (this.a(text) >> 1);
                break;
            default:
                drawX = x;
                break;
        }

        for (int i = 0; i < len; ++i) {
            int glyphIdx = CHARSET.indexOf(text.charAt(i));
            if (glyphIdx == -1) {
                glyphIdx = 0;
            } else {
                g.drawRegion(
                        this.fontStripImage,
                        this.glyphOffsetsX[glyphIdx],
                        0,
                        this.glyphWidths[glyphIdx],
                        this.a(),
                        0,
                        drawX,
                        y,
                        Graphics.TOP | Graphics.LEFT);
            }
            drawX += this.glyphWidths[glyphIdx] + this.charSpacing;
        }
    }

    public int a(char ch) {
        int glyphIdx = CHARSET.indexOf(ch);
        return glyphIdx > -1 ? this.glyphWidths[glyphIdx] : this.glyphWidths[0];
    }

    public String[] a(String text, int maxWidth) {
        int spaceW = this.a(' ');
        ObjectList tokens = new ObjectList();
        String word = "";
        int pos = 0;

        while (pos < text.length()) {
            word = "";
            while (pos < text.length() && text.charAt(pos) == ' ') {
                ++pos;
            }
            if (pos > 0 && tokens.b() == 0 && pos * spaceW < maxWidth) {
                String leadingSpaces = "";
                for (int i = 0; i < pos; ++i) {
                    leadingSpaces = leadingSpaces + ' ';
                }
                tokens.a(leadingSpaces);
            }
            while (pos < text.length() && text.charAt(pos) != ' ') {
                word = word + text.charAt(pos);
                ++pos;
            }

            if (word.length() > 0) {
                word = word + " ";
                if (this.a(word) < maxWidth) {
                    tokens.a(word);
                } else {
                    int charIdx = 0;
                    String chunk = "";
                    int chunkW = 0;
                    while (charIdx < word.length()) {
                        int chW = this.a(word.charAt(charIdx));
                        if (chunkW + chW + spaceW < maxWidth) {
                            chunkW += chW;
                            chunk = chunk + word.charAt(charIdx);
                            ++charIdx;
                            if (charIdx >= word.length()) {
                                if (chunk.length() > 0) {
                                    tokens.a(chunk);
                                }
                                break;
                            }
                        } else {
                            tokens.a(chunk);
                            chunkW = 0;
                            chunk = "";
                        }
                    }
                }
            }
        }

        String[] wordList = new String[tokens.b()];
        for (int i = 0; i < wordList.length; ++i) {
            wordList[i] = (String) tokens.b(i);
        }

        ObjectList wrappedLines = new ObjectList();
        String currentLine = "";
        int currentLineW = 0;
        int idx = 0;
        while (idx < wordList.length) {
            int wordW = this.a(wordList[idx]);
            if (currentLineW + wordW - spaceW < maxWidth) {
                currentLine = currentLine + wordList[idx];
                currentLineW = this.a(currentLine);
                ++idx;
            } else {
                if (wordW > maxWidth) {
                    wrappedLines.a(wordList[idx]);
                    ++idx;
                } else {
                    wrappedLines.a(currentLine);
                }
                currentLine = "";
                currentLineW = 0;
            }
        }
        if (currentLine.length() > 0) {
            wrappedLines.a(currentLine);
        }

        String[] result = new String[wrappedLines.b()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (String) wrappedLines.b(i);
        }
        return result;
    }
}
