package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.ui.CollectionScreenBase;
import com.mg.engine.ui.ScreenBackground;
import com.mg.engine.ui.SoftKeyBar;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.GameStrings;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * SuQuan Sacred Relics screen ("Báu vật") for crafting and enhancing the 4 permanent relics
 * using Enhancement Shards ("Mảnh Cường Hóa") and Gold.
 */
public class SuQuanCollectionScreen extends CollectionScreenBase {
    private Image boardEmblem = MgImageLoader.load("/r/br");
    private Image menuBg;
    private int emblemX;
    private int emblemY;
    private int nexusTick;

    public SuQuanCollectionScreen(int returnScreenId) {
        super(returnScreenId, GameStrings.g, Graphics.HCENTER | Graphics.VCENTER);
        this.emblemX = (240 - this.boardEmblem.getWidth()) >> 1;
        this.emblemY = (320 - this.boardEmblem.getHeight()) >> 1;
        super.C.a((ScreenBackground) null);
        super.C.a((SoftKeyBar) null);
        super.q = 3;
    }

    protected void b() {
        super.B = new Image[4];
        try {
            super.z = MgImageLoader.load("/r/q");
            super.A = MgImageLoader.load("/r/g");
            for (int i = 0; i < super.B.length; ++i) {
                super.B[i] = MgImageLoader.load("/r/" + (i + 1));
            }
            this.menuBg = MgImageLoader.load("/bkmenu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void e(Graphics g) {
        g.setColor(0x160D08);
        g.fillRect(0, 0, super.b_, super.c_);
        if (this.menuBg != null) {
            g.drawImage(this.menuBg, GameConfig.contentOffsetX, GameConfig.contentOffsetY, 0);
        }
        DrawUtils.drawHatchedOverlay(g, 0x1A120B, 0, 0, super.b_, super.c_);
    }

    protected void onRelicStatsUpdated() {
        if (CampaignData.e != null) {
            CampaignData.e.refreshAfterRelicUnlock();
        }
    }

    protected void b(Graphics g) {
        ++this.nexusTick;
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdRelicNexusLines(g.getHdCanvas(), GameConfig.contentOffsetX, GameConfig.contentOffsetY, this.nexusTick);
        } else if (this.boardEmblem != null) {
            g.drawImage(this.boardEmblem, GameConfig.contentOffsetX + this.emblemX, GameConfig.contentOffsetY + this.emblemY - 40, 0);
        }
        super.b(g);
    }
}
