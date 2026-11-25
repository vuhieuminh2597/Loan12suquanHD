package com.mic.smsgame.suquan.data;

/**
 * Location vertex on the world map (city, fortress, waypoint, or quest site).
 */
public class MapNode {
    /** Node type: {@code 0} = named location/citadel, {@code 1} = road junction/outpost. */
    public int a;
    /** World map X coordinate. */
    public int b;
    /** World map Y coordinate. */
    public int c;
    /** Whether this node is unlocked on the map. */
    public boolean d = true;
    /** Active main-quest index attached to this node, or {@code -1}. */
    public int e = -1;
    /** Active side-quest index attached to this node, or {@code -1}. */
    public int f = -1;
}
