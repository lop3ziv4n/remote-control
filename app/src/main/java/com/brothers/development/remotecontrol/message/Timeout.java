package com.brothers.development.remotecontrol.message;

/**
 * Created by iv4nlop3z on 12/12/15.
 */
public class Timeout implements Request {

    /**
     * @return TypeMessage
     */
    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.TIMEOUT;
    }

    /**
     * @return string
     */
    @Override
    public String getMessage() {
        return String.format("%1$-" + Constants.LENGTH_NAME_MESSAGE + "s",Constants.MESSAGE_TIMEOUT);
    }
}
