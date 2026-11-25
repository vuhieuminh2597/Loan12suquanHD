package com.mic.smsgame.suquan.ui;

import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.ArialBitmapFont;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Interactive speech-bubble guide and scripted step sequence for the first tutorial battle.
 */
public class BattleTutorialOverlay extends Drawable {
    private static final String[][] TUTORIAL_STEPS = new String[][]{
            {"Tướng quân hãy làm theo chỉ dẫn của tôi.", "Hãy bấm phím (5) hoặc phím OK để chọn thanh kiếm0*#"},
            {"Bấm phím (8) hoặc phím xuống để di chuyển xuống"},
            {"Khá lắm!", "Để tấn công kẻ thù hãy xếp 3 thanh kiếm các loại sát nhau theo hàng ngang hoặc dọc.", "Bây giờ đến lượt ta..."},
            {"Tướng quân đã bị thương", "Để hồi phục sinh lực hãy xếp 3 trái tim2*#"},
            {"Tốt lắm.", "Phía dưới có 1 thanh màu xanh lá tượng trưng cho lương thực, hết lương thực thì sẽ thua cuộc.", "Để lấy lương thực hãy xếp 3 bánh chưng1*#"},
            {"Rất tốt!", "Thanh màu xanh ngọc bích là năng lượng", "Để lấy năng lượng hãy xếp 3 biểu tượng âm dương3*#"},
            {"Cây năng lượng có 3 phần ứng với 3 tuyệt chiêu", "Tướng quân hãy mở bảng phép thuật bằng cách nhấn phím 'T.Chiêu' ,chọn một loại tuyệt chiêu và bấm 'Dùng'."},
            {"Tuyệt vời!", "Tướng quân sẽ được thăng cấp khi thu được nhiều kinh nghiệm chiến đấu, cấp càng cao thì chỉ số tấn công và sinh lực càng cao.", "Để lấy kinh nghiệm chiến đấu hãy xếp các biểu tượng cuốn sách.5*#", "Ngoài ra, để mua các vật phẩm hỗ trợ tại cửa hàng tướng quân phải thu thập các thỏi vàng.4*#", "Nếu xếp 4 biểu tượng trở lên sẽ được đi thêm 1 lượt.", "Và xếp 5 biểu tượng trở lên sẽ nhận được thêm các vật phẩm hỗ trợ", "Bây giờ hãy thử xếp 5 biểu tượng1*#"},
            {"Tướng quân đã được thưởng 1 vật phẩm, hãy bấm phím # để chọn và sử dụng vật phẩm."},
            {"Chú ý, một lượt đi phải ghép ít nhất 3 biểu tượng giống nhau, nếu thực hiện không đúng sẽ bị mất lượt đi và tiêu hao sinh lực. Hãy cẩn trọng."}
    };

    private static final int[] STEP_ACTION_TYPES = new int[]{0, 6, 0, 7, 0, 2, 0, 1, 0, 1, 0, 1, 0, 3, 0, 1, 0, 5, 0, 4};

    private static final int[][] SCRIPTED_SWAPS = new int[][]{
            {2, 4, 3, 4},
            {6, 9, 6, 8},
            {7, 7, 6, 7},
            {8, 4, 9, 4},
            {6, 3, 7, 3},
            {2, 2, 2, 2},
            {4, 3, 4, 2},
            {2, 2, 2, 2}
    };

    private Image inlineGemIcon;
    private int lastLineWidth;
    private int currentMessageIndex;
    private int messageCount;
    private int lineHeight;
    private String[] wrappedLines;
    private int actionCursor;
    private int stageIndex;
    private int swapCursor;
    private ArialBitmapFont tutorialFont = new ArialBitmapFont("/f/ari_b");

    public void a() {
        ++this.swapCursor;
    }

    public int[] b() {
        return SCRIPTED_SWAPS[this.swapCursor];
    }

    public BattleTutorialOverlay() {
        this.x = GameConfig.contentOffsetX + 32;
        this.y = GameConfig.contentOffsetY + 225;
        this.width = 176;
        this.visible = false;
        this.lineHeight = this.tutorialFont.a() + 2;
        this.stageIndex = -1;
    }

    public void c() {
        ++this.actionCursor;
    }

    public void d() {
        ++this.stageIndex;
        if (this.stageIndex < TUTORIAL_STEPS.length) {
            this.messageCount = TUTORIAL_STEPS[this.stageIndex].length;
            this.currentMessageIndex = 0;
            this.visible = true;
            this.setMessage(TUTORIAL_STEPS[this.stageIndex][this.currentMessageIndex]);
        }
    }

    public void e() {
        this.messageCount = 1;
        this.currentMessageIndex = TUTORIAL_STEPS[this.stageIndex].length - 1;
        this.visible = true;
        this.setMessage(TUTORIAL_STEPS[this.stageIndex][this.currentMessageIndex]);
    }

    public void f() {
        ++this.currentMessageIndex;
        if (this.currentMessageIndex >= this.messageCount) {
            this.visible = false;
        } else {
            this.setMessage(TUTORIAL_STEPS[this.stageIndex][this.currentMessageIndex]);
        }
    }

    private void setMessage(String message) {
        this.inlineGemIcon = null;
        if (message.substring(message.length() - 2).equals("*#")) {
            int gemType = Integer.parseInt(message.substring(message.length() - 3, message.length() - 2));
            this.inlineGemIcon = BattleResourceManager.a().d[gemType];
            message = message.substring(0, message.length() - 3);
        }

        int wrapWidth = this.width - 4;
        this.wrappedLines = this.tutorialFont.a(message, wrapWidth);
        this.height = this.lineHeight * this.wrappedLines.length + (this.inlineGemIcon != null ? 15 : 10);
        this.lastLineWidth = this.tutorialFont.a(this.wrappedLines[this.wrappedLines.length - 1]);
    }

    public int g() {
        return STEP_ACTION_TYPES[this.actionCursor];
    }

    private void drawSpeechBubble(Graphics g, int x, int y, int w, int h, int tailX, int fillRgb, int borderRgb) {
        g.setColor(borderRgb);
        g.fillRect(x + 4, y, w - 8, h);
        g.fillRect(x, y + 4, w, h - 8);
        g.fillRect(x + 1, y + 2, w - 2, h - 4);
        g.fillRect(x + 2, y + 1, w - 4, h - 2);
        g.setColor(fillRgb);
        g.fillRect(x + 2, y + 2, w - 4, h - 4);
        g.fillRect(x + 4, y + 1, w - 8, h - 2);
        g.fillRect(x + 1, y + 4, w - 2, h - 8);
        if (tailX >= 0) {
            g.fillTriangle(tailX - 5, y + h - 1, tailX + 5, y + h - 1, tailX, y + h + 7);
            g.setColor(borderRgb);
            g.drawLine(tailX, y + h + 7, tailX - 5, y + h - 1);
            g.drawLine(tailX, y + h + 7, tailX + 5, y + h - 1);
        }
    }

    public void h() {
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.visible) {
            return;
        }
        int bubbleY = this.y - this.height;
        int textX = this.x + 4;
        this.drawSpeechBubble(g, this.x, bubbleY, this.width, this.height, this.x + this.width - 25, 0xFFFFFF, 0x000000);
        int textY = bubbleY + 5;
        for (int i = 0; i < this.wrappedLines.length; ++i) {
            this.tutorialFont.a(g, this.wrappedLines[i], textX, textY, 0);
            textY += this.lineHeight;
        }
        if (this.inlineGemIcon != null) {
            g.drawImage(this.inlineGemIcon, textX + this.lastLineWidth - 1, textY - this.lineHeight - 2, 0);
        }
    }
}

