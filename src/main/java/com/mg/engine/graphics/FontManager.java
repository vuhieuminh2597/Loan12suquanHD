package com.mg.engine.graphics;

import com.mg.engine.util.ObjectList;
import javax.microedition.lcdui.Graphics;

/** Global font registry, Vietnamese word-wrapper, and multi-line text renderer. */
public class FontManager {
    private static FontManager instance = null;

    public static GameFont activeFont;
    public static SystemFont systemFont;
    public static VietnameseBitmapFont primaryFont;
    public static VietnameseBitmapFont secondaryFont;

    private FontManager() {
        systemFont = new SystemFont();
        primaryFont = new VietnameseBitmapFont("/_font1");
        secondaryFont = new VietnameseBitmapFont("/_font2");
        setActiveFont(primaryFont);
    }

    public static void init() {
        instance = new FontManager();
    }

    public static void setActiveFont(GameFont font) {
        activeFont = font;
    }

    public static String[] wrapText(String text, int maxWidth) {
        ObjectList tokens = new ObjectList();
        int idx = 0;

        while (idx < text.length()) {
            while (idx < text.length() && text.charAt(idx) == ' ') {
                ++idx;
            }

            if (idx > 0 && tokens.b() == 0 && idx * activeFont.a(' ') < maxWidth) {
                String leadingSpaces = "";
                for (int i = 0; i < idx; ++i) {
                    leadingSpaces = leadingSpaces + ' ';
                }
                tokens.a(leadingSpaces);
            }

            String word = "";
            while (idx < text.length() && text.charAt(idx) != ' ') {
                word = word + text.charAt(idx);
                ++idx;
            }

            if (word.length() > 0) {
                word = word + " ";
                if (activeFont.a(word) < maxWidth) {
                    tokens.a(word);
                } else {
                    int charIdx = 0;
                    String chunk = "";
                    int chunkWidth = 0;
                    while (charIdx < word.length()) {
                        if (chunkWidth + activeFont.a(word.charAt(charIdx)) + activeFont.a(' ') < maxWidth) {
                            chunkWidth += activeFont.a(word.charAt(charIdx));
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
                            chunkWidth = 0;
                            chunk = "";
                        }
                    }
                }
            }
        }

        String[] words = new String[tokens.b()];
        for (int i = 0; i < words.length; ++i) {
            words[i] = (String) tokens.b(i);
        }

        ObjectList lines = new ObjectList();
        String currentLine = "";
        int currentWidth = 0;
        idx = 0;

        while (idx < words.length) {
            int wordWidth = activeFont.a(words[idx]);
            if (currentWidth + wordWidth - activeFont.a(' ') < maxWidth) {
                currentWidth += wordWidth;
                currentLine = currentLine + words[idx];
                ++idx;
            } else {
                if (wordWidth > maxWidth) {
                    lines.a(words[idx]);
                    ++idx;
                } else {
                    lines.a(currentLine);
                }
                currentLine = "";
                currentWidth = 0;
            }
        }

        if (currentLine.length() > 0) {
            lines.a(currentLine);
        }

        String[] result = new String[lines.b()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (String) lines.b(i);
        }
        return result;
    }

    public static void drawLines(Graphics g, GameFont font, String[] lines, int startLine, int lineCount,
                                 int x, int y, int width, int height, int align) {
        int drawX = x;
        if (align == 1) {
            drawX = x + width / 2;
        } else if (align == 2) {
            drawX = x + width;
        }

        int endLine = startLine + lineCount;
        int offsetY = 0;
        for (int i = startLine; i < endLine && offsetY + font.a() <= height; ++i) {
            font.a(g, lines[i], drawX, y + offsetY, align);
            offsetY += font.a();
        }
    }

    public static void drawLines(Graphics g, GameFont font, String[] lines,
                                 int x, int y, int width, int height, int align) {
        drawLines(g, font, lines, 0, lines.length, x, y, width, height, align);
    }
}



