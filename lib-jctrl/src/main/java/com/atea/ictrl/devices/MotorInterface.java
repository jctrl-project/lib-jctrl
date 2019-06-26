/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.devices;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author maar01
 */
public abstract class MotorInterface {
    public static final int MotorStopped = 0;
    public static final int DirectionUp = 1;
    public static final int DirectionDown = 2;
    public static final int DirectionStop = 3;
    public int currentStatus;
    private int upTime;
    private int downTime;
    private int changeDirectionTime;
    private Timer timer;
    private int lastCompletedRun = 0;
    public MotorInterface(int upTime, int downTime, int changeDirectionTime) {
        this.upTime = 1000 * upTime;
        this.downTime = 1000 * downTime;
        this.changeDirectionTime = 1000 * changeDirectionTime;        
    }
    private TimerTask createStopTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if(currentStatus != MotorStopped) {
                    lastCompletedRun = currentStatus;
                }
                currentStatus = MotorStopped;
                relayStop();
            }
        };
    }
    private TimerTask createUpDownTask(final int direction) {
        return new TimerTask() {
            @Override
            public void run() {
                switch(direction) {
                    case DirectionUp:
                        currentStatus = DirectionUp;
                        relayUp();
                        timer.schedule(createStopTask(), upTime);
                        break;
                    case DirectionDown:
                        currentStatus = DirectionDown;
                        relayDown();
                        timer.schedule(createStopTask(), downTime);
                        break;
                    }
            }
        };
    }
    public void runMotor(final int direction) {
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        // Motor is running and in wrong direction. Stop it and change direction.
        if(currentStatus != MotorStopped && currentStatus != direction) {
            relayStop();
            currentStatus = MotorStopped;
            lastCompletedRun = MotorStopped;
            timer.schedule(createUpDownTask(direction), changeDirectionTime);
        // Motor is stopped. Motor is not running in a direction which it already at? Run it directly.
        } else if(currentStatus == MotorStopped && lastCompletedRun != direction) {
            timer.schedule(createUpDownTask(direction), 0);
        }

    }
    
    public abstract void relayUp();
    public abstract void relayDown();
    public abstract void relayStop();
}
