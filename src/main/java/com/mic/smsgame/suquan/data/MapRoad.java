package com.mic.smsgame.suquan.data;

/**
 * Road edge connecting two {@link MapNode} indices on the world map.
 */
public class MapRoad {
    /** Index of start node in {@link WorldMapData#b}. */
    public int a;
    /** Index of end node in {@link WorldMapData#b}. */
    public int b;
    /** Whether this road is currently visible/unlocked. */
    public boolean c = true;
}
