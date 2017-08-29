package com.brothers.development.remotecontrol.joystick;

/**
 * Created by iv4nlop3z on 14/10/15.
 */
public interface JoystickMovedListener {

    /**
     * @param pan
     * @param tilt
     */
    void OnMoved(int pan, int tilt);

    /**
     *
     */
    void OnReleased();

    /**
     *
     */
    void OnReturnedToCenter();
}
