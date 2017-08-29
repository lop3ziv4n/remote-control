package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 12/12/15.
 */
public class PinDevice implements Request {

    private String message;

    /**
     * @param message     type String
     */
    public PinDevice(String message) {
        this.message = String.format("%1$-" + Constants.LENGTH_PIN_MESSAGE + "s",message);
    }

    /**
     * @return
     */
    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.CHANGE_PIN_DEVICE;
    }

    /**
     * @return
     */
    @Override
    public String getMessage() {
        return message.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_PIN_MESSAGE);
    }
}
