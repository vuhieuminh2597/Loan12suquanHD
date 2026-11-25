package com.mg.engine.graphics;

import com.mg.engine.audio.BattleSfxPlayer;
import com.mg.engine.audio.JinglePlayer;
import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Loads, caches, and disposes all match-3 battle sprites, HUD bars, spell animations,
 * and battle sound effects.
 */
public class BattleResourceManager {
    private static BattleResourceManager instance;

    public Image a;
    public Image b;
    public Image[] c;
    public Image[] d;
    public Image[] e;
    public Image[] f;
    public Image[] g;
    public Image[] h;
    public Image[] i;
    public Image[] j;
    public Image[] k;
    public Image l;
    public Image m;
    public Image n;
    public Image o;
    public Image p;
    public Image q;
    public Image r;
    public Image s;
    public Image t;
    public Image u;
    public Image v;
    public Image w;
    public Image x;
    public Image y;
    public Image z;
    public Image A;
    public Image B;
    public Image C;
    public Image D;
    public Image E;
    public BattleSfxPlayer[] F;
    public BattleSfxPlayer G;
    public BattleSfxPlayer H;
    public BattleSfxPlayer I;
    public JinglePlayer J;

    public static BattleResourceManager a() {
        return instance;
    }

    public static void b() {
        instance = new BattleResourceManager();
    }

    public void a(int[] skillIds) {
        for (int idx = 0; idx < skillIds.length; ++idx) {
            int vfxId = skillIds[idx] % 9;
            switch (vfxId) {
                case 0:
                case 1:
                    if (this.r == null) {
                        this.r = MgImageLoader.load("/fireball");
                    }
                    break;
                case 2:
                    if (this.s == null) {
                        this.s = MgImageLoader.load("/hellfire");
                    }
                    break;
                case 3:
                    if (this.y == null) {
                        this.y = MgImageLoader.load("/chainlighting");
                        this.h = new Image[]{
                                MgImageLoader.load("/chainlightingpart1"),
                                MgImageLoader.load("/chainlightingpart2")
                        };
                    }
                    break;
                case 4:
                    if (this.x == null) {
                        this.x = MgImageLoader.load("/shieldlighting");
                    }
                    break;
                case 5:
                    if (this.t == null) {
                        this.t = MgImageLoader.load("/blastlighting");
                    }
                    break;
                case 7:
                    if (this.w == null) {
                        this.w = MgImageLoader.load("/healing");
                    }
                    break;
                case 8:
                    if (this.v == null) {
                        this.v = MgImageLoader.load("/frozen");
                    }
                    // Fall through to also load /icebolt (matches original behavior).
                case 6:
                    if (this.u == null) {
                        this.u = MgImageLoader.load("/icebolt");
                    }
                    break;
            }
        }
    }

    public void c() {
        this.G = new BattleSfxPlayer("explodechess");
        if (!GameConfig.isNokiaSeries40) {
            this.I = new BattleSfxPlayer("attack");
            this.H = new BattleSfxPlayer("useitem");
        }
        this.J = new JinglePlayer();
        this.J.load("win", 1);
    }

    public void d() {
        this.i();
        if (this.J != null) {
            this.J.load("win", 1);
            this.J.setTempo(123015);
            this.J.playAsync();
        }
    }

    public void e() {
        this.i();
        if (this.J != null) {
            this.J.load("lose", 1);
            this.J.setTempo(155041);
            this.J.playAsync();
        }
    }

    public void b(int[] skillIds) {
        if (GameConfig.isNokiaSeries40) {
            return;
        }
        String[] sfxNames = new String[]{
                "fire1", "fire2", "fire3",
                "lightning1", "lightning2", "lightning3",
                "ice1", "ice2", "ice3"
        };
        for (int idx = 0; idx < skillIds.length; ++idx) {
            int id = skillIds[idx] % 9;
            if (id >= 0 && id < sfxNames.length && this.F[id] == null) {
                this.F[id] = new BattleSfxPlayer(sfxNames[id]);
            }
        }
    }

    private void stopIfPlaying(BattleSfxPlayer player) {
        if (player != null && player.isPlaying()) {
            player.stop();
        }
    }

    private boolean isAnySkillSfxPlaying() {
        for (int idx = this.F.length - 1; idx >= 0; --idx) {
            if (this.F[idx] != null && this.F[idx].isPlaying()) {
                return true;
            }
        }
        return false;
    }

    public void a(int skillId) {
        int vfxId = skillId % 9;
        if (this.F != null && vfxId >= 0 && vfxId < this.F.length && !this.isPlaying(this.F[vfxId])) {
            this.i();
            this.a(this.F[vfxId]);
        }
    }

    public void f() {
        if (!this.isAnySkillSfxPlaying() && !this.isPlaying(this.I)) {
            this.stopIfPlaying(this.H);
            this.stopIfPlaying(this.G);
            this.a(this.I);
        }
    }

    public void g() {
        if (!this.isAnySkillSfxPlaying() && !this.isPlaying(this.I) && !this.isPlaying(this.H) && !this.isPlaying(this.G)) {
            this.a(this.G);
        }
    }

    public void h() {
        if (!this.isAnySkillSfxPlaying() && !this.isPlaying(this.I) && !this.isPlaying(this.H)) {
            this.stopIfPlaying(this.G);
            this.a(this.H);
        }
    }

    public void i() {
        for (int idx = this.F.length - 1; idx >= 0; --idx) {
            this.stopIfPlaying(this.F[idx]);
        }
        this.stopIfPlaying(this.I);
        this.stopIfPlaying(this.G);
        this.stopIfPlaying(this.H);
    }

    private void destroyAllSfx() {
        for (int idx = this.F.length - 1; idx >= 0; --idx) {
            this.b(this.F[idx]);
        }
        this.b(this.I);
        this.b(this.H);
        this.F = null;
        this.I = null;
        this.G = null;
        this.H = null;
    }

    public void j() {
        this.destroyAllSfx();
        if (this.J != null) {
            this.J.destroy();
        }
    }

    private boolean isPlaying(BattleSfxPlayer player) {
        return player != null && player.isPlaying();
    }

    public void a(BattleSfxPlayer player) {
        if (player != null) {
            player.playAsync();
        }
    }

    public void b(BattleSfxPlayer player) {
        if (player != null) {
            player.destroy();
        }
    }

    public void c(int[] skillIds) {
        String[] iconPaths = new String[]{
                "/fireballicon", "/meteoricon", "/hellfireicon",
                "/chainlightingicon", "/shieldlightingicon", "/blastlightingicon",
                "/icebolticon", "/healingicon", "/frozenicon"
        };
        this.i = new Image[skillIds.length];
        for (int idx = 0; idx < skillIds.length; ++idx) {
            int id = skillIds[idx] % 9;
            if (id >= 0 && id < iconPaths.length) {
                this.i[idx] = MgImageLoader.load(iconPaths[id]);
            }
        }
    }

    protected BattleResourceManager() {
        this.b = Image.createImage(GameConfig.screenWidth, GameConfig.screenHeight);
        Graphics bgGraphics = this.b.getGraphics();
        Image boardBg = MgImageLoader.load("/bkboard");
        bgGraphics.drawImage(boardBg, GameConfig.contentOffsetX, GameConfig.contentOffsetY, 0);

        String[] crystalPaths = new String[]{"/crystalgray", "/crystalgreen", "/crystalred", "/crystalblue", "/crystalyellow", "/crystalviolet", ""};
        String[] gemPaths = new String[]{"/sword", "/rice", "/heart", "/yinyang", "/gold", "/book", "/swordred"};
        String[] explodePaths = new String[]{"/explodesword", "/exploderice", "/explodeheart", "/explodeyinyang", "/explodegold", "/explodebook", ""};
        String[] superExplodePaths = new String[]{"", "/explodericex2", "/explodeheartx2", "/explodeyinyangx2", "/explodegoldx2", "/explodebookx2", ""};

        this.e = new Image[7];
        this.f = new Image[7];
        this.d = new Image[7];
        this.c = new Image[7];

        for (int idx = this.e.length - 1; idx > -1; --idx) {
            this.d[idx] = MgImageLoader.load(gemPaths[idx]);
            this.e[idx] = MgImageLoader.load(explodePaths[idx]);
            this.f[idx] = MgImageLoader.load(superExplodePaths[idx]);
            this.c[idx] = MgImageLoader.load(crystalPaths[idx]);
        }

        this.c[6] = this.c[0];
        this.f[0] = this.f[6] = this.e[6] = this.e[0];
        this.a = MgImageLoader.load("/star");
        this.l = MgImageLoader.load("/blood");
        this.m = MgImageLoader.load("/bloodext");
        this.n = MgImageLoader.load("/mana");
        this.o = MgImageLoader.load("/food");
        this.p = MgImageLoader.load("/bloodthrowaround");

        this.j = new Image[2];
        this.g = new Image[2];
        for (int idx = 0; idx < this.j.length; ++idx) {
            this.j[idx] = MgImageLoader.load("/arrowfocus" + (idx + 1));
            this.g[idx] = MgImageLoader.load("/focus" + (idx + 1));
        }

        this.q = MgImageLoader.load("/zoomfocus1");
        this.k = new Image[]{
                MgImageLoader.load("/frame1"),
                MgImageLoader.load("/frame2")
        };
        this.z = MgImageLoader.load("/frame3");
        this.A = MgImageLoader.load("/focusitem");
        this.B = MgImageLoader.load("/numbersmall");
        this.D = MgImageLoader.load("/powerattack");
        this.C = MgImageLoader.load("/defenceshield");
        this.E = MgImageLoader.load("/itemshops");
        this.F = new BattleSfxPlayer[9];
    }

    public Image[] a(int playerAvatarId, int enemyAvatarId) {
        Image avatarSheet = MgImageLoader.load("/avatar");
        return new Image[]{
                Image.createImage(avatarSheet, (playerAvatarId % 10) * 30, (playerAvatarId / 10) * 30, 30, 30, Sprite.TRANS_MIRROR),
                Image.createImage(avatarSheet, (enemyAvatarId % 10) * 30, (enemyAvatarId / 10) * 30, 30, 30, Sprite.TRANS_NONE)
        };
    }

    public static void k() {
        if (instance != null) {
            instance.j();
            instance = null;
        }
    }
}
