/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.devices;

/**
 *
 * @author Martin
 */
public interface Camera {
    public static final boolean PowerOn = true;
    public static final boolean PowerOff = false;
    public static final int DirectionUp = 1;
    public static final int DirectionDown = 2;
    public static final int DirectionLeft = 3;
    public static final int DirectionRight = 4;
    public static final int ZoomIn = 5;
    public static final int ZoomOut = 6;
    public static final int Stop = 7;
    public static final int FocusFar = 8;
    public static final int FocusNear = 9;
    /**
     * Controls camera pan/tilt/zoom/focus/stop
     * Use Camera static fields for direction. Camera.DirectionUp e.g.
     *
     * @param direction
     */
    public void controlCamera(int direction);
    /**
     * Recalls a previously stored camera position. Use positionStore() to store
     * position.
     *
     * @param memoryNumber Memory index
     */
    public void positionRecall(int memoryNumber);
     /**
     * Not available on this camera. Does nothing.
     * @param memoryNumber Memory index
     */
    public void positionClear(int memoryNumber);
    /**
     * Stores current camera position. Use positionRecall() to recall position.
     *
     * @param memoryNumber Memory index
     */
    public void positionSave(int memoryNumber);
}
