package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 12/12/15.
 */
public class NameDevice implements Request {

    private String message;

    /**
     * @param message     type String
     */
    public NameDevice(String message) {
        this.message = String.format("%1$-" + Constants.LENGTH_NAME_MESSAGE + "s",message);
    }

    /**
     * @return TypeMessage
     */
    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.CHANGE_NAME_DEVICE;
    }

    /**
     * @return String
     */
    @Override
    public String getMessage() {
        return message.substring(Constants.LENGTH_INITIAL_MESSAGE, Constants.LENGTH_NAME_MESSAGE);
    }

}
