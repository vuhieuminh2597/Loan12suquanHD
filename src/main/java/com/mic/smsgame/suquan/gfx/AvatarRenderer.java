package com.mic.smsgame.suquan.gfx;

import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.DialogueData;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Singleton loader/renderer for 30x30 character portraits from {@code /avatar}
 * and character display names.
 */
public class AvatarRenderer {
    private static final int AVATAR_SIZE = 30;
    private static final int COLUMNS_PER_ROW = 10;

    private static AvatarRenderer instance = null;

    private Image avatarSheet = MgImageLoader.load("/avatar");
    private String cachedPlayerName = null;

    private AvatarRenderer() {
    }

    public static final AvatarRenderer a() {
        if (instance == null) {
            instance = new AvatarRenderer();
        }
        return instance;
    }

    public static final void a(AvatarRenderer renderer) {
        instance = renderer;
        if (renderer == null) {
            System.gc();
        }
    }

    private String resolveHeroName(int avatarId) {
        String name = DialogueData.a[avatarId];
        if (name.equals("PLAYER")) {
            if (this.cachedPlayerName == null) {
                this.cachedPlayerName = CampaignData.c();
            }
            name = this.cachedPlayerName;
        }
        return name;
    }

    public static final String a(int avatarId) {
        return a().resolveHeroName(avatarId);
    }

    public static final void a(Graphics g, int avatarId, int x, int y, boolean faceRight) {
        a().drawAvatar(g, avatarId, x, y, faceRight);
    }

    public static final Image getAvatarSheet() {
        return a().avatarSheet;
    }

    private void drawAvatar(Graphics g, int avatarId, int x, int y, boolean faceRight) {
        int srcX = (avatarId % COLUMNS_PER_ROW) * AVATAR_SIZE;
        int srcY = (avatarId / COLUMNS_PER_ROW) * AVATAR_SIZE;
        if (!faceRight) {
            DrawUtils.drawRegion(g, this.avatarSheet, srcX, srcY, AVATAR_SIZE, AVATAR_SIZE, x, y, 0);
        } else {
            g.drawRegion(this.avatarSheet, srcX, srcY, AVATAR_SIZE, AVATAR_SIZE, Sprite.TRANS_MIRROR, x, y, 0);
        }
    }
}

