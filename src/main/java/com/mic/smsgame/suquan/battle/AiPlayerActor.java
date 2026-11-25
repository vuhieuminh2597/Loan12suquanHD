package com.mic.smsgame.suquan.battle;

import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.core.AsyncTask;
import com.mg.engine.core.AsyncTaskPool;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.util.ObjectList;
import com.mg.engine.util.RandomUtils;
import com.mic.smsgame.suquan.model.SwapMove;

/**
 * Background AI actor that scores all valid match-3 swaps and decides whether
 * to cast an available spell or execute the highest-value swap.
 * Supports both blocking worker threads (Desktop) and non-blocking event-loop timers (Web / iPhone 17).
 */
public class AiPlayerActor extends BattleActor implements AsyncTask, BattleConstants {

    private static final class AiDecision {
        final SwapMove chosenMove;
        final int chosenSkillTier;

        AiDecision(SwapMove chosenMove, int chosenSkillTier) {
            this.chosenMove = chosenMove;
            this.chosenSkillTier = chosenSkillTier;
        }
    }

    public AiPlayerActor(BattleController controller, int actorIndex) {
        super(controller, actorIndex);
    }

    public void a() {
        AsyncTaskPool.getInstance().submit(this);
    }

    public void b() {
    }

    private AiDecision computeAiDecision() {
        synchronized (GUIManager.ENGINE_LOCK) {
            ObjectList validMoves = this.controller.q();
            if (validMoves == null || validMoves.b() <= 0) {
                return null;
            }

            int bestScore = -1;
            int bestIndex = 0;
            int secondBestScore = -1;
            int secondBestIndex = -1;
            for (int i = validMoves.b() - 1; i > -1; --i) {
                int moveScore = 0;
                int matchedDirections = 0;
                int totalMatchedCells = 0;
                SwapMove move = (SwapMove) validMoves.b(i);
                int gemA = this.controller.d(move.a, move.b);
                int gemB = this.controller.d(move.c, move.d);
                int[] swappedGems = new int[]{gemB, gemB, gemA, gemA};

                for (int dir = move.e.length - 1; dir > -1; --dir) {
                    int matchLen = move.e[dir] & 255;
                    if (matchLen >= 3) {
                        ++matchedDirections;
                        totalMatchedCells += matchLen;
                        int before = (move.e[dir] & 0xFF0000) >> 16;
                        int after = (move.e[dir] & 0xFF00) >> 8;
                        int anchorRow = (dir < 2) ? move.a : move.c;
                        int anchorCol = (dir < 2) ? move.b : move.d;
                        if (dir != 0 && dir != 2) {
                            swappedGems[dir] = this.controller.b(swappedGems[dir], anchorRow, anchorCol, before, after);
                        } else {
                            swappedGems[dir] = this.controller.a(swappedGems[dir], anchorRow, anchorCol, before, after);
                        }
                        moveScore += this.controller.e(swappedGems[dir], matchLen);
                        if (matchLen >= 4) {
                            moveScore += 18;
                        }
                    }
                }

                if (matchedDirections >= 2) {
                    moveScore += (totalMatchedCells >= 5) ? 16 : 8;
                }

                if (this.actorIndex == 1
                        && moveScore < 100
                        && (this.controller.hasBossSealAt(move.a, move.b)
                        || this.controller.hasBossSealAt(move.c, move.d))) {
                    moveScore -= 8;
                    if (moveScore < 1) {
                        moveScore = 1;
                    }
                }

                if (moveScore > bestScore || (moveScore == bestScore && RandomUtils.nextInt(2) == 0)) {
                    if (bestScore >= 0) {
                        secondBestScore = bestScore;
                        secondBestIndex = bestIndex;
                    }
                    bestScore = moveScore;
                    bestIndex = i;
                } else if (moveScore > secondBestScore) {
                    secondBestScore = moveScore;
                    secondBestIndex = i;
                }
            }

            int chosenIndex = bestIndex;
            if (secondBestIndex >= 0 && secondBestIndex != bestIndex && this.controller.e(this.actorIndex)) {
                chosenIndex = secondBestIndex;
            }
            SwapMove chosenMove = (SwapMove) validMoves.b(chosenIndex);
            int availableSkillCount = this.controller.d();
            int chosenSkillTier = this.controller.chooseBestAiSkillTier(availableSkillCount, bestScore);
            return new AiDecision(chosenMove, chosenSkillTier);
        }
    }

    public void runTask() {
        if (HdPlatform.isWeb()) {
            final int thinkDelayMs = GameConfig.battleFastMode ? 320 : 650;
            HdPlatform.backend().scheduleDelayed(thinkDelayMs, new Runnable() {
                @Override
                public void run() {
                    try {
                        final AiDecision decision = computeAiDecision();
                        if (decision == null) {
                            return;
                        }
                        final int execDelayMs = GameConfig.battleFastMode ? 350 : 650;
                        if (decision.chosenSkillTier >= 0) {
                            HdPlatform.backend().scheduleDelayed(execDelayMs, new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (GUIManager.ENGINE_LOCK) {
                                        controller.a(decision.chosenSkillTier);
                                    }
                                }
                            });
                        } else {
                            synchronized (GUIManager.ENGINE_LOCK) {
                                controller.a(decision.chosenMove);
                            }
                            HdPlatform.backend().scheduleDelayed(execDelayMs, new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (GUIManager.ENGINE_LOCK) {
                                        controller.a(
                                                decision.chosenMove.a,
                                                decision.chosenMove.b,
                                                decision.chosenMove.c,
                                                decision.chosenMove.d);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        DebugLogForm.log(e);
                        e.printStackTrace();
                    }
                }
            });
            return;
        }

        try {
            try {
                Thread.sleep(GameConfig.battleFastMode ? 320L : 650L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            AiDecision decision = computeAiDecision();
            if (decision == null) {
                return;
            }

            long execDelayMs = GameConfig.battleFastMode ? 350L : 650L;
            if (decision.chosenSkillTier >= 0) {
                try {
                    Thread.sleep(execDelayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (GUIManager.ENGINE_LOCK) {
                    this.controller.a(decision.chosenSkillTier);
                }
            } else {
                synchronized (GUIManager.ENGINE_LOCK) {
                    this.controller.a(decision.chosenMove);
                }
                try {
                    Thread.sleep(execDelayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (GUIManager.ENGINE_LOCK) {
                    this.controller.a(
                            decision.chosenMove.a,
                            decision.chosenMove.b,
                            decision.chosenMove.c,
                            decision.chosenMove.d);
                }
            }
        } catch (Exception e) {
            DebugLogForm.log(e);
            e.printStackTrace();
        }
    }
}
