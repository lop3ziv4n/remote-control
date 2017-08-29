package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 16/11/15.
 */
public enum TypeMessage {

    TIMEOUT ("t"),
    CHANGE_NAME_DEVICE ("i"),
    CHANGE_PIN_DEVICE("p"),
    MOTION ("m");

    private final String value;

    /**
     * @param value type String
     */
    TypeMessage (String value) {
        this.value = value;
    }

    /**
     * @return String
     */
    public String getValue() { return value; }
}
