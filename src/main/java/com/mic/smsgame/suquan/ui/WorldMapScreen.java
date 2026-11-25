package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.loan12suquan.hd.HdWorldMapEnhancer;
import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.audio.SfxPlayer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.Rect;
import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.storage.CampaignStorage;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.AnimatedSpriteBlock;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.RichContentDialog;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.SoftKeyButton;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mg.engine.ui.TouchDPad;
import com.mg.engine.ui.Window;
import com.mg.engine.util.FixedPointMath;
import com.mg.engine.util.MathUtils;
import com.mg.engine.util.ObjectList;
import com.mg.engine.util.RandomUtils;
import com.mg.engine.util.StringUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.DialogueData;
import com.mic.smsgame.suquan.data.MapNode;
import com.mic.smsgame.suquan.data.MapRoad;
import com.mic.smsgame.suquan.data.WorldMapData;
import com.mic.smsgame.suquan.gfx.AvatarRenderer;
import com.mic.smsgame.suquan.gfx.SwordClashEffect;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.HeroStats;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class WorldMapScreen extends BaseScreen implements ActionListener {
    private int mapState = -1;
    private Image hudHeaderImage;
    private Image worldMapImage;
    private Image citadelFlagSheet;
    private Image nodeDotSheet;
    private int nodeDotWidth;
    private int nodeDotHeight;
    private MapNode[] mapNodes;
    private MapRoad[] mapRoads;
    private boolean[] hdUnlockedFlags;
    private int currentNodeIndex = 0;
    private int cameraX;
    private int cameraY;
    private int targetNodeIndex;
    private int playerMapX;
    private int playerMapY;
    private int playerFixedX;
    private int playerFixedY;
    private int stepFixedX;
    private int stepFixedY;
    private int travelStepsRemaining;
    private int ambushCheckStep;
    private BattleIntroPanel battleIntroPanel;
    private DialogueBox activeDialogueBox;
    private ArenaSelectPanel arenaSelectPanel;
    private AnimatedSprite playerFlagSprite;
    private int dialoguePhase;
    private int[] nodeOwnerFactions;
    private int currentQuestId;
    private Rect viewportRect;
    private int mapPixelWidth;
    private int mapPixelHeight;
    private int pendingDirection;
    private int activeRoadIndex;
    private SwordClashEffect swordClashEffect;
    private boolean isRandomAmbush;
    private int activeMainQuestNode;
    private int activeSideQuestNode;
    private int lastSideQuestId;
    private int stepsUntilNextAmbush;
    private AnimatedSprite questMarkerSprite;
    private int questMarkerDelay;
    private boolean cameraFollowPlayer = true;
    private boolean hudVisible;
    private SfxPlayer clashSfxPlayer;
    private boolean pendingRelicDrop;
    private boolean pendingLevelUpNotify;
    private boolean isFinalQuestRelic;
    private int droppedRelicSlotIndex;
    private int pendingQuestShards = 0;
    public static boolean c;
    private Image goldIconImage;
    private boolean[] arenaTierUnlocked;
    private TouchDPad mapTouchDPad;
    private int restPillX = -1;
    private int restPillY = -1;
    private int restPillW = 0;
    private int restPillH = 0;
    private int compassPillX = -1;
    private int compassPillY = -1;
    private int compassPillW = 0;
    private int compassPillH = 0;
    private int mapAnimTick = 0;
    private String mapFeedbackText = null;
    private int mapFeedbackTicks = 0;
    public static boolean d = false;
    public static boolean e = false;

    public WorldMapScreen() {
        System.gc();
        c = false;
        ((Window)this).c(false);
        ((Window)this).a(new SoftKeyBar());
        ((Window)this).a((ActionListener)this);
        this.hudHeaderImage = MgImageLoader.load("/m/hm");
        this.worldMapImage = MgImageLoader.load("/m/m");
        this.citadelFlagSheet = MgImageLoader.load("/m/b");
        this.nodeDotSheet = MgImageLoader.load("/m/r");
        this.nodeDotWidth = this.nodeDotSheet.getWidth() / 4;
        this.nodeDotHeight = this.nodeDotSheet.getHeight();
        super.b_ = this.worldMapImage.getWidth();
        super.c_ = this.worldMapImage.getHeight();
        this.viewportRect = new Rect(0, 0, GameConfig.screenWidth, GameConfig.screenHeight);
        this.playerFlagSprite = new AnimatedSprite(MgImageLoader.load("/m/mt1"), 3);
        this.playerFlagSprite.c(3);
        this.playerFlagSprite.b(true);
        this.playerFlagSprite.a(new byte[][]{{0, 1, 2, 1}});
        this.playerFlagSprite.a_(0);
        this.questMarkerSprite = new AnimatedSprite(MgImageLoader.load("/m/ar"), 6);
        this.questMarkerSprite.c(1);
        this.questMarkerSprite.b(true);
        this.questMarkerSprite.a(new byte[][]{{0, 1, 2, 3, 4, 5, 5, 4, 3, 2, 1, 0}});
        this.questMarkerSprite.a_(0);
        AvatarRenderer.a((AvatarRenderer)null);
        this.pendingRelicDrop = false;
        this.pendingLevelUpNotify = false;
        this.isFinalQuestRelic = false;
        this.mapTouchDPad = new TouchDPad("/m/vk", GameConfig.screenWidth - 55, 0);
        this.goldIconImage = MgImageLoader.load("/gold");
        this.cameraFollowPlayer = false;
        this.questMarkerDelay = 0;
        this.u();
        this.v();
        this.D();
        this.f(7);
        this.clashSfxPlayer = new SfxPlayer("vs");
        BgmPlayer.getInstance().load("m", -1);
        BgmPlayer.getInstance().setTempo(180184);
        BgmPlayer.getInstance().playAsync();
        super.q = 7;
    }

    private void u() {
        this.mapNodes = new MapNode[WorldMapData.a.length];

        for(int num1 = 0; num1 < this.mapNodes.length; ++num1) {
            this.mapNodes[num1] = new MapNode();
            this.mapNodes[num1].b = WorldMapData.b[num1][0];
            this.mapNodes[num1].c = WorldMapData.b[num1][1];
            this.mapNodes[num1].a = WorldMapData.b[num1][2];
            this.mapNodes[num1].d = false;
        }

        this.mapNodes[0].d = true;
        this.mapNodes[0].e = 0;
        int num3 = 0;

        for(int num2 = CampaignData.f(); num3 < num2; this.mapNodes[CampaignData.q(num3)].f = num3++) {
        }

        this.mapRoads = new MapRoad[WorldMapData.d.length];

        for(int num4 = 0; num4 < this.mapRoads.length; ++num4) {
            this.mapRoads[num4] = new MapRoad();
            this.mapRoads[num4].a = WorldMapData.d[num4][0];
            this.mapRoads[num4].b = WorldMapData.d[num4][1];
            this.mapRoads[num4].c = false;
        }

        this.nodeOwnerFactions = new int[WorldMapData.a()];

        for(int num5 = 0; num5 < this.nodeOwnerFactions.length; ++num5) {
            this.nodeOwnerFactions[num5] = CampaignData.z(num5);
        }

        this.currentNodeIndex = 0;
        this.cameraY = -1;
        this.cameraX = -1;
        this.dialoguePhase = CampaignData.a();
        this.currentQuestId = 0;
        this.stepsUntilNextAmbush = RandomUtils.nextRange(3, 10);
        this.mapPixelHeight = CampaignData.b();
        this.pendingDirection = EngineStorage.f(2);
        this.hudVisible = false;
        this.arenaTierUnlocked = new boolean[CampaignData.k().length];

        for(int num6 = 0; num6 < this.arenaTierUnlocked.length; ++num6) {
            this.arenaTierUnlocked[num6] = false;
        }

        this.arenaTierUnlocked[0] = this.arenaTierUnlocked[1] = true;
        this.activeMainQuestNode = -1;
        this.activeSideQuestNode = -1;
    }

    private void a(int[] num1) {
        if (num1 != null) {
            for(int num2 = 0; num2 < num1.length; ++num2) {
                this.mapNodes[num1[num2]].d = true;
            }

            for(int num3 = 0; num3 < this.mapRoads.length; ++num3) {
                if (!this.mapRoads[num3].c && this.mapNodes[this.mapRoads[num3].a].d && this.mapNodes[this.mapRoads[num3].b].d) {
                    this.mapRoads[num3].c = true;
                }
            }

        }
    }

    private void v() {
        if (!CampaignStorage.hasHeroSave()) {
            CampaignData.e = CampaignData.t(CampaignData.d());
        } else {
            try {
                byte[] bVal1 = CampaignStorage.loadHeroData();
                ByteArrayInputStream byteIn2 = new ByteArrayInputStream(bVal1);
                DataInputStream inStream3 = new DataInputStream(byteIn2);
                this.currentNodeIndex = inStream3.readInt();
                this.targetNodeIndex = inStream3.readByte();
                this.cameraY = inStream3.readInt();
                this.currentQuestId = inStream3.readInt();

                for(int num4 = 0; num4 < this.mapNodes.length; ++num4) {
                    this.mapNodes[num4].d = inStream3.readBoolean();
                    this.mapNodes[num4].e = inStream3.readByte();
                    this.mapNodes[num4].f = inStream3.readByte();
                    this.nodeOwnerFactions[num4] = inStream3.readByte();
                }

                for(int num7 = 0; num7 < this.mapRoads.length; ++num7) {
                    this.mapRoads[num7].c = inStream3.readBoolean();
                }

                this.stepsUntilNextAmbush = inStream3.readByte();
                this.travelStepsRemaining = inStream3.readByte();
                this.ambushCheckStep = inStream3.readByte();
                this.stepFixedX = inStream3.readInt();
                this.stepFixedY = inStream3.readInt();
                this.playerMapX = inStream3.readInt();
                this.playerMapY = inStream3.readInt();
                HeroStats hero8 = new HeroStats();
                hero8.e = inStream3.readByte();
                hero8.c = inStream3.readInt();
                hero8.g = inStream3.readInt();
                hero8.h = inStream3.readInt();
                CampaignData.b = inStream3.readInt();
                this.hudVisible = inStream3.readBoolean();

                for(int num5 = 0; num5 < this.arenaTierUnlocked.length; ++num5) {
                    this.arenaTierUnlocked[num5] = inStream3.readBoolean();
                }

                boolean relicStatsChanged = false;
                if (CampaignData.e == null) {
                    CampaignData.e = CampaignData.t(CampaignData.d());
                    CampaignData.e.e = hero8.e;
                    CampaignData.e.c = hero8.c;
                    CampaignData.e.h = hero8.h;
                    CampaignData.e.g = hero8.g;
                    if (this.dialoguePhase == 0) {
                        int persistedExp = CampaignData.b();
                        while (CampaignData.e.e < HeroStats.MAX_LEVEL
                                && CampaignData.c(CampaignData.e.e + 1, persistedExp)) {
                            CampaignData.e.a();
                        }
                    }
                    CampaignData.e.b();
                } else {
                    relicStatsChanged = CampaignData.e.refreshAfterRelicUnlock();
                }

                inStream3.close();
                if (relicStatsChanged) {
                    this.w();
                }
            } catch (IOException err6) {
                ((Throwable)err6).printStackTrace();
            }

        }
    }

    private void w() {
        try {
            ByteArrayOutputStream byteOut1 = new ByteArrayOutputStream();
            DataOutputStream outStream2 = new DataOutputStream(byteOut1);
            outStream2.writeInt(this.currentNodeIndex);
            outStream2.writeByte(this.targetNodeIndex);
            outStream2.writeInt(this.cameraY);
            outStream2.writeInt(this.currentQuestId);

            for(int num3 = 0; num3 < this.mapNodes.length; ++num3) {
                outStream2.writeBoolean(this.mapNodes[num3].d);
                outStream2.writeByte(this.mapNodes[num3].e);
                outStream2.writeByte(this.mapNodes[num3].f);
                outStream2.writeByte(this.nodeOwnerFactions[num3]);
            }

            for(int num5 = 0; num5 < this.mapRoads.length; ++num5) {
                outStream2.writeBoolean(this.mapRoads[num5].c);
            }

            outStream2.writeByte(this.stepsUntilNextAmbush);
            outStream2.writeByte(this.travelStepsRemaining);
            outStream2.writeByte(this.ambushCheckStep);
            outStream2.writeInt(this.stepFixedX);
            outStream2.writeInt(this.stepFixedY);
            outStream2.writeInt(this.playerMapX);
            outStream2.writeInt(this.playerMapY);
            outStream2.writeByte(CampaignData.e.e);
            outStream2.writeInt(CampaignData.e.c);
            outStream2.writeInt(CampaignData.e.g);
            outStream2.writeInt(CampaignData.e.h);
            outStream2.writeInt(CampaignData.b);
            outStream2.writeBoolean(this.hudVisible);

            for(int num6 = 0; num6 < this.arenaTierUnlocked.length; ++num6) {
                outStream2.writeBoolean(this.arenaTierUnlocked[num6]);
            }

            outStream2.flush();
            CampaignStorage.saveHeroData(byteOut1.toByteArray());
            outStream2.close();
        } catch (IOException err4) {
            ((Throwable)err4).printStackTrace();
        }

    }

    private void x() {
        if (this.travelStepsRemaining == 0) {
            int retreatRoad = -1;
            if (this.cameraY >= 0) {
                switch (this.cameraY) {
                    case 0:
                        this.cameraX = 1;
                        break;
                    case 1:
                        this.cameraX = 0;
                        break;
                    case 2:
                        this.cameraX = 3;
                        break;
                    case 3:
                        this.cameraX = 2;
                        break;
                    default:
                        this.cameraX = -1;
                        break;
                }
                if (this.cameraX >= 0) {
                    int candidate = WorldMapData.c[this.currentNodeIndex][this.cameraX];
                    if (candidate >= 0 && this.mapRoads[candidate].c) {
                        retreatRoad = candidate;
                    }
                }
            }

            if (retreatRoad < 0) {
                int fallbackRoad = -1;
                int fallbackDir = -1;
                for (int dir = 0; dir < 4; ++dir) {
                    int candidate = WorldMapData.c[this.currentNodeIndex][dir];
                    if (candidate >= 0 && this.mapRoads[candidate].c) {
                        if (fallbackRoad < 0) {
                            fallbackRoad = candidate;
                            fallbackDir = dir;
                        }
                        int neighbor = this.mapRoads[candidate].a == this.currentNodeIndex
                                ? this.mapRoads[candidate].b
                                : this.mapRoads[candidate].a;
                        if (neighbor >= 0 && neighbor < this.nodeOwnerFactions.length
                                && this.nodeOwnerFactions[neighbor] < 0
                                && !this.e(neighbor)) {
                            retreatRoad = candidate;
                            this.cameraX = dir;
                            break;
                        }
                    }
                }
                if (retreatRoad < 0 && fallbackRoad >= 0) {
                    retreatRoad = fallbackRoad;
                    this.cameraX = fallbackDir;
                }
            }

            if (retreatRoad >= 0) {
                this.activeRoadIndex = retreatRoad;
                ++this.stepsUntilNextAmbush;
                this.f(1);
            } else {
                this.f(0);
            }
        } else {
            this.stepFixedX = -this.stepFixedX;
            this.stepFixedY = -this.stepFixedY;
            this.targetNodeIndex = this.currentNodeIndex;
            ++this.stepsUntilNextAmbush;
            this.f(1);
        }

    }

    private void a(Graphics gfx1, MapNode val2, MapNode val3) {
        if (this.viewportRect.a(val2.b + super.g, val2.c + super.a_) || this.viewportRect.a(val3.b + super.g, val3.c + super.a_)) {
            boolean hd = HdConfig.isHdRemaster() && gfx1.getHdCanvas() != null;
            int num4 = FixedPointMath.fromInt(val2.b);
            int num5 = FixedPointMath.fromInt(val2.c);
            int num6 = FixedPointMath.fromInt(val3.b) - num4;
            int num7 = FixedPointMath.fromInt(val3.c) - num5;
            int num8 = FixedPointMath.sqrt(FixedPointMath.mul(num6, num6) + FixedPointMath.mul(num7, num7));
            int num9 = FixedPointMath.div(FixedPointMath.mul(FixedPointMath.fromInt(10), num6), num8);
            int num10 = FixedPointMath.div(FixedPointMath.mul(FixedPointMath.fromInt(10), num7), num8);
            int num11 = FixedPointMath.toInt(FixedPointMath.div(num8, FixedPointMath.fromInt(10)));

            for(int num12 = 0; num12 < num11; ++num12) {
                num4 += num9;
                num5 += num10;
                int rx = FixedPointMath.toInt(num4) + super.g;
                int ry = FixedPointMath.toInt(num5) + super.a_;
                if (hd) {
                    HdWorldMapEnhancer.drawHdRoadDot(
                            gfx1.getHdCanvas(), rx, ry,
                            ((num12 + (this.mapAnimTick >> 2)) & 3) == 0);
                } else {
                    DrawUtils.drawRegion(gfx1, this.nodeDotSheet, 0, 0, this.nodeDotWidth, this.nodeDotHeight, rx, ry, 3);
                }
            }
        }

    }

    private void b(Graphics gfx1) {
        for(int num2 = 0; num2 < this.mapRoads.length; ++num2) {
            if (this.mapRoads[num2].c) {
                this.a(gfx1, this.mapNodes[this.mapRoads[num2].a], this.mapNodes[this.mapRoads[num2].b]);
            }
        }

        boolean hd = HdConfig.isHdRemaster() && gfx1.getHdCanvas() != null;
        if (hd) {
            if (this.hdUnlockedFlags == null || this.hdUnlockedFlags.length != this.mapNodes.length) {
                this.hdUnlockedFlags = new boolean[this.mapNodes.length];
            }
            for (int num3 = 0; num3 < this.mapNodes.length; ++num3) {
                boolean unlocked = this.mapNodes[num3].d;
                this.hdUnlockedFlags[num3] = unlocked;
                if (unlocked) {
                    int nx = super.g + this.mapNodes[num3].b;
                    int ny = super.a_ + this.mapNodes[num3].c;
                    boolean isCitadel = (this.mapNodes[num3].a == 1);
                    boolean enemyOwned = (isCitadel && this.nodeOwnerFactions[num3] >= 0);
                    boolean hovered = (this.mapState == 0 && HdInputState.isHovered(nx - 9, ny - 9, 18, 18));
                    HdWorldMapEnhancer.drawHdMapNode(gfx1.getHdCanvas(), nx, ny, isCitadel, enemyOwned, hovered);
                }
            }
            HdWorldMapEnhancer.drawHdCitadelNameplates(
                    gfx1.getHdCanvas(), super.g, super.a_, this.currentNodeIndex, this.hdUnlockedFlags);
            return;
        }

        for(int num3 = 0; num3 < this.mapNodes.length; ++num3) {
            if (this.mapNodes[num3].d) {
                if (this.mapNodes[num3].a == 1) {
                    if (this.nodeOwnerFactions[num3] < 0) {
                        DrawUtils.drawRegion(gfx1, this.nodeDotSheet, 9, 0, this.nodeDotWidth, this.nodeDotHeight, super.g + this.mapNodes[num3].b, super.a_ + this.mapNodes[num3].c, 3);
                    } else {
                        DrawUtils.drawRegion(gfx1, this.nodeDotSheet, 18, 0, this.nodeDotWidth, this.nodeDotHeight, super.g + this.mapNodes[num3].b, super.a_ + this.mapNodes[num3].c, 3);
                    }
                } else if (this.mapNodes[num3].a == 0) {
                    DrawUtils.drawRegion(gfx1, this.nodeDotSheet, 27, 0, this.nodeDotWidth, this.nodeDotHeight, super.g + this.mapNodes[num3].b, super.a_ + this.mapNodes[num3].c, 3);
                }
            }
        }

    }

    private boolean h(int num1, int num2) {
        int num5 = (GameConfig.screenWidth >> 1) - super.g;
        int num6 = (GameConfig.screenHeight >> 1) - super.a_;
        int num7 = GameConfig.screenWidth >> 1;
        int num8 = GameConfig.screenHeight >> 1;
        if (num1 > super.b_ - num7) {
            num1 = super.b_ - num7;
        }

        if (num1 < num7) {
            num1 = num7;
        }

        if (num2 > super.c_ - num8) {
            num2 = super.c_ - num8;
        }

        if (num2 < num8) {
            num2 = num8;
        }

        int num3;
        if (num1 - num5 <= 4 && num1 - num5 >= 4) {
            if (num1 == num5) {
                num3 = 0;
            } else {
                num3 = num1 - num5 > 0 ? 1 : -1;
            }
        } else {
            num3 = (num1 - num5) / 4;
        }

        int num4;
        if (num2 - num6 <= 4 && num2 - num6 >= 4) {
            if (num2 == num6) {
                num4 = 0;
            } else {
                num4 = num2 - num6 > 0 ? 1 : -1;
            }
        } else {
            num4 = (num2 - num6) / 4;
        }

        if (num3 == 0 && num4 == 0) {
            return false;
        } else {
            super.g -= num3;
            super.a_ -= num4;
            return true;
        }
    }

    public void a(int num1) {
        if (this.mapState != 1) {
            switch (this.mapState) {
                case 0:
                    this.cameraX = -1;
                    this.activeRoadIndex = -1;
                    switch (num1) {
                        case 12:
                            this.cameraX = 0;
                            break;
                        case 13:
                            this.cameraX = 1;
                            break;
                        case 14:
                            this.cameraX = 2;
                            break;
                        case 15:
                            this.cameraX = 3;
                    }

                    if (this.cameraX >= 0) {
                        this.activeRoadIndex = WorldMapData.c[this.currentNodeIndex][this.cameraX];
                        if (this.activeRoadIndex >= 0 && this.mapRoads[this.activeRoadIndex].c) {
                            this.f(1);
                        }
                    }
                    break;
                case 9:
                    if (this.arenaSelectPanel != null) {
                        this.arenaSelectPanel.a(num1);
                    }
            }

        }
    }

    public int c(int pointerX, int pointerY) {
        if (this.mapState != 0) {
            return -1;
        }
        // Let taps on citadel Rest pill, off-screen quest compass, or adjacent unlocked nodes bypass the virtual D-Pad.
        if (this.isPointInRestPill(pointerX, pointerY) || this.isPointInCompassPill(pointerX, pointerY)) {
            return -1;
        }
        if (this.findAdjacentRoadAt(pointerX, pointerY) >= 0) {
            return -1;
        }
        return super.c(pointerX, pointerY);
    }

    private boolean canRestAtCurrentNode() {
        if (this.mapState != 0 || this.travelStepsRemaining > 0 || CampaignData.e == null) {
            return false;
        }
        if (this.currentNodeIndex < 0 || this.currentNodeIndex >= this.mapNodes.length) {
            return false;
        }
        return this.mapNodes[this.currentNodeIndex].a == 0
                && this.nodeOwnerFactions[this.currentNodeIndex] < 0
                && CampaignData.e.c < CampaignData.e.b;
    }

    private String getCurrentCitadelName() {
        return WorldMapData.getLocationName(this.currentNodeIndex);
    }

    private boolean isPointInRestPill(int px, int py) {
        return this.canRestAtCurrentNode()
                && this.restPillW > 0
                && px >= this.restPillX && px <= this.restPillX + this.restPillW
                && py >= this.restPillY && py <= this.restPillY + this.restPillH;
    }

    private boolean isPointInCompassPill(int px, int py) {
        return this.mapState == 0
                && this.compassPillW > 0
                && px >= this.compassPillX && px <= this.compassPillX + this.compassPillW
                && py >= this.compassPillY && py <= this.compassPillY + this.compassPillH;
    }

    private void panCameraToMainQuest() {
        if (this.currentQuestId < 0 || this.currentQuestId >= CampaignData.e()) {
            return;
        }
        int targetNode = CampaignData.e(this.currentQuestId);
        if (targetNode < 0 || targetNode >= this.mapNodes.length) {
            return;
        }
        this.activeMainQuestNode = this.mapNodes[targetNode].b;
        this.activeSideQuestNode = this.mapNodes[targetNode].c;
        this.f(8);
        this.lastSideQuestId = 32;
    }

    private int getNodeEnemyLevel(int nodeIndex, int heroLevel) {
        if (nodeIndex < 0 || nodeIndex >= this.mapNodes.length) {
            return -1;
        }
        MapNode node = this.mapNodes[nodeIndex];
        if (node.e >= 0) {
            return CampaignData.getMainQuestEnemyLevel(node.e);
        }
        if (node.f >= 0) {
            return CampaignData.getSideQuestEnemyLevel(node.f);
        }
        if (this.nodeOwnerFactions[nodeIndex] >= 0) {
            return CampaignData.getNodeGuardLevel(nodeIndex, heroLevel);
        }
        return -1;
    }

    private boolean isAdjacentUnlockedNode(int nodeIndex) {
        if (this.currentNodeIndex < 0 || this.currentNodeIndex >= WorldMapData.c.length) {
            return false;
        }
        for (int dir = 0; dir < 4; ++dir) {
            int roadIdx = WorldMapData.c[this.currentNodeIndex][dir];
            if (roadIdx >= 0 && this.mapRoads[roadIdx].c) {
                int other = this.mapRoads[roadIdx].a == this.currentNodeIndex
                        ? this.mapRoads[roadIdx].b
                        : this.mapRoads[roadIdx].a;
                if (other == nodeIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    private String buildMainQuestSummary() {
        if (this.currentQuestId < 0 || this.currentQuestId >= CampaignData.e()) {
            return "Chính: Đã thống nhất Đại Cồ Việt!";
        }
        int targetNode = CampaignData.e(this.currentQuestId);
        String loc = WorldMapData.getLocationName(targetNode);
        int enemyTid = CampaignData.getMainQuestEnemyTemplateId(this.currentQuestId);
        int enemyLv = CampaignData.getMainQuestEnemyLevel(this.currentQuestId);
        if (enemyTid >= 0 && enemyLv > 0) {
            String enemyName = AvatarRenderer.a(enemyTid);
            int heroLv = CampaignData.e != null ? CampaignData.e.e : 1;
            int tier = CampaignData.getThreatTier(heroLv, enemyLv);
            String summary = "Chính: " + loc + " - " + enemyName + " (C." + enemyLv + ") [" + CampaignData.getThreatLabel(tier) + "]";
            String bossIntel = BattleFighter.getBossTacticalSummaryForTemplate(enemyTid);
            if (bossIntel != null && this.currentQuestId > 0) {
                summary = summary + " - " + bossIntel;
            }
            return summary;
        }
        return "Chính: Tới " + loc + " (Gặp mặt)";
    }

    private int getMainQuestSummaryColor() {
        if (this.currentQuestId < 0 || this.currentQuestId >= CampaignData.e()) {
            return 0xFFE680;
        }
        int enemyLv = CampaignData.getMainQuestEnemyLevel(this.currentQuestId);
        if (enemyLv > 0) {
            int heroLv = CampaignData.e != null ? CampaignData.e.e : 1;
            return CampaignData.getThreatColor(CampaignData.getThreatTier(heroLv, enemyLv));
        }
        return 0x8CF5E8;
    }

    private int findBestOpenSideQuestNode(int[] countsOut) {
        int openUnlocked = 0;
        int totalRemaining = 0;
        int bestNode = -1;
        int bestScore = 9999;
        for (int i = 0; i < this.mapNodes.length; ++i) {
            if (this.mapNodes[i].f >= 0) {
                ++totalRemaining;
                if (this.mapNodes[i].d) {
                    ++openUnlocked;
                    int sqLv = CampaignData.getSideQuestEnemyLevel(this.mapNodes[i].f);
                    int score = sqLv > 0 ? sqLv : 0;
                    if (score < bestScore) {
                        bestScore = score;
                        bestNode = i;
                    }
                }
            }
        }
        countsOut[0] = openUnlocked;
        countsOut[1] = totalRemaining;
        return bestNode;
    }

    private String buildSideQuestSummary(int[] colorOut) {
        int[] counts = new int[2];
        int bestNode = this.findBestOpenSideQuestNode(counts);
        int openUnlocked = counts[0];
        int totalRemaining = counts[1];
        if (bestNode >= 0) {
            int sqId = this.mapNodes[bestNode].f;
            String loc = WorldMapData.getLocationName(bestNode);
            String prefix = openUnlocked > 1 ? ("Phụ (" + openUnlocked + "): ") : "Phụ: ";
            int sqTid = CampaignData.getSideQuestEnemyTemplateId(sqId);
            int sqLv = CampaignData.getSideQuestEnemyLevel(sqId);
            if (sqTid >= 0 && sqLv > 0) {
                String eName = AvatarRenderer.a(sqTid);
                int heroLv = CampaignData.e != null ? CampaignData.e.e : 1;
                int tier = CampaignData.getThreatTier(heroLv, sqLv);
                colorOut[0] = CampaignData.getThreatColor(tier);
                String sqSummary = prefix + loc + " - " + eName + " (C." + sqLv + ") [" + CampaignData.getThreatLabel(tier) + "]";
                int sqArch = (sqTid == 47) ? BattleFighter.BOSS_ARCHETYPE_CONTROLLER : BattleFighter.resolveBossArchetypeForTemplate(sqTid);
                if (sqArch != BattleFighter.BOSS_ARCHETYPE_NONE) {
                    sqSummary = sqSummary + " - " + BattleFighter.getBossArchetypeBadgeForArchetype(sqArch)
                            + " " + BattleFighter.getBossCounterStrategyForArchetype(sqArch);
                }
                return sqSummary;
            }
            colorOut[0] = 0x99F2FF;
            return prefix + "Tới " + loc + " (Sự kiện)";
        }
        if (totalRemaining == 0) {
            colorOut[0] = 0xA8C8B8;
            return "Phụ: Đã hoàn thành tất cả sự kiện";
        }
        colorOut[0] = 0xC8B8A0;
        return "Phụ: Chưa mở thêm sự kiện mới";
    }

    private void promptCitadelRest() {
        if (!this.canRestAtCurrentNode()) {
            return;
        }
        int cost = CampaignData.computeCitadelRestCost(CampaignData.e);
        String citadel = this.getCurrentCitadelName();
        String msg = "Nghỉ ngơi tại " + citadel + " để hồi đầy 100% sinh lực với giá " + cost + " vàng?";
        BaseDialog confirm = GUIManager.a("Nghỉ ngơi", msg, "Nghỉ", 16, "Không", 5, 1);
        ((Window) confirm).a((ActionListener) this);
        GUIManager.getInstance().a(confirm, true);
    }

    private int findAdjacentRoadAt(int pointerX, int pointerY) {
        int bestDir = -1;
        int bestDistSq = 24 * 24;
        for (int dir = 0; dir < 4; ++dir) {
            int roadIdx = WorldMapData.c[this.currentNodeIndex][dir];
            if (roadIdx < 0 || !this.mapRoads[roadIdx].c) {
                continue;
            }
            int neighborIdx = this.mapRoads[roadIdx].a;
            if (neighborIdx == this.currentNodeIndex) {
                neighborIdx = this.mapRoads[roadIdx].b;
            }
            int nx = super.g + this.mapNodes[neighborIdx].b;
            int ny = super.a_ + this.mapNodes[neighborIdx].c;
            int dx = pointerX - nx;
            int dy = pointerY - ny;
            int distSq = dx * dx + dy * dy;
            if (distSq <= bestDistSq) {
                bestDistSq = distSq;
                bestDir = dir;
            }
        }
        return bestDir;
    }

    public void a(int pointerX, int pointerY) {
        switch (this.mapState) {
            case 0:
                // Top-left HD Hero Mini-Status Pill -> open Hero Info immediately.
                if (HdConfig.isHdRemaster() && CampaignData.e != null
                        && pointerX >= 2 && pointerX <= 116 && pointerY >= 2 && pointerY <= 26) {
                    this.b();
                    return;
                }
                // Paid Citadel Rest button at top-right when wounded at a liberated citadel.
                if (this.isPointInRestPill(pointerX, pointerY)) {
                    this.promptCitadelRest();
                    return;
                }
                // Off-screen Main Quest compass pill -> smoothly pan camera to show target citadel.
                if (this.isPointInCompassPill(pointerX, pointerY)) {
                    this.panCameraToMainQuest();
                    return;
                }
                // 1. Direct tap on an adjacent unlocked citadel/node.
                int directDir = this.findAdjacentRoadAt(pointerX, pointerY);
                if (directDir >= 0) {
                    this.cameraX = directDir;
                    this.activeRoadIndex = WorldMapData.c[this.currentNodeIndex][directDir];
                    this.f(1);
                    return;
                }
                // 2. Tap directly on player's current position -> open Hero Info.
                int playerScreenX = super.g + this.playerMapX;
                int playerScreenY = super.a_ + this.playerMapY;
                int pdx = pointerX - playerScreenX;
                int pdy = pointerY - playerScreenY;
                if (pdx * pdx + pdy * pdy <= 16 * 16) {
                    this.b();
                    return;
                }
                // 3. Directional tap on open map: choose the unlocked road best aligned with the tap vector.
                int bestDir = -1;
                long bestScore = 0L;
                for (int dir = 0; dir < 4; ++dir) {
                    int roadIdx = WorldMapData.c[this.currentNodeIndex][dir];
                    if (roadIdx < 0 || !this.mapRoads[roadIdx].c) {
                        continue;
                    }
                    int neighborIdx = this.mapRoads[roadIdx].a;
                    if (neighborIdx == this.currentNodeIndex) {
                        neighborIdx = this.mapRoads[roadIdx].b;
                    }
                    int rdx = this.mapNodes[neighborIdx].b - this.playerMapX;
                    int rdy = this.mapNodes[neighborIdx].c - this.playerMapY;
                    long dot = (long) pdx * (long) rdx + (long) pdy * (long) rdy;
                    if (dot > 0L) {
                        long rLen = MathUtils.isqrt((long) rdx * (long) rdx + (long) rdy * (long) rdy);
                        if (rLen > 0L) {
                            long score = dot * 1000L / rLen;
                            if (score > bestScore) {
                                bestScore = score;
                                bestDir = dir;
                            }
                        }
                    }
                }
                if (bestDir >= 0) {
                    this.cameraX = bestDir;
                    this.activeRoadIndex = WorldMapData.c[this.currentNodeIndex][bestDir];
                    this.f(1);
                }
                break;
            case 2:
                if (this.activeDialogueBox != null) {
                    this.onAction(super.q, 13);
                }
                break;
            case 3:
                if (this.swordClashEffect == null && this.battleIntroPanel != null) {
                    this.onAction(super.q, 2);
                }
                break;
            case 8:
                this.onAction(super.q, 6);
                break;
            case 9:
                if (this.arenaSelectPanel != null) {
                    if (this.arenaSelectPanel.isOutsidePanel(pointerX, pointerY)) {
                        this.onAction(super.q, 4);
                    } else if (this.arenaSelectPanel.a(pointerX, pointerY)) {
                        this.onAction(super.q, 2);
                    }
                }
                break;
        }
    }

    private boolean e(int num1) {
        MapNode val2 = this.mapNodes[num1];
        if (val2.e >= 0) {
            return true;
        } else if (val2.f >= 0) {
            return true;
        } else {
            return this.nodeOwnerFactions[num1] >= 0;
        }
    }

    private void y() {
        this.activeDialogueBox = null;
        switch (this.dialoguePhase) {
            case 0:
                if (this.mapNodes[this.currentNodeIndex].e >= 0) {
                    int num1 = CampaignData.d(this.mapNodes[this.currentNodeIndex].e);
                    switch (num1) {
                        case 0:
                            this.dialoguePhase = 2;
                            this.f(2);
                            CampaignData.a(this.dialoguePhase);
                            break;
                        default:
                            CampaignData.f = CampaignData.h(this.mapNodes[this.currentNodeIndex].e);
                            this.f(3);
                    }
                } else if (this.mapNodes[this.currentNodeIndex].f >= 0) {
                    int num2 = CampaignData.p(this.mapNodes[this.currentNodeIndex].f);
                    switch (num2) {
                        case 0:
                            this.dialoguePhase = 2;
                            this.f(2);
                            CampaignData.a(this.dialoguePhase);
                            break;
                        default:
                            CampaignData.f = CampaignData.s(this.mapNodes[this.currentNodeIndex].f);
                            this.f(3);
                    }
                } else if (this.nodeOwnerFactions[this.currentNodeIndex] >= 0) {
                    CampaignData.f = CampaignData.a(this.currentNodeIndex, CampaignData.e.e);
                    this.f(3);
                } else {
                    this.f(0);
                }
                break;
            case 1:
            default:
                this.dialoguePhase = 0;
                CampaignData.a(0);
                this.f(0);
                break;
            case 2:
                this.f(5);
                break;
            case 3:
                this.f(6);
        }

    }

    private void z() {
        BaseDialog dialog1 = GUIManager.a("Chúc mừng", "Bạn đã mở thêm được 1 mức luyện tập tại đấu trường. Hãy vào đó thử sức!", "Đồng ý", 5, 1);
        ((Window)dialog1).a((ActionListener)this);
        GUIManager.getInstance().a(dialog1, true);
    }

    private void A() {
        this.dialoguePhase = 0;
        CampaignData.a(this.dialoguePhase);
        this.mapPixelHeight = CampaignData.b();
        int num1 = 0;
        this.activeMainQuestNode = -1;
        this.activeSideQuestNode = -1;
        if (this.travelStepsRemaining == 0) {
            if (this.mapNodes[this.currentNodeIndex].e >= 0) {
                int completedMainQuest = this.mapNodes[this.currentNodeIndex].e;
                this.mapPixelHeight += CampaignData.i(completedMainQuest);
                num1 += CampaignData.j(completedMainQuest);
                int qShards = CampaignData.getMainQuestShardReward(completedMainQuest);
                if (qShards > 0) {
                    this.pendingQuestShards += qShards;
                }
                if (this.currentQuestId == 0) {
                    e = true;
                }

                this.C();
            } else if (this.mapNodes[this.currentNodeIndex].f >= 0) {
                HeroStats heroTmp0 = CampaignData.e;
                heroTmp0.h += CampaignData.n(this.mapNodes[this.currentNodeIndex].f);
                heroTmp0 = CampaignData.e;
                heroTmp0.g += CampaignData.o(this.mapNodes[this.currentNodeIndex].f);
                heroTmp0 = CampaignData.e;
                heroTmp0.c += CampaignData.o(this.mapNodes[this.currentNodeIndex].f);
                CampaignData.e.b();
                this.pendingQuestShards += 2;
                if (CampaignData.r(this.mapNodes[this.currentNodeIndex].f) == 9) {
                    if (!this.arenaTierUnlocked[2]) {
                        this.arenaTierUnlocked[2] = true;
                        this.pendingLevelUpNotify = true;
                    }
                } else if (CampaignData.r(this.mapNodes[this.currentNodeIndex].f) == 14 && !this.arenaTierUnlocked[3]) {
                    this.arenaTierUnlocked[3] = true;
                    this.pendingLevelUpNotify = true;
                }

                this.mapNodes[this.currentNodeIndex].f = -1;
            }

            this.nodeOwnerFactions[this.currentNodeIndex] = -1;
        }

        CampaignData.b(this.mapPixelHeight);
        EngineStorage.a(2, num1, 6);
        this.pendingDirection = EngineStorage.f(2);
        GUIManager.getController().a(true);
        if (CampaignData.e.e >= HeroStats.MAX_LEVEL || !CampaignData.c(CampaignData.e.e + 1, this.mapPixelHeight)) {
            if (this.travelStepsRemaining > 0) {
                this.f(1);
            } else {
                this.f(7);
            }
        } else {
            while(CampaignData.e.e < HeroStats.MAX_LEVEL && CampaignData.c(CampaignData.e.e + 1, this.mapPixelHeight)) {
                CampaignData.e.a();
            }

            this.f(7);
        }

        CampaignData.k = false;
        CampaignData.e.b();
        CampaignData.j = 0;
        CampaignData.i = 0;
        if (CampaignData.e.e >= 30 && !this.arenaTierUnlocked[4]) {
            this.arenaTierUnlocked[4] = true;
            this.pendingLevelUpNotify = true;
        }

        if (this.currentQuestId >= CampaignData.e()) {
            this.f(10);
        } else if (this.travelStepsRemaining == 0) {
            this.w();
        }

    }

    private void B() {
        this.dialoguePhase = 0;
        CampaignData.a(this.dialoguePhase);
        this.mapPixelHeight = CampaignData.b();
        this.pendingDirection = EngineStorage.f(2);
        this.x();
        CampaignData.k = false;
        GUIManager.getController().a(true);
        if (this.travelStepsRemaining == 0) {
            this.w();
        }

    }

    private void C() {
        if (this.currentQuestId < CampaignData.e()) {
            this.a(CampaignData.l(this.mapNodes[this.currentNodeIndex].e));
            this.mapNodes[this.currentNodeIndex].e = -1;
            ++this.currentQuestId;
            this.mapNodes[CampaignData.e(this.currentQuestId)].e = this.currentQuestId;
            d = true;
            this.D();
        }

    }

    private void D() {
        if (!d) {
            this.activeMainQuestNode = -1;
            this.activeSideQuestNode = -1;
        } else if (this.currentQuestId >= 0 && this.currentQuestId < CampaignData.e()) {
            this.activeMainQuestNode = this.mapNodes[CampaignData.e(this.currentQuestId)].b;
            this.activeSideQuestNode = this.mapNodes[CampaignData.e(this.currentQuestId)].c;
            if (this.viewportRect.b(super.g + this.activeMainQuestNode, super.a_ + this.activeSideQuestNode)) {
                this.activeMainQuestNode = -1;
                this.activeSideQuestNode = -1;
            }
        }

    }

    private void f(int num1) {
        this.E();
        this.mapState = num1;
        switch (num1) {
            case 0:
                ((BaseScreen)this).a((TouchDPad)null);
                if (this.travelStepsRemaining > 0) {
                    if (this.dialoguePhase == 1) {
                        this.dialoguePhase = 0;
                        CampaignData.a(this.dialoguePhase);
                        this.travelStepsRemaining = 0;
                        this.playerMapX = this.mapNodes[this.currentNodeIndex].b;
                        this.playerMapY = this.mapNodes[this.currentNodeIndex].c;
                        this.f(0);
                    } else if (this.dialoguePhase == 2) {
                        this.f(5);
                    } else if (this.dialoguePhase == 3) {
                        this.f(6);
                    } else {
                        this.f(1);
                    }
                } else if (this.dialoguePhase == 1) {
                    this.dialoguePhase = 0;
                    CampaignData.a(this.dialoguePhase);
                    if (this.cameraY >= 0) {
                        this.x();
                    } else {
                        this.f(0);
                    }
                } else {
                    String infoLabel = EngineStorage.canCraftOrUpgradeAnyRelic() ? "T.Tin (!)" : "T.Tin";
                    ((Window)this).a((SoftKeyButton)(new TextSoftKeyButton("C.Hàng", 0)));
                    ((Window)this).c((SoftKeyButton)(new TextSoftKeyButton(infoLabel, 8)));
                    ((Window)this).b(new TextSoftKeyButton("Thoát", 7));
                    if (this.e(this.currentNodeIndex)) {
                        CampaignData.d = this.mapNodes[this.currentNodeIndex].f;
                        CampaignData.c = this.mapNodes[this.currentNodeIndex].e;
                        this.f(2);
                    } else if (this.currentNodeIndex == 24) {
                        this.dialoguePhase = 0;
                        CampaignData.a(this.dialoguePhase);
                        this.f(9);
                    }
                }
                break;
            case 1:
                d = false;
                this.playerFixedX = this.playerMapX * 10000;
                this.playerFixedY = this.playerMapY * 10000;
                if (this.cameraFollowPlayer) {
                    this.questMarkerDelay = 0;
                    this.cameraFollowPlayer = false;
                } else {
                    this.questMarkerDelay = 75;
                }

                if (this.travelStepsRemaining <= 0) {
                    this.cameraY = this.cameraX;
                    this.targetNodeIndex = this.mapRoads[this.activeRoadIndex].a;
                    if (this.targetNodeIndex == this.currentNodeIndex) {
                        this.targetNodeIndex = this.mapRoads[this.activeRoadIndex].b;
                    }

                    this.playerFixedX = this.playerMapX * 10000;
                    this.playerFixedY = this.playerMapY * 10000;
                    long lVal3 = (long)((this.mapNodes[this.targetNodeIndex].b - this.playerMapX) * 10000);
                    long lVal5 = (long)((this.mapNodes[this.targetNodeIndex].c - this.playerMapY) * 10000);
                    long lVal7 = MathUtils.isqrt(lVal3 * lVal3 + lVal5 * lVal5);
                    this.stepFixedX = (int)(2L * lVal3 * 10000L / lVal7);
                    this.stepFixedY = (int)(2L * lVal5 * 10000L / lVal7);
                    this.travelStepsRemaining = (int)(lVal7 / 20000L);
                    this.ambushCheckStep = this.travelStepsRemaining >> 1;
                    --this.stepsUntilNextAmbush;
                }
                break;
            case 2:
                this.activeDialogueBox = null;
                String[][] str2 = (String[][])null;
                if (this.mapNodes[this.currentNodeIndex].e >= 0) {
                    switch (this.dialoguePhase) {
                        case 0:
                            str2 = DialogueData.b[this.mapNodes[this.currentNodeIndex].e];
                        case 1:
                        default:
                            break;
                        case 2:
                            str2 = DialogueData.c[this.mapNodes[this.currentNodeIndex].e];
                            break;
                        case 3:
                            str2 = DialogueData.d[this.mapNodes[this.currentNodeIndex].e];
                    }
                } else if (this.mapNodes[this.currentNodeIndex].f >= 0) {
                    switch (this.dialoguePhase) {
                        case 0:
                            str2 = DialogueData.e[this.mapNodes[this.currentNodeIndex].f];
                        case 1:
                        default:
                            break;
                        case 2:
                            str2 = DialogueData.f[this.mapNodes[this.currentNodeIndex].f];
                            break;
                        case 3:
                            str2 = DialogueData.g[this.mapNodes[this.currentNodeIndex].f];
                    }
                }

                if (str2 != null) {
                    ((Window)this).b(new TextSoftKeyButton("Bỏ qua", 3));
                    ((Window)this).c((SoftKeyButton)(new TextSoftKeyButton("Tiếp", 13)));
                    this.activeDialogueBox = new DialogueBox(str2);
                } else {
                    this.y();
                }
                break;
            case 3:
                this.swordClashEffect = new SwordClashEffect();
                this.swordClashEffect.d(this.playerMapX, this.playerMapY);
                this.swordClashEffect.a();
                if (EngineStorage.f(5) > 0) {
                    CampaignData.h = true;
                    EngineStorage.g(5);
                } else if (CampaignData.e.e > CampaignData.f.e) {
                    CampaignData.h = true;
                } else if (CampaignData.e.e < CampaignData.f.e) {
                    CampaignData.h = false;
                } else {
                    CampaignData.h = RandomUtils.nextInt(2) == 0;
                }

                this.battleIntroPanel = new BattleIntroPanel(CampaignData.e, CampaignData.f);
                this.clashSfxPlayer.playAsync();
            case 4:
            case 5:
            default:
                break;
            case 6:
                this.B();
                break;
            case 7:
                if (this.activeMainQuestNode >= 0 && this.activeSideQuestNode >= 0) {
                    this.f(8);
                }

                if (this.travelStepsRemaining == 0) {
                    this.playerMapX = this.mapNodes[this.currentNodeIndex].b;
                    this.playerMapY = this.mapNodes[this.currentNodeIndex].c;
                }

                this.mapPixelWidth = 20;
                break;
            case 8:
                this.isRandomAmbush = true;
                this.lastSideQuestId = 15;
                ((Window)this).a((SoftKeyButton)null);
                ((Window)this).c((SoftKeyButton)null);
                ((Window)this).b(new TextSoftKeyButton("Bỏ qua", 6));
                break;
            case 9:
                this.arenaSelectPanel = new ArenaSelectPanel(this.arenaTierUnlocked);
                if (this.arenaSelectPanel.a()) {
                    ((Window)this).c((SoftKeyButton)(new TextSoftKeyButton("Đánh", 2)));
                }

                ((Window)this).b(new TextSoftKeyButton("Rút lui", 4));
        }

    }

    private void E() {
        ((Window)this).a((SoftKeyButton)null);
        ((Window)this).c((SoftKeyButton)null);
        ((Window)this).b((SoftKeyButton)null);
        CampaignData.a = -1;
        switch (this.mapState) {
            case 2:
                this.activeDialogueBox = null;
                break;
            case 3:
                this.battleIntroPanel = null;
                this.swordClashEffect = null;
                break;
            case 9:
                this.arenaSelectPanel = null;
        }

    }

    public void b() {
        if (CampaignData.e != null) {
            CampaignData.e.refreshAfterRelicUnlock();
        }
        int[] sideColorOut = new int[]{0x99F2FF};
        String locLabel = this.getCurrentCitadelName();
        String mainQuestSummary = this.buildMainQuestSummary();
        int mainQuestColor = this.getMainQuestSummaryColor();
        String sideQuestSummary = this.buildSideQuestSummary(sideColorOut);
        HeroInfoDialog val1 = new HeroInfoDialog(
                CampaignData.e, this.mapPixelHeight, this.pendingDirection,
                locLabel, mainQuestSummary, mainQuestColor,
                sideQuestSummary, sideColorOut[0]);
        ((Window)val1).a((ActionListener)this);
        String relicBtnLabel = EngineStorage.canCraftOrUpgradeAnyRelic() ? "Báu vật!" : "Báu vật";
        ((Window)val1).a((SoftKeyButton)(new TextSoftKeyButton(relicBtnLabel, 18)));
        if (this.canRestAtCurrentNode()) {
            int rCost = CampaignData.computeCitadelRestCost(CampaignData.e);
            ((Window)val1).c((SoftKeyButton)(new TextSoftKeyButton("Nghỉ (" + rCost + "v)", 15)));
        } else if (this.currentQuestId >= 0 && this.currentQuestId < CampaignData.e()) {
            ((Window)val1).c((SoftKeyButton)(new TextSoftKeyButton("Định vị", 17)));
        }
        ((Window)val1).b(new TextSoftKeyButton("Đóng", 5));
        GUIManager.getInstance().a((BaseDialog)val1, true);
    }

    private void F() {
        CampaignData.c = this.currentQuestId;
        if (this.arenaSelectPanel != null) {
            CampaignData.b = CampaignData.e.c;
            CampaignData.e.c = CampaignData.e.b;
            this.arenaSelectPanel.b();
        }

        AvatarRenderer.a((AvatarRenderer)null);
        GUIManager.getInstance().e.a(7, 5, true, true);
        BgmPlayer.getInstance().destroy();
        this.clashSfxPlayer.destroy();
    }

    public void a() {
        if (this.pendingQuestShards > 0) {
            int gained = this.pendingQuestShards;
            this.pendingQuestShards = 0;
            EngineStorage.addEnhancementShards(gained);
            int totalShards = EngineStorage.getEnhancementShards();
            String msg = "Bạn nhận được +" + gained + " Mảnh Cường Hóa (hiện có: " + totalShards + " Mảnh).";
            if (EngineStorage.canCraftOrUpgradeAnyRelic()) {
                msg = msg + " Đã đủ điều kiện ghép/nâng cấp Báu Vật, mở ngay không?";
            } else {
                msg = msg + " Bạn có muốn xem bảng Báu Vật không?";
            }
            BaseDialog dialog2 = GUIManager.a("Mảnh Cường Hóa", msg, "Báu Vật", 11, "Để sau", 5, 1);
            ((Window)dialog2).a((ActionListener)this);
            GUIManager.getInstance().a(dialog2, true);
        }

        if (this.pendingLevelUpNotify) {
            this.pendingLevelUpNotify = false;
            this.z();
        }

        ++this.mapAnimTick;
        if (this.mapFeedbackTicks > 0) {
            --this.mapFeedbackTicks;
        }
        this.playerFlagSprite.e();
        if (this.questMarkerDelay == 0) {
            this.questMarkerSprite.e();
        }

        switch (this.mapState) {
            case 0:
                if (e) {
                    e = false;
                    this.G();
                }

                if (this.questMarkerDelay > 0) {
                    --this.questMarkerDelay;
                }
                break;
            case 1:
                this.playerFixedX += this.stepFixedX;
                this.playerFixedY += this.stepFixedY;
                this.playerMapX = this.playerFixedX / 10000;
                this.playerMapY = this.playerFixedY / 10000;
                --this.travelStepsRemaining;
                if (this.stepsUntilNextAmbush == 0 && this.travelStepsRemaining == this.ambushCheckStep) {
                    CampaignData.d = -1;
                    CampaignData.c = -1;
                    this.stepsUntilNextAmbush = RandomUtils.nextRange(3, 10);
                    CampaignData.f = CampaignData.x(this.activeRoadIndex);
                    this.f(3);
                }

                if (this.travelStepsRemaining <= 0) {
                    this.ambushCheckStep = -1;
                    this.currentNodeIndex = this.targetNodeIndex;
                    this.f(0);
                }
                break;
            case 2:
                if (this.activeDialogueBox != null) {
                    this.activeDialogueBox.b();
                }
                break;
            case 3:
                if (this.swordClashEffect != null) {
                    this.swordClashEffect.b();
                    if (this.swordClashEffect.c()) {
                        this.swordClashEffect = null;
                        ((Window)this).c((SoftKeyButton)(new TextSoftKeyButton("Đánh", 2)));
                        if (this.cameraY >= 0) {
                            ((Window)this).b(new TextSoftKeyButton("Rút lui", 4));
                        }
                    }
                } else if (this.battleIntroPanel != null) {
                    this.battleIntroPanel.a();
                }
            case 4:
            case 6:
            default:
                break;
            case 5:
                String str6 = "";
                if (this.travelStepsRemaining == 0) {
                    if (CampaignData.k(this.mapNodes[this.currentNodeIndex].e)) {
                        int num11 = CampaignData.i(this.mapNodes[this.currentNodeIndex].e);
                        int num3 = CampaignData.j(this.mapNodes[this.currentNodeIndex].e);
                        str6 = str6 + "Bạn đã hoàn thành nhiệm vụ. Bạn được thưởng thêm {e} kinh nghiệm, {g} vàng.";
                        str6 = StringUtils.replace(str6, "{e}", "" + num11);
                        str6 = StringUtils.replace(str6, "{g}", "" + num3);
                    } else if (CampaignData.m(this.mapNodes[this.currentNodeIndex].f)) {
                        int num12 = CampaignData.o(this.mapNodes[this.currentNodeIndex].f);
                        int num14 = CampaignData.n(this.mapNodes[this.currentNodeIndex].f);
                        str6 = str6 + "Bạn đã hoàn thành sự kiện. Bạn được thưởng thêm ";
                        boolean flag4 = false;
                        if (num12 > 0) {
                            str6 = str6 + "{h} máu";
                            flag4 = true;
                        }

                        if (num14 > 0) {
                            if (flag4) {
                                str6 = str6 + ", ";
                            }

                            str6 = str6 + "{a} tấn công";
                        }

                        str6 = StringUtils.replace(str6, "{h}", "" + num12);
                        str6 = StringUtils.replace(str6, "{a}", "" + num14);
                    }
                }

                if (!str6.equals("")) {
                    BaseDialog dialog13 = GUIManager.a("Chúc mừng", str6, "Đồng ý", 9, 1);
                    ((Window)dialog13).a((ActionListener)this);
                    GUIManager.getInstance().a(dialog13, true);
                } else {
                    this.A();
                }
                break;
            case 7:
                --this.mapPixelWidth;
                if (this.mapPixelWidth <= 0) {
                    this.f(0);
                }
                break;
            case 8:
                if (this.isRandomAmbush) {
                    this.isRandomAmbush = this.h(this.activeMainQuestNode, this.activeSideQuestNode);
                } else {
                    --this.lastSideQuestId;
                    if (this.lastSideQuestId <= 0) {
                        this.f(0);
                    }
                }
                break;
            case 9:
                if (!this.hudVisible) {
                    this.hudVisible = true;
                    RichContentDialog dialog5 = RichContentDialog.a("Đấu trường", "Từ bây giờ, bạn có thể vào đấu trường để luyện điểm kinh nghiệm.@Nếu bạn đánh thắng, bạn sẽ được một số điểm kinh nghiệm tùy theo cấp độ hiện tại.@Nếu thua, bạn sẽ bị trừ một chút kinh nghiệm và một lượng nhỏ vàng theo cấp độ.@*Lưu ý:@*-Bạn phải trả vàng để có thể tham gia đấu trường.@*-Vàng và điểm kinh nghiệm thu thập được trong quá trình đánh sẽ không có hiệu lực.@*-Máu của bạn sẽ không bị ảnh hưởng khi tham gia đấu trường.@*-Hiện tại chỉ có 2 mức luyện tập, hãy tìm khám phá để unlock thêm 3 mức luyện tập cao hơn.", (ObjectList)null, (ObjectList)null, (SoftKeyButton)null, (SoftKeyButton)null);
                    ((Window)dialog5).c((SoftKeyButton)(new TextSoftKeyButton("Đồng ý", 5)));
                    ((Window)dialog5).a((ActionListener)this);
                    ((Window)dialog5).a((SoftKeyBar)null);
                    GUIManager.getInstance().a((BaseDialog)dialog5, true);
                }

                if (this.arenaSelectPanel != null) {
                    this.arenaSelectPanel.c();
                }
                break;
            case 10:
                GUIManager.getInstance().a((BaseScreen)(new StoryCutsceneScreen(false)), false);
        }

        if (this.mapState != 8) {
            this.h(this.playerMapX, this.playerMapY);
        }

    }

    private void G() {
        ObjectList list1 = new ObjectList();
        AnimatedSpriteBlock val2 = new AnimatedSpriteBlock(MgImageLoader.load("/m/mt1"), 3);
        val2.a(new byte[][]{{0, 1, 2, 1}});
        val2.a(4);
        val2.a(true);
        val2.b(0);
        list1.a(val2);
        RichContentDialog dialog3 = RichContentDialog.a(
                "Hướng dẫn hành quân",
                "- Địa điểm có Nhiệm vụ chính hiện cờ hiệu kèm vòng sáng:@{i}@- Sự kiện / Nhiệm vụ phụ hiện dấu chấm hỏi [?] màu lam ngọc.@- Di chuyển: Chạm trực tiếp vào thành trì kế cận, chạm mũi tên chỉ hướng quanh nhân vật, hoặc dùng các phím điều hướng (2, 4, 6, 8).@- Chạm vào cờ nhân vật hoặc bấm [T.Tin] để xem Hồ sơ Tướng, Nhiệm vụ, Định vị đường đi và ghép Báu Vật.@- Khi mất máu đứng tại thành trì đã giải phóng, chạm nút [Nghỉ ngơi] góc trên phải để hồi đầy 100% sinh lực.",
                (ObjectList)null, list1, (SoftKeyButton)null, (SoftKeyButton)null);
        ((Window)dialog3).c((SoftKeyButton)(new TextSoftKeyButton("Đóng", 5)));
        ((Window)dialog3).a((ActionListener)this);
        GUIManager.getInstance().a((BaseDialog)dialog3, true);
    }

    /** Draws clean directional road chevrons along unlocked roads around the hero instead of a bulky full D-Pad. */
    private void drawUnlockedRoadChevrons(Graphics g, int centerScreenX, int centerScreenY, int pulseFrame) {
        if (this.currentNodeIndex < 0 || this.currentNodeIndex >= WorldMapData.c.length) {
            return;
        }
        int bob = (pulseFrame >> 1) % 3;
        int radius = 21 + bob;
        for (int dir = 0; dir < 4; ++dir) {
            int roadIdx = WorldMapData.c[this.currentNodeIndex][dir];
            if (roadIdx < 0 || !this.mapRoads[roadIdx].c) {
                continue;
            }
            int neighborIdx = this.mapRoads[roadIdx].a;
            if (neighborIdx == this.currentNodeIndex) {
                neighborIdx = this.mapRoads[roadIdx].b;
            }
            int rdx = this.mapNodes[neighborIdx].b - this.playerMapX;
            int rdy = this.mapNodes[neighborIdx].c - this.playerMapY;
            int len = (int) MathUtils.isqrt((long) rdx * (long) rdx + (long) rdy * (long) rdy);
            if (len <= 0) {
                continue;
            }
            int ax = centerScreenX + (rdx * radius) / len;
            int ay = centerScreenY + (rdy * radius) / len;
            if (ay < 6 || ay > GameConfig.screenHeight - 28 || ax < 6 || ax > GameConfig.screenWidth - 6) {
                continue;
            }
            int absX = rdx < 0 ? -rdx : rdx;
            int absY = rdy < 0 ? -rdy : rdy;
            int arrowDir;
            if (absX >= absY) {
                arrowDir = rdx < 0 ? 2 : 3;
            } else {
                arrowDir = rdy < 0 ? 0 : 1;
            }
            if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                HdUiRenderer.drawHdDirectionalChevron(
                        g.getHdCanvas(), ax, ay, arrowDir, 0xFFF5B8, true,
                        HdInputState.isHovered(ax - 8, ay - 8, 16, 16));
            } else {
                g.setColor(0x140C07);
                g.fillRect(ax - 6, ay - 6, 13, 13);
                g.setColor(0x1D5E52);
                g.fillRect(ax - 5, ay - 5, 11, 11);
                g.setColor(0xFFD700);
                g.drawRect(ax - 6, ay - 6, 12, 12);
                this.drawPixelArrow(g, ax, ay, arrowDir, 0xFFF5B8);
            }
        }
    }

    private void e(Graphics gfx1) {
        if (HdConfig.isHdRemaster() && gfx1.getHdCanvas() != null) {
            HdUiRenderer.drawHdDimOverlay(
                    gfx1.getHdCanvas(), 0, 0, GameConfig.screenWidth, GameConfig.screenHeight, 0x0E0805, 178);
            return;
        }
        boolean flag2 = false;
        int num3 = 0;
        int num4 = this.citadelFlagSheet.getWidth();

        for(int num5 = this.citadelFlagSheet.getHeight(); num3 < GameConfig.screenHeight; num3 += num5) {
            for(int num6 = 0; num6 < GameConfig.screenWidth; num6 += num4) {
                gfx1.drawImage(this.citadelFlagSheet, num6, num3, 0);
            }
        }

    }

    private void drawSideQuestMarker(Graphics g, int cx, int baseY) {
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdWorldMapEnhancer.drawHdSideQuestBeacon(g.getHdCanvas(), cx, baseY + 22, this.mapAnimTick);
            return;
        }
        int phase = (this.mapAnimTick >> 2) & 3;
        int bob = phase == 1 ? -1 : (phase == 2 ? -2 : (phase == 3 ? -1 : 0));
        int x = cx - 5;
        int y = baseY + bob;
        // Compact antique cyan marker badge with "?" for unlocked side quests
        g.setColor(0x0B2D38);
        g.fillRect(x, y, 11, 12);
        g.setColor(0x4DF5E8);
        g.drawRect(x, y, 10, 11);
        g.setColor(0xA6FFF6);
        g.drawLine(x + 1, y + 1, x + 9, y + 1);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "?",
                cx + 1, y - 1, Graphics.TOP | Graphics.HCENTER,
                0xD6FFFF, 0x061920);
    }

    private void drawMiniThreatBadge(Graphics g, int cx, int topY, int enemyLv, int heroLv) {
        int tier = CampaignData.getThreatTier(heroLv, enemyLv);
        int accent = CampaignData.getThreatColor(tier);
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdWorldMapEnhancer.drawHdMiniThreatBadge(g.getHdCanvas(), cx, topY, enemyLv, accent);
            return;
        }
        String tag = "C." + enemyLv;
        int textW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, tag);
        int badgeW = textW + 6;
        int badgeH = 12;
        int x = cx - (badgeW >> 1);
        if (x < 2) {
            x = 2;
        } else if (x + badgeW > GameConfig.screenWidth - 2) {
            x = GameConfig.screenWidth - 2 - badgeW;
        }
        int y = topY;
        if (y < 2 || y + badgeH > GameConfig.screenHeight - 24) {
            return;
        }
        g.setColor(0x160C07);
        g.fillRect(x, y, badgeW, badgeH);
        g.setColor(accent);
        g.drawRect(x, y, badgeW - 1, badgeH - 1);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, tag,
                x + (badgeW >> 1), y - 1, Graphics.TOP | Graphics.HCENTER,
                accent, 0x0A0503);
    }

    private void drawPixelArrow(Graphics g, int cx, int cy, int dir, int color) {
        g.setColor(color);
        switch (dir) {
            case 0: // UP
                g.fillRect(cx, cy - 2, 1, 1);
                g.fillRect(cx - 1, cy - 1, 3, 1);
                g.fillRect(cx - 2, cy, 5, 1);
                g.fillRect(cx - 2, cy + 1, 5, 1);
                break;
            case 1: // DOWN
                g.fillRect(cx - 2, cy - 1, 5, 1);
                g.fillRect(cx - 2, cy, 5, 1);
                g.fillRect(cx - 1, cy + 1, 3, 1);
                g.fillRect(cx, cy + 2, 1, 1);
                break;
            case 2: // LEFT
                g.fillRect(cx - 2, cy, 1, 1);
                g.fillRect(cx - 1, cy - 1, 1, 3);
                g.fillRect(cx, cy - 2, 1, 5);
                g.fillRect(cx + 1, cy - 2, 1, 5);
                break;
            default: // RIGHT
                g.fillRect(cx - 1, cy - 2, 1, 5);
                g.fillRect(cx, cy - 2, 1, 5);
                g.fillRect(cx + 1, cy - 1, 1, 3);
                g.fillRect(cx + 2, cy, 1, 1);
        }
    }

    private void drawOffScreenQuestCompass(Graphics g, int heroLv) {
        this.compassPillW = 0;
        if (this.currentQuestId < 0 || this.currentQuestId >= CampaignData.e()) {
            return;
        }
        int qNodeIdx = CampaignData.e(this.currentQuestId);
        if (qNodeIdx < 0 || qNodeIdx >= this.mapNodes.length || !this.mapNodes[qNodeIdx].d) {
            return;
        }
        int qScreenX = super.g + this.mapNodes[qNodeIdx].b;
        int qScreenY = super.a_ + this.mapNodes[qNodeIdx].c;
        int screenW = GameConfig.screenWidth;
        int usableH = GameConfig.screenHeight - 24;
        if (qScreenX >= 8 && qScreenX <= screenW - 8 && qScreenY >= 16 && qScreenY <= usableH - 6) {
            return;
        }

        String loc = WorldMapData.getLocationName(qNodeIdx);
        int qLv = CampaignData.getMainQuestEnemyLevel(this.currentQuestId);
        String label = qLv > 0 ? (loc + " C." + qLv) : loc;
        int accentColor = qLv > 0
                ? CampaignData.getThreatColor(CampaignData.getThreatTier(heroLv, qLv))
                : 0xFFE680;

        int textW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, label);
        int textH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int pillW = textW + 18;
        int pillH = textH + 6;

        int cx = screenW >> 1;
        int cy = usableH >> 1;
        int dx = qScreenX - cx;
        int dy = qScreenY - cy;
        int absDx = dx < 0 ? -dx : dx;
        int absDy = dy < 0 ? -dy : dy;
        int halfW = cx - 6;
        int halfH = cy - 6;

        int dir;
        int pillX;
        int pillY;
        if (absDx * halfH >= absDy * halfW && absDx > 0) {
            if (dx < 0) {
                dir = 2; // LEFT
                pillX = 4;
            } else {
                dir = 3; // RIGHT
                pillX = screenW - pillW - 4;
            }
            pillY = cy + (dy * halfW / absDx) - (pillH >> 1);
        } else if (absDy > 0) {
            if (dy < 0) {
                dir = 0; // UP
                pillY = 4;
            } else {
                dir = 1; // DOWN
                pillY = usableH - pillH - 3;
            }
            pillX = cx + (dx * halfH / absDy) - (pillW >> 1);
        } else {
            dir = 0;
            pillX = cx - (pillW >> 1);
            pillY = 4;
        }

        if (pillX < 4) {
            pillX = 4;
        } else if (pillX + pillW > screenW - 4) {
            pillX = screenW - 4 - pillW;
        }
        if (pillY < 4) {
            pillY = 4;
        } else if (pillY + pillH > usableH - 3) {
            pillY = usableH - 3 - pillH;
        }

        // Avoid overlapping top-left HD Hero Mini Pill if visible
        if (HdConfig.isHdRemaster() && pillX < 118 && pillY < 28) {
            pillY = 28;
        }

        // Avoid overlapping top-right Rest pill if visible
        if (this.restPillW > 0
                && pillX + pillW > this.restPillX - 3
                && pillX < this.restPillX + this.restPillW + 3
                && pillY < this.restPillY + this.restPillH + 3
                && pillY + pillH > this.restPillY - 3) {
            pillY = this.restPillY + this.restPillH + 4;
        }

        this.compassPillX = pillX;
        this.compassPillY = pillY;
        this.compassPillW = pillW;
        this.compassPillH = pillH;

        boolean hdCompass = HdConfig.isHdRemaster() && g.getHdCanvas() != null;
        boolean compassHover = HdInputState.isHovered(pillX, pillY, pillW, pillH);
        DrawUtils.drawAntiquePanel(
                g, pillX, pillY, pillW, pillH,
                compassHover ? 0x3D2617 : 0x2B1B10,
                compassHover ? 0x26180E : 0x1A1009,
                0x0E0804,
                compassHover ? 0xFFD700 : accentColor, 0xFFF2B8, 0xFFFFFF);

        int arrowCy = pillY + (pillH >> 1);
        int textY = pillY + ((pillH - textH) >> 1);
        if (dir == 3) {
            // Arrow on the right side for RIGHT-pointing compass
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, label,
                    pillX + 5, textY, Graphics.TOP | Graphics.LEFT,
                    accentColor, 0x0C0603);
            if (hdCompass) {
                HdUiRenderer.drawHdDirectionalChevron(g.getHdCanvas(), pillX + pillW - 7, arrowCy, dir, accentColor, false, false);
            } else {
                this.drawPixelArrow(g, pillX + pillW - 7, arrowCy, dir, accentColor);
            }
        } else {
            // Arrow on the left side for UP / DOWN / LEFT
            if (hdCompass) {
                HdUiRenderer.drawHdDirectionalChevron(g.getHdCanvas(), pillX + 7, arrowCy, dir, accentColor, false, false);
            } else {
                this.drawPixelArrow(g, pillX + 7, arrowCy, dir, accentColor);
            }
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, label,
                    pillX + 13, textY, Graphics.TOP | Graphics.LEFT,
                    accentColor, 0x0C0603);
        }
    }

    private void drawRoadTravelStrip(Graphics g, int heroLv) {
        if (this.activeRoadIndex < 0 || this.activeRoadIndex >= WorldMapData.d.length) {
            return;
        }
        int minLv = WorldMapData.d[this.activeRoadIndex][2];
        int maxLv = WorldMapData.d[this.activeRoadIndex][3];
        String destName = WorldMapData.getLocationName(this.targetNodeIndex);
        String text = "Tới " + destName + "  |  Quái: C." + minLv + "-" + maxLv;
        int tier = CampaignData.getThreatTier(heroLv, maxLv);
        int accent = CampaignData.getThreatColor(tier);
        int textW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, text);
        int textH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int barW = textW + 16;
        if (barW > GameConfig.screenWidth - 8) {
            barW = GameConfig.screenWidth - 8;
        }
        int barH = textH + 6;
        int barX = (GameConfig.screenWidth - barW) >> 1;
        int barY = GameConfig.screenHeight - barH - 3;
        DrawUtils.drawAntiquePanel(
                g, barX, barY, barW, barH,
                0x2B1B10, 0x1A1009, 0x0E0804,
                accent, 0xFFF2B8, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, text,
                barX + (barW >> 1), barY + ((barH - textH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                accent, 0x0C0603);
    }

    private void drawLocatorBeacon(Graphics g, int heroLv) {
        if (this.activeMainQuestNode < 0 || this.activeSideQuestNode < 0) {
            return;
        }
        int bx = super.g + this.activeMainQuestNode;
        int by = super.a_ + this.activeSideQuestNode;
        int pulse = (this.mapAnimTick >> 1) % 6;
        int r1 = 10 + pulse;
        int r2 = 14 + pulse;
        g.setColor(0xFFE680);
        g.drawArc(bx - r1, by - r1, r1 << 1, r1 << 1, 0, 360);
        g.setColor(0x33D6C4);
        g.drawArc(bx - r2, by - r2, r2 << 1, r2 << 1, 0, 360);

        if (this.currentQuestId >= 0 && this.currentQuestId < CampaignData.e()) {
            int qNodeIdx = CampaignData.e(this.currentQuestId);
            String loc = WorldMapData.getLocationName(qNodeIdx);
            int qLv = CampaignData.getMainQuestEnemyLevel(this.currentQuestId);
            String label = qLv > 0 ? ("Mục tiêu: " + loc + " (C." + qLv + ")") : ("Mục tiêu: " + loc);
            int accent = qLv > 0
                    ? CampaignData.getThreatColor(CampaignData.getThreatTier(heroLv, qLv))
                    : 0xFFE680;
            int textW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, label);
            int textH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
            int pillW = textW + 14;
            int pillH = textH + 6;
            int pillX = bx - (pillW >> 1);
            if (pillX < 4) {
                pillX = 4;
            } else if (pillX + pillW > GameConfig.screenWidth - 4) {
                pillX = GameConfig.screenWidth - 4 - pillW;
            }
            int pillY = by + 16;
            if (pillY + pillH > GameConfig.screenHeight - 28) {
                pillY = by - 36 - pillH;
            }
            if (pillY < 4) {
                pillY = 4;
            }
            DrawUtils.drawAntiquePanel(
                    g, pillX, pillY, pillW, pillH,
                    0x2B1B10, 0x1A1009, 0x0E0804,
                    accent, 0xFFF2B8, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, label,
                    pillX + (pillW >> 1), pillY + ((pillH - textH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    accent, 0x0C0603);
        }
    }

    public void a(Graphics gfx1) {
        int renderPlayerX = this.playerMapX;
        int renderPlayerY = this.playerMapY;
        if (this.mapState == 1 && GameConfig.highFpsEnabled && this.travelStepsRemaining > 0) {
            renderPlayerX = (this.playerFixedX + ((this.stepFixedX * GameConfig.renderSubTick256) >> 8)) / 10000;
            renderPlayerY = (this.playerFixedY + ((this.stepFixedY * GameConfig.renderSubTick256) >> 8)) / 10000;
            this.h(renderPlayerX, renderPlayerY);
        }

        gfx1.setClip(0, 0, GameConfig.screenWidth, GameConfig.screenHeight);
        gfx1.drawImage(this.worldMapImage, super.g, super.a_, 0);
        this.b(gfx1);

        int heroLv = CampaignData.e != null ? CampaignData.e.e : 1;
        boolean showMapHints = this.mapState != 2 && this.mapState != 3 && this.mapState != 9;
        int pulseFrame = GameConfig.highFpsEnabled ? (GameConfig.visualFrameCounter >> 2) : this.mapAnimTick;
        boolean hdMap = HdConfig.isHdRemaster() && gfx1.getHdCanvas() != null;

        for(int num2 = 0; num2 < this.mapNodes.length; ++num2) {
            if (!this.mapNodes[num2].d) {
                continue;
            }
            int sx = super.g + this.mapNodes[num2].b;
            int sy = super.a_ + this.mapNodes[num2].c;
            if (sx < -24 || sx > GameConfig.screenWidth + 24 || sy < -32 || sy > GameConfig.screenHeight + 24) {
                continue;
            }
            if (this.mapNodes[num2].e != -1) {
                if (CampaignData.d(this.mapNodes[num2].e) > 0) {
                    int auraR = 7 + (pulseFrame % 5);
                    gfx1.setColor((pulseFrame & 2) == 0 ? 0xFFD700 : 0xFF6B3D);
                    gfx1.drawArc(sx - auraR, sy - auraR, auraR << 1, auraR << 1, 0, 360);
                }
                if (hdMap) {
                    HdWorldMapEnhancer.drawHdMainQuestBeacon(gfx1.getHdCanvas(), sx, sy, pulseFrame);
                } else {
                    this.playerFlagSprite.a(gfx1, sx, sy - 27);
                }
            } else if (this.mapNodes[num2].f != -1) {
                if (CampaignData.p(this.mapNodes[num2].f) > 0) {
                    int auraR = 6 + (pulseFrame % 4);
                    gfx1.setColor(0x4DF5E8);
                    gfx1.drawArc(sx - auraR, sy - auraR, auraR << 1, auraR << 1, 0, 360);
                }
                this.drawSideQuestMarker(gfx1, sx, sy - 22);
            }
            if (showMapHints && num2 != this.currentNodeIndex) {
                boolean isQuestOrAdjacent = this.mapNodes[num2].e != -1
                        || this.mapNodes[num2].f != -1
                        || (this.mapState == 0 && this.isAdjacentUnlockedNode(num2));
                if (isQuestOrAdjacent) {
                    int enemyLv = this.getNodeEnemyLevel(num2, heroLv);
                    if (enemyLv > 0) {
                        this.drawMiniThreatBadge(gfx1, sx, sy + 7, enemyLv, heroLv);
                    }
                }
            }
        }

        if (hdMap) {
            HdWorldMapEnhancer.drawHdHeroToken(
                    gfx1.getHdCanvas(),
                    super.g + renderPlayerX, super.a_ + renderPlayerY,
                    AvatarRenderer.getAvatarSheet(),
                    CampaignData.e != null ? CampaignData.e.a : 0,
                    pulseFrame);
            if (showMapHints && CampaignData.e != null) {
                HdWorldMapEnhancer.drawTopLeftHeroMiniPill(
                        gfx1.getHdCanvas(),
                        CampaignData.e,
                        this.pendingDirection,
                        HdInputState.isHovered(3, 3, 112, 22));
            }
        } else {
            gfx1.drawImage(this.hudHeaderImage, super.g + renderPlayerX, super.a_ + renderPlayerY, 3);
        }
        if (showMapHints && this.canRestAtCurrentNode()) {
            // Paid Citadel Rest pill at top-right when wounded at a liberated citadel
            int restCost = CampaignData.computeCitadelRestCost(CampaignData.e);
            String restLabel = "Nghỉ ngơi (" + restCost + "v)";
            int pillW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, restLabel) + 12;
            int pillH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 6;
            int pillX = GameConfig.screenWidth - pillW - 3;
            int pillY = 4;
            this.restPillX = pillX;
            this.restPillY = pillY;
            this.restPillW = pillW;
            this.restPillH = pillH;
            boolean canAfford = this.pendingDirection >= restCost;
            boolean restHover = HdInputState.isHovered(pillX, pillY, pillW, pillH);
            DrawUtils.drawAntiquePanel(
                    gfx1, pillX, pillY, pillW, pillH,
                    canAfford ? (restHover ? 0x268270 : 0x1D6355) : 0x452A1D,
                    canAfford ? (restHover ? 0x17574A : 0x124238) : 0x2B1A11,
                    0x0D241E,
                    canAfford ? 0xFFD700 : 0x8F6932,
                    canAfford ? 0xFFF2A6 : 0xCCA052,
                    0xFFFFFF);
            DrawUtils.drawShadowText(
                    gfx1, DrawUtils.UI_FONT_BOLD, restLabel,
                    pillX + (pillW >> 1), pillY + 3, Graphics.TOP | Graphics.HCENTER,
                    canAfford ? 0xFFF5B8 : 0xC8B098, 0x120804);
        } else {
            this.restPillW = 0;
        }
        if (this.mapState == 0) {
            this.drawOffScreenQuestCompass(gfx1, heroLv);
        } else {
            this.compassPillW = 0;
        }
        switch (this.mapState) {
            case 0:
                this.drawUnlockedRoadChevrons(
                        gfx1, super.g + renderPlayerX, super.a_ + renderPlayerY, pulseFrame);
            case 7:
                if (!hdMap && this.questMarkerDelay == 0) {
                    this.questMarkerSprite.a(gfx1, super.g + this.playerMapX - 5, super.a_ + this.playerMapY - 20);
                }
                break;
            case 1:
                this.drawRoadTravelStrip(gfx1, heroLv);
                break;
            case 8:
                this.drawLocatorBeacon(gfx1, heroLv);
                break;
            case 4:
            case 5:
            case 6:
            default:
                break;
            case 2:
                this.e(gfx1);
                if (this.activeDialogueBox != null) {
                    this.activeDialogueBox.a(gfx1);
                }
                break;
            case 3:
                if (this.swordClashEffect != null) {
                    this.swordClashEffect.a(gfx1, super.g, super.a_);
                } else {
                    this.e(gfx1);
                    if (this.battleIntroPanel != null) {
                        this.battleIntroPanel.a(gfx1);
                    }
                }
                break;
            case 9:
                this.e(gfx1);
                if (this.arenaSelectPanel != null) {
                    this.arenaSelectPanel.a(gfx1);
                }
        }

        if (this.mapFeedbackTicks > 0 && this.mapFeedbackText != null && this.mapState == 0) {
            int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
            int fbW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, this.mapFeedbackText) + 16;
            if (fbW > GameConfig.screenWidth - 8) {
                fbW = GameConfig.screenWidth - 8;
            }
            int fbH = plainH + 6;
            int fbX = (GameConfig.screenWidth - fbW) >> 1;
            int fbY = GameConfig.screenHeight - 24 - fbH - 3;
            DrawUtils.drawAntiquePanel(
                    gfx1, fbX, fbY, fbW, fbH,
                    0x1D6355, 0x113D34, 0x0A241E,
                    0x7CFF9E, 0xC4FFD4, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    gfx1, DrawUtils.UI_FONT_BOLD, this.mapFeedbackText,
                    fbX + (fbW >> 1), fbY + ((fbH - plainH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    0xFFF5B8, 0x0A1F1A);
        }

    }

    public void onAction(int num1, int num2) {
        switch (num2) {
            case 0:
                AvatarRenderer.a((AvatarRenderer)null);
                GUIManager.getController().a(7, 4, true, true);
                break;
            case 1:
                GUIManager.getInstance().a(false);
                AvatarRenderer.a((AvatarRenderer)null);
                GUIManager.getController().a(7, 2, true, true);
                break;
            case 2:
                this.F();
                break;
            case 3:
                this.y();
                break;
            case 4:
                if (this.travelStepsRemaining == 0) {
                    this.x();
                } else {
                    this.stepFixedX = -this.stepFixedX;
                    this.stepFixedY = -this.stepFixedY;
                    this.targetNodeIndex = this.currentNodeIndex;
                    this.f(1);
                }
                break;
            case 5:
                GUIManager.getInstance().a(true);
                break;
            case 6:
                this.activeMainQuestNode = this.activeSideQuestNode = -1;
                this.f(0);
                break;
            case 7:
                BaseDialog dialog3 = GUIManager.a("Chú ý", "Bạn có muốn trở về menu hay không?", "Có", 1, "Không", 5, 1);
                ((Window)dialog3).a((ActionListener)this);
                GUIManager.getInstance().a(dialog3, true);
                break;
            case 8:
                this.b();
                break;
            case 9:
                if (CampaignData.m(CampaignData.d)) {
                    this.A();
                } else {
                    BaseDialog dialog4 = CampaignData.a(true);
                    ((Window)dialog4).a((ActionListener)this);
                    ((Window)dialog4).c((SoftKeyButton)(new TextSoftKeyButton("Đóng", 10)));
                    GUIManager.getInstance().a(true);
                    GUIManager.getInstance().a(dialog4, true);
                }
                break;
            case 10:
                GUIManager.getInstance().a(true);
                this.A();
                break;
            case 11:
            case 18:
                GUIManager.getInstance().a(false);
                AvatarRenderer.a((AvatarRenderer)null);
                GUIManager.getController().a(7, 3, true, true);
                break;
            case 13:
                if (!this.activeDialogueBox.a()) {
                    this.y();
                }
                break;
            case 14:
                GUIManager.getInstance().a(false);
                break;
            case 15:
                GUIManager.getInstance().a(true);
                this.promptCitadelRest();
                break;
            case 16:
                GUIManager.getInstance().a(true);
                if (this.canRestAtCurrentNode()) {
                    int restCost = CampaignData.computeCitadelRestCost(CampaignData.e);
                    String citadelName = this.getCurrentCitadelName();
                    int currentGold = EngineStorage.f(2);
                    if (currentGold < restCost) {
                        BaseDialog noGold = GUIManager.a(
                                "Chú ý",
                                "Bạn không đủ vàng để nghỉ ngơi tại " + citadelName + " (cần " + restCost + " vàng)!",
                                "Đóng", 5, 1);
                        ((Window) noGold).a((ActionListener) this);
                        GUIManager.getInstance().a(noGold, true);
                    } else {
                        EngineStorage.b(2, restCost);
                        this.pendingDirection = EngineStorage.f(2);
                        CampaignData.e.c = CampaignData.e.b;
                        this.w();
                        this.mapFeedbackText = "Đã nghỉ tại " + citadelName + ": Đầy 100% Máu (-" + restCost + "v)!";
                        this.mapFeedbackTicks = 60;
                        this.f(0);
                    }
                }
                break;
            case 17:
                GUIManager.getInstance().a(true);
                this.panCameraToMainQuest();
                break;
        }

    }

    public void e() {
        long lVal1 = System.currentTimeMillis();
        if (this.mapState != 10) {
            this.w();
        }

        DebugLogForm.log("DONE SAVEMAP." + (System.currentTimeMillis() - lVal1));
    }
}





